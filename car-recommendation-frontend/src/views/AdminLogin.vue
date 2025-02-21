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