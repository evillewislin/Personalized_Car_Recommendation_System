<template>
  <div>
    <h2>用户历史分析</h2>
    <div class="chart-container">
      <canvas ref="userHistoryChart"></canvas>
    </div>
  </div>
</template>

<script>
import { Chart, registerables } from 'chart.js';
import axios from 'axios';

export default {
  mounted() {
    // 注册所有可用的插件和控制器
    Chart.register(...registerables);
    this.fetchDataAndRenderChart();
  },
  methods: {
    async fetchDataAndRenderChart() {
      try {
        const response = await axios.get('/api/user-history-analysis');
        const data = response.data;

        const labels = [];
        const datasetData = [];

        data.forEach(item => {
          labels.push(item[0].toString()); // timestamp 作为 x 轴标签
          datasetData.push(item[1]); // car_brand 的 name 作为 y 轴数据
        });

        const ctx = this.$refs.userHistoryChart.getContext('2d');
        new Chart(ctx, {
          type: 'bar',
          data: {
            labels: labels,
            datasets: [{
              label: '用户历史数据',
              data: datasetData,
              backgroundColor: 'rgba(75, 192, 192, 0.2)',
              borderColor: 'rgb(75, 192, 192)',
              borderWidth: 1
            }]
          },
          options: {
            scales: {
              y: {
                beginAtZero: true
              }
            }
          }
        });
      } catch (error) {
        console.error('获取数据失败:', error);
      }
    }
  }
};
</script>

<style scoped>
.chart-container {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  margin-top: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style>