<template>
  <div>
    <!-- 顶部导航栏 -->
    <nav class="nav-container">
      <div class="nav-left">
        <router-link to="/">首页</router-link>
        <router-link to="/recommendations">个性化推荐</router-link>
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

    <!-- 搜索框 -->
    <div class="search-container">
      <div class="search-wrapper">
        <input
            type="text"
            placeholder="搜索汽车..."
            v-model="searchQuery"
        />
      </div>
    </div>

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


    const handleLogout = () => {
      userStore.logout();  // 清除登录状态
      ElMessage.info('用户已退出');
      router.push('/');  // 跳转回登录页
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
.nav-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background-color: #f0f0f0;
  border-bottom: 1px solid #ddd;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.nav-left {
  display: flex;
  gap: 2rem;
  margin-left: 2rem;
}

.nav-left a {
  text-decoration: none;
  color: #333;
  font-weight: 500;
  padding: 0.5rem;
  border-radius: 4px;
}

.nav-left a:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-right: 2rem;
}

.welcome-text {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.search-container {
  margin: 10px auto 30px; /* 增加底部间距 */
  max-width: 600px;
  padding: 0 20px;
}

.search-wrapper {
  display: flex;
  gap: 10px; /* 输入框和按钮之间的间距 */
}

.search-container input {
  width: 100%;
  padding: 12px 20px;
  border: 2px solid #4CAF50;
  border-radius: 25px;
  font-size: 16px;
  outline: none;
  transition: all 0.3s ease;
}

.search-container input:focus {
  border-color: #45a049;
  box-shadow: 0 0 8px rgba(76, 175, 80, 0.3);
}

button {
  width: 90px;
  height: 40px;
  padding: 4px 3px;
  cursor: pointer;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  transition: background-color 0.3s;
  align-self: center; /* 按钮垂直居中 */
}

button:hover {
  background-color: #45a049;
}

.car-list-container {
  padding: 0 20px; /* 给车型列表添加左右内边距 */
}
</style>