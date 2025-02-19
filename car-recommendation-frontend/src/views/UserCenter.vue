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
/* 全局布局样式 */
.user-center {
  display: flex;
  min-height: 100vh;
  position: relative; /* 为了让退出按钮定位 */
}

/* 退出按钮容器样式 */
.logout-button-container {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 10; /* 确保按钮显示在最上层 */
}



/* 侧边栏样式 */
.sidebar {
  width: 150px;
  background-color: aliceblue;
  border-right: 1px solid #ddd;
  padding: 50px;
  position: sticky;
  top: 0;
  height: 100vh;
}

.sidebar ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.sidebar li {
  padding: 12px 16px;
  margin: 8px 0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #333;
}

.sidebar li:hover {
  background-color: rgba(76, 175, 80, 0.1);
}

.sidebar li.active {
  background-color: #4CAF50;
  color: white;
  font-weight: 500;
}

/* 主内容区样式 */
.main-content {
  flex-grow: 1;
  padding: 2rem;
  background-color: #f5f5f5;
}
</style>