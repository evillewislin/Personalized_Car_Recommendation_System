<template>
  <el-button
      type="primary"
      @click="handleGenerate"
      :loading="isLoading"
      class="generate-btn"
  >
    生成推荐
  </el-button>
  <div class="car-recommendation-container">
    <el-loading
        v-show="isLoading"
        :text="loadingText"
        :spinner="loadingSpinner"
        :background="loadingBackground"
    ></el-loading>
    <div v-if="errorMessage" class="error-message">
      <el-icon><ErrorFilled /></el-icon>
      {{ errorMessage }}
    </div>
    <div class="result-container" v-if="recommendations.length > 0">
      <h3 class="recommendation-result-title">结合您的收藏和偏好，推荐如下车型</h3>
      <div class="recommendation-card">
        <el-table
            :data="recommendations"
            stripe
            border
            class="recommendation-table"
            :key="tableKey"
            empty-text="暂无符合条件的推荐车型"
        >
          <el-table-column prop="name" label="品牌" width="120" />
          <el-table-column prop="fullName" label="车型" min-width="200" />
          <el-table-column label="价格区间" width="180">
            <template #default="{ row }">
              {{ row.price }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { ErrorFilled } from '@element-plus/icons-vue';

const form = reactive({
  maxPrice: 50,
  rank: 10,
  iterations: 15,
  lambda: 0.1
});

const recommendations = ref([]);
const isLoading = ref(false);
const errorMessage = ref('');
const tableKey = ref(0);

const loadingText = ref('正在分析您的偏好...');
const loadingSpinner = ref('el-icon-loading');
const loadingBackground = ref('rgba(255, 255, 255, 0.9)');

const handleGenerate = async () => {
  if (isLoading.value) return;
  if (form.maxPrice < 0) {
    ElMessage.error('价格预算不能为负数');
    return;

  }
  isLoading.value = true;
  errorMessage.value = '';
  recommendations.value = [];
  try {
    const response = await axios.post('/api/ai/Ex_cars', {
      rank: form.rank,
      iterations: form.iterations,
      lambda: form.lambda,
      maxPrice: form.maxPrice * 10000
    });
    tableKey.value++;
    if (response.data.length === 0) {
      ElMessage.warning('暂无符合条件的推荐车型');
    } else {
      recommendations.value = response.data.map(item => ({
        ...item,
        price: `${(item.minPrice / 10000).toFixed(2)}万元`
      }));
    }
  } catch (error) {
    console.error('推荐请求失败', error);
    errorMessage.value = '生成推荐时发生错误，请稍后再试';
    ElMessage.error('推荐失败，请检查网络或重试');
    if (error.response) {
      switch (error.response.status) {
        case 400:
          errorMessage.value = '请求参数错误，请检查输入';
          break;
        case 500:
          errorMessage.value = '服务器内部错误，请联系管理员';
          break;
      }
    }
  } finally {
    isLoading.value = false;
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