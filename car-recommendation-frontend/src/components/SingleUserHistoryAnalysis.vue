<template>
  <div>
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
    // 去除可能存在的 "Bearer " 前缀
    token = token.replace("Bearer ", "");
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    const payload = JSON.parse(jsonPayload);
    return parseInt(payload.sub); // 从 sub 字段获取 userId
  } catch (error) {
    console.error('解析 Token 出错:', error);
    return null;
  }
};

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
/* 用户历史分析的图表容器 */
.chart-container {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  margin-top: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
.chart-container {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 800px;
  max-height: 800px;
  display: flex;
  flex-direction: column;
  flex-grow: 1; /* 让图表容器占满剩余空间 */
}
/* 图表的 canvas */
canvas {
  width: 100% !important;
  height: 400px !important;
  margin-bottom: 20px;
}
</style>