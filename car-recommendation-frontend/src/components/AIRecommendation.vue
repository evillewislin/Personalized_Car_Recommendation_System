<template>
  <h1>该AI基于Deepseek的R1推理模型，输入您的要求进行推荐吧！！！</h1>
  <div class="main-container">
    <!-- 预定义建议列表 -->
    <div class="suggestions-sidebar">
      <h3>常用推荐请求</h3>
      <ul class="suggestion-list">
        <li
            v-for="(suggestion, index) in suggestions"
            :key="index"
            @click="applySuggestion(suggestion)"
            class="suggestion-item"
        >
          {{ suggestion }}
        </li>
      </ul>
    </div>

    <!-- 主内容区 -->
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

      <div class="personalized-recommendation">
        <h3>★ 个性化推荐结果</h3>
        <div v-if="!loading && aiResponse.length" class="personalized-response-box">
          <pre>{{ aiResponse }}</pre>
        </div>
        <div v-else class="no-result-message">
          输入预算后点击推荐按钮获取结果
        </div>
      </div>
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

    // 预定义建议列表
    const suggestions = ref([
      "给我推荐20万左右的汽车",
      "我需要性价比高的汽车",
      "我需要安全性能好一点的汽车",
      "推荐适合家庭使用的SUV",
      "我想要新能源电动车推荐",
      "适合年轻人的第一辆车",
      "豪华品牌入门级车型推荐",
      "油耗低的城市通勤车"
    ]);

    const applySuggestion = (suggestion) => {
      userRequest.value = suggestion;
    };

    const fetchAIRecommendations = async () => {
      loading.value = true;
      errorMessage.value = '';
      aiResponse.value = '';

      try {
        const message = `给我推荐几款汽车，需求是${userRequest.value}的汽车，结果以品牌名，全名，价格范围,续航和平均评分形式进行回答，`;
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
          errorMessage.value = `获取推荐信息失败，状态码: ${error.response.status}，错误信息: ${error.response.statusText}`;
        } else if (error.request) {
          errorMessage.value = '请求未收到响应，请检查网络连接。';
        } else {
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
      suggestions,
      applySuggestion,
      fetchAIRecommendations
    };
  }
};
</script>

<style scoped>
.main-container {
  display: flex;
  max-width: 1000px;
  margin: 0 auto;
  gap: 20px;
}

.suggestions-sidebar {
  width: 280px;
  padding: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.suggestions-sidebar h3 {
  color: #333;
  font-size: 16px;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.suggestion-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.suggestion-item {
  padding: 10px 12px;
  margin-bottom: 8px;
  background-color: #f5f7fa;
  border-radius: 6px;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
}

.suggestion-item:hover {
  background-color: #ebf5ff;
  color: #409eff;
  transform: translateX(3px);
}

.ai-recommendation-container {
  flex: 1;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

h1 {
  text-align: center;
  color: #333;
  font-size: 24px;
  margin-bottom: 30px;
}

.message-text {
  width: 100%;
  padding: 12px 15px;
  font-size: 16px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  margin-bottom: 20px;
  box-sizing: border-box;
  transition: all 0.3s;
}

.message-text:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.error-message {
  color: #f56c6c;
  font-size: 14px;
  margin: -10px 0 15px 0;
  padding: 5px 0;
}

.recommend-btn {
  width: 100%;
  padding: 12px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 25px;
}

.recommend-btn:hover {
  background-color: #66b1ff;
}

.recommend-btn:disabled {
  background-color: #a0cfff;
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

.personalized-recommendation {
  margin-top: 20px;
  background-color: white;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.personalized-recommendation h3 {
  color: #333;
  font-size: 18px;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.personalized-response-box {
  padding: 15px;
  background-color: #f9fafb;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
}

.personalized-response-box pre {
  margin: 0;
  font-family: inherit;
  white-space: pre-wrap;
}

.no-result-message {
  text-align: center;
  color: #909399;
  margin: 20px 0;
  padding: 30px 0;
  background-color: #f9fafb;
  border-radius: 6px;
  font-size: 14px;
}

@media (max-width: 768px) {
  .main-container {
    flex-direction: column;
  }

  .suggestions-sidebar {
    width: 100%;
    margin-bottom: 20px;
  }
}
</style>