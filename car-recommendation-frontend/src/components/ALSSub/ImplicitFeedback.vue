<template>
  <div class="implicit-feedback">
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

      <el-form-item label="价格预算(万元)">
        <el-input-number
            v-model.number="params.maxPrice"
            :min="0"
            :step="1"
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

    <div class="result-container" v-if="recommendations.length > 0">
      <h4>结合您的收藏和偏好，推荐如下车型</h4>
      <el-table
          :data="recommendations"
          empty-text="暂无数据"
      >
        <el-table-column prop="brandName" label="品牌" width="120" />
        <el-table-column prop="fullName" label="车型" />
        <el-table-column prop="priceRange" label="价格区间" width="180" >
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue';
import axios from 'axios';
import {ElMessage} from "element-plus";

export default {
  setup() {
    const params = reactive({
      rank: 10,
      iterations: 15,
      lambda: 0.1,
      maxPrice: 50 // 单位改为万元，与后端一致
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
          maxPrice: params.maxPrice * 10000  // 转换为元
        }, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          params: {
            page: 1,
            size: 10
          }
        });
        console.log("数据",response)
        // 处理返回数据
        let rawData = response.data.content || response.data;
        recommendations.value = rawData.slice(0, 10).map(item => {
          return {
            ...item,
            // 将价格从元转换为万元，并保留2位小数
            priceRange: item.minPrice === item.maxPrice
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
.implicit-feedback {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
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

.error-message {
  color: #f56c6c;
  margin: 10px 0;
}

.el-table {
  margin-top: 20px;
  width: 100%;
}
</style>