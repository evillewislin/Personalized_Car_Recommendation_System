<template>
  <div>
    <h2>汽车管理</h2>
    <!-- 操作按钮和搜索框 -->
    <div class="action-buttons">
      <el-button type="primary" @click="showAddModal">添加汽车</el-button>
      <el-input v-model="searchQuery" placeholder="请输入搜索关键词"></el-input>
      <el-button @click="searchCars">搜索</el-button>
    </div>
    <!-- 汽车列表表格 -->
    <el-table :data="paginatedCars" style="width: 100%">
      <el-table-column prop="name" label="品牌"></el-table-column>
      <el-table-column prop="fullName" label="全名"></el-table-column>
      <el-table-column label="价格区间">
        <template #default="scope">
          {{ scope.row.minPrice }} - {{ scope.row.maxPrice }}
        </template>
      </el-table-column>
      <!-- 操作列 -->
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" @click="showEditModal(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteCar(scope.row.carId)">删除</el-button>
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
          :total="total">
      </el-pagination>
    </div>

    <!-- 添加汽车模态框 -->
    <div v-if="addModalVisible" class="custom-modal">
      <div class="custom-modal-content">
        <h3>添加汽车</h3>
        <el-form :model="addForm" :rules="addRules" ref="addFormRef">
          <el-form-item label="品牌" prop="name">
            <el-input v-model="addForm.name"></el-input>
          </el-form-item>
          <el-form-item label="全名" prop="fullName">
            <el-input v-model="addForm.fullName"></el-input>
          </el-form-item>
          <el-form-item label="最低价格" prop="minPrice">
            <el-input v-model.number="addForm.minPrice" type="number"></el-input>
          </el-form-item>
          <el-form-item label="最高价格" prop="maxPrice">
            <el-input v-model.number="addForm.maxPrice" type="number"></el-input>
          </el-form-item>
        </el-form>
        <div class="custom-modal-footer">
          <el-button @click="addModalVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAddForm">确定</el-button>
        </div>
      </div>
    </div>

    <!-- 编辑汽车模态框 -->
    <div v-if="editModalVisible" class="custom-modal">
      <div class="custom-modal-content">
        <h3>编辑汽车</h3>
        <el-form :model="editForm" :rules="editRules" ref="editFormRef">
          <el-form-item label="品牌" prop="name">
            <el-input v-model="editForm.name" disabled></el-input>
          </el-form-item>
          <el-form-item label="全名" prop="fullName">
            <el-input v-model="editForm.fullName"></el-input>
          </el-form-item>
          <el-form-item label="最低价格" prop="minPrice">
            <el-input v-model.number="editForm.minPrice" type="number"></el-input>
          </el-form-item>
          <el-form-item label="最高价格" prop="maxPrice">
            <el-input v-model.number="editForm.maxPrice" type="number"></el-input>
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
import { ref, onMounted, watch, computed } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

// 存储所有汽车数据
const cars = ref([]);
// 总数据条数
const total = ref(0);
// 每页条数
const pageSize = ref(10);
// 当前页码
const currentPage = ref(1);
// 存储搜索关键词
const searchQuery = ref('');

// 计算当前页显示的汽车数据
const paginatedCars = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return cars.value.slice(start, end);
});

// 添加汽车模态框相关
const addModalVisible = ref(false);
const addForm = ref({
  name: '',
  fullName: '',
  minPrice: null,
  maxPrice: null
});
const addRules = ref({
  name: [
    { required: true, message: '请输入品牌', trigger: 'blur' }
  ],
  fullName: [
    { required: true, message: '请输入全名', trigger: 'blur' }
  ],
  minPrice: [
    { required: true, message: '请输入最低价格', trigger: 'blur' },
    { type: 'number', message: '最低价格必须为数字', trigger: 'blur' }
  ],
  maxPrice: [
    { required: true, message: '请输入最高价格', trigger: 'blur' },
    { type: 'number', message: '最高价格必须为数字', trigger: 'blur' }
  ]
});
const addFormRef = ref(null);

// 编辑汽车模态框相关
const editModalVisible = ref(false);
const editForm = ref({
  carId: null,
  name: '',
  fullName: '',
  minPrice: null,
  maxPrice: null
});
const editRules = ref({
  name: [
    { required: true, message: '请输入品牌', trigger: 'blur' }
  ],
  fullName: [
    { required: true, message: '请输入全名', trigger: 'blur' }
  ],
  minPrice: [
    { required: true, message: '请输入最低价格', trigger: 'blur' },
    { type: 'number', message: '最低价格必须为数字', trigger: 'blur' }
  ],
  maxPrice: [
    { required: true, message: '请输入最高价格', trigger: 'blur' },
    { type: 'number', message: '最高价格必须为数字', trigger: 'blur' }
  ]
});
const editFormRef = ref(null);

const fetchCars = async () => {
  try {
    const token = localStorage.getItem('token'); // 获取 token
    if (!token) {
      console.error('Token 不存在，请重新登录');
      return;
    }

    // 发送分页请求
    const response = await axios.get('/api/cars/search', {
      headers: {
        Authorization: `Bearer ${token}`,
      },
      params: {
        page: currentPage.value,
        pageSize: pageSize.value,
        keyword: searchQuery.value
      },
    });

    console.log("Response data:", response.data.data); // 打印响应，确保返回正常
    if (response.status === 200 && Array.isArray(response.data.data)) {
      // 计算当前页的平均最低价格和平均最高价格
      const minPrices = response.data.data.map(car => car.minPrice).filter(price => price !== null && price !== undefined);
      const maxPrices = response.data.data.map(car => car.maxPrice).filter(price => price !== null && price !== undefined);

      const avgMinPrice = minPrices.length > 0 ? minPrices.reduce((sum, price) => sum + price, 0) / minPrices.length : 0;
      const avgMaxPrice = maxPrices.length > 0 ? maxPrices.reduce((sum, price) => sum + price, 0) / maxPrices.length : 0;

      cars.value = response.data.data.map((car) => ({
        ...car,
        minPrice: car.minPrice || avgMinPrice,
        maxPrice: car.maxPrice || avgMaxPrice,
      }));
      total.value = response.data.total; // 确保使用正确的总条数字段
    } else {
      ElMessage.error('获取车型列表失败: ' + response.statusText);
    }
  } catch (error) {
    console.error('请求失败:', error);
    if (error.response) {
      ElMessage.error(`请求失败: ${error.response.status} - ${error.response.statusText}`);
    } else if (error.request) {
      ElMessage.error('请求失败: 无响应');
    } else {
      ElMessage.error('请求失败: ' + error.message);
    }
  }
};

// 分页相关方法
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1; // 改变每页条数时重置到第一页
  fetchCars();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchCars();
};

// 监听 searchQuery 变化，变化时重新获取数据并重置页码
watch(() => searchQuery.value, (newQuery) => {
  currentPage.value = 1; // 重置页码为第一页
  fetchCars();
});

// 显示添加汽车模态框
const showAddModal = () => {
  addModalVisible.value = true;
  addForm.value = {
    name: '',
    fullName: '',
    minPrice: null,
    maxPrice: null
  };
};

// 提交添加汽车表单
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
          const { name, fullName, minPrice, maxPrice } = addForm.value;
          // 构建符合后端要求的数据结构
          const requestData = {
            carInfo: {
              fullName,
              minPrice,
              maxPrice
            },
            carBrand: {
              name
            }
          };
          await axios.post('/api/cars/add', requestData, { headers });
          ElMessage.success('汽车添加成功');
          addModalVisible.value = false;
          await fetchCars();
        } catch (error) {
          console.error('添加汽车失败:', error);
          if (error.response) {
            if (error.response.status === 401) {
              ElMessage.error('未授权，请重新登录');
            } else if (error.response.data && error.response.data.message) {
              ElMessage.error(error.response.data.message);
            } else {
              ElMessage.error('添加汽车失败，请稍后重试');
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

// 显示编辑汽车模态框
const showEditModal = (car) => {
  editModalVisible.value = true;
  editForm.value = {
    carId: car.carId,
    name: car.name,
    fullName: car.fullName,
    minPrice: car.minPrice,
    maxPrice: car.maxPrice
  };
};

// 提交编辑汽车表单
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
          const { carId, name, fullName, minPrice, maxPrice } = editForm.value;
          // 构建符合后端要求的数据结构
          const requestData = {
            carInfo: {
              fullName,
              minPrice,
              maxPrice
            },
            carBrand: {
              name
            }
          };
          await axios.put(`/api/cars/${carId}`, requestData, { headers });
          ElMessage.success('汽车信息更新成功');
          editModalVisible.value = false;
          await fetchCars();
        } catch (error) {
          console.error('编辑汽车失败:', error);
          if (error.response) {
            const errorMessage = error.response.data.error || '编辑汽车失败，请稍后重试';
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

// 删除汽车
const deleteCar = async (carId) => {
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      console.error('Token 不存在，请重新登录');
      return;
    }
    await axios.delete(`/api/cars/${carId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    // 删除成功后重新获取汽车列表
    fetchCars();
    ElMessage.success('删除成功');
  } catch (error) {
    console.error('删除汽车失败:', error);
    if (error.response) {
      ElMessage.error(`删除失败: ${error.response.status} - ${error.response.statusText}`);
    } else if (error.request) {
      ElMessage.error('删除失败: 无响应');
    } else {
      ElMessage.error('删除失败: ' + error.message);
    }
  }
};

// 搜索汽车
const searchCars = () => {
  currentPage.value = 1; // 重置页码为第一页
  fetchCars();
};

// 组件挂载时获取汽车列表
onMounted(() => {
  fetchCars();
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>