<template>
  <!-- 顶部导航栏 -->
  <nav class="nav-container">
    <div class="nav-left">
      <div @click="handleGoToPage('/')">首页</div>
      <div @click="handleGoToPage('/cars')">车型列表</div>
      <div @click="handleGoToPage('/recommendations')">个性化推荐</div>
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

  <!-- 3D轮播容器 -->
  <div class="carousel-container">
    <div class="carousel-track">
      <div
          v-for="(car, index) in carsData"
          :key="index"
          class="carousel-item"
          :style="getItemStyle(index)"
          @click="goToCarsPage(car)"
      >
        <img :src="getImageUrl(car.image)" :alt="car.name" @error="handleImageError">
        <div class="car-info">
          <div class="car-name">{{ car.name }}</div>
          <div class="car-type">{{ car.type }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent, ref, computed, onMounted } from 'vue';
import { useUserStore } from '@/store';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import axios from 'axios';

export default defineComponent({
  name: 'Home',
  setup() {
    const userStore = useUserStore();
    const router = useRouter();

    // 计算属性，从 userStore 获取用户信息
    const isAuthenticated = computed(() => !!userStore.token);
    const username = computed(() => userStore.username);
    const userRole = computed(() => userStore.role);

    const handleLogout = () => {
      userStore.logout();  // 清除登录状态
      ElMessage.info('用户已退出');
      router.push('/');  // 跳转回登录页
    };

    // 车辆数据
    const carsData = ref([]);

    // 获取图片URL
    const getImageUrl = (imageName) => {
      return `/api/images/${imageName}`;
    };

    // 图片加载失败处理
    const handleImageError = (event) => {
      event.target.src = '/placeholder-car.jpg'; // 准备一个默认图片
    };

    // 跳转到车型列表页
    const goToCarsPage = () => {
      router.push('/cars');
    };

    // 初始化车辆数据
    const initCarData = async () => {
      try {
        // 获取所有图片名称
        const response = await axios.get('/api/images');
        const imageNames = response.data;

        // 确保有足够的车辆信息
        const carInfos = [
          { name: '法拉利 LaFerrari', type: '超级跑车', price: '¥22,500,000' },
          { name: '保时捷 918', type: '跑车', price: '¥1,368,000' },
          { name: '小米su7 Ultra', type: '轿车', price: '¥529,900' },
          { name: '劳斯莱斯 Spectre', type: '豪华轿车', price: '¥299,900' },
          { name: '柯尼塞格 Agera RS', type: '超级跑车', price: '¥45,000,000' },
          { name: '五菱宏光 S', type: '面包车', price: '¥36,900' },
          { name: '兰博基尼 Centenario LP 770-4', type: '超级跑车', price: '¥18,000,000' },
          { name: '迈凯伦 senna', type: '超级跑车', price: '¥34,980,000' }
        ];

        // 创建车辆数据，确保不超过API返回的图片数量
        carsData.value = imageNames.slice(0, 8).map((name, index) => ({
          id: index + 1,
          ...carInfos[index % carInfos.length],
          image: name
        }));
      } catch (error) {
        console.error('获取图片列表失败:', error);
        // 如果API调用失败，使用默认数据
        carsData.value = [
          { id: 1, name: '法拉利LaFerrari', type: '超级跑车', price: '¥22,500,000', image: 'car1.jpg' },
          { id: 2, name: '保时捷918', type: '跑车', price: '¥1,368,000', image: 'car2.jpg' },
          { id: 3, name: '特斯拉Model S', type: '轿车', price: '¥799,900', image: 'car3.jpg' },
          { id: 4, name: '特斯拉Model 3', type: '中型车', price: '¥299,900', image: 'car4.jpg' },
          { id: 5, name: '兰博基尼Aventador', type: '超级跑车', price: '¥6,500,000', image: 'car5.jpg' },
          { id: 6, name: '奔驰S级', type: '豪华轿车', price: '¥1,458,000', image: 'car6.jpg' },
          { id: 7, name: '宝马7系', type: '豪华轿车', price: '¥828,000', image: 'car7.jpg' },
          { id: 8, name: '奥迪A8', type: '豪华轿车', price: '¥878,000', image: 'car8.jpg' }
        ];
      }
    };

    // 3D轮播样式计算
    const getItemStyle = (index) => {
      const angle = (360 / carsData.value.length) * index;
      return {
        transform: `
                    translateX(-50%)
                    translateY(-50%)
                    rotateY(${angle}deg)
                    translateZ(400px)
                `,
        zIndex: carsData.value.length - index
      };
    };

    // 统一的页面跳转处理函数
    const handleGoToPage = (path) => {
      if (!isAuthenticated.value) {
        // 未登录时跳转登录页，并记录目标页面
        router.push({ path: '/login', query: { redirect: path } });
        ElMessage.info('请先登录以访问该页面');
      } else {
        router.push(path);
      }
    };

    // 组件挂载时初始化数据
    onMounted(() => {
      initCarData();
    });

    return {
      isAuthenticated,
      username,
      userRole,
      handleLogout,
      carsData,
      goToCarsPage,
      getItemStyle,
      getImageUrl,
      handleImageError,
      handleGoToPage
    };
  }
});
</script>

<style scoped>
/* 顶部导航栏样式 */
.nav-container {
  position: sticky; /* 或 fixed */
  top: 0; /* 固定定位时需要 */
  z-index: 1000; /* 关键层级设置 */
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

.nav-left div {
  text-decoration: none;
  color: white;
  font-size: 16px;
  cursor: pointer;
}

.nav-left div:hover {
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

/* 修改后的3D轮播样式 */
.carousel-container {
  margin-top: 50px;
  perspective: 1000px;
  height: 400px;
  overflow: visible;
}

.carousel-track {
  z-index: 100;
  position: relative;
  width: 100%;
  height: 100%;
  transform-style: preserve-3d;
  animation: rotate 25s infinite linear;
}

/* 通用图片样式 */
.carousel-item {
  position: absolute;
  width: 250px;
  height: 180px;
  left: 50%;
  top: 50%;
  transform-style: preserve-3d;
  transition: all 0.5s ease;
  display: block;
  text-decoration: none;
  cursor: pointer;
}

.carousel-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.car-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 8px;
  text-align: center;
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
}

.car-name {
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 4px;
}

.car-type {
  font-size: 14px;
}

.carousel-item:hover {
  transform: translateX(-50%) translateY(-50%) scale(1.1) !important;
}

.carousel-item:hover img {
  transform: scale(1.1);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.4);
}

@keyframes rotate {
  from {
    transform: rotateY(0deg);
  }
  to {
    transform: rotateY(360deg);
  }
}

.carousel-container:hover .carousel-track {
  animation-play-state: paused;
}

.carousel-container::after {
  content: '';
  position: absolute;
  bottom: -50px;
  left: 50%;
  width: 60%;
  height: 100px;
  background: radial-gradient(ellipse at center,
  rgba(0, 0, 0, 0.5) 0%,
  rgba(0, 0, 0, 0) 100%);
  transform: translateX(-50%);
  filter: blur(10px);
  z-index: -1;
}
</style>