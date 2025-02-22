<template>
  <div>
    <!-- 错误提示信息 -->
    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
    <!-- 收藏历史表格 -->
    <table v-if="!loading && favoriteHistory.length" class="favorite-history-table">
      <thead>
      <tr>
        <th>品牌名</th>
        <th>全名</th>
        <th>价格范围</th>
        <th>平均评分</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="item in favoriteHistory" :key="item.fullName">
        <td>{{ item.name }}</td>
        <td>{{ item.fullName }}</td>
        <td>{{ item.priceRange }}</td>
        <td>{{ item.avgScore }}</td>
      </tr>
      </tbody>
    </table>
    <div v-else-if="loading">加载中...</div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import axios from 'axios';

export default {
  setup() {
    const loading = ref(true);
    const errorMessage = ref('');
    const favoriteHistory = ref([]);

    const fetchFavoriteHistory = async () => {
      loading.value = true;
      errorMessage.value = '';
      favoriteHistory.value = [];
      try {
        const token = localStorage.getItem('token');
        if (!token) {
          throw new Error('Token is missing');
        }
        const cleanToken = token.replace('Bearer ', '');

        const recommendResponseData = await axios.post('/api/ai/recommend', null, {
          headers: {
            'Authorization': `Bearer ${cleanToken}`
          }
        });
        favoriteHistory.value = recommendResponseData.data;
      } catch (error) {
        console.error('获取默认推荐数据失败:', error);
        errorMessage.value = '获取收藏历史信息失败，请稍后重试。';
      } finally {
        loading.value = false;
      }
    };

    onMounted(async () => {
      await fetchFavoriteHistory();
    });

    return {
      loading,
      errorMessage,
      favoriteHistory
    };
  }
};
</script>

<style scoped>
/* 样式保持不变 */
.recommendations-container {
  display: flex;
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  font-family: 'Arial', sans-serif;
}

.sidebar {
  width: 20%;
  padding: 10px;
  background-color: #f4f4f4;
  border-right: 1px solid #ddd;
}

.sidebar-btn {
  display: block;
  width: 100%;
  padding: 10px;
  margin-bottom: 10px;
  background-color: #1a73e8;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  color: white;
  cursor: pointer;
  text-decoration: none;
  text-align: center;
}

.sidebar-btn:hover {
  background-color: #003c8f;
}

.main-content {
  width: 80%;
  padding: 10px;
}

.nav-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #333;
  padding: 15px 30px;
}

.nav-left {
  display: flex;
  gap: 20px;
}

.nav-left a {
  text-decoration: none;
  color: white;
  font-size: 16px;
}

.nav-left a:hover {
  color: #ffeb3b;
}

.error-message {
  margin-top: 20px;
  color: red;
}

.favorite-history-table {
  width: 100%;
  margin-top: 30px;
  border-collapse: collapse;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.favorite-history-table th,
.favorite-history-table td {
  padding: 10px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.favorite-history-table th {
  background-color: #1a73e8;
  color: white;
}

.favorite-history-table td {
  background-color: #f9f9f9;
}
</style>