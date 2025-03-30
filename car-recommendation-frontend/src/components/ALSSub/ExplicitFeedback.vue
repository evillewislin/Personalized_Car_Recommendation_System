<template>
  <div class="sub-page">
    <el-loading
        :target="loadingTarget"
        :text="loadingText"
        :spinner="loadingSpinner"
        :background="loadingBackground"
        v-show="isLoading"
    ></el-loading>

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

      <el-form-item label="价格预算">
        <el-input-number
            v-model.number="params.maxPrice"
            :min="0"
            :step="10000"
            suffix="万元"
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

    <div class="pagination-container" v-if="recommendations.total > 0">
      <el-button
          type="primary"
          @click="handleNextPage"
          :loading="nextPageLoading"
          :disabled="!hasNextPage"
      >
        加载更多
      </el-button>
    </div>
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
      lambda: 0.1,
      maxPrice: 500000 // 默认50万元
    });

    const recommendations = ref({
      content: [],
      total: 0,
      page: 1,
      size: 10,
      totalPages: 0 // 添加总页数
    });
    const error = ref('');
    const isLoading = ref(false);
    const nextPageLoading = ref(false);
    const hasNextPage = ref(false);

    // 加载条配置
    const loadingTarget = ref(null);
    const loadingText = ref('正在生成推荐...');
    const loadingSpinner = ref('el-icon-loading');
    const loadingBackground = ref('rgba(0, 0, 0, 0.7)');

    const handleRecommend = async () => {
      isLoading.value = true;
      error.value = '';
      try {
        const requestData = {
          rank: parseInt(params.rank),
          iterations: parseInt(params.iterations),
          lambda: parseFloat(params.lambda),
          maxPrice: parseInt(params.maxPrice)
        };

        const response = await axios.post('/api/ai/Ex_cars', requestData, {
          headers: {
            'Content-Type': 'application/json'
          },
          params: {
            page: 1, // 初始加载第一页
            size: recommendations.value.size
          }
        });

        recommendations.value.content = response.data.content;
        recommendations.value.total = response.data.total;
        recommendations.value.totalPages = response.data.totalPages; // 更新总页数
        recommendations.value.page = 2; // 下一页从2开始
        hasNextPage.value = recommendations.value.page <= recommendations.value.totalPages;
      } catch (err) {
        error.value = err.response?.data?.message || '推荐失败，请重试';
        if (err.response?.status === 400) {
          error.value = '请求参数错误，请检查输入';
        } else if (err.response?.status === 500) {
          error.value = '服务器内部错误，请稍后重试';
        }
      } finally {
        isLoading.value = false;
      }
    };

    const handleNextPage = async () => {
      if (!hasNextPage.value || nextPageLoading.value) return;
      nextPageLoading.value = true;
      try {
        const requestData = {
          rank: parseInt(params.rank),
          iterations: parseInt(params.iterations),
          lambda: parseFloat(params.lambda),
          maxPrice: parseInt(params.maxPrice)
        };

        const response = await axios.post('/api/ai/Ex_cars', requestData, {
          headers: {
            'Content-Type': 'application/json'
          },
          params: {
            page: recommendations.value.page,
            size: recommendations.value.size
          }
        });

        recommendations.value.content = [
          ...recommendations.value.content,
          ...response.data.content
        ];
        recommendations.value.total = response.data.total;
        recommendations.value.totalPages = response.data.totalPages; // 更新总页数
        recommendations.value.page = response.data.page + 1;
        hasNextPage.value = recommendations.value.page <= recommendations.value.totalPages;
      } catch (err) {
        error.value = err.response?.data?.message || '推荐失败，请重试';
        if (err.response?.status === 400) {
          error.value = '请求参数错误，请检查输入';
        } else if (err.response?.status === 500) {
          error.value = '服务器内部错误，请稍后重试';
        }
      } finally {
        nextPageLoading.value = false;
      }
    };

    return {
      params,
      recommendations,
      error,
      isLoading,
      nextPageLoading,
      hasNextPage,
      handleRecommend,
      handleNextPage,
      loadingTarget,
      loadingText,
      loadingSpinner,
      loadingBackground
    };
  }
};
</script>

<style scoped>
.sub-page {
  position: relative;
  padding: 20px;
}

.el-loading-mask {
  z-index: 9999;
}

.el-form-item {
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

.pagination-container {
  margin-top: 20px;
  text-align: center;
}
</style>