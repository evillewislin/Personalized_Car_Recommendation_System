<template>
  <div class="admin-dashboard">
    <!-- 退出按钮 -->
    <div class="logout-button-container">
      <el-button type="danger" @click="handleLogout">退出</el-button>
    </div>
    <!-- 侧边栏导航 -->
    <div class="sidebar">
      <ul>
        <li :class="{ active: currentTab === 'userManagement' }" @click="currentTab = 'userManagement'">
          用户管理
        </li>
        <li :class="{ active: currentTab === 'carManagement' }" @click="currentTab = 'carManagement'">
          汽车管理
        </li>
        <li :class="{ active: currentTab === 'carAnalysis' }" @click="currentTab = 'carAnalysis'">
          汽车分析
        </li>
        <li :class="{ active: currentTab === 'userHistoryAnalysis' }" @click="currentTab = 'userHistoryAnalysis'">
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
import UserManagement from '../components/UserManagement.vue';
import CarManagement from '../components/CarManagement.vue';
import CarAnalysis from '../components/CarAnalysis.vue';
import UserHistoryAnalysis from '../components/UserHistoryAnalysis.vue';
import { useAdminStore } from '@/store'; // 使用管理员的 Store
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

export default defineComponent({
  components: {
    UserManagement,
    CarManagement,
    CarAnalysis,
    UserHistoryAnalysis
  },
  setup() {
    const adminStore = useAdminStore();
    const router = useRouter();
    const currentTab = ref('userManagement');

    const handleLogout = () => {
      adminStore.logout(); // 调用管理员 Store 的退出方法
      ElMessage.info('用户已退出');
      router.push('/'); // 跳转回根页面
    };

    return {
      currentTab,
      handleLogout
    };
  }
});
</script>

<style scoped>
/* 全局布局样式 */
.admin-dashboard {
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
.el-button{
  margin:0 8px 10px 0;

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