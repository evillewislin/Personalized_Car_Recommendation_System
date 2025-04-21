<template>
  <el-button
      type="primary"
      @click="handleRecommend"
      :loading="isLoading"
      class="generate-btn"
  >
    生成推荐
  </el-button>
  <div class="car-recommendation-container">
    <div v-if="error" class="recommendation-error-message">
      {{ error }}
    </div>
    <div class="recommendation-result-container" v-if="recommendations.length > 0">
      <h4 class="recommendation-result-title">结合您的收藏和偏好，推荐如下车型</h4>
      <el-table
          :data="recommendations"
          empty-text="暂无数据"
          class="recommendation-table"
          :key="tableKey"
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
    const tableKey = ref(0);
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
        tableKey.value++;
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
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  min-height: 400px;
  background-color: #f8fafc;
  border-radius: 12px;
}

.generate-btn {
  margin-top: 0;
  padding: 20px 20px;
  font-size: 14px;
  border-radius: 6px;
  transition: all 0.3s ease;
  margin-left: 1000px;
  margin-bottom: 10px;
}

.generate-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.error-message {
  color: #f56c6c;
  margin: 16px 0;
  padding: 12px 16px;
  background: #fef0f0;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  border-left: 4px solid #f56c6c;
}

.result-container {
  margin-top: 30px;
  background: #fff;
  padding: 0;
  border-radius: 12px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.recommendation-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.recommendation-result-title {
  margin: 0;
  padding: 20px 24px;
  color: #303133;
  font-weight: 600;
  font-size: 18px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.recommendation-table {
  width: 100%;
  border: none;
}

.recommendation-table :deep(.el-table__header-wrapper) {
  border-bottom: 1px solid #e4e7ed;
}

.recommendation-table :deep(.el-table__body-wrapper) {
  border-bottom: 1px solid #e4e7ed;
}

.recommendation-table :deep(th) {
  background-color: #f5f7fa !important;
  color: #606266;
  font-weight: 600;
}

.recommendation-table :deep(.el-table__row--striped) {
  background-color: #fafbfc !important;
}

.recommendation-table :deep(td) {
  border-right: none;
}

.recommendation-table :deep(.el-table__cell) {
  padding: 14px 0;
}

.recommendation-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}
</style>