<template>
  <div class="user-center">
    <!-- 退出按钮 -->
    <div class="logout-button-container">
      <el-button type="danger" @click="handleBack">返回</el-button>
    </div>
    <!-- 侧边栏导航 -->
    <div class="sidebar">
      <ul>
        <li :class="{ active: currentTab === 'personalInfo' }" @click="currentTab = 'personalInfo'">
          个人信息
        </li>
        <li :class="{ active: currentTab === 'singleUserHistoryAnalysis' }" @click="currentTab = 'singleUserHistoryAnalysis'">
          用户历史分析
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
import PersonalInfo from '../components/PersonalInfo.vue';
import SingleUserHistoryAnalysis from '../components/SingleUserHistoryAnalysis.vue';
import { useUserStore } from '@/store'; // 假设使用 Pinia 存储用户信息
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

export default defineComponent({
  components: {
    PersonalInfo,
    SingleUserHistoryAnalysis
  },
  setup() {
    const userStore = useUserStore();
    const router = useRouter();
    const currentTab = ref('personalInfo');

    const handleBack = () => {
      ElMessage.info('返回');
      router.push('/'); // 跳转回根页面
    };

    return {
      currentTab,
      handleBack
    };
  }
});
</script>

<style scoped>
.user-center {
  --primary-color: #4cafac;
  --secondary-color: #607D8B;
  display: flex;
  min-height: 100vh;
  background: #f8fafb;
}

.logout-button-container {
  margin-left: 20px;
  top: 24px;
  left: 24px;
  z-index: 100;
}

.sidebar {
  width: 240px;
  background: linear-gradient(180deg, #2C3E50 0%, #3498DB 100%);
  box-shadow: 4px 0 16px rgba(0,0,0,0.1);
  padding: 32px 0;
  position: sticky;
  top: 0;
}

.sidebar li {
  padding: 16px 32px;
  margin: 8px 16px;
  color: rgba(255,255,255,0.8);
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar li.active {
  background: rgba(255,255,255,0.15);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.main-content {
  flex: 1;
  padding: 40px;
  background: #ffffff;
  min-height: calc(100vh - 80px);
  margin: 40px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.05);
}
</style>