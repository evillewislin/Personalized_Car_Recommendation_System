<template>
  <div>
    <h2>用户管理</h2>
    <!-- 操作按钮和搜索框 -->
    <div class="action-buttons">
      <el-button type="primary" @click="addUser">添加用户</el-button>
      <el-input v-model="searchQuery" placeholder="请输入搜索关键词"></el-input>
      <el-button @click="searchUsers">搜索</el-button>
    </div>
    <!-- 用户列表表格 -->
    <el-table :data="filteredUsers">
      <el-table-column prop="id" label="用户 ID"></el-table-column>
      <el-table-column prop="username" label="用户名"></el-table-column>
      <el-table-column prop="role" label="身份"></el-table-column>

      <!-- 操作列 -->
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" @click="editUser(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteUser(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加用户模态框 -->
    <el-dialog :visible.sync="addDialogVisible" title="添加用户">
      <el-form :model="addUserForm" :rules="addUserRules" ref="addUserFormRef">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="addUserForm.username"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="addUserForm.password" type="password"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="addUserForm.confirmPassword" type="password"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddUser">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑用户模态框 -->
    <el-dialog ref="editDialog" :visible.sync="editDialogVisible" title="编辑用户">
      <el-form :model="editUserForm" :rules="editUserRules" ref="editUserFormRef">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editUserForm.username"></el-input>
        </el-form-item>
        <el-form-item label="身份" prop="role">
          <!-- 建议改为下拉选择 -->
          <el-select v-model="editUserForm.role" placeholder="请选择身份">
            <el-option label="用户" value="user"></el-option>
            <el-option label="管理员" value="admin"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEditUser">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

export default {
  name: 'UserManagement',
  setup() {
    // 存储所有用户数据
    const users = ref([]);
    // 存储搜索关键词
    const searchQuery = ref('');
    // 存储过滤后的用户数据
    const filteredUsers = ref([]);

    // 添加用户模态框相关
    const addDialogVisible = ref(false);
    const addUserForm = ref({
      username: '',
      password: '',
      confirmPassword: ''
    });
    const addUserRules = ref({
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请输入确认密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== addUserForm.value.password) {
              callback(new Error('两次输入的密码不一致'));
            } else {
              callback();
            }
          },
          trigger: 'blur'
        }
      ]
    });
    const addUserFormRef = ref(null);

    // 编辑用户模态框相关
    const editDialogVisible = ref(false);
    const editUserForm = ref({
      id: null,
      username: '',
      role: ''
    });
    const validRoles = ['user', 'admin'];
    const editUserRules = ref({
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      role: [
        { required: true, message: '请输入身份', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (!validRoles.includes(value)) {
              callback(new Error('请输入有效的身份，只能是 user 或 admin'));
            } else {
              callback();
            }
          },
          trigger: 'blur'
        }
      ]
    });
    const editUserFormRef = ref(null);

    // 获取用户列表
    const getUsers = async () => {
      try {
        const response = await axios.get('/api/users');
        users.value = response.data;
        filteredUsers.value = response.data;
        searchUsers();
      } catch (error) {
        console.error('获取用户列表失败:', error);
        if (error.response) {
          ElMessage.error(`获取用户列表失败: ${error.response.status} - ${error.response.statusText}`);
        } else {
          ElMessage.error('获取用户列表失败: 网络错误');
        }
      }
    };

    // 添加用户
    const addUser = async () => {
      console.log('准备打开模态框');
      addDialogVisible.value = true;
      console.log('addDialogVisible 设置为 true:', addDialogVisible.value);
      addUserForm.value = {
        username: '',
        password: '',
        confirmPassword: ''
      };
      await nextTick(() => {
        console.log('nextTick 已经执行');
        const dialogElement = document.querySelector('.el-dialog');
        if (dialogElement) {
          console.log('模态框元素存在');
        } else {
          console.log('模态框元素不存在');
        }
      });
    };

    // 提交添加用户表单
    const submitAddUser = async () => {
      const form = addUserFormRef.value;
      if (form) {
        form.validate(async (valid) => {
          if (valid) {
            try {
              const { username, password } = addUserForm.value;
              const response = await axios.post('/api/auth/register', {
                username,
                password
              });
              if (response.status === 200) {
                ElMessage.success('用户添加成功');
                addDialogVisible.value = false;
                getUsers();
              } else {
                ElMessage.error(`用户添加失败: ${response.status} - ${response.statusText}`);
              }
              form.resetFields();
            } catch (error) {
              console.error('添加用户失败:', error);
              if (error.response) {
                ElMessage.error(`用户添加失败: ${error.response.status} - ${error.response.statusText}`);
              } else {
                ElMessage.error('用户添加失败: 网络错误');
              }
            }
          }
        });
      }
    };

    // 编辑用户
    const editUser = (user) => {
      console.log('editUser method called');
      editDialogVisible.value = true;
      editUserForm.value = {
        id: user.id,
        username: user.username,
        role: user.role
      };
    };

    // 提交编辑用户表单
    const submitEditUser = async () => {
      const form = editUserFormRef.value;
      if (form) {
        form.validate(async (valid) => {
          if (valid) {
            try {
              const response = await axios.put(`/api/users/${editUserForm.value.id}`, editUserForm.value);
              if (response.status === 200) {
                ElMessage.success('用户信息更新成功');
                editDialogVisible.value = false;
                getUsers();
              } else {
                ElMessage.error(`用户信息更新失败: ${response.status} - ${response.statusText}`);
              }
            } catch (error) {
              console.error('编辑用户失败:', error);
              if (error.response) {
                ElMessage.error(`编辑用户失败: ${error.response.status} - ${error.response.statusText}`);
              } else {
                ElMessage.error('编辑用户失败: 网络错误');
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
        // 删除成功后重新获取用户列表
        getUsers();
        ElMessage.success('用户删除成功');
      } catch (error) {
        console.error('删除用户失败:', error);
        if (error.response) {
          ElMessage.error(`删除用户失败: ${error.response.status} - ${error.response.statusText}`);
        } else {
          ElMessage.error('删除用户失败: 网络错误');
        }
      }
    };

    // 搜索用户
    const searchUsers = () => {
      filteredUsers.value = users.value.filter(user => {
        return user.username.includes(searchQuery.value) || user.role.includes(searchQuery.value);
      });
    };

    // 组件挂载时获取用户列表
    onMounted(() => {
      getUsers();
    });

    return {
      users,
      searchQuery,
      filteredUsers,
      addUser,
      editUser,
      deleteUser,
      searchUsers,
      addDialogVisible,
      addUserForm,
      addUserRules,
      addUserFormRef,
      editDialogVisible,
      editUserForm,
      editUserRules,
      editUserFormRef,
      submitAddUser,
      submitEditUser
    };
  }
};
</script>

<style scoped>
.action-buttons {
  margin-bottom: 10px;
  display: flex;
  gap: 10px;
}

.el-dialog {
  display: block !important;
  opacity: 1 !important;
  z-index: 9999 !important;
}

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}
</style>