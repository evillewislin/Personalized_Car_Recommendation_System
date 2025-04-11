<template>
  <div>
    <!-- 用户输入要求的文本输入框 -->
    <input v-model="userRequest" class="message-text" type="text" placeholder="请输入你的要求" :disabled="loading" />
    <!-- 错误提示信息 --><br>
    <br>
    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
    <!-- AI 推荐按钮 -->
    <button   @click="fetchAIRecommendations" class="el-button el-button--primary" :disabled="loading">
      <span v-if="!loading">AI智能推荐</span>
      <span v-else>
        <span class="loading-spinner"></span> 加载中...
      </span>
    </button>
    <!-- AI 文本响应显示区域 -->
    <div v-if="!loading && aiResponse.length" class="ai-text-response">
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
/* 提取颜色和尺寸变量 */
:root {
  --primary-color: #e564ff;
  --secondary-color: #535bf2;
  --input-padding: 16px;
  --input-font-size: 18px;
  --input-border-radius: 8px;
  --box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  --transition-time: 0.3s;
  --button-depth: 8px;
}


  .message-text {
    width: 70%;
    height: 30px;
    padding: var(--input-padding);
    font-size: var(--input-font-size);
    border: 1px solid #e5e7eb;
    border-radius: var(--input-border-radius);
    margin-top: 20px;
    transition: all var(--transition-time) ease;
  }


.message-text:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(100, 108, 255, 0.2);
}

.error-message {
  margin-top: 10px;
  color: #ef4444;
  font-size: 14px;
}

.ai-recommend-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-top: 20px;
  padding: var(--input-padding) 32px;
  background-color: var(--primary-color);
  color: #0a0808;
  font-size: var(--input-font-size);
  border-radius: var(--input-border-radius);
  cursor: pointer;
  border: 3px solid #e5e7eb;
  transition: all var(--transition-time) ease;
  box-shadow: 0 var(--button-depth) 0 var(--secondary-color);
  transform: translateY(0);
}

.ai-recommend-btn:hover {
  background-color: var(--secondary-color);
  box-shadow: 0 calc(var(--button-depth) - 2px) 0 var(--secondary-color);
  transform: translateY(2px);
}

.ai-recommend-btn:active {
  box-shadow: 0 0 0 var(--secondary-color);
  transform: translateY(var(--button-depth));
}

.ai-recommend-btn:disabled {
  background-color: #d1d5db;
  cursor: not-allowed;
  box-shadow: 0 var(--button-depth) 0 #a0a4ab;
}

.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 8px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.ai-text-response {
  margin-top: 30px;
  padding: 24px;
  background-color: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: var(--input-border-radius);
  white-space: pre-line;
}
</style>