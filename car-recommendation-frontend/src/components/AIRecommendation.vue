<template>
  <h1>该AI基于Deepseek的R1推理模型，输入您的要求进行推荐吧！！！</h1>
  <div class="ai-recommendation-container">
    <!-- 用户输入要求的文本输入框 -->
    <input
        v-model="userRequest"
        class="message-text"
        type="text"
        placeholder="请输入你的要求"
        :disabled="loading"
    />

    <!-- 错误提示信息 -->
    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>

    <!-- AI 推荐按钮 -->
    <button
        @click="fetchAIRecommendations"
        class="recommend-btn"
        :disabled="loading"
    >
      <span v-if="!loading">AI智能推荐</span>
      <span v-else class="loading-wrapper">
        <span class="loading-spinner"></span>加载中...
      </span>
    </button>

    <!-- AI 文本响应显示区域 -->
    <div v-if="!loading && aiResponse.length" class="ai-response-box">
      <pre>{{ aiResponse }}</pre>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
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
        const message = `给我推荐几款汽车，${userRequest.value}，以品牌名，全名，价格范围,续航和平均评分形式进行回答，`;
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
.ai-recommendation-container {
  max-width: 500px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.message-text {
  width: 100%;
  padding: 12px 15px;
  font-size: 16px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  margin-bottom: 15px;
  box-sizing: border-box;
}

.message-text:focus {
  outline: none;
  border-color: #1e40af;
  box-shadow: 0 0 0 3px #1e40af;
}

.error-message {
  color: #ef4444;
  font-size: 14px;
  margin-bottom: 15px;
}

.recommend-btn {
  width: 200px;
  padding: 12px;
  background-color: rgba(64, 158, 255, 1);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 20px;
  margin-left: 150px;
}

.recommend-btn:hover {
  background-color: #1e40af;
}

.recommend-btn:disabled {
  background-color: #d1d5db;
  cursor: not-allowed;
}

.loading-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.ai-response-box {
  padding: 20px;
  background-color: #f9fafb;
  border: 2px solid #1e40af;
  border-radius: 8px;
  white-space: pre-wrap;
  word-wrap: break-word;
  width: 350px;
}

.ai-response-box pre {
  margin: 0;
  font-family: inherit;
  white-space: pre-wrap;
}
</style>