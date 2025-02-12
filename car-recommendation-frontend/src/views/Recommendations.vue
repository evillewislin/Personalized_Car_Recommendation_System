<template>
  <div class="recommendations-container">
    <h1>个性化汽车推荐</h1>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="recommendations.length === 0" class="no-data">暂无推荐数据</div>
    <div v-else class="car-list">
      <div v-for="car in recommendations" :key="car.id" class="car-card">
        <img :src="car.imageUrl" :alt="car.name" class="car-image"/>
        <div class="car-info">
          <h2>{{ car.name }}</h2>
          <p>推荐指数: {{ car.score.toFixed(2) }}</p>
          <button @click="viewCarDetails(car.id)">查看详情</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import axios from 'axios';

export default {
  name: 'RecommendationList',
  setup() {
    const recommendations = ref([]);
    const loading = ref(true);

    // 获取推荐车辆数据
    const fetchRecommendations = async () => {
      try {
        const response = await axios.get('/api/recommendations'); // 假设后端提供此 API
        recommendations.value = response.data
            .sort((a, b) => b.score - a.score) // 按评分降序排序
            .slice(0, 10); // 只取前 10 条推荐
      } catch (error) {
        console.error('获取推荐数据失败:', error);
      } finally {
        loading.value = false;
      }
    };

    // 查看车辆详情
    const viewCarDetails = (carId) => {
      console.log(`查看车辆 ${carId} 的详情`);
      // 这里可以跳转到车辆详情页
    };

    onMounted(fetchRecommendations);

    return { recommendations, loading, viewCarDetails };
  }
};
</script>

<style scoped>
.recommendations-container {
  padding: 20px;
  text-align: center;
}

.car-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 20px;
}

.car-card {
  width: 250px;
  padding: 15px;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.car-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 8px;
}

.car-info h2 {
  font-size: 18px;
  margin: 10px 0;
}

button {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 4px;
}

button:hover {
  background-color: #45a049;
}

.loading, .no-data {
  font-size: 18px;
  color: #666;
}
</style>
