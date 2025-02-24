<template>
  <div>
    <!-- 用户输入要求的文本输入框 -->
    <input v-model="userRequest" type="text" placeholder="请输入你的要求" :disabled="loading" />
    <!-- 错误提示信息 -->
    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
    <!-- 加载状态提示 -->
    <div v-if="loading" class="loading-message">加载中...</div>
    <!-- AI 推荐按钮 -->
    <button @click="fetchAIRecommendations" class="ai-recommend-btn" :disabled="loading">
      {{ loading? '加载中...' : 'AI智能推荐' }}
    </button>
    <!-- AI 文本响应显示区域 -->
    <div v-if="!loading && aiResponse.length" class="ai-text-response">
      <!-- 直接显示文本，避免使用 v-html 可能带来的安全风险 -->
      <p>{{ aiResponse }}</p>
    </div>
  </div>
</template>

<script>
import {ref} from 'vue';
import axios from 'axios';

export default {
  setup() {
    const userRequest = ref('');
    const loading = ref(false);
    const errorMessage = ref('');
    const aiResponse = ref('');

    const fetchAIRecommendations = async () => {
      loading.value = true;
      errorMessage.value = '';
      aiResponse.value = '';

      try {
        const message = `你是一位优秀的汽车咨询师，${userRequest.value}，以品牌名，全名，价格范围和平均评分形式进行回答，`;
        const encodedMessage = encodeURIComponent(message);

        const aiChatResponse = await axios.get('/api/ai/chat', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          params: {
            message: encodedMessage
          }
        });
        aiResponse.value = aiChatResponse.data.replace(/<think>[\s\S]*?<\/think>/g, '').trim();
      } catch (error) {
        console.error('获取AI推荐失败:', error);
        if (error.response) {
          // 处理HTTP错误
          errorMessage.value = `获取推荐信息失败，状态码: ${error.response.status}，错误信息: ${error.response.statusText}`;
        } else if (error.request) {
          // 处理请求未收到响应的情况
          errorMessage.value = '请求未收到响应，请检查网络连接。';
        } else {
          // 处理其他错误
          errorMessage.value = '获取推荐信息失败，请稍后重试。';
        }
      } finally {
        loading.value = false;
      }
    };

    return {
      userRequest,
      loading,
      errorMessage,
      aiResponse,
      fetchAIRecommendations
    };
  }
};
</script>


<style scoped>
/* 提取颜色和尺寸变量 */
:root {
  --primary-color: #1a73e8;
  --secondary-color: #003c8f;
  --input-padding: 12px;
  --input-font-size: 16px;
  --input-border-radius: 25px;
}

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
  background-color: var(--primary-color);
  border: none;
  border-radius: 5px;
  font-size: 16px;
  color: white;
  cursor: pointer;
  text-decoration: none;
  text-align: center;
}

.sidebar-btn:hover {
  background-color: var(--secondary-color);
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

input {
  width: 100%;
  padding: var(--input-padding);
  font-size: var(--input-font-size);
  border: 2px solid var(--primary-color);
  border-radius: var(--input-border-radius);
  margin-top: 20px;
}

input:focus {
  border-color: var(--secondary-color);
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.1);
}

.error-message {
  margin-top: 20px;
  color: red;
}

.ai-text-response {
  margin-top: 30px;
  padding: 20px;
  background-color: #f4f4f4;
  border-radius: 5px;
}

.loading-message {
  margin-top: 20px;
  color: var(--primary-color);
}
</style>