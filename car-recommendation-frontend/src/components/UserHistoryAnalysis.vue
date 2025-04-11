<template>
  <div>
    <h1 class="text-2xl font-bold mb-4">用户历史分析</h1>
    <canvas ref="scoreChart" class="mb-8 score-chart"></canvas>
    <canvas ref="ageDistributionChart" class="age-distribution-chart"></canvas>
  </div>
</template>

<script setup>
import {ref, onMounted, nextTick} from 'vue';
import axios from 'axios';
import Chart from 'chart.js/auto';

const scoreChartRef = ref(null);
const ageDistributionChartRef = ref(null);
const users = ref([]);
const recommendationHistory = ref([]);

const fetchData = async () => {
  try {
    const [usersResponse, historyResponse] = await Promise.all([
      axios.get('/api/history/users'),
      axios.get('/api/history/recommendation-history')
    ]);
    users.value = usersResponse.data;
    recommendationHistory.value = historyResponse.data;
    console.log('用户数据:', users.value);
    console.log('推荐历史数据:', recommendationHistory.value);
  } catch (error) {
    console.error('获取数据时出错:', error);
  }
};

onMounted(async () => {
  await fetchData();

  await nextTick(); // 确保 DOM 更新后再创建 Chart 实例

  // 绘制推荐分数图表
  if (scoreChartRef.value) {
    const scoreChart = new Chart(scoreChartRef.value, {
      type: 'bar',
      data: {
        labels: recommendationHistory.value.map(history => history.name),
        datasets: [{
          label: '推荐分数',
          data: recommendationHistory.value.map(history => history.score),
          backgroundColor: 'rgba(75, 192, 192, 0.2)',
          borderColor: 'rgba(75, 192, 192, 1)',
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
  }

  // 绘制用户年龄分布图表
  if (ageDistributionChartRef.value) {
    const ageData = users.value.map(user => user.age);
    const ageDistributionChart = new Chart(ageDistributionChartRef.value, {
      type: 'histogram',
      data: {
        datasets: [{
          label: '用户年龄分布',
          data: ageData,
          backgroundColor: 'rgba(54, 162, 235, 0.2)',
          borderColor: 'rgba(54, 162, 235, 1)',
          borderWidth: 1
        }]
      },
      options: {
        scales: {
          x: {
            title: {
              display: true,
              text: '年龄'
            }
          },
          y: {
            title: {
              display: true,
              text: '用户数量'
            }
          }
        }
      }
    });
  }
});
</script>

<style scoped>
.score-chart,
.age-distribution-chart {
  width: 100%;
  height: 300px;
}
</style>