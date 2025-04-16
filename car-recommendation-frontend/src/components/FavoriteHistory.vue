<template>
  <div>
    <h2>收藏历史</h2>
    <!-- 添加搜索框 -->
    <el-input
        v-model="searchInput"
        placeholder="请输入搜索关键词"
        style="width: 300px; margin-right: 10px;"
    ></el-input>
    <el-button @click="handleSearch">搜索</el-button>

    <el-table :data="recommendedCars" style="width: 100%">
      <el-table-column prop="brandName" label="品牌名称"></el-table-column>
      <el-table-column prop="fullName" label="汽车全名"></el-table-column>
      <el-table-column label="价格区间">
        <template #default="scope">
          {{ scope.row.priceRange }}
        </template>
      </el-table-column>
      <el-table-column prop="avgScore" label="平均评分"></el-table-column>
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
          :total="total"
      ></el-pagination>
    </div>
  </div>
</template>

<script>
import { defineComponent, onMounted, ref, watch } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

export default defineComponent({
  name: 'RecommendedCarList',
  setup() {
    const recommendedCars = ref([]);
    const total = ref(0); // 总数据条数
    const pageSize = ref(10); // 每页条数
    const currentPage = ref(1); // 当前页码
    const searchInput = ref(''); // 搜索框输入值

    const fetchRecommendedCars = async () => {
      try {
        const token = localStorage.getItem('token'); // 获取 token
        if (!token) {
          console.error('Token 不存在，请重新登录');
          return;
        }

        // 发送分页请求，将搜索关键词传递给后端
        const response = await axios.get('/api/ai/recommend', {
          headers: {
            Authorization: `Bearer ${token}`
          },
          params: {
            page: currentPage.value,
            size: pageSize.value,
            keyword: searchInput.value // 使用搜索框输入值作为关键词
          }
        });

        console.log("Response:", response); // 打印响应，确保返回正常
        if (response.status === 200 && response.data.data) {
          recommendedCars.value = response.data.data;
          total.value = response.data.total || 0;
        } else {
          ElMessage.error('获取推荐汽车列表失败: ' + response.statusText);
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

    // 分页尺寸变化处理
    const handleSizeChange = (val) => {
      pageSize.value = val;
      currentPage.value = 1; // 改变每页条数时重置到第一页
      fetchRecommendedCars(); // 修正为正确的函数名
    };

    // 页码变化处理
    const handleCurrentChange = (val) => {
      currentPage.value = val;
      fetchRecommendedCars(); // 修正为正确的函数名
    };

    // 搜索按钮点击处理
    const handleSearch = () => {
      currentPage.value = 1; // 重置页码为第一页
      fetchRecommendedCars(); // 重新获取数据
    };

    // 添加搜索框实时监听（可选：按需开启）
    watch(searchInput, (newVal) => {
      if (newVal.trim() === '') { // 空值处理
        currentPage.value = 1;
        fetchRecommendedCars();
      }
    });

    onMounted(fetchRecommendedCars);

    return {
      recommendedCars,
      total,
      pageSize,
      currentPage,
      searchInput,
      handleSearch,
      handleSizeChange,
      handleCurrentChange // 修正返回对象中的方法名
    };
  },
});
</script>

<style scoped>
/* 表格容器 */
.el-table {
  margin-top: 20px;
  width: 100%;
}

/* 分页组件样式 */
.el-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 表格按钮样式 */
.el-button {
  background-color: #1a73e8;
  color: white;
  border-radius: 4px;
}

.el-button:hover {
  background-color: #003c8f;
}

/* 分页控件样式 */
.el-pagination .el-button {
  padding: 5px 15px;
  font-size: 14px;
}

/* 搜索框和按钮样式 */
.el-input {
  margin-bottom: 10px;
}
</style>