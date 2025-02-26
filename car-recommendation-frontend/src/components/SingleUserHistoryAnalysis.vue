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
import { useUserStore } from '@/store';
import 'chartjs-adapter-date-fns'; // 引入时间轴适配器

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
const formatDateByUnit = (timestamp, unit) => {
  const date = new Date(timestamp);
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
      return timestamp;
  }
};

// 检查数据是否包含 undefined 或 null 值
const isValidData = (data) => {
  return data.every(item => item !== undefined && item !== null);
};

export default {
  data() {
    return {
      selectedTimeUnit: 'day', // 默认时间单位
      barChart: null,
      lineChart: null,
      // 定义 chartInstances
      chartInstances: {
        newLineChart: null
      }
    };
  },
  watch: {
    selectedTimeUnit(newUnit) {
      // 当时间单位改变时，销毁之前的图表实例
      if (this.barChart) {
        this.barChart.destroy();
      }
      if (this.lineChart) {
        this.lineChart.destroy();
      }
      if (this.chartInstances.newLineChart) {
        this.chartInstances.newLineChart.destroy();
      }
      // 重新绘制图表
      this.fetchDataAndRenderChart();
    }
  },
  mounted() {
    Chart.register(...registerables);
    this.fetchDataAndRenderChart();
  },
  methods: {
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
        const data = response.data;

        if (!data || data.length === 0) {
          console.error('数据为空，无法绘制图表');
          return;
        }

        const carBrandScoreMap = new Map();
        const timeScoreMap = new Map();

        data.forEach(item => {
          const carBrand = item[1];
          const score = item[2];
          const timestamp = item[0];
          const formattedTime = formatDateByUnit(timestamp, this.selectedTimeUnit);

          // 处理直方图数据：合并相同品牌并计算平均分数
          if (carBrandScoreMap.has(carBrand)) {
            const [totalScore, count] = carBrandScoreMap.get(carBrand);
            carBrandScoreMap.set(carBrand, [totalScore + score, count + 1]);
          } else {
            carBrandScoreMap.set(carBrand, [score, 1]);
          }

          // 处理折线图数据：按时间单位聚合分数
          if (timeScoreMap.has(formattedTime)) {
            const [totalScore, count] = timeScoreMap.get(formattedTime);
            timeScoreMap.set(formattedTime, [totalScore + score, count + 1]);
          } else {
            timeScoreMap.set(formattedTime, [score, 1]);
          }
        });

        const carBrandLabels = [];
        const scoreDataForBar = [];

        carBrandScoreMap.forEach(([totalScore, count], carBrand) => {
          carBrandLabels.push(carBrand);
          scoreDataForBar.push(totalScore / count);
        });

        const timeLabels = [];
        const scoreDataForLine = [];

        const sortedTimes = Array.from(timeScoreMap.keys()).sort();
        sortedTimes.forEach(time => {
          const [totalScore, count] = timeScoreMap.get(time);
          timeLabels.push(time);
          scoreDataForLine.push(totalScore / count);
        });

        if (isValidData(scoreDataForBar) && isValidData(scoreDataForLine)) {
          // 绘制直方图
          const barCtx = this.$refs.userHistoryBarChart.getContext('2d');
          this.barChart = new Chart(barCtx, {
            type: 'bar',
            data: {
              labels: carBrandLabels,
              datasets: [{
                label: '汽车品牌平均得分分布',
                data: scoreDataForBar,
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

          // 绘制折线图
          const lineCtx = this.$refs.userHistoryLineChart.getContext('2d');
          this.lineChart = new Chart(lineCtx, {
            type: 'line',
            data: {
              labels: timeLabels,
              datasets: [{
                label: '推荐分数变化',
                data: scoreDataForLine,
                fill: false,
                borderColor: 'rgba(255, 99, 132, 1)',
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
                    unit: this.selectedTimeUnit,
                    tooltipFormat: 'yyyy-MM-dd',
                    displayFormats: {
                      year: 'yyyy',
                      month: 'yyyy-MM',
                      week: 'yyyy-II',
                      day: 'yyyy-MM-dd'
                    }
                  },
                  title: {
                    display: true,
                    text: '时间'
                  }
                },
                y: {
                  beginAtZero: true,
                  title: {
                    display: true,
                    text: '平均评分'
                  }
                }
              }
            }
          });
        } else {
          console.error('数据包含无效值，无法绘制图表');
        }

        // 模拟新折线图数据
        const newLineData = {
          timeLabels: timeLabels,
          brandDataForLine: scoreDataForLine.map((score, index) => index + 1) // 简单示例数据
        };
        this.renderNewLineChart(newLineData);
      } catch (error) {
        console.error('获取数据失败:', error);
      }
    },
    // 将 renderNewLineChart 方法移动到 methods 内部
    renderNewLineChart(data) {
      console.log("新折线图", data);
      const newLineCtx = this.$refs.newUserHistoryLineChart.getContext('2d');
      if (this.chartInstances.newLineChart) {
        this.chartInstances.newLineChart.destroy();
      }
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