<template>
  <div>
    <h2>用户历史分析</h2>
    <div class="chart-container">
      <canvas ref="userHistoryBarChart"></canvas>
      <canvas ref="userHistoryLineChart"></canvas>
    </div>
  </div>
</template>

<script>
import {Chart, registerables} from 'chart.js';
import axios from 'axios';
import {useUserStore} from '@/store';

// 格式化时间戳为月日
const formatTimestamp = (timestamp) => {
  const date = new Date(timestamp);
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${month}-${day}`;
};

export default {
  mounted() {
    // 注册所有可用的插件和控制器
    Chart.register(...registerables);
    this.fetchDataAndRenderChart();
  },
  methods: {
    async fetchDataAndRenderChart() {
      try {
        // 始终使用固定的 URL
        const url = '/api/user-history-analysis';

        const response = await axios.get(url);
        const data = response.data;

        const carBrandScoreMap = new Map();
        const timestampLabels = [];
        const scoreDataForLine = [];

        data.forEach(item => {
          const carBrand = item[1];
          const score = item[2];
          const timestamp = item[0];

          // 处理直方图数据：合并相同品牌并计算平均分数
          if (carBrandScoreMap.has(carBrand)) {
            const [totalScore, count] = carBrandScoreMap.get(carBrand);
            carBrandScoreMap.set(carBrand, [totalScore + score, count + 1]);
          } else {
            carBrandScoreMap.set(carBrand, [score, 1]);
          }

          // 处理折线图数据：格式化时间戳
          timestampLabels.push(formatTimestamp(timestamp));
          scoreDataForLine.push(score);
        });

        const carBrandLabels = [];
        const scoreDataForBar = [];

        carBrandScoreMap.forEach(([totalScore, count], carBrand) => {
          carBrandLabels.push(carBrand);
          scoreDataForBar.push(totalScore / count);
        });

        // 绘制直方图
        const barCtx = this.$refs.userHistoryBarChart.getContext('2d');
        new Chart(barCtx, {
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
            scales: {
              y: {
                beginAtZero: true
              }
            }
          }
        });

        // 绘制折线图
        const lineCtx = this.$refs.userHistoryLineChart.getContext('2d');
        new Chart(lineCtx, {
          type: 'line',
          data: {
            labels: timestampLabels,
            datasets: [{
              label: '推荐分数变化',
              data: scoreDataForLine,
              fill: false,
              borderColor: 'rgba(255, 99, 132, 1)',
              tension: 0.1
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