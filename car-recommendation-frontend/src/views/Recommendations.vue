<template>
  <div class="recommendation-center">

    <!-- 侧边栏导航 -->
    <div class="sidebar">
      <!-- 退出按钮 -->
      <div class="back-button-container">
        <el-button type="danger" @click="handleBack">返回</el-button>
      </div>
      <ul>
        <li :class="{ active: currentTab === 'FavoriteHistory' }" @click="currentTab = 'FavoriteHistory'">
          收藏历史
        </li>
        <li :class="{ active: currentTab === 'AIRecommendation' }" @click="currentTab = 'AIRecommendation'">
          AI 推荐
        </li>
        <li :class="{ active: currentTab === 'ALSRecommendation' }" @click="currentTab = 'ALSRecommendation'">
          ALS 推荐
        </li>
      </ul>
    </div>
    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 根据当前选中的标签展示不同的内容 -->
      <component :is="currentTab"></component>
    </div>
  </div>
</template>

<script>
import { defineComponent, ref } from 'vue';
import AIRecommendation from '../components/AIRecommendation.vue';
import ALSRecommendation from '../components/ALSRecommendation.vue';
import FavoriteHistory from '../components/FavoriteHistory.vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

export default defineComponent({
  components: {
    AIRecommendation,
    ALSRecommendation,
    FavoriteHistory
  },
  setup() {
    const router = useRouter();
    const currentTab = ref('FavoriteHistory');

    const handleBack = () => {
      // 显示返回提示信息
      ElMessage.info('返回');
      // 跳转回根页面
      router.push('/');
    };

    return {
      currentTab,
      handleBack
    };
  }
});
</script>

<style scoped>
.recommendation-center {
  display: flex;
  min-height: 100vh;
  background: #f8fafb;
}

.back-button-container {
  top: 24px;
  left: 24px;
  z-index: 100;
}

.sidebar {
  width: 240px;
  background: linear-gradient(180deg, #2C3E50 0%, #3498DB 100%);
  box-shadow: 4px 0 16px rgba(0, 0, 0, 0.1);
  padding: 32px 0;
  position: sticky;
  top: 0;
}

.sidebar li {
  padding: 16px 32px;
  margin: 8px 16px;
  color: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar li.active {
  background: rgba(255, 255, 255, 0.15);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.main-content {
  flex: 1;
  padding: 40px;
  background: #ffffff;
  min-height: calc(100vh - 80px);
  margin: 40px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
}
</style>