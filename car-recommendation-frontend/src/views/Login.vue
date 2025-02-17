<template>
  <div class="login-container">
    <!-- 登录标题 -->
    <h1 class="login-title">欢迎登录汽车推荐系统</h1>

    <!-- 登录表单 -->
    <div class="form-wrapper">
      <div class="back-button-wrapper">
        <router-link to="/" class="register-link">
          返回
        </router-link>
      </div>
      <el-form
          :model="loginForm"
          label-position="top"
          class="login-form"
      >
        <el-form-item label="用户名">
          <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              class="custom-input"
          ></el-input>
        </el-form-item>

        <el-form-item label="密码">
          <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              class="custom-input"
              show-password
          ></el-input>
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              @click="handleLogin"
              class="login-btn"
          >
            立即登录
          </el-button>
        </el-form-item>

        <!-- 注册提示 -->
        <div class="register-tip">
          如果未注册，请点击
          <router-link to="/register" class="register-link">
            注册
          </router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import {defineComponent, reactive, ref} from 'vue';
import axios from 'axios';
import { useUserStore } from '@/store';
import router from "@/router";
import {ElMessage} from "element-plus";

export default defineComponent({
  name: 'Login',
  setup() {
    // 保持原有逻辑不变
    const loginForm = reactive({
      username: '',
      password: ''
    });
    const userStore = useUserStore();
    const loading = ref(false);

    const handleLogin = async () => {
      loading.value = true;
      try {
        const response = await axios.post('/api/auth/login', loginForm);
        if (response.data) {
          const token = response.data.token;
          userStore.setToken(token); // 保存 token 到 Pinia store
          axios.defaults.headers['Authorization'] = `Bearer ${token}`;// 设置全局 Authorization 头部
          userStore.setUsername(loginForm.username);
          ElMessage.success('登录成功');
          await router.push('/')
        } else {
          ElMessage.error('用户名或密码错误');
        }
      } catch (error) {
        ElMessage.error('用户名或密码错误');
      } finally {
        loading.value = false;
      }
    };

    return { loginForm, handleLogin };
  }
});
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: #f8f9fa;
  padding-top: 50px;
}

.login-title {
  color: #333;
  margin-bottom: 2rem;
  font-size: 2rem;
  text-align: center;
}

.form-wrapper {
  width: 100%;
  max-width: 400px;
  padding: 2rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
.custom-input :deep(.el-input__wrapper) {
  /* 添加你的自定义样式 */
  padding: 0; /* 例如，设置输入框的内边距 */
  border-radius: 4px; /* 例如，设置输入框的边框圆角 */
}
.login-form {
  margin: 0 auto;
}

.custom-input {
  margin-bottom: 1rem;
}

.custom-input :deep(.el-input__inner) {
  height: 45px;
  border-radius: 0;
  border: 2px solid #e0e0e0;
  padding: 10px;
  transition: border-color 0.3s;
}

.custom-input :deep(.el-input__inner:focus) {
  border-color: #4CAF50;
  box-shadow: 0 0 0 rgba(76, 175, 80, 0.3);
}
.back-button-wrapper {
  width: 100%;
  max-width: 400px;
  margin-bottom: 1rem;
}
.login-btn {
  width: 100%;
  height: 45px;
  font-size: 16px;
  background-color: #4CAF50;
  border: none;
  transition: background-color 0.3s;
}

.login-btn:hover {
  background-color: #45a049;
}

.register-tip {
  text-align: center;
  margin-top: 1.5rem;
  color: #666;
}

.register-link {
  color: #4CAF50;
  margin-left: 0.5rem;
  font-weight: 500;
  transition: color 0.3s;
}

.register-link:hover {
  color: #45a049;
  text-decoration: underline;
}
</style>