<template>
  <div class="dashboard-container">
    <h1>用户行为分析仪表盘</h1>

    <div class="stats-container">
      <div class="stat-card">
        <h3>总用户数</h3>
        <p>{{ userStats.total }}</p>
      </div>
      <div class="stat-card">
        <h3>平均年龄</h3>
        <p>{{ userStats.avgAge }}岁</p>
      </div>
      <div class="stat-card">
        <h3>地区数量</h3>
        <p>{{ userStats.regions }}</p>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-container">
        <v-chart :option="ageChartOption" style="height: 400px;"></v-chart>
      </div>
      <div class="chart-container">
        <v-chart :option="regionChartOption" style="height: 400px;"></v-chart>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-container">
        <v-chart :option="trendChartOption" style="height: 400px;"></v-chart>
      </div>
      <div class="chart-container">
        <v-chart :option="brandChartOption" style="height: 400px;"></v-chart>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import VChart from 'vue-echarts';
import 'echarts';

const users = ref([]);
const recommendations = ref([]);
const loading = ref(true);

const fetchData = async () => {
  try {
    const [usersRes, recRes] = await Promise.all([
      axios.get('/api/history/users/'),
      axios.get('/api/history/recommendation-history')
    ]);
    users.value = usersRes.data;
    recommendations.value = recRes.data;
    loading.value = false;
  } catch (error) {
    console.error('Error fetching data:', error);
    loading.value = false;
  }
};

onMounted(async () => {
  await fetchData();
});

const processUserData = () => {
  const ageData = users.value.reduce((acc, user) => {
    const ageGroup = `${Math.floor(user.age / 10) * 10}-${Math.floor(user.age / 10) * 10 + 9}`;
    acc[ageGroup] = (acc[ageGroup] || 0) + 1;
    return acc;
  }, {});

  const regionData = users.value.reduce((acc, user) => {
    acc[user.region] = (acc[user.region] || 0) + 1;
    return acc;
  }, {});

  return { ageData, regionData };
};

const processRecommendationData = () => {
  const sortedData = [...recommendations.value].sort((a, b) =>
      new Date(a.timestamp) - new Date(b.timestamp)
  );

  const trendData = sortedData.map(item => ({
    date: item.timestamp.split('T')[0],
    score: item.score,
    car: item.carName
  }));

  const brandScores = recommendations.value.reduce((acc, item) => {
    if (!acc[item.carName]) {
      acc[item.carName] = { total: 0, count: 0 };
    }
    acc[item.carName].total += item.score;
    acc[item.carName].count += 1;
    return acc;
  }, {});

  const brandData = Object.entries(brandScores).map(([brand, { total, count }]) => ({
    brand,
    average: total / count
  })).sort((a, b) => b.average - a.average);

  return { trendData, brandData };
};

const { ageData, regionData } = processUserData();
const { trendData, brandData } = processRecommendationData();

const ageChartOption = {
  title: { text: '用户年龄分布' },
  tooltip: {},
  xAxis: {
    type: 'category',
    data: Object.keys(ageData).map(key => `${key}岁`)
  },
  yAxis: { type: 'value' },
  series: [{
    data: Object.values(ageData),
    type: 'bar',
    itemStyle: { color: '#5470C6' }
  }]
};

const regionChartOption = {
  title: { text: '用户地区分布' },
  tooltip: {
    formatter: '{b}: {c} ({d}%)'
  },
  series: [{
    type: 'pie',
    radius: '60%',
    data: Object.entries(regionData).map(([name, value]) => ({
      name, value,
      itemStyle: {
        color: ['#91CC75', '#FAC858', '#EE6666', '#73C0DE', '#3BA272'][Object.keys(regionData).indexOf(name) % 5]
      }
    })),
    emphasis: {
      itemStyle: {
        shadowBlur: 10,
        shadowOffsetX: 0,
        shadowColor: 'rgba(0, 0, 0, 0.5)'
      }
    }
  }]
};

const trendChartOption = {
  title: { text: '用户4的汽车评分趋势' },
  tooltip: {
    formatter: (params) => {
      const data = trendData[params.dataIndex];
      return `日期: ${data.date}<br/>评分: ${data.score}<br/>车型: ${data.car}`;
    }
  },
  xAxis: {
    type: 'category',
    data: trendData.map(item => item.date),
    axisLabel: { rotate: 45 }
  },
  yAxis: {
    type: 'value',
    min: 0,
    max: 10
  },
  series: [{
    data: trendData.map(item => item.score),
    type: 'line',
    smooth: true,
    symbolSize: 8,
    lineStyle: { width: 3 },
    itemStyle: { color: '#EE6666' }
  }]
};

const brandChartOption = {
  title: { text: '各汽车品牌平均评分' },
  tooltip: {},
  xAxis: {
    type: 'category',
    data: brandData.map(item => item.brand),
    axisLabel: { rotate: 45 }
  },
  yAxis: {
    type: 'value',
    min: 0,
    max: 10
  },
  series: [{
    data: brandData.map(item => item.average),
    type: 'bar',
    itemStyle: {
      color: (params) => params.value > 5 ? '#91CC75' : '#EE6666'
    }
  }]
};

const userStats = {
  total: users.value.length,
  avgAge: (users.value.reduce((sum, user) => sum + user.age, 0) / users.value.length).toFixed(1),
  regions: Object.keys(regionData).length
};
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.stats-container {
  display: flex;
  justify-content: space-around;
  margin: 20px 0;
}

.stat-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  width: 30%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-card h3 {
  margin: 0 0 10px 0;
  color: #666;
}

.stat-card p {
  font-size: 24px;
  font-weight: bold;
  margin: 0;
  color: #333;
}

.charts-row {
  display: flex;
  margin-bottom: 20px;
}

.chart-container {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 15px;
  margin: 0 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.loading {
  text-align: center;
  padding: 50px;
  font-size: 18px;
}

@media (max-width: 768px) {
  .charts-row {
    flex-direction: column;
  }

  .chart-container {
    margin: 10px 0;
  }

  .stats-container {
    flex-direction: column;
  }

  .stat-card {
    width: 100%;
    margin-bottom: 10px;
  }
}
</style>    