<template>
  <div class="login-container">
    <!-- 注册标题 -->
    <h1 class="login-title">欢迎注册汽车推荐系统</h1>

    <!-- 注册表单 -->
    <div class="form-wrapper">
      <el-form
          :model="registerForm"
          label-position="top"
          class="login-form"
      >
        <el-form-item label="用户名">
          <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              class="custom-input"
          ></el-input>
        </el-form-item>

        <el-form-item label="密码">
          <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              class="custom-input"
              show-password
          ></el-input>
        </el-form-item>

        <el-form-item label="确认密码">
          <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              class="custom-input"
              show-password
          ></el-input>
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              @click="handleRegister"
              class="login-btn"
          >
            立即注册
          </el-button>
        </el-form-item>

        <!-- 登录提示 -->
        <div class="register-tip">
          已有账号？请点击
          <router-link to="/login" class="register-link">
            登录
          </router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { defineComponent, reactive } from 'vue';
import axios from 'axios';
import router from "@/router";
import {ElMessage} from "element-plus";

export default defineComponent({
  name: 'Register',
  setup() {
    const registerForm = reactive({
      username: '',
      password: '',
      confirmPassword: ''
    });

    const handleRegister = async () => {
      if (registerForm.password !== registerForm.confirmPassword) {
        ElMessage.error('密码和确认密码不匹配');
        return;
      }
      try {
        const response = await axios.post('/api/auth/register', {
          username: registerForm.username,
          password: registerForm.password,
          confirmPassword:registerForm.confirmPassword
        });
        if (response.data) {
          ElMessage.success('注册成功，请登录');
          await router.push('/login');
        }
      } catch (error) {
        ElMessage.error('用户已存在');
      }
    };

    return { registerForm, handleRegister };
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