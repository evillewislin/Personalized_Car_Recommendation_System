<template>
  <div class="recommendations-container">
    <!-- 导航栏 -->
    <nav class="nav-container">
      <div class="nav-left">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/recommendations" class="nav-link">个性化推荐</router-link>
      </div>
    </nav>
    <!-- AI 推荐按钮 -->
    <button @click="fetchAIRecommendations" class="ai-recommend-btn" :disabled="loading">
      {{ loading ? '加载中...' : 'AI智能推荐' }}
    </button>
    <!-- 错误提示信息 -->
    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
    <!-- ALS 推荐结果显示区域 -->
    <div v-if="alsResponse.length" class="als-response">
      <h2>ALS 推荐结果</h2>
      <table class="recommendations-table">
        <thead>
        <tr>
          <th>品牌</th>
          <th>车型</th>
          <th>价格区间</th>
          <th>推荐指数</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="car in alsResponse" :key="car.name">
          <td>{{ car.name }}</td>
          <td>{{ car.fullName }}</td>
          <td>{{ car.priceRange }}万</td>
          <td>{{ car.score?.toFixed(2) }}</td>
        </tr>
        </tbody>
      </table>
    </div>
    <!-- AI 响应显示区域 -->
    <div v-if="chatResponse.length" class="ai-response">
      <h2>AI 分析响应</h2>
      <table class="recommendations-table">
        <thead>
        <tr>
          <th>品牌</th>
          <th>车型</th>
          <th>价格区间</th>
          <th>推荐指数</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="car in chatResponse" :key="car.name">
          <td>{{ car.name }}</td>
          <td>{{ car.fullName }}</td>
          <td>{{ car.priceRange }}万</td>
          <td>{{ car.score?.toFixed(2) }}</td>
        </tr>
        </tbody>
      </table>
    </div>
    <!-- AI 文本响应显示区域 -->
    <div v-if="aiResponse.length" class="ai-text-response">
      <h2>AI 文本响应</h2>
      <p>{{ aiResponse }}</p>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import axios from 'axios';

export default {
  setup() {
    const chatResponse = ref([]);
    const alsResponse = ref([]);
    const loading = ref(false);
    const errorMessage = ref('');
    const aiResponse = ref('');

    const fetchAIRecommendations = async () => {
      loading.value = true;
      chatResponse.value = [];
      alsResponse.value = [];
      errorMessage.value = '';
      aiResponse.value = '';

      try {
        const token = localStorage.getItem('token');
        console.log('Token:', token);
        if (!token) {
          throw new Error('Token is missing');
        }

        // 调用 recommend 接口
        const recommendResponse = await axios.post('/api/ai/recommend', {
          headers: { Authorization: `Bearer ${token}` }
        });
        const recommendData = recommendResponse.data;

        // 调用 ALS 接口
        const alsResponseData = await axios.get('/api/ai/als', {
          headers: { Authorization: `Bearer ${token}` }
        });
        alsResponse.value = alsResponseData.data;

        if (recommendData.value.length > 0) {
          const dataString = JSON.stringify(recommendData.value);
          const escapedDataString = dataString.replace(/"/g, '\\"');
          const message = `为我分析一下这些汽车推荐：${escapedDataString}`;
          const encodedMessage = encodeURIComponent(message);

          // 调用 AI 聊天接口
          const aiChatResponse = await axios.get('/api/ai/chat', {
            headers: { Authorization: `Bearer ${token}` },
            params: { message: encodedMessage }
          });

          // 假设 chat 接口返回的数据是一个汽车列表
          chatResponse.value = aiChatResponse.data;

          // 处理 AI 文本响应
          aiResponse.value = aiChatResponse.data.join('\n');
        }
      } catch (error) {
        console.error('获取AI推荐失败:', error);
        errorMessage.value = '获取推荐信息失败，请稍后重试。';
      } finally {
        loading.value = false;
      }
    };

    return { chatResponse, alsResponse, fetchAIRecommendations, loading, errorMessage, aiResponse };
  }
};
</script>

<style scoped>
/* 整体容器样式 */
.recommendations-container {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 导航栏容器样式 */
.nav-container {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  background-color: #f8f9fa;
  border-radius: 5px;
  padding: 10px 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

/* 导航链接样式 */
.nav-link {
  text-decoration: none;
  color: #007bff;
  font-size: 1.1rem;
  margin-right: 20px;
  transition: color 0.3s ease;
}

.nav-link:hover {
  color: #0056b3;
}

/* AI 推荐按钮样式 */
.ai-recommend-btn {
  display: block;
  width: 200px;
  margin: 0 auto 20px;
  padding: 12px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 1.1rem;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.ai-recommend-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.ai-recommend-btn:hover:not(:disabled) {
  background-color: #0056b3;
}

/* 错误提示信息样式 */
.error-message {
  color: #dc3545;
  text-align: center;
  font-size: 1.1rem;
  margin-bottom: 20px;
}

/* 推荐表格样式 */
.recommendations-table {
  width: 100%;
  border-collapse: collapse;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.recommendations-table th,
.recommendations-table td {
  border: 1px solid #ddd;
  padding: 12px;
  text-align: center;
}

.recommendations-table th {
  background-color: #f4f4f4;
  color: #333;
  font-weight: 600;
}

.recommendations-table tr:nth-child(even) {
  background-color: #f9f9f9;
}

.recommendations-table tr:hover {
  background-color: #e9ecef;
}

/* AI 响应显示区域样式 */
.ai-response {
  margin-top: 20px;
  padding: 10px;
  background-color: #f8f9fa;
  border: 1px solid #ddd;
  border-radius: 5px;
}

/* ALS 推荐结果显示区域样式 */
.als-response {
  margin-top: 20px;
  padding: 10px;
  background-color: #f8f9fa;
  border: 1px solid #ddd;
  border-radius: 5px;
}

/* AI 文本响应显示区域样式 */
.ai-text-response {
  margin-top: 20px;
  padding: 10px;
  background-color: #f8f9fa;
  border: 1px solid #ddd;
  border-radius: 5px;
}
</style>