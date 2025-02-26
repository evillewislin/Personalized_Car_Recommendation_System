<template>
  <div class="chart-container">
    <!-- 时间单位选择器 -->
    <div class="time-unit-selector">
      <label for="time-unit">时间单位：</label>
      <select v-model="selectedTimeUnit" id="time-unit">
        <option value="year">年</option>
        <option value="month">月</option>
        <option value="week">周</option>
        <option value="day">日</option>
      </select>
    </div>

    <!-- 图表容器 -->
    <div class="chart-item">
      <canvas ref="userHistoryBarChart"></canvas>
    </div>
    <div class="chart-item">
      <canvas ref="userHistoryLineChart"></canvas>
    </div>
    <div class="chart-item">
      <canvas ref="newUserHistoryLineChart"></canvas>
    </div>
  </div>
</template>

<script>
import { Chart, registerables } from 'chart.js';
import axios from 'axios';
import 'chartjs-adapter-date-fns';
import { useUserStore } from '@/store';

// 请求拦截器，添加 Token 到请求头
axios.interceptors.request.use(config => {
  const userStore = useUserStore();
  const token = userStore.token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 从 Token 中解析用户 ID
const getUserIdFromToken = (token) => {
  if (!token) return null;
  try {
    token = token.replace("Bearer ", "");
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => `%${('00' + c.charCodeAt(0).toString(16)).slice(-2)}`).join(''));
    const payload = JSON.parse(jsonPayload);
    return parseInt(payload.sub); // 从 sub 字段获取 userId
  } catch (error) {
    console.error('解析 Token 出错:', error);
    return null;
  }
};

// 根据时间单位格式化日期
const formatDateByUnit = (dateString, unit) => {
  const date = new Date(dateString);
  switch (unit) {
    case 'year':
      return date.getFullYear();
    case 'month':
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
    case 'week': {
      const oneDay = 24 * 60 * 60 * 1000;
      const firstDayOfYear = new Date(date.getFullYear(), 0, 1);
      const dayOfYear = Math.floor((date - firstDayOfYear) / oneDay);
      return `${date.getFullYear()}-W${Math.ceil((dayOfYear + firstDayOfYear.getDay() + 1) / 7)}`;
    }
    case 'day':
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    default:
      return dateString;
  }
};

export default {
  data() {
    return {
      selectedTimeUnit: 'day', // 默认时间单位
      rawData: null, // 缓存原始数据
      chartInstances: {
        barChart: null,
        lineChart: null,
        newLineChart: null
      }
    };
  },
  watch: {
    selectedTimeUnit() {
      if (this.rawData) {
        this.processDataAndRenderCharts();
      }
    }
  },
  mounted() {
    Chart.register(...registerables);
    this.fetchDataAndRenderChart();
  },
  methods: {
    // 销毁旧图表
    destroyCharts() {
      Object.values(this.chartInstances).forEach(chart => {
        if (chart) {
          chart.destroy();
        }
      });
      this.chartInstances = {
        barChart: null,
        lineChart: null,
        newLineChart: null
      };
    },
    // 获取数据
    async fetchDataAndRenderChart() {
      try {
        const userStore = useUserStore();
        const role = userStore.role;
        const token = userStore.token;
        const userId = getUserIdFromToken(token);

        let url = '/api/user-history-analysis';
        if (role === 'user' && userId !== null) {
          url += `?userId=${userId}`;
        }

        const response = await axios.get(url);
        this.rawData = response.data;
        this.processDataAndRenderCharts();
      } catch (error) {
        console.error('获取数据失败:', error);
      }
    },
    // 处理数据并渲染图表
    processDataAndRenderCharts() {
      if (!this.rawData || this.rawData.length === 0) {
        console.error('获取的数据为空');
        return;
      }

      const { carBrandScoreMap, timeScoreMap, brandIndexMap, timeBrandMap } = this.processData();

      // 准备图表数据
      const carBrandLabels = [];
      const scoreDataForBar = [];
      carBrandScoreMap.forEach(([totalScore, count], carBrand) => {
        carBrandLabels.push(carBrand);
        scoreDataForBar.push(totalScore / count);
      });

      const timeLabels = [];
      const scoreDataForLine = [];
      const brandDataForLine = [];
      const sortedTimes = Array.from(timeScoreMap.keys()).sort();
      sortedTimes.forEach(time => {
        const [totalScore, count] = timeScoreMap.get(time);
        timeLabels.push(time);
        scoreDataForLine.push(totalScore / count);
        const brands = timeBrandMap.get(time);
        const avgBrand = brands.reduce((sum, brand) => sum + brand, 0) / brands.length;
        brandDataForLine.push(avgBrand);
      });

      // 销毁旧图表
      this.destroyCharts();

      // 渲染新图表
      this.renderBarChart({ carBrandLabels, scoreDataForBar });
      this.renderLineChart({ timeLabels, scoreDataForLine });
      this.renderNewLineChart({ timeLabels, brandDataForLine });
    },
    // 数据处理
    processData() {
      const { selectedTimeUnit, rawData } = this;
      const carBrandScoreMap = new Map();
      const timeScoreMap = new Map();
      const brandIndexMap = new Map();
      const timeBrandMap = new Map();
      let brandIndex = 0;

      rawData.forEach(item => {
        const carBrand = item[1];
        const score = item[2];
        const dateString = item[0];
        const formattedTime = formatDateByUnit(dateString, selectedTimeUnit);

        // 处理直方图数据
        if (carBrandScoreMap.has(carBrand)) {
          const [totalScore, count] = carBrandScoreMap.get(carBrand);
          carBrandScoreMap.set(carBrand, [totalScore + score, count + 1]);
        } else {
          carBrandScoreMap.set(carBrand, [score, 1]);
        }

        // 处理折线图数据
        if (timeScoreMap.has(formattedTime)) {
          const [totalScore, count] = timeScoreMap.get(formattedTime);
          timeScoreMap.set(formattedTime, [totalScore + score, count + 1]);
        } else {
          timeScoreMap.set(formattedTime, [score, 1]);
        }

        // 品牌索引
        if (!brandIndexMap.has(carBrand)) {
          brandIndexMap.set(carBrand, brandIndex++);
        }

        // 时间品牌映射
        if (!timeBrandMap.has(formattedTime)) {
          timeBrandMap.set(formattedTime, []);
        }
        timeBrandMap.get(formattedTime).push(brandIndexMap.get(carBrand));
      });

      return {
        carBrandScoreMap,
        timeScoreMap,
        brandIndexMap,
        timeBrandMap
      };
    },
    // 渲染直方图
    renderBarChart(data) {
      const barCtx = this.$refs.userHistoryBarChart.getContext('2d');
      this.chartInstances.barChart = new Chart(barCtx, {
        type: 'bar',
        data: {
          labels: data.carBrandLabels,
          datasets: [{
            label: '汽车品牌平均得分分布',
            data: data.scoreDataForBar,
            backgroundColor: 'rgba(75, 192, 192, 0.2)',
            borderColor: 'rgb(75, 192, 192)',
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: {
              beginAtZero: true
            }
          }
        }
      });
    },
    // 渲染折线图
    renderLineChart(data) {
      const lineCtx = this.$refs.userHistoryLineChart.getContext('2d');
      this.chartInstances.lineChart = new Chart(lineCtx, {
        type: 'line',
        data: {
          labels: data.timeLabels,
          datasets: [{
            label: '推荐分数变化',
            data: data.scoreDataForLine,
            fill: false,
            borderColor: 'rgba(255, 99, 132, 1)',
            tension: 0.1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: {
              beginAtZero: true
            }
          }
        }
      });
    },
    // 渲染新折线图
    renderNewLineChart(data) {
      console.log("新折线图",data);
      const newLineCtx = this.$refs.newUserHistoryLineChart.getContext('2d');
      this.chartInstances.newLineChart = new Chart(newLineCtx, {
        type: 'line',
        data: {
          labels: data.timeLabels,
          datasets: [{
            label: '车品牌随时间变化',
            data: data.brandDataForLine,
            fill: false,
            borderColor: 'rgba(54, 162, 235, 1)',
            tension: 0.1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            x: {
              type: 'time',
              time: {
                unit: this.selectedTimeUnit
              },
              title: {
                display: true,
                text: '时间'
              }
            },
            y: {
              title: {
                display: true,
                text: '车品牌索引'
              }
            }
          }
        }
      });
    }
  }
};
</script>

<style scoped>
.chart-container {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 800px;
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.time-unit-selector {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
}

.time-unit-selector label {
  margin-right: 0.5rem;
}

.time-unit-selector select {
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.chart-item {
  width: 100%;
  height: 40vh; /* 使用视口高度 */
  min-height: 250px; /* 最小高度 */
  position: relative;
}

canvas {
  width: 100% !important;
  height: 100% !important;
}
</style>