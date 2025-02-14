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
/* 复用登录页面的样式 */
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
  padding: 0;
  border-radius: 4px;
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
  text-decoration: none;
  margin-left: 0.5rem;
  font-weight: 500;
  transition: color 0.3s;
}

.register-link:hover {
  color: #45a049;
  text-decoration: underline;
}
</style>