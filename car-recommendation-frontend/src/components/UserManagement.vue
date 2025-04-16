<template>
  <div>
    <h2>用户管理</h2>
    <!-- 操作按钮和搜索框 -->
    <div class="action-buttons">
      <el-button type="primary" @click="showAddModal">添加用户</el-button>
      <el-input class="el-input__inner" v-model="searchQuery" placeholder="请输入用户名搜索" style="width: 200px"></el-input>
      <el-button @click="searchUsers">搜索</el-button>
    </div>
    <!-- 用户列表表格 -->
    <el-table :data="paginatedUsers" style="width: 100%">
      <el-table-column prop="userId" label="用户 ID"></el-table-column>
      <el-table-column prop="username" label="用户名"></el-table-column>
      <el-table-column prop="role" label="身份"></el-table-column>
      <el-table-column prop="age" label="年龄"></el-table-column>
      <el-table-column prop="region" label="地区"></el-table-column>
      <!-- 操作列 -->
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" @click="showEditModal(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteUser(scope.row.userId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <div class="pagination-container">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 30, 50]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredUsers.length">
      </el-pagination>
    </div>

    <!-- 添加用户模态框 -->
    <div v-if="addModalVisible" class="custom-modal">
      <div class="custom-modal-content">
        <h3>添加用户</h3>
        <el-form :model="addForm" :rules="addRules" ref="addFormRef">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="addForm.username"></el-input>
          </el-form-item>
          <el-form-item label="年龄" prop="age">
            <el-input v-model.number="addForm.age" type="number"></el-input>
          </el-form-item>
          <el-form-item label="地区" prop="region">
            <el-input v-model="addForm.region"></el-input>
          </el-form-item>
        </el-form>
        <h4>Tip:默认密码为123456</h4>
        <div class="custom-modal-footer">
          <el-button @click="addModalVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAddForm">确定</el-button>
        </div>
      </div>
    </div>

    <!-- 编辑用户模态框 -->
    <div v-if="editModalVisible" class="custom-modal">
      <div class="custom-modal-content">
        <h3>编辑用户</h3>
        <el-form :model="editForm" :rules="editRules" ref="editFormRef">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="editForm.username" disabled></el-input>
          </el-form-item>
          <el-form-item label="年龄" prop="age">
            <el-input v-model.number="editForm.age" type="number"></el-input>
          </el-form-item>
          <el-form-item label="地区" prop="region">
            <el-input v-model="editForm.region"></el-input>
          </el-form-item>
          <el-checkbox v-model="isChangePassword">是否修改密码</el-checkbox>
          <template v-if="isChangePassword">
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="editForm.newPassword" type="password"></el-input>
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmNewPassword">
              <el-input v-model="editForm.confirmNewPassword" type="password"></el-input>
            </el-form-item>
          </template>
        </el-form>
        <div class="custom-modal-footer">
          <el-button @click="editModalVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEditForm">确定</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

// 存储所有用户数据
const users = ref([]);
// 存储搜索关键词
const searchQuery = ref('');
// 存储过滤后的用户数据
const filteredUsers = ref([]);

// 分页相关
const currentPage = ref(1);
const pageSize = ref(10);

// 计算当前页显示的用户数据
const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredUsers.value.slice(start, end);
});

// 添加用户模态框相关
const addModalVisible = ref(false);
const addForm = ref({
  username: '',
  age: null,
  region: '',
  password: '',
  confirmPassword: ''
});
const addRules = ref({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  age: [
    { type: 'number', message: '年龄必须为数字', trigger: 'blur' }
  ],
  region: [],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请输入确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value!== addForm.value.password) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
});
const addFormRef = ref(null);

// 编辑用户模态框相关
const editModalVisible = ref(false);
const editForm = ref({
  userId: null,
  username: '',
  age: null,
  region: '',
  newPassword: '',
  confirmNewPassword: ''
});
const isChangePassword = ref(false);
const showPassword = ref(false);
const editRules = ref({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  age: [
    { type: 'number', message: '年龄必须为数字', trigger: 'blur' }
  ],
  region: [],
  newPassword: [
    { required: false, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmNewPassword: [
    { required: false, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (isChangePassword.value && value!== editForm.value.newPassword) {
          callback(new Error('两次输入的新密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
});
const editFormRef = ref(null);

// 获取用户列表
const getUsers = async () => {
  try {
    const response = await axios.get('/api/users');
    users.value = response.data;
    filteredUsers.value = response.data;
    // 重置到第一页
    currentPage.value = 1;
  } catch (error) {
    console.error('获取用户列表失败:', error);
    ElMessage.error('获取用户列表失败，请稍后重试');
  }
};

// 显示添加用户模态框
const showAddModal = () => {
  addModalVisible.value = true;
  addForm.value = {
    username: '',
    age: null,
    region: '',
    password: '123456',
    confirmPassword: '123456'
  };
};

// 提交添加用户表单
const submitAddForm = async () => {
  const form = addFormRef.value;
  if (form) {
    form.validate(async (valid) => {
      if (valid) {
        try {
          const { username, age, region, password, confirmPassword } = addForm.value;
          const requestData = { username, password, confirmPassword };
          if (age!== null) {
            requestData.age = age;
          }
          if (region) {
            requestData.region = region;
          }
          console.log('请求数据:', requestData);
          await axios.post('/api/auth/register', requestData);
          ElMessage.success('用户添加成功');
          addModalVisible.value = false;
          await getUsers();
        } catch (error) {
          console.error('添加用户失败:', error);
          ElMessage.error('添加用户失败，请稍后重试');
        }
      }
    });
  }
};

// 显示编辑用户模态框
const showEditModal = async (user) => {
  editModalVisible.value = true;
  try {
    const token = localStorage.getItem('token');
    const headers = {
      Authorization: `Bearer ${token}`
    };
    const { data } = await axios.get(`/api/users/${user.userId}`, { headers });
    editForm.value = {
      userId: user.userId,
      username: user.username,
      age: user.age,
      region: user.region,
      newPassword: '',
      confirmNewPassword: ''
    };
    isChangePassword.value = false;
    showPassword.value = false;
  } catch (error) {
    console.error('获取用户信息失败:', error);
    ElMessage.error('获取用户信息失败，请稍后重试');
  }
};

// 提交编辑用户表单
const submitEditForm = async () => {
  const form = editFormRef.value;
  if (form) {
    form.validate(async (valid) => {
      if (valid) {
        try {
          const token = localStorage.getItem('token');
          const headers = {
            Authorization: `Bearer ${token}`
          };
          const {userId, username, age, region, newPassword} = editForm.value;
          const requestData = {username};
          if (age !== null) {
            requestData.age = age;
          }
          if (region) {
            requestData.region = region;
          }
          if (isChangePassword.value) {
            requestData.newPassword = newPassword;
          }
          await axios.put(`/api/users/${userId}`, requestData, {headers});
          ElMessage.success('用户信息更新成功');
          editModalVisible.value = false;
          await getUsers();
        } catch (error) {
          console.error('编辑用户失败:', error);
          if (error.response) {
            if (error.response.status === 401) {
              ElMessage.error('未授权，请重新登录');
            } else if (error.response.status === 404) {
              ElMessage.error('用户不存在');
            } else if (error.response.data && error.response.data.message) {
              ElMessage.error(error.response.data.message);
            } else {
              ElMessage.error('编辑用户失败，请稍后重试');
            }
          } else if (error.request) {
            ElMessage.error('网络请求失败，请检查网络连接');
          } else {
            ElMessage.error('发生未知错误，请稍后重试');
          }
        }
      }
    });
  }
};

// 删除用户
const deleteUser = async (userId) => {
  try {
    await axios.delete(`/api/users/${userId}`);
    ElMessage.success('用户删除成功');
    await getUsers();
  } catch (error) {
    console.error('删除用户失败:', error);
    ElMessage.error('删除用户失败，请稍后重试');
  }
};

// 搜索用户
const searchUsers = () => {
  if (searchQuery.value.trim() === '') {
    filteredUsers.value = users.value;
  } else {
    filteredUsers.value = users.value.filter(user => {
      return user.username.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
          (user.role && user.role.toLowerCase().includes(searchQuery.value.toLowerCase())) ||
          (user.age !== null && String(user.age).includes(searchQuery.value)) ||
          (user.region && user.region.toLowerCase().includes(searchQuery.value.toLowerCase()));
    });
  }
  // 搜索后重置到第一页
  currentPage.value = 1;
};

// 分页相关方法
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1; // 改变每页条数时重置到第一页
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
};

// 组件挂载时获取用户列表
onMounted(() => {
  getUsers();
});
</script>

<style scoped>
/* 操作按钮容器样式 */
.action-buttons {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}
.el-input__inner{
  width: 50px;
}

/* 表格样式 */
.el-table {
  margin-top: 20px;
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
}

.el-table th {
  background-color: #1a73e8;
  color: white;
}

.el-table td {
  background-color: #f9f9f9;
}

/* 编辑和删除按钮 */
.el-button {
  border-radius: 4px;
}

.el-button[type="danger"] {
  background-color: #ff5252;
  color: white;
}

.el-button[type="danger"]:hover {
  background-color: #ff1744;
}

/* 模态框样式 */
.custom-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.custom-modal-content {
  background-color: white;
  padding: 20px;
  border-radius: 5px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

/* 分页样式 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>