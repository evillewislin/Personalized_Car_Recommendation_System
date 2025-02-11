<template>
  <div>
    <h2>车型列表</h2>
    <el-table :data="cars" style="width: 100%">
      <el-table-column prop="brand" label="品牌"></el-table-column>
      <el-table-column prop="price" label="价格"></el-table-column>
      <el-table-column prop="fuelType" label="燃油类型"></el-table-column>
    </el-table>
  </div>
</template>

<script>
import { defineComponent, onMounted, ref } from 'vue';
import axios from 'axios';

export default defineComponent({
  name: 'CarList',
  setup() {
    const cars = ref([]);

    const fetchCars = async () => {
      try {
        const response = await axios.get('/api/cars');
        cars.value = response.data;
      } catch (error) {
        console.error(error);
      }
    };

    onMounted(fetchCars);
    return { cars };
  }
});
</script>
