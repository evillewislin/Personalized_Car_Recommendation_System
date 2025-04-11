<template>
  <div class="car-recommendation-container">
    <el-loading
        v-show="isLoading"
        :target="loadingTarget"
        :text="loadingText"
        :spinner="loadingSpinner"
        :background="loadingBackground"
        class="recommendation-loading-mask"
    ></el-loading>
    <el-button
        type="primary"
        @click="handleGenerate"
        :loading="isLoading"
        class="generate-btn"
    >
      生成推荐
    </el-button>
    <div v-if="errorMessage" class="error-message">
      <el-icon><error-filled /></el-icon>
      {{ errorMessage }}
    </div>
    <div class="result-container" v-if="recommendations.length > 0">
      <h3 class="recommendation-result-title">结合您的收藏和偏好，推荐如下车型</h3>
      <el-table
          :data="recommendations"
          stripe
          border
          class="recommendation-table"
          empty-text="暂无符合条件的推荐车型"
      >
        <el-table-column prop="name" label="品牌" width="120" />
        <el-table-column prop="fullName" label="车型" min-width="200" />
        <el-table-column label="区间" width="180">
          <template #default="{ row }">
            {{ row.price }}
          </template>
        </el-table-column>
      </el-table>
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

const loadingTarget = ref(null);
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
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  min-height: 400px;
}

.generate-btn {
  margin-top: 2px;
}

.error-message {
  color: #f56c6c;
  margin: 10px 0;
  padding: 10px;
  background: #fef0f0;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.result-container {
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