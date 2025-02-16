<template>
  <div>
    <h2>车型列表</h2>
    <el-table :data="cars" style="width: 100%">
      <el-table-column prop="name" label="品牌"></el-table-column>
      <el-table-column prop="fullName" label="全名"></el-table-column>
      <el-table-column label="价格区间">
        <template #default="scope">
          {{ scope.row.minPrice }} - {{ scope.row.maxPrice }}
        </template>
      </el-table-column>
      <el-table-column label="照片">
        <template #default="scope">
          <img :src="scope.row.img" alt="car image" style="width: 100px; height: auto;" @error="handleImageError">
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
import { defineComponent, onMounted, ref } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

export default defineComponent({
  name: 'CarList',
  setup() {
    const cars = ref([]);
    const total = ref(0); // 总数据条数
    const pageSize = ref(10); // 每页条数
    const currentPage = ref(1); // 当前页码

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
          },
        });

        console.log("Response:", response); // 打印响应，确保返回正常
        if (response.status === 200 && Array.isArray(response.data.data)) {
          cars.value = response.data.data.map((car) => ({
            ...car,
            minPrice: car.minPrice || 'N/A',
            maxPrice: car.maxPrice || 'N/A',
            img: car.img || 'default-car-image.jpg',
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

    onMounted(fetchCars);
    return { cars, handleImageError, total, pageSize, currentPage, handlePageChange };
  },
});
</script>
<style scoped>
.el-pagination {
margin-top: 20px;
justify-content: center;
}
</style>