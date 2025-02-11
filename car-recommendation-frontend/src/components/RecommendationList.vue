<template>
  <div>
    <h2>推荐车型</h2>
    <el-table :data="recommendations" style="width: 100%">
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
  name: 'RecommendationList',
  setup() {
    const recommendations = ref([]);
    // 此处示例中直接使用 userId=1，请根据实际情况调整
    const fetchRecommendations = async () => {
      try {
        const response = await axios.get('/api/recommendations/1');
        recommendations.value = response.data;
      } catch (error) {
        console.error(error);
      }
    };

    onMounted(fetchRecommendations);
    return { recommendations };
  }
});
</script>
