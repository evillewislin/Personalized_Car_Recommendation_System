<template>
  <div>
    <el-form :model="registerForm">
      <el-form-item label="用户名">
        <el-input v-model="registerForm.username"></el-input>
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="registerForm.password" type="password"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleRegister">注册</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { defineComponent, reactive } from 'vue';
import axios from 'axios';

export default defineComponent({
  name: 'Register',
  setup() {
    const registerForm = reactive({
      username: '',
      password: ''
    });

    const handleRegister = async () => {
      try {
        const response = await axios.post('/api/auth/register', registerForm);
        if (response.data) {
          alert('注册成功，请登录');
        }
      } catch (error) {
        console.error(error);
      }
    };

    return { registerForm, handleRegister };
  }
});
</script>
