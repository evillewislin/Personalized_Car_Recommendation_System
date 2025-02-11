<template>
  <div class="user-center">
    <el-card class="box-card">
      <h2>个人中心</h2>
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 个人信息标签 -->
        <el-tab-pane label="个人信息" name="profile">
          <el-form :model="profileForm" ref="profileFormRef" label-width="100px">
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" autocomplete="off"></el-input>
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="profileForm.password" type="password" autocomplete="off" placeholder="输入新密码"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="updateProfile">保存信息</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <!-- 偏好设置标签 -->
        <el-tab-pane label="偏好设置" name="preference">
          <el-form :model="preferenceForm" ref="preferenceFormRef" label-width="100px">
            <el-form-item label="最低价格">
              <el-input v-model="preferenceForm.minPrice" autocomplete="off" placeholder="请输入最低价格"></el-input>
            </el-form-item>
            <el-form-item label="最高价格">
              <el-input v-model="preferenceForm.maxPrice" autocomplete="off" placeholder="请输入最高价格"></el-input>
            </el-form-item>
            <el-form-item label="车型偏好">
              <el-input v-model="preferenceForm.preferredType" autocomplete="off" placeholder="例如SUV、轿车"></el-input>
            </el-form-item>
            <el-form-item label="燃油偏好">
              <el-input v-model="preferenceForm.preferredFuel" autocomplete="off" placeholder="例如汽油、电动"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="updatePreference">保存偏好</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import { defineComponent, ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
// 假设用户相关信息存储在 Pinia 中，如有需要可引入使用
import { useUserStore } from '@/store';

export default defineComponent({
  name: 'UserCenter',
  setup() {
    // 假设当前用户ID（实际使用时可从 store 中获取）
    const userId = 1;

    const activeTab = ref('profile');

    // 个人信息表单数据
    const profileForm = ref({
      username: '',
      password: ''
    });
    // 偏好设置表单数据
    const preferenceForm = ref({
      minPrice: '',
      maxPrice: '',
      preferredType: '',
      preferredFuel: ''
    });

    const profileFormRef = ref(null);
    const preferenceFormRef = ref(null);

    // 获取用户基本信息
    const fetchUserProfile = async () => {
      try {
        const response = await axios.get(`/api/user/${userId}`);
        if (response.data) {
          profileForm.value.username = response.data.username;
          // 密码一般不返回，可留空等待用户输入新密码
        }
      } catch (error) {
        ElMessage.error('获取用户信息失败');
        console.error(error);
      }
    };

    // 获取用户偏好设置
    const fetchUserPreference = async () => {
      try {
        const response = await axios.get(`/api/user/${userId}/preference`);
        if (response.data) {
          preferenceForm.value.minPrice = response.data.minPrice;
          preferenceForm.value.maxPrice = response.data.maxPrice;
          preferenceForm.value.preferredType = response.data.preferredType;
          preferenceForm.value.preferredFuel = response.data.preferredFuel;
        }
      } catch (error) {
        ElMessage.error('获取用户偏好失败');
        console.error(error);
      }
    };

    // 更新用户个人信息
    const updateProfile = async () => {
      try {
        const response = await axios.put(`/api/user/${userId}`, profileForm.value);
        if (response.data) {
          ElMessage.success('用户信息更新成功');
        }
      } catch (error) {
        ElMessage.error('更新用户信息失败');
        console.error(error);
      }
    };

    // 更新或新增用户偏好设置
    const updatePreference = async () => {
      try {
        const response = await axios.put(`/api/user/${userId}/preference`, preferenceForm.value);
        if (response.data) {
          ElMessage.success('用户偏好更新成功');
        }
      } catch (error) {
        ElMessage.error('更新用户偏好失败');
        console.error(error);
      }
    };

    onMounted(() => {
      fetchUserProfile();
      fetchUserPreference();
    });

    return {
      activeTab,
      profileForm,
      preferenceForm,
      profileFormRef,
      preferenceFormRef,
      updateProfile,
      updatePreference
    };
  }
});
</script>

<style scoped>
.user-center {
  max-width: 600px;
  margin: 20px auto;
}
.box-card {
  padding: 20px;
}
</style>
