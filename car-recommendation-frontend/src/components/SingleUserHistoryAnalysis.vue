<template>
  <div class="chart-container">
    <!-- 时间单位选择器 -->
    <div class="time-unit-selector">
      <label for="time-unit">时间单位：</label>
      <select v-model="selectedTimeUnit" id="time-unit" @change="handleTimeUnitChange">
        <option value="year">年</option>
        <option value="month">月</option>
        <option value="day">日</option>
      </select>
    </div>

    <!-- 新增数据为空时的提示信息 -->
    <div v-if="!hasData" class="no-data-tip">没有历史数据</div>

    <!-- 图表容器 - 并排显示 -->
    <div v-if="hasData" class="chart-row">
      <div class="chart-item">
        <canvas ref="userHistoryBarChart" v-show="!isLoading"></canvas>
      </div>
      <div class="chart-item">
        <canvas ref="userHistoryLineChart" v-show="!isLoading"></canvas>
      </div>
    </div>
  </div>
</template>

<script>
import { Chart, registerables } from 'chart.js';
import axios from 'axios';
import { useUserStore } from '@/store';
import { nextTick } from 'vue';
import 'chartjs-adapter-date-fns';

Chart.register(...registerables);

export default {
  data() {
    return {
      selectedTimeUnit: 'day',
      barChart: null,
      lineChart: null,
      hasData: false,
      isLoading: true
    };
  },
  mounted() {
    this.initializeCharts();
  },
  beforeUnmount() {
    this.destroyCharts();
  },
  methods: {
    async initializeCharts() {
      try {
        this.isLoading = true;
        await this.fetchDataAndRenderChart();
      } catch (error) {
        console.error('初始化图表失败:', error);
      } finally {
        this.isLoading = false;
      }
    },

    handleTimeUnitChange() {
      this.destroyCharts();
      this.initializeCharts();
    },

    destroyCharts() {
      if (this.barChart) {
        this.barChart.destroy();
        this.barChart = null;
      }
      if (this.lineChart) {
        this.lineChart.destroy();
        this.lineChart = null;
      }
    },

    async fetchDataAndRenderChart() {
      try {
        const userStore = useUserStore();
        const role = userStore.role;
        const token = userStore.token;
        const userId = this.getUserIdFromToken(token);

        let url = '/api/history/user-history-analysis';
        if (role === 'user' && userId !== null) {
          url += `?userId=${userId}`;
        }

        const response = await axios.get(url);
        const data = response.data;

        if (!data || data.length === 0) {
          this.hasData = false;
          return;
        }

        this.hasData = true;

        // Process data
        const {carBrandLabels, scoreDataForBar, timeLabels, scoreDataForLine} = this.processChartData(data);

        // 检查数据有效性
        if (
            carBrandLabels.length === 0 ||
            scoreDataForBar.length === 0 ||
            carBrandLabels.length !== scoreDataForBar.length ||
            timeLabels.length === 0 ||
            scoreDataForLine.length === 0 ||
            timeLabels.length !== scoreDataForLine.length
        ) {
          this.hasData = false;
          console.error('图表数据无效');
          return;
        }

        if (this.isValidData(scoreDataForBar) && this.isValidData(scoreDataForLine)) {
          await nextTick();

          // Ensure canvas elements are available
          if (!this.$refs.userHistoryBarChart || !this.$refs.userHistoryLineChart) {
            console.error('Canvas elements not found');
            return;
          }

          // Render charts
          this.renderBarChart(carBrandLabels, scoreDataForBar);
          this.renderLineChart(timeLabels, scoreDataForLine);
        } else {
          this.hasData = false;
        }
      } catch (error) {
        this.hasData = false;
        console.error('获取数据失败:', error);
      }
    },

    processChartData(data) {
      const carBrandScoreMap = new Map();
      const timeScoreMap = new Map();

      data.forEach(item => {
        const carBrand = item[1];
        const score = item[2];
        const timestampStr = item[0];

        if (!carBrand || score === undefined || !timestampStr) return;

        const timestamp = new Date(timestampStr).getTime();
        const formattedTime = this.formatDateByUnit(timestamp, this.selectedTimeUnit);

        // Process bar chart data
        if (carBrandScoreMap.has(carBrand)) {
          const [totalScore, count] = carBrandScoreMap.get(carBrand);
          carBrandScoreMap.set(carBrand, [totalScore + score, count + 1]);
        } else {
          carBrandScoreMap.set(carBrand, [score, 1]);
        }

        // Process line chart data
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

      return {carBrandLabels, scoreDataForBar, timeLabels, scoreDataForLine};
    },

    renderBarChart(labels, data) {
      const barCtx = this.$refs.userHistoryBarChart?.getContext('2d');
      if (!barCtx || this.barChart) return;
      this.barChart = new Chart(barCtx, {
        type: 'bar',
        data: {
          labels: labels,
          datasets: [{
            label: '汽车品牌平均得分分布',
            data: data,
            backgroundColor: 'rgba(235, 148, 102, 0.6)',
            borderColor: 'rgb(235, 148, 102)',
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: {
              beginAtZero: true,
              grid: {
                color: 'rgba(0, 0, 0, 0.1)',
                drawBorder: false
              }
            }
          },
          plugins: {
            legend: {
              position: 'top',
              labels: {
                color: '#333'
              }
            }
          }
        }
      });
    },

    renderLineChart(labels, data) {
      const lineCtx = this.$refs.userHistoryLineChart?.getContext('2d');
      if (!lineCtx || this.lineChart) return;
      this.lineChart = new Chart(lineCtx, {
        type: 'line',
        data: {
          labels: labels,
          datasets: [{
            label: '推荐分数变化',
            data: data,
            fill: false,
            borderColor: 'rgba(54, 162, 235, 1)',
            tension: 0.1,
            pointRadius: 4,
            pointBackgroundColor: 'white',
            pointBorderColor: 'rgba(54, 162, 235, 1)',
            pointBorderWidth: 2
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
                  week: 'yyyy-WW',
                  day: 'yyyy-MM-dd'
                }
              },
              title: {
                display: true,
                text: '时间',
                color: '#333'
              },
              grid: {
                color: 'rgba(0, 0, 0, 0.1)',
                drawBorder: false
              }
            },
            y: {
              beginAtZero: true,
              title: {
                display: true,
                text: '平均评分',
                color: '#333'
              },
              grid: {
                color: 'rgba(0, 0, 0, 0.1)',
                drawBorder: false
              }
            }
          },
          plugins: {
            legend: {
              position: 'top',
              labels: {
                color: '#333'
              }
            }
          }
        }
      });
    },

    getUserIdFromToken(token) {
      if (!token) return null;
      try {
        token = token.replace("Bearer ", "");
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => `%${('00' + c.charCodeAt(0).toString(16)).slice(-2)}`).join(''));
        const payload = JSON.parse(jsonPayload);
        return parseInt(payload.sub);
      } catch (error) {
        console.error('解析 Token 出错:', error);
        return null;
      }
    },

    formatDateByUnit(timestamp, unit) {
      const date = new Date(timestamp);
      switch (unit) {
        case 'year':
          return date.getFullYear();
        case 'month':
          return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
        case 'week': {
          const year = date.getFullYear();
          const weekNumber = Math.ceil((date - new Date(year, 0, 1 - (new Date(year, 0, 1).getDay() || 7))) / (7 * 24 * 60 * 60 * 1000));
          return `${year}-W${weekNumber.toString().padStart(2, '0')}`;
        }
        case 'day':
          return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
        default:
          return timestamp;
      }
    },

    isValidData(data) {
      return data.every((item, index) => {
        if (index === 2) {
          return typeof item === 'number' && item >= 0 && item <= 10;
        }
        return item !== undefined && item !== null;
      });
    }
  }
};
</script>

<style scoped>
.chart-container {
  background: #f9f9f9;
  padding: 2rem;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.time-unit-selector {
  display: flex;
  align-items: center;
  margin-bottom: 1.5rem;
}

.time-unit-selector label {
  margin-right: 1rem;
  font-size: 1.1rem;
  color: #333;
}

.time-unit-selector select {
  padding: 0.6rem 1rem;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 1rem;
  color: #666;
}

.chart-row {
  display: flex;
  flex-wrap: wrap;
  gap: 2rem;
  width: 100%;
}

.chart-item {
  flex: 1;
  min-width: 350px;
  height: 45vh;
  min-height: 300px;
  position: relative;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
  border-radius: 8px;
  overflow: hidden;
  background-color: white;
}

canvas {
  width: 100% !important;
  height: 100% !important;
}

.no-data-tip {
  text-align: center;
  color: #999;
  padding: 30px;
  font-size: 1.1rem;
}

@media (max-width: 768px) {
  .chart-row {
    flex-direction: column;
  }

  .chart-item {
    width: 100%;
  }
}
</style>