<template>
    <div>
      <!-- 顶部导航栏 -->
      <nav class="nav-container">
        <div class="nav-left">
          <router-link to="/" :class="{ 'router-link-active': $route.path === '/' }">首页</router-link>
          <router-link to="/cars" :class="{ 'router-link-active': $route.path === '/cars' }">车型列表</router-link>
          <router-link to="/recommendations" :class="{ 'router-link-active': $route.path === '/recommendations' }">个性化推荐</router-link>
        </div>
        <div class="nav-right">
          <template v-if="!isAuthenticated">
            <router-link to="/login">
              <button>登录</button>
            </router-link>
          </template>
          <template v-if="!isAuthenticated">
            <router-link to="/adminlogin">
              <button>管理员登录</button>
            </router-link>
          </template>
          <template v-else>
            <span class="welcome-text">欢迎，{{ username }}</span>
            <router-link v-if="userRole === 'admin'" to="/admin">
              <button>管理员后台</button>
            </router-link>
            <router-link v-if="userRole === 'user'" to="/user">
              <button>用户中心</button>
            </router-link>
            <button @click="handleLogout">退出</button>
          </template>
        </div>
      </nav>
  
      <!-- 页面内容 -->
      <div class="car-list-container">
        <CarList :searchQuery="searchQuery" />
      </div>
    </div>
  </template>
  
  <script>
  import { defineComponent, ref, computed } from 'vue';
  import { useUserStore } from '@/store';
  import { useRouter } from 'vue-router';
  import CarList from '../components/CarList.vue';
  import { ElMessage } from 'element-plus';
  
  export default defineComponent({
    name: 'Home',
    components: { CarList },
    setup() {
      const userStore = useUserStore();
      const router = useRouter();
  
      // 计算属性，从 userStore 获取用户信息
      const isAuthenticated = computed(() => !!userStore.token);
      const username = computed(() => userStore.username);
      const userRole = computed(() => userStore.role);
  
      const searchQuery = ref('');
  
      const handleLogout = async () => {
        try {
          await userStore.logout();  // 清除登录状态
          ElMessage.info('用户已退出');
          router.push('/');  // 跳转回登录页
        } catch (error) {
          ElMessage.error('退出登录失败，请稍后重试');
        }
      };
  
      return {
        isAuthenticated,
        username,
        userRole,
        searchQuery,
        handleLogout
      };
    }
  });
  </script>
  
  <style scoped>
  /* 顶部导航栏样式 */
  .nav-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: #333;
    padding: 15px 30px;
  }
  
  .nav-left {
    display: flex;
    gap: 20px;
  }
  
  .nav-left a {
    text-decoration: none;
    color: white;
    font-size: 16px;
  }
  
  .nav-left a.router-link-active {
    color: #ffeb3b;
  }
  
  .nav-left a:hover {
    color: #ffeb3b;
  }
  
  /* 导航栏右侧部分 */
  .nav-right {
    display: flex;
    gap: 15px;
    align-items: center;
  }
  
  .welcome-text {
    font-size: 16px;
    color: white;
    font-weight: bold;
  }
  
  /* 搜索框 */
  .search-container {
    margin: 30px auto;
    max-width: 600px;
    padding: 0 20px;
  }
  
  .search-container input {
    width: 100%;
    padding: 12px 20px;
    border: 2px solid #1a73e8;
    border-radius: 25px;
    font-size: 16px;
  }
  
  .search-container input:focus {
    border-color: #003c8f;
    box-shadow: 0 0 5px rgba(0, 0, 0, 0.2);
  }
  
  /* 按钮样式 */
  button {
    padding: 10px 20px;
    background-color: #1a73e8;
    border: none;
    border-radius: 4px;
    color: white;
    cursor: pointer;
  }
  
  button:hover {
    background-color: #003c8f;
  }
  
  /* 车型列表容器 */
  .car-list-container {
    padding: 20px;
    background-color: #ffffff;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  }
  </style>