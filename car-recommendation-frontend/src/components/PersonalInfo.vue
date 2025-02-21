<template>
  <div class="personal-info-container">
    <h2>个人信息</h2>
    <el-form :model="profileForm" ref="profileFormRef" label-width="120px" :rules="rules" status-icon>
      <el-form-item label="姓名" prop="username">
        <el-input v-model="profileForm.username" placeholder="请输入姓名"></el-input>
      </el-form-item>
      <el-form-item label="旧密码" prop="oldPassword" v-if="isChangingPassword">
        <el-input v-model="profileForm.oldPassword" type="password" placeholder="请输入旧密码"></el-input>
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword" v-if="isChangingPassword">
        <el-input v-model="profileForm.newPassword" type="password" placeholder="请输入新密码"></el-input>
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword" v-if="isChangingPassword">
        <el-input v-model="profileForm.confirmPassword" type="password" placeholder="请再次输入新密码"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveProfile" :loading="isLoading">保存信息</el-button>
        <el-button @click="togglePasswordChange">
          {{ isChangingPassword ? '取消修改密码' : '修改密码' }}
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { defineComponent, ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/store';

// 添加请求拦截器
axios.interceptors.request.use(config => {
  const userStore = useUserStore();
  const token = userStore.token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 解析 JWT token 中的 userId
const getUserIdFromToken = (token) => {
  if (!token) return null;
  try {
    // 去除可能存在的 "Bearer " 前缀
    token = token.replace("Bearer ", "");
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    const payload = JSON.parse(jsonPayload);
    return parseInt(payload.sub); // 从 sub 字段获取 userId
  } catch (error) {
    console.error('解析 Token 出错:', error);
    return null;
  }
};

export default defineComponent({
  setup() {
    const userStore = useUserStore();
    const token = userStore.token;
    const userId = ref(getUserIdFromToken(token));

    const profileForm = ref({
      username: '',
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    });

    const profileFormRef = ref(null);

    const isChangingPassword = ref(false);
    const isLoading = ref(false);

    const rules = {
      username: [
        { required: true, message: '请输入姓名', trigger: 'blur' }
      ],
      oldPassword: [
        { required: true, message: '请输入旧密码', trigger: 'blur' }
      ],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认新密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== profileForm.value.newPassword) {
              callback(new Error('两次输入的密码不一致'));
            } else {
              callback();
            }
          },
          trigger: 'blur'
        }
      ]
    };

    const fetchUserProfile = async () => {
      if (!userId.value) {
        ElMessage.error('未获取到用户 ID，请重新登录');
        return;
      }
      try {
        isLoading.value = true;
        const response = await axios.get(`/api/users/${userId.value}`);
        if (response.data) {
          profileForm.value.username = response.data.username || '';
        }
      } catch (error) {
        if (error.response) {
          const status = error.response.status;
          if (status === 401) {
            ElMessage.error('未授权，请重新登录');
          } else if (status === 404) {
            ElMessage.error('未找到用户信息');
          } else {
            ElMessage.error('获取用户信息失败');
          }
        } else {
          ElMessage.error('网络错误，请稍后重试');
        }
        console.error(error);
      } finally {
        isLoading.value = false;
      }
    };

    const saveProfile = async () => {
      if (!userId.value) {
        ElMessage.error('未获取到用户 ID，请重新登录');
        return;
      }
      profileFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            isLoading.value = true;
            let dataToSend = {username: profileForm.value.username};
            if (isChangingPassword.value) {
              dataToSend = {
                ...dataToSend,
                oldPassword: profileForm.value.oldPassword,
                newPassword: profileForm.value.newPassword
              };
            }
            const response = await axios.put(`/api/users/${userId.value}`, dataToSend);
            if (response.data) {
              ElMessage.success('用户信息更新成功');
              location.reload();
              if (isChangingPassword.value) {
                isChangingPassword.value = false;
                profileForm.value.oldPassword = '';
                profileForm.value.newPassword = '';
                profileForm.value.confirmPassword = '';
                profileFormRef.value.resetFields();
              }
            }
          } catch (error) {
            if (error.response) {
              const status = error.response.status;
              if (status === 401) {
                ElMessage.error('未授权，请重新登录');
              } else if (status === 404) {
                ElMessage.error('未找到用户信息');
              } else if (status === 400) {
                ElMessage.error('旧密码不正确，请重新输入');
              } else {
                ElMessage.error('更新用户信息失败');
              }
            } else {
              ElMessage.error('网络错误，请稍后重试');
            }
            console.error(error);
          } finally {
            isLoading.value = false;
          }
        } else {
          ElMessage.warning('请完善表单信息');
        }
      });
    };

    const togglePasswordChange = () => {
      if (isChangingPassword.value) {
        profileForm.value.oldPassword = '';
        profileForm.value.newPassword = '';
        profileForm.value.confirmPassword = '';
        profileFormRef.value.resetFields();
      }
      isChangingPassword.value = !isChangingPassword.value;
    };

    onMounted(() => {
      fetchUserProfile();
    });

    return {
      profileForm,
      profileFormRef,
      isChangingPassword,
      rules,
      saveProfile,
      togglePasswordChange,
      isLoading
    };
  }
});
</script>

<style scoped>
/* 个人信息容器 */
.personal-info-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  margin: 20px auto;
}

/* 按钮样式 */
el-button {
  background-color: #4caf50;
  color: white;
  border-radius: 4px;
}

el-button:hover {
  background-color: #45a049;
}

/* 修改密码切换按钮 */
el-button[type="default"] {
  background-color: #f0f0f0;
  color: #333;
  border-radius: 4px;
}

el-button[type="default"]:hover {
  background-color: #d1d1d1;
}

/* 表单输入样式 */
el-form-item {
  margin-bottom: 20px;
}

el-input {
  border-radius: 4px;
  padding: 10px;
}

el-input[type="password"] {
  width: 100%;
}
</style>