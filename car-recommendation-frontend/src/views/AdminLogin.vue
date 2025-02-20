<template>
  <div class="login-container">
    <!-- 登录标题 -->
    <h1 class="login-title">欢迎登录汽车管理后台</h1>

    <!-- 登录表单 -->
    <div class="form-wrapper">
      <div class="back-button-wrapper">
        <router-link to="/" class="register-link">
          返回
        </router-link>
      </div>
      <el-form
          :model="loginForm"
          :rules="rules"
          label-position="top"
          class="login-form"
          ref="loginFormRef"
      >
        <el-form-item label="管理员名" prop="adminname">
          <el-input
              v-model="loginForm.adminname"
              placeholder="请输入管理员名"
              class="custom-input"
          ></el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
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
              :loading="loading"
          >
            立即登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { defineComponent, reactive, ref } from 'vue';
import axios from 'axios';
import { useAdminStore } from '@/store';
import router from "@/router";
import { ElMessage } from "element-plus";

export default defineComponent({
  name: 'AdminLogin',
  setup() {
    // 表单数据
    const loginForm = reactive({
      adminname: '',
      password: ''
    });

    // 表单验证规则
    const rules = reactive({
      adminname: [
        {required: true, message: '请输入管理员名', trigger: 'blur'}
      ],
      password: [
        {required: true, message: '请输入密码', trigger: 'blur'}
      ]
    });

    const adminStore = useAdminStore();
    const loading = ref(false);
    const loginFormRef = ref(null);

    const handleLogin = async () => {
      const form = loginFormRef.value;
      form.validate(async (valid) => {
        if (valid) {
          loading.value = true;
          try {
            const response = await axios.post('/api/auth/adminlogin', loginForm);
            if (response.data) {
              const token = response.data.token;
              adminStore.setToken(token);
              axios.defaults.headers['Authorization'] = `Bearer ${token}`;
              adminStore.setAdminname(loginForm.adminname);
              ElMessage.success('登录成功');
              await router.push('/admin');
            } else {
              ElMessage.error('管理员名或密码错误');
            }
          } catch (error) {
            console.error(error);
            if (error.response) {
              ElMessage.error(`登录失败: ${error.response.status} - ${error.response.statusText}`);
            } else if (error.request) {
              ElMessage.error('登录失败: 无响应');
            } else {
              ElMessage.error('登录失败: ' + error.message);
            }
          } finally {
            loading.value = false;
          }
        } else {
          ElMessage.error('请填写完整信息');
        }
      });
    };

    return {
      loginForm,
      rules,
      loading,
      loginFormRef,
      handleLogin
    };
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

.back-button-wrapper {
  width: 100%;
  max-width: 400px;
  margin-bottom: 1rem;
}

.form-wrapper {
  width: 100%;
  max-width: 400px;
  padding: 2rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.login-form {
  margin: 0 auto;
}

.custom-input {
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