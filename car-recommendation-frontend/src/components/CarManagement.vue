<template>
  <div>
    <h2>汽车管理</h2>
    <!-- 操作按钮和搜索框 -->
    <div class="action-buttons">
      <el-button type="primary" @click="addCar">添加汽车</el-button>
      <el-input v-model="searchQuery" placeholder="请输入搜索关键词"></el-input>
    </div>
    <!-- 汽车列表表格 -->
    <el-table :data="cars" style="width: 100%">
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
          <el-button size="small" @click="editCar(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteCar(scope.row.id)">删除</el-button>
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
  </div>
</template>

<script>
import {defineComponent, onMounted, ref, watch} from 'vue';
import axios from 'axios';
import {ElMessage} from 'element-plus';

export default defineComponent({
  name: 'CarManagement',
  setup() {
    const cars = ref([]);
    const total = ref(0); // 总数据条数
    const pageSize = ref(10); // 每页条数
    const currentPage = ref(1); // 当前页码
    const searchQuery = ref('');

    const handleImageError = (event) => {
      event.target.src = 'default-car-image.jpg';
    };

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

        console.log("Response:", response); // 打印响应，确保返回正常
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

    // 处理页码变化
    const handlePageChange = (page) => {
      currentPage.value = page;
      fetchCars(); // 重新获取数据
    };

    // 监听 searchQuery 变化，变化时重新获取数据并重置页码
    watch(() => searchQuery.value, (newQuery) => {
      currentPage.value = 1; // 重置页码为第一页
      fetchCars();
    });

    // 添加汽车
    const addCar = () => {
      // 这里可以实现添加汽车的逻辑，例如弹出模态框输入汽车信息
      console.log('添加汽车');
    };

    // 编辑汽车
    const editCar = (car) => {
      // 这里可以实现编辑汽车的逻辑，例如弹出模态框显示汽车信息并允许修改
      console.log('编辑汽车:', car);
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

    onMounted(fetchCars);
    return {
      cars,
      handleImageError,
      total,
      pageSize,
      currentPage,
      handlePageChange,
      searchQuery,
      addCar,
      editCar,
      deleteCar,
      searchCars
    };
  },
});
</script>

<style scoped>
.el-pagination {
  margin-top: 20px;
  justify-content: center;
}

.action-buttons {
  margin-bottom: 10px;
  display: flex;
  gap: 10px;
}
</style>