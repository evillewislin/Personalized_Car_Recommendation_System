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

    const handleImageError = (event) => {
      event.target.src = 'default-car-image.jpg';
    };

    const fetchCars = async () => {
      try {
        const token = localStorage.getItem('token');  // 获取 token
        console.log("carlist", token);  // 打印 token，确认获取成功
        if (!token) {
          console.error('Token 不存在，请重新登录');
          return;
        }
        const response = await axios.get('/api/cars/search', {
          headers: {
            Authorization: `Bearer ${token}`  // 发送请求时包含 token
          }
        });
        console.log("Response:", response);  // 打印响应，确保返回正常
        if (response.status === 200 && Array.isArray(response.data)) {
          cars.value = response.data.map(car => ({
            ...car,
            minPrice: car.minPrice || 'N/A',
            maxPrice: car.maxPrice || 'N/A',
            img: car.img || 'default-car-image.jpg'
          }));
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



    onMounted(fetchCars);
    return { cars, handleImageError };
  }
});
</script>