<template>
  <div class="login-container">
    <!-- 登录标题 -->
    <h1 class="login-title">欢迎登录汽车管理后台</h1>

    <!-- 登录表单 -->
    <router-link to="/" class="register-link">
      返回
    </router-link>
    <div class="form-wrapper">
      <el-form
          :model="loginForm"
          label-position="top"
          class="login-form"
      >
        <el-form-item label="管理员名">
          <el-input
              v-model="loginForm.adminname"
              placeholder="请输入管理员名"
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

      </el-form>
    </div>
  </div>
</template>

<script>
import { defineComponent, reactive } from 'vue';
import axios from 'axios';
import {useAdminStore} from '@/store';
import router from "@/router";
import {ElMessage} from "element-plus";

export default defineComponent({
  name: 'AdminLogin',
  setup() {
    // 保持原有逻辑不变
    const loginForm = reactive({
      adminname: '',
      password: ''
    });
    const adminStore = useAdminStore();
    const loading = ref(false);
    const handleLogin = async () => {
      loading.value = true;
      try {
        const response = await axios.post('/api/auth/adminlogin', loginForm);
        if (response.data) {
          const token = response.data.token;
          adminStore.setToken(token);
          axios.defaults.headers['Authorization'] = `Bearer ${token}`;
          adminStore.setAdminname(loginForm.adminname);
          ElMessage.success('登录成功');
          await router.push('/admin')
        } else {
          ElMessage.error('管理员名或密码错误');
        }
      } catch (error) {
        console.error(error);
      }finally {
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