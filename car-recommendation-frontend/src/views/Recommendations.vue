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
    <!-- 使用 v-if 确保 car 存在 -->
    <div class="car-info" v-if="car">
      <h2>{{ car.name }}</h2>
      <p class="full-name">{{ car.fullName }}</p>
      <p>价格区间: {{ car.priceRange }}万</p>
      <!-- 使用可选链操作符确保 score 存在 -->
      <p>推荐指数: {{ car.score?.toFixed(2) }}</p>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import axios from 'axios';

export default {
  setup() {
    const car = ref(null); // 初始化为 null
    const loading = ref(false);

    const fetchAIRecommendations = async () => {
      loading.value = true;
      try {
        const token = localStorage.getItem('token');
        console.log("token:", token);
        if (!token) {
          console.log("Error");
          throw new Error('Token is missing');
        }

        const response = await axios.post(
            '/ai/recommend',
            {}, // 空对象作为请求体
            {
              headers: { Authorization: `Bearer ${token}` }
            }
        );

        if (response.data.length > 0) {
          car.value = {
            id: response.data[0].name,
            name: response.data[0].name,
            fullName: response.data[0].fullName,
            priceRange: response.data[0].priceRange,
            imageUrl: response.data[0].imageUrl,
            score: response.data[0].avgScore // 确保后端返回了 avgScore
          };
        }
      } catch (error) {
        if (error.response && error.response.status === 403) {
          console.error('Access denied: Check your token or permissions');
          alert('Access denied: Please check your login status or permissions');
        } else {
          console.error('获取AI推荐失败:', error);
        }
      } finally {
        loading.value = false;
      }
    };

    return {car, fetchAIRecommendations, loading};
  }
};
</script>

<style>
.nav-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background-color: #f0f0f0;
  border-bottom: 1px solid #ddd;
  position: sticky;
  top: 0;
  z-index: 1000;
}
.nav-left {
  display: flex;
  gap: 2rem;
  margin-left: 2rem;
}

.nav-left a {
  text-decoration: none;
  color: #333;
  font-weight: 500;
  padding: 0.5rem;
  border-radius: 4px;
}

.nav-left a:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.nav-left a.router-link-exact-active {
  color: #4CAF50;
  border-bottom: 2px solid #4CAF50;
}
.ai-recommend-btn {
  background-color: #2196F3;
  margin-bottom: 20px;
  padding: 12px 24px;
  font-size: 16px;
  border: none;
  color: white;
  cursor: pointer;
}

.ai-recommend-btn:hover {
  background-color: #1976D2;
}

.ai-recommend-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
</style>