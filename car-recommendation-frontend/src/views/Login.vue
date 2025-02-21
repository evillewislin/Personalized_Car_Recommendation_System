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
/* 登录页面整体容器 */
.login-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #eceff1;
  min-height: 100vh;
}

/* 登录标题 */
.login-title {
  font-size: 2.5rem;
  color: #333;
  margin-bottom: 30px;
  font-weight: 600;
}

/* 表单容器 */
.form-wrapper {
  width: 100%;
  max-width: 450px;
  background-color: white;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.login-form {
  width: 100%;
}

.custom-input {
  margin-bottom: 1.5rem;
}

/* 登录按钮样式 */
.login-btn {
  width: 100%;
  padding: 14px;
  background-color: #1a73e8;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  color: white;
  transition: background-color 0.3s;
}

.login-btn:hover {
  background-color: #003c8f;
}

/* 注册链接 */
.register-link {
  color: #1a73e8;
  font-weight: 500;
  transition: color 0.3s;
}

.register-link:hover {
  color: #003c8f;
}
</style>