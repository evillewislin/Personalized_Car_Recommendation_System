<template>
  <div class="login-container">
    <h1 class="login-title">欢迎注册汽车推荐系统</h1>

    <div class="form-wrapper">
      <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="rules"
          label-position="top"
          class="login-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
              v-model="registerForm.username"
              placeholder="字母/数字组合，不能全为数字"
              class="custom-input"
          ></el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              class="custom-input"
              show-password
          ></el-input>
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              class="custom-input"
              show-password
          ></el-input>
        </el-form-item>

        <el-form-item label="年龄" prop="age">
          <el-input
              v-model.number="registerForm.age"
              type="number"
              placeholder="选填，范围18-80"
              class="custom-input"
          ></el-input>
        </el-form-item>

        <el-form-item label="地区" prop="region">
          <el-input
              v-model="registerForm.region"
              placeholder="选填，请输入所在市（如：北京市）"
              class="custom-input"
              clearable
              :maxlength="10"
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
import { defineComponent, reactive, ref } from 'vue';
import axios from 'axios';
import router from "@/router";
import {ElMessage, ElMessageBox} from "element-plus";

export default defineComponent({
  name: 'Register',
  setup() {
    const registerFormRef = ref(null);
    const registerForm = reactive({
      username: '',
      password: '',
      confirmPassword: '',
      age: '',
      region: ''
    });

    const validateUsername = (rule, value, callback) => {
      if (!value) {
        return callback(new Error('用户名不能为空'));
      }
      if (/^\d+$/.test(value)) {
        callback(new Error('不能全为数字'));
      } else if (!/^[a-zA-Z0-9]+$/.test(value)) {
        callback(new Error('只能包含字母和数字'));
      } else {
        callback();
      }
    };

    const validateConfirmPass = (rule, value, callback) => {
      if (value !== registerForm.password) {
        callback(new Error('两次输入密码不一致'));
      } else {
        callback();
      }
    };

    const validateAge = (rule, value, callback) => {
      if (value) {
        const age = Number(value);
        if (isNaN(age)) {
          callback(new Error('必须为数字'));
        } else if (age < 18 || age > 80) {
          callback(new Error('年龄需在18-80之间'));
        } else {
          callback();
        }
      } else {
        callback();
      }
    };

    const validateRegion = (rule, value, callback) => {
      if (value && !value.includes('市')) {
        callback(new Error('必须包含"市"字'));
      } else {
        callback();
      }
    };

    const rules = reactive({
      username: [
        { required: true, validator: validateUsername, trigger: 'blur' }
      ],
      password: [
        { required: true, message: '密码不能为空', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        { validator: validateConfirmPass, trigger: 'blur' }
      ],
      age: [
        { validator: validateAge, trigger: 'blur' }
      ],
      region: [
        { validator: validateRegion, trigger: 'blur' }
      ]
    });

    const handleRegister = async () => {
      await registerFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            const response = await axios.post('/api/auth/register', registerForm);
            if (response.data) {
              ElMessage.success('注册成功，请登录');
              await ElMessageBox.alert('新用户如果没有设置个人信息，可能会导致推荐结果不精准，请新用户进入个人中心填写！！！', '提示', {
                confirmButtonText: '确定'
              });
              await router.push('/login');
            }
          } catch (error) {
            ElMessage.error(error.response?.data?.message || '用户已存在');
          }
        } else {
          ElMessage.warning('请完善表单信息');
          return false;
        }
      });
    };

    return { registerForm, registerFormRef, rules, handleRegister };
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
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  padding: 12px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.custom-input:hover {
  border-color: #1a73e8;
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

/* 输入框聚焦样式 */
.custom-input:focus {
  outline: none;
  border-color: #1a73e8;
  box-shadow: 0 0 0 2px rgba(26, 115, 232, 0.1);
}
</style>