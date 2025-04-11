<template>
  <div class="car-recommendation-container">


    <el-button
        type="primary"
        @click="handleRecommend"
        :loading="isLoading"
        class="recommendation-action-btn"
    >
      生成推荐
    </el-button>

    <div v-if="error" class="recommendation-error-message">
      {{ error }}
    </div>

    <div class="recommendation-result-container" v-if="recommendations.length > 0">
      <h4 class="recommendation-result-title">结合您的收藏和偏好，推荐如下车型</h4>
      <el-table
          :data="recommendations"
          empty-text="暂无数据"
          class="recommendation-table"
      >
        <el-table-column prop="brandName" label="品牌" width="120" />
        <el-table-column prop="fullName" label="车型" />
        <el-table-column prop="price" label="区间" width="180" />
      </el-table>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

export default {
  setup() {
    const params = reactive({
      rank: 10,
      iterations: 15,
      lambda: 0.1,
      maxPrice: 50
    });

    const recommendations = ref([]);
    const error = ref('');
    const isLoading = ref(false);

    const handleRecommend = async () => {
      isLoading.value = true;
      error.value = '';
      recommendations.value = [];

      try {
        const response = await axios.post('/api/ai/Im_cars', {
          rank: params.rank,
          iterations: params.iterations,
          lambda: params.lambda,
          maxPrice: params.maxPrice * 10000
        }, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          params: {
            page: 1,
            size: 10
          }
        });

        let rawData = response.data.content || response.data;
        recommendations.value = rawData.slice(0, 10).map(item => {
          return {
            ...item,
            price: item.minPrice === item.maxPrice
                ? `${(item.minPrice / 10000).toFixed(2)}万元`
                : `${(item.minPrice / 10000).toFixed(2)}-${(item.maxPrice / 10000).toFixed(2)}万元`
          };
        });

      } catch (err) {
        error.value = err.response?.data?.message || '推荐失败，请重试';
        ElMessage.info('推荐失败，请重试');
        console.error('推荐请求失败:', err);
      } finally {
        isLoading.value = false;
      }
    };

    return {
      params,
      recommendations,
      error,
      isLoading,
      handleRecommend
    };
  }
};
</script>

<style scoped>
.car-recommendation-container {
  position: relative;
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  min-height: 400px;
}

.recommendation-form {
  margin-bottom: 20px;
}

.recommendation-form-item {
  margin-bottom: 15px;
}

.recommendation-action-btn {

}

.recommendation-result-container {
  margin-top: 30px;
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.recommendation-result-title {
  margin-bottom: 20px;
  color: #333;
  font-weight: normal;
  font-size: 16px;
}

.recommendation-error-message {
  color: #f56c6c;
  margin: 10px 0;
  padding: 10px;
  background: #fef0f0;
  border-radius: 4px;
}

.recommendation-table {
  margin-top: 20px;
  width: 100%;
  border: 1px solid #ebeef5;
}

.recommendation-table::before {
  height: 0;
}

.recommendation-table th {
  background-color: #f5f7fa;
  color: #333;
}

.recommendation-table td,
.recommendation-table th {
  padding: 12px 0;
  text-align: center;
}
</style>