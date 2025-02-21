<template>
  <div class="recommendations-container">
    <!-- 导航栏 -->
    <nav class="nav-container">
      <div class="nav-left">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/recommendations" class="nav-link">个性化推荐</router-link>
      </div>
    </nav>
    <!-- 用户输入要求的文本输入框 -->
    <input v-model="userRequest" type="text" placeholder="请输入你的要求" :disabled="loading" />
    <!-- AI 推荐按钮 -->
    <button @click="fetchAIRecommendations" class="ai-recommend-btn" :disabled="loading">
      {{ loading ? '加载中...' : 'AI智能推荐' }}
    </button>
    <!-- 错误提示信息 -->
    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
    <!-- AI 文本响应显示区域 -->
    <div v-if="aiResponse.length" class="ai-text-response">
      <h2>AI 文本响应</h2>
      <div v-html="formattedAiResponse"></div>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue';
import axios from 'axios';

export default {
  setup() {
    const userRequest = ref('');
    const chatResponse = ref([]);
    const recommendResponse = ref([]);
    const loading = ref(false);
    const errorMessage = ref('');
    const aiResponse = ref('');

    const formattedAiResponse = computed(() => {
      if (aiResponse.value) {
        // 假设后端返回的是纯文本，这里简单将其用 <p> 标签包裹显示
        return `<p>${aiResponse.value}</p>`;
      }
      return '';
    });

    const fetchAIRecommendations = async () => {
      loading.value = true;
      chatResponse.value = [];
      recommendResponse.value = [];
      errorMessage.value = '';
      aiResponse.value = '';

      try {
        const token = localStorage.getItem('token');
        console.log('Token:', token);
        if (!token) {
          throw new Error('Token is missing');
        }
        const cleanToken = token.replace('Bearer ', '');

        console.log('Clean Token:', cleanToken);

        // 修正请求头传递方式
        const recommendResponseData = await axios.post('/api/ai/recommend', null, {
          headers: {
            'Authorization': `Bearer ${cleanToken}`
          }
        });

        console.log('推荐结果响应数据:', recommendResponseData);
        recommendResponse.value = recommendResponseData.data;

        if (recommendResponse.value.length > 0) {
          const dataString = JSON.stringify(recommendResponse.value);
          const escapedDataString = dataString.replace(/"/g, '\\"');
          const message = `为我分析一下这些汽车推荐：${escapedDataString}，并且给我推荐，以品牌名，全名，价格范围和平均评分列形成的表回答，同时考虑我的要求：${userRequest.value}`;
          const encodedMessage = encodeURIComponent(message);

          const aiChatResponse = await axios.get('/api/ai/chat', {
            headers: {
              'Authorization': `Bearer ${token}`
            },
            params: {
              message: encodedMessage
            }
          });

          console.log('AI 聊天响应数据:', aiChatResponse);
          aiResponse.value = aiChatResponse.data.join('\n');
        }
      } catch (error) {
        console.error('获取AI推荐失败:', error);
        errorMessage.value = '获取推荐信息失败，请稍后重试。';
      } finally {
        loading.value = false;
      }
    };




    return {
      userRequest,
      chatResponse,
      recommendResponse,
      fetchAIRecommendations,
      loading,
      errorMessage,
      aiResponse,
      formattedAiResponse
    };
  }
};
</script>

<style scoped>
/* 推荐系统整体容器 */
.recommendations-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  font-family: 'Arial', sans-serif;
}

/* 顶部导航栏样式 */
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

/* 导航栏右侧部分 */
.nav-right {
  display: flex;
  gap: 15px;
  align-items: center;
}

/* 用户输入框 */
input {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  border: 2px solid #1a73e8;
  border-radius: 25px;
  margin-top: 20px;
}

input:focus {
  border-color: #003c8f;
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.1);
}

/* AI 推荐按钮 */
.ai-recommend-btn {
  margin-top: 20px;
  padding: 12px 20px;
  background-color: #1a73e8;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  color: white;
  cursor: pointer;
}

.ai-recommend-btn:disabled {
  background-color: #cccccc;
}

.ai-recommend-btn:hover:not(:disabled) {
  background-color: #003c8f;
}

/* 错误信息 */
.error-message {
  margin-top: 20px;
  color: red;
}

/* 推荐结果表格 */
.recommendations-table {
  width: 100%;
  margin-top: 30px;
  border-collapse: collapse;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.recommendations-table th,
.recommendations-table td {
  padding: 10px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.recommendations-table th {
  background-color: #1a73e8;
  color: white;
}

.recommendations-table td {
  background-color: #f9f9f9;
}

/* AI 响应区域 */
.ai-response, .ai-text-response {
  margin-top: 30px;
  padding: 20px;
  background-color: #f4f4f4;
  border-radius: 5px;
}

.ai-text-response h3 {
  margin-top: 20px;
}

.ai-text-response ul {
  margin-left: 20px;
}

.ai-text-response li {
  margin: 5px 0;
}
</style>