<template>
  <div>
    <h2>用户管理</h2>
    <!-- 操作按钮和搜索框 -->
    <div class="action-buttons">
      <el-button type="primary" @click="showAddModal">添加用户</el-button>
      <el-input v-model="searchQuery" placeholder="请输入搜索关键词" style="width: 200px"></el-input>
      <el-button @click="searchUsers">搜索</el-button>
    </div>
    <!-- 用户列表表格 -->
    <el-table :data="paginatedUsers" style="width: 100%">
      <el-table-column prop="userId" label="用户 ID"></el-table-column>
      <el-table-column prop="username" label="用户名"></el-table-column>
      <el-table-column prop="role" label="身份"></el-table-column>
      <!-- 操作列 -->
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" @click="showEditModal(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteUser(scope.row.userId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <el-pagination
        background
        layout="prev, pager, next, jumper"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        :pager-count="5"
        @current-change="handlePageChange"
    />

    <!-- 添加用户模态框 -->
    <div v-if="addModalVisible" class="custom-modal">
      <div class="custom-modal-content">
        <h3>添加用户</h3>
        <el-form :model="addForm" :rules="addRules" ref="addFormRef">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="addForm.username"></el-input>
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
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="editForm.newPassword" type="password"></el-input>
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmNewPassword">
            <el-input v-model="editForm.confirmNewPassword" type="password"></el-input>
          </el-form-item>
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
import { ref, onMounted, computed, watch } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

// 存储所有用户数据
const users = ref([]);
// 总数据条数
const total = ref(0);
// 每页条数
const pageSize = ref(10);
// 当前页码
const currentPage = ref(1);
// 存储搜索关键词
const searchQuery = ref('');

// 计算当前页显示的用户数据
const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredUsers.value.slice(start, end);
});

// 存储过滤后的用户数据
const filteredUsers = ref([]);

// 添加用户模态框相关
const addModalVisible = ref(false);
const addForm = ref({
  username: '',
  password: '123456',
  confirmPassword: '123456'
});
const addRules = ref({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ]
});
const addFormRef = ref(null);

// 编辑用户模态框相关
const editModalVisible = ref(false);
const editForm = ref({
  userId: null,
  username: '',
  newPassword: '',
  confirmNewPassword: ''
});
const editRules = ref({
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmNewPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== editForm.value.newPassword) {
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
    const token = localStorage.getItem('token');
    if (!token) {
      console.error('Token 不存在，请重新登录');
      return;
    }

    const response = await axios.get('/api/users', {
      headers: {
        Authorization: `Bearer ${token}`
      },
      params: {
        keyword: searchQuery.value
      }
    });

    if (response.status === 200 && Array.isArray(response.data)) {
      users.value = response.data;
      filteredUsers.value = response.data;
      total.value = response.data.length;
    } else {
      ElMessage.error('获取用户列表失败: ' + response.statusText);
    }
  } catch (error) {
    console.error('获取用户列表失败:', error);
    if (error.response) {
      ElMessage.error(`请求失败: ${error.response.status} - ${error.response.statusText}`);
    } else if (error.request) {
      ElMessage.error('请求失败: 无响应');
    } else {
      ElMessage.error('请求失败: ' + error.message);
    }
  }
};

// 处理页码变化
const handlePageChange = (page) => {
  currentPage.value = page;
};

// 监听 searchQuery 变化，变化时重新获取数据并重置页码
watch(() => searchQuery.value, (newQuery) => {
  currentPage.value = 1;
  searchUsers();
});

// 显示添加用户模态框
const showAddModal = () => {
  addModalVisible.value = true;
  addForm.value = {
    username: '',
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
          const token = localStorage.getItem('token');
          const headers = {
            Authorization: `Bearer ${token}`
          };
          const { username, password, confirmPassword } = addForm.value;
          const requestData = { username, password, confirmPassword };

          await axios.post('/api/auth/register', requestData, { headers });
          ElMessage.success('用户添加成功');
          addModalVisible.value = false;
          await getUsers();
        } catch (error) {
          console.error('添加用户失败:', error);
          if (error.response) {
            if (error.response.status === 401) {
              ElMessage.error('未授权，请重新登录');
            } else if (error.response.data && error.response.data.message) {
              ElMessage.error(error.response.data.message);
            } else {
              ElMessage.error('添加用户失败，请稍后重试');
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

// 显示编辑用户模态框
const showEditModal = (user) => {
  editModalVisible.value = true;
  editForm.value = {
    userId: user.userId,
    username: user.username,
    newPassword: '',
    confirmNewPassword: ''
  };
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
          const { userId, username, newPassword } = editForm.value;
          const requestData = { username, newPassword };

          await axios.put(`/api/users/${userId}`, requestData, { headers });
          ElMessage.success('用户信息更新成功');
          editModalVisible.value = false;
          await getUsers();
        } catch (error) {
          console.error('编辑用户失败:', error);
          if (error.response) {
            const errorMessage = error.response.data.error || '编辑用户失败，请稍后重试';
            ElMessage.error(errorMessage);
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
    const token = localStorage.getItem('token');
    if (!token) {
      console.error('Token 不存在，请重新登录');
      return;
    }

    await axios.delete(`/api/users/${userId}`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    ElMessage.success('删除成功');
    await getUsers();
  } catch (error) {
    console.error('删除用户失败:', error);
    if (error.response) {
      ElMessage.error(`删除失败: ${error.response.status} - ${error.response.statusText}`);
    } else if (error.request) {
      ElMessage.error('删除失败: 无响应');
    } else {
      ElMessage.error('删除失败: ' + error.message);
    }
  }
};

// 搜索用户
const searchUsers = () => {
  if (searchQuery.value.trim() === '') {
    filteredUsers.value = users.value;
  } else {
    filteredUsers.value = users.value.filter(user => {
      return user.username.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
          (user.role && user.role.toLowerCase().includes(searchQuery.value.toLowerCase()));
    });
  }
  total.value = filteredUsers.value.length;
  currentPage.value = 1;
};

// 组件挂载时获取用户列表
onMounted(() => {
  getUsers();
});
</script>

<style scoped>
.action-buttons {
  margin-bottom: 10px;
  display: flex;
  gap: 10px;
}

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

.custom-modal-footer {
  margin-top: 20px;
  text-align: right;
}

.el-pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>