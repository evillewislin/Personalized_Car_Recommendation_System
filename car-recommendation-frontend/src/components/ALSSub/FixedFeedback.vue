<template>
  <div class="fixed-feedback">
    <el-form :model="params">
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

    <!-- 添加 key 强制表格重新渲染 -->
    <div v-if="recommendations.length > 0 && !isLoading" class="result-container">
      <h4>结合您的各种信息以及浏览收藏等偏好，推荐如下车型</h4>
      <el-table
          :data="recommendations"
          empty-text="暂无数据"
          :key="tableKey"
      >
        <el-table-column prop="brandName" label="品牌" width="120" />
        <el-table-column prop="fullName" label="车型" width="300" />
        <el-table-column prop="price" label="价格" width="180" />
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
      const tableKey = ref(0);  // 新增表格渲染标识
      const params = reactive({
        maxPrice: 50
      });

      const recommendations = ref([]);
      const error = ref('');
      const isLoading = ref(false);

      const handleRecommend = async () => {
        if (isNaN(params.maxPrice) || params.maxPrice < 0) {
          error.value = '请输入有效的价格预算';
          ElMessage.info('请输入有效的价格预算');
          return;
        }

        isLoading.value = true;
        error.value = '';
        recommendations.value = [];

        try {
          const token = localStorage.getItem('token');
          if (!token) {
            error.value = '未授权，请重新登录';
            ElMessage.info('未授权，请重新登录');
            isLoading.value = false;
            return;
          }

          const recommendResponse = await axios.get('/api/ai/recommend', {
            headers: { Authorization: `Bearer ${token}` },
            params: { page: 1, size: 10 }
          });

          const alsResponse = await axios.post('/api/ai/als',
              recommendResponse.data.data,
              {
                headers: { Authorization: `Bearer ${token}` },
                params: { maxPrice: params.maxPrice * 10000 }
              }
          );

          recommendations.value = alsResponse.data.slice(0, 10);
          tableKey.value++;  // 关键修复：更新 key 强制表格重新渲染
        } catch (err) {
          // 错误处理保持不变...
        } finally {
          isLoading.value = false;
        }
      };

      return {
        params,
        recommendations,
        error,
        isLoading,
        handleRecommend,
        tableKey  // 关键修复：必须返回 tableKey
      };
    }
  };
</script>

<style scoped>
.fixed-feedback {
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