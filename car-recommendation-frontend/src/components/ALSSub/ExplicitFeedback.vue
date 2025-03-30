<template>
  <div class="sub-page">
    <h3>显式反馈参数设置</h3>
    <el-form :model="params">
      <el-form-item label="特征维度">
        <el-input-number
            v-model.number="params.rank"
            :min="5"
            :max="50"
            size="small"
        />
      </el-form-item>

      <el-form-item label="迭代次数">
        <el-slider
            v-model.number="params.iterations"
            :min="5"
            :max="30"
            show-input
            size="small"
        />
      </el-form-item>

      <el-form-item label="正则化系数">
        <el-input-number
            v-model.number="params.lambda"
            :step="0.01"
            size="small"
        />
      </el-form-item>
    </el-form>

    <el-button
        type="primary"
        @click="handleRecommend"
        :loading="isLoading"
        class="action-btn"
    >
      生成推荐
    </el-button>

    <div v-if="error" class="error-message">
      {{ error }}
    </div>

    <div class="result-container" v-if="recommendations.content.length > 0">
      <h4>推荐结果</h4>
      <el-card v-for="car in recommendations.content" :key="car.carId" class="car-card">
        <div class="car-name">{{ car.name }}</div>
        <div class="car-price">价格区间：{{ car.minPrice }} - {{ car.maxPrice }} 万元</div>
        <div class="car-description">{{ car.fullName }}</div>
      </el-card>
    </div>

    <el-pagination
        v-if="recommendations.total > 0"
        :total="recommendations.total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
    />
  </div>
</template>

<script>
import {ref, reactive} from 'vue';
import axios from 'axios';

export default {
  setup() {
    const params = reactive({
      rank: 10,
      iterations: 15,
      lambda: 0.1
    });

    const recommendations = ref({content: [], total: 0});
    const error = ref('');
    const isLoading = ref(false);
    const currentPage = ref(1);
    const pageSize = ref(10);

    const handleRecommend = async () => {
      isLoading.value = true;
      error.value = '';
      try {
        const response = await axios.post('/api/recommend/Ex_cars', {
          ...params.value,
          page: currentPage.value,
          size: pageSize.value
        });
        recommendations.value = response.data;
      } catch (err) {
        error.value = err.response?.data?.message || '推荐失败，请重试';
      } finally {
        isLoading.value = false;
      }
    };

    const handlePageChange = (page) => {
      currentPage.value = page;
      handleRecommend();
    };

    return {
      params,
      recommendations,
      error,
      isLoading,
      currentPage,
      pageSize,
      handleRecommend,
      handlePageChange
    };
  }
};
</script>

<style scoped>
.sub-page {
  padding: 20px;
}

el-form-item {
  margin-bottom: 15px;
}

.action-btn {
  margin: 20px 0;
}

.result-container {
  margin-top: 30px;
}

.car-card {
  margin: 10px 0;
  padding: 15px;
}

.car-name {
  font-size: 1.2em;
  font-weight: bold;
  margin-bottom: 5px;
}

.car-price {
  color: #666;
}

.car-description {
  color: #666;
  margin-top: 10px;
}

.error-message {
  color: #f56c6c;
  margin: 10px 0;
}

.el-pagination {
  margin-top: 20px;
}
</style>