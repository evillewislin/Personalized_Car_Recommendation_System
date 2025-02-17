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
      <el-table-column label="收藏">
        <template #default="scope">
          <el-button @click="handleCollect(scope.row.id, scope.row.score)">收藏</el-button>
        </template>
      </el-table-column>
      <el-table-column label="评分">
        <template #default="scope">
          <el-input v-model="scope.row.score" type="number" min="1" max="5" placeholder="1-5分"></el-input>
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
import { defineComponent, onMounted, ref, watch } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/store';

export default defineComponent({
  name: 'CarList',
  props: {
    searchQuery: {
      type: String,
      default: ''
    }
  },
  setup(props) {
    const userStore = useUserStore();
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
            keyword: props.searchQuery
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
            score: '' // 初始化评分字段
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
    watch(() => props.searchQuery, (newQuery) => {
      currentPage.value = 1; // 重置页码为第一页
      fetchCars();
    });

    const handleCollect = async (carId, score) => {
      const token = localStorage.getItem('token');
      if (!token) {
        ElMessage.warning('请先登录');
        return;
      }

      if (!score || isNaN(score) || score < 1 || score > 5) {
        ElMessage.warning('请输入 1 - 5 分的评分');
        return;
      }

      try {
        const response = await axios.post('/api/collect', {
          car_id: carId,
          timestamp: new Date().toISOString(),
          score: parseInt(score)
        }, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });

        if (response.status === 200) {
          ElMessage.success('收藏成功');
        } else {
          ElMessage.error('收藏失败');
        }
      } catch (error) {
        console.error('收藏出错:', error);
        ElMessage.error('收藏出错，请稍后重试');
      }
    };

    onMounted(fetchCars);
    return {
      cars,
      handleImageError,
      total,
      pageSize,
      currentPage,
      handlePageChange,
      handleCollect
    };
  },
});
</script>

<style scoped>
.el-pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>