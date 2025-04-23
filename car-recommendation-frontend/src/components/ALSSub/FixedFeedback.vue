<template>
  <div class="recommendation-container">
    <div class="control-panel">
      <div class="budget-input-group">
        <span class="input-label">购车预算：</span>
        <el-input
            v-model.number="params.maxPrice"
            type="number"
            :min="0"
            step="1"
            placeholder="请输入预算"
            class="budget-input"
            :disabled="isLoading"
            @keyup.enter="handleRecommend"
        >
          <template #append>万元</template>
        </el-input>
        <el-button
            type="primary"
            @click="handleRecommend"
            :loading="isLoading"
            class="generate-btn"
            icon="MagicStick"
        >
          {{ isLoading ? '推荐中...' : '智能推荐' }}
        </el-button>
      </div>
    </div>

    <div class="result-panel">
      <el-card shadow="never" class="result-card">
        <template #header>
          <div class="card-header">
            <el-icon><StarFilled /></el-icon>
            <span>个性化推荐结果</span>
          </div>
        </template>

        <el-skeleton :rows="5" animated v-if="isLoading"/>

        <el-empty
            v-else-if="!isLoading && recommendations.length === 0 && !errorMessage"
            description="输入预算后点击推荐按钮获取结果"
            :image-size="100"
        />

        <div v-else>
          <div class="error-message" v-if="errorMessage">
            <el-icon color="#F56C6C"><WarningFilled /></el-icon>
            <span>{{ errorMessage }}</span>
          </div>

          <div v-if="recommendations.length > 0">
            <el-table
                :data="recommendations"
                stripe
                style="width: 100%"
                empty-text="暂无符合条件的推荐车型"
                class="recommend-table"
            >
              <el-table-column
                  prop="brandName"
                  label="品牌"
                  width="120"
                  align="center"
              >
                <template #default="{ row }">
                  <el-tag effect="light">{{ row.brandName }}</el-tag>
                </template>
              </el-table-column>

              <el-table-column
                  prop="fullName"
                  label="车型"
                  min-width="100"
              />

              <el-table-column
                  label="价格"
                  width="200"
                  align="right"
              >
                <template #default="{ row }">
    <span>
      {{
        row.priceRange
            ? (parseInt(row.priceRange.split('-')[0]) / 10000).toFixed(1) + ' 万元'
            : 'NaN 万元'
      }}
    </span>
                </template>
              </el-table-column>


            </el-table>

            <div class="recommend-tips">
              <el-icon><StarFilled /></el-icon>
              <span>共推荐 {{ recommendations.length }} 款车型，数据更新于 {{ new Date().toLocaleDateString() }}</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { StarFilled} from "@element-plus/icons-vue";

export default {
  components: {StarFilled},
  setup() {
    const tableKey = ref(0);
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
      if (params.maxPrice < 1) {
        error.value = '预算不能低于1万元';
        ElMessage.warning('预算不能低于1万元');
        return;
      }
      if (params.maxPrice > 1000) {
        error.value = '预算不能超过1000万元';
        ElMessage.warning('预算不能超过1000万元');
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
        tableKey.value++;
      } catch (err) {
        error.value = err.response?.data?.message || '推荐失败，请重试';
        ElMessage.error('推荐失败，请重试');
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
      tableKey
    };
  }
};
</script>

<style scoped>
.recommendation-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.control-panel {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}

.budget-input-group {
  display: flex;
  align-items: center;
  gap: 15px;
}

.input-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.budget-input {
  width: 200px;
}

.budget-input :deep(.el-input-group__append) {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.generate-btn {
  margin-left:auto;
  padding: 15px 10px ;
  font-size: 14px;

}

.result-panel {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}



.recommendation-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 20px 0;
  font-size: 16px;
  color: var(--el-text-color-primary);
}

.recommendation-title .el-icon {
  color: var(--el-color-primary);
}
.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.card-header .el-icon {
  color: var(--el-color-primary);
}
.recommend-table {
  margin-top: 10px;
  border-radius: 4px;
}

.recommend-table :deep(.el-table__header) th {
  background-color: #f5f7fa;
  font-weight: 600;
}

.price-tag {
  color: var(--el-color-primary);
  font-weight: 500;
}

.detail-btn {
  color: var(--el-color-primary);
}

.error-message {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #f56c6c;
  padding: 10px 0;
}

.recommend-tips {
  margin-top: 15px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 5px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .budget-input-group {
    flex-direction: column;
    align-items: flex-start;
  }

  .budget-input {
    width: 100%;
  }

  .generate-btn {
    width: 100%;
    margin-left: 0;
  }
}
</style>