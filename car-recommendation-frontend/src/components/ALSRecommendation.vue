<template>
  <div>
    <!-- 错误提示信息 -->
    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
    <!-- Als推荐结果表格 -->
    <table v-if="alsResponse.length" class="alsResponse-table">
      <thead>
      <tr>
        <th>品牌名</th>
        <th>全名</th>
        <th>价格范围</th>
        <th>平均评分</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="item in alsResponse" :key="item.fullName">
        <td>{{ item.name }}</td>
        <td>{{ item.fullName }}</td>
        <td>{{ item.priceRange }}</td>
        <td>{{ item.avgScore }}</td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import axios from 'axios';

// 提取获取和处理token的公共函数
const getCleanToken = () => {
  const token = localStorage.getItem('token');
  if (!token) {
    throw new Error('Token is missing');
  }
  return token.replace('Bearer ', '');
};

export default {
  setup() {
    const loading = ref(false);
    const errorMessage = ref('');
    const alsResponse = ref([]);
    const recommendResponse = ref([]);

    const fetchALSRecommendations = async () => {
      loading.value = true;
      errorMessage.value = '';
      alsResponse.value = [];
      recommendResponse.value = [];

      try {
        const cleanToken = getCleanToken();

        const recommendResponseData = await axios.get('/api/ai/allrecommend', null, {
          headers: {
            'Authorization': `Bearer ${cleanToken}`
          }
        });

        recommendResponse.value = recommendResponseData.data;

        if (recommendResponse.value.length > 0) {
          console.log(recommendResponseData.data);
          const alsResponseData = await axios.post('/api/ai/als', recommendResponse.value, {
            headers: {
              'Authorization': `Bearer ${cleanToken}`
            }
          });

          alsResponse.value = alsResponseData.data;
        }
      } catch (error) {
        console.error('获取ALS推荐失败:', error);
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

    onMounted(async () => {
      await fetchALSRecommendations();
    });

    return {
      loading,
      errorMessage,
      alsResponse
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
.alsResponse-table {
  width: 100%;
  margin-top: 30px;
  border-collapse: collapse;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.alsResponse-table th,
.alsResponse-table td {
  padding: 10px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.alsResponse-table th {
  background-color: #1a73e8;
  color: white;
}

.alsResponse-table td {
  background-color: #f9f9f9;
}
</style>