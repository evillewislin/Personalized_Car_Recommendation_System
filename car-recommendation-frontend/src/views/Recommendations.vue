<template>
  <div class="recommendations-container">
    <h1>个性化汽车推荐</h1>
    <nav class="nav-container">
      <div class="nav-left">
        <router-link to="/">首页</router-link>
        <router-link to="/recommendations">个性化推荐</router-link>
      </div>
    </nav>

    <button @click="fetchAIRecommendations" class="ai-recommend-btn" :disabled="loading">
      {{ loading ? '加载中...' : 'AI智能推荐' }}
    </button>

    <table v-if="cars.length" class="recommendations-table">
      <thead>
      <tr>
        <th>品牌</th>
        <th>车型</th>
        <th>价格区间</th>
        <th>图片</th>
        <th>推荐指数</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="car in cars" :key="car.name">
        <td>{{ car.name }}</td>
        <td>{{ car.fullName }}</td>
        <td>{{ car.priceRange }}万</td>
        <td><img :src="car.imageUrl" alt="car.name" class="car-image"/></td>
        <td>{{ car.score?.toFixed(2) }}</td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import { ref } from 'vue';
import axios from 'axios';

export default {
  setup() {
    const cars = ref([]);
    const loading = ref(false);

    const fetchAIRecommendations = async () => {
      loading.value = true;
      cars.value = [];

      try {
        const token = localStorage.getItem('token');
        console.log(token)
        if (!token) {
          throw new Error('Token is missing');
        }
        const response = await axios.post('/ai/recommend',{}, {
          headers: { Authorization: `Bearer ${token}` }
        });

        if (response.data.length > 0) {

          cars.value = response.data;
        }
      } catch (error) {
        console.error('获取AI推荐失败:', error);
      } finally {
        loading.value = false;
      }
    };
    return { cars, fetchAIRecommendations, loading };
  }
};
</script>

<style>
.recommendations-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
}
.recommendations-table th, .recommendations-table td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: center;
}
.recommendations-table th {
  background-color: #f4f4f4;
}
.car-image {
  width: 100px;
  height: auto;
}
</style>