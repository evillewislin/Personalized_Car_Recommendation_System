<template>
  <div>
    <h2>车型列表</h2>
    <!-- 搜索及筛选区域 -->
    <div class="filter-container">
      <el-input v-model="searchInput" placeholder="请输入搜索关键词" style="width: 200px; margin-right: 10px;"></el-input>

      <!-- 价格范围筛选 -->
      <div style="display: inline-flex; align-items: center; margin-right: 20px;">
        <span>价格范围：</span>
        <el-input
            v-model.number="priceRange[0]"
            type="number"
            :min="0"
            :max="priceRange[1]"
            style="width: 80px; margin: 0 5px;"
            placeholder="最低"
        ></el-input>
        <el-slider
            v-model="priceRange"
            :min="0"
            :max="maxPrice || 100000"
            :step="1000"
            show-input
            range
            style="width: 200px; margin: 0 5px;"
        ></el-slider>
        <el-input
            v-model.number="priceRange[1]"
            type="number"
            :min="priceRange[0]"
            :max="maxPrice || 100000"
            style="width: 80px;"
            placeholder="最高"
        ></el-input>
      </div>

      <!-- 排序方式 -->
      <el-select v-model="sortBy" placeholder="选择排序方式" style="width: 180px; margin-right: 10px;">
        <el-option label="价格升序" value="price_asc"></el-option>
        <el-option label="价格降序" value="price_desc"></el-option>
        <el-option label="评分升序" value="score_asc"></el-option>
        <el-option label="评分降序" value="score_desc"></el-option>
      </el-select>

      <el-button class="el-button" @click="handleSearch">搜索</el-button>
    </div>

    <el-table :data="cars" style="width: 100%" :empty-text="getEmptyText()">
      <el-table-column prop="name" label="品牌"></el-table-column>
      <el-table-column prop="fullName" label="全名"></el-table-column>
      <el-table-column label="价格区间">
        <template #default="scope">
          {{ scope.row.minPrice }} - {{ scope.row.maxPrice }}
        </template>
      </el-table-column>
      <el-table-column label="收藏">
        <template #default="scope">
          <el-button
              @click="handleCollect(scope.row.carId, scope.row.name, scope.row.score)"
              :disabled="isCollecting"
          >
            {{ isCollecting? '正在收藏...' : '收藏' }}
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="评分">
        <template #default="scope">
          <el-input v-model="scope.row.score" type="number" min="1" max="10" placeholder="1-10分"></el-input>
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
          :total="total"
      >
      </el-pagination>
    </div>
  </div>
</template>

<script>
import { defineComponent, onMounted, ref, watch } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

export default defineComponent({
  name: 'CarList',
  setup() {
    const cars = ref([]);
    const total = ref(0); // 总数据条数
    const pageSize = ref(10); // 每页条数
    const currentPage = ref(1); // 当前页码
    const searchInput = ref(''); // 搜索框输入值
    const token = ref(localStorage.getItem('token'));
    const isCollecting = ref(false); // 用于控制收藏按钮的禁用状态

    // 新增筛选排序参数
    const priceRange = ref([0, 100000]); // 初始价格范围
    const sortBy = ref(''); // 排序方式
    const maxPrice = ref(0); // 用于记录最高价格（可根据后端返回动态调整）

    const fetchCars = async () => {
      token.value = localStorage.getItem('token');
      if (!token.value) {
        cars.value = [];
        return;
      }

      try {
        // 发送分页请求，将搜索关键词传递给后端
        const response = await axios.get('/api/cars/search', {
          headers: {
            Authorization: `Bearer ${token.value}`,
          },
          params: {
            page: currentPage.value,
            pageSize: pageSize.value,
            keyword: searchInput.value, // 使用搜索框输入值作为关键词
            minPrice: priceRange.value[0],
            maxPrice: priceRange.value[1],
            sort: sortBy.value
          },
        });

        console.log("Response:", response); // 打印响应，确保返回正常
        if (response.status === 200 && Array.isArray(response.data.data)) {
          // 计算当前页的平均最低价格和平均最高价格
          const minPrices = response.data.data.map(car => car.minPrice).filter(price => price!== null && price!== undefined);
          const maxPrices = response.data.data.map(car => car.maxPrice).filter(price => price!== null && price!== undefined);

          const avgMinPrice = minPrices.length > 0? minPrices.reduce((sum, price) => sum + price, 0) / minPrices.length : 0;
          const avgMaxPrice = maxPrices.length > 0? maxPrices.reduce((sum, price) => sum + price, 0) / maxPrices.length : 0;

          cars.value = response.data.data.map((car) => ({
            ...car,
            minPrice: car.minPrice || avgMinPrice,
            maxPrice: car.maxPrice || avgMaxPrice,
            score: null // 初始化评分字段
          }));
          total.value = response.data.total; // 确保使用正确的总条数字段
          maxPrice.value = response.data.maxPrice || 100000;
        } else {
          ElMessage.error('获取车型列表失败:' + response.statusText);
        }
      } catch (error) {
        console.error('请求失败:', error);
        if (error.response) {
          ElMessage.error(`请求失败: ${error.response.status} - ${error.response.statusText}`);
        } else if (error.request) {
          ElMessage.error('请求失败: 无响应');
        } else {
          ElMessage.error('请求失败: '+ error.message);
        }
      }
    };

    // 处理搜索按钮点击事件
    const handleSearch = () => {
      currentPage.value = 1; // 重置页码为第一页
      fetchCars(); // 重新获取数据
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

    const handleCollect = async (carId, name, score) => {
      if (!token.value) {
        ElMessage.warning('请先登录');
        return;
      }

      if (!score || isNaN(score) || score < 1 || score > 10) {
        ElMessage.warning('请输入 1 - 10 分的评分');
        return;
      }

      // 点击后禁用按钮
      isCollecting.value = true;

      try {
        const response = await axios.post('/api/collect', {
          carId: carId,
          name: name,
          timestamp: new Date().toISOString(),
          score: parseInt(score)
        }, {
          headers: {
            Authorization: `Bearer ${token.value}`
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
      } finally {
        // 操作完成后启用按钮
        isCollecting.value = false;
      }
    };

    const getEmptyText = () => {
      return token.value? '暂无数据' : '请先登录';
    };

    onMounted(fetchCars);
    return {
      cars,
      total,
      pageSize,
      currentPage,
      handleCollect,
      searchInput,
      handleSearch,
      handleSizeChange,
      handleCurrentChange,
      getEmptyText,
      isCollecting,
      priceRange,
      sortBy,
      maxPrice
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

/* 分页样式 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
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

.el-button {
  background-color: rgba(64, 158, 255, 1);
}

.filter-container {
  display: flex;
  align-items: center;
  margin: 10px 0 20px;
}

/* 滑动条输入框样式优化 */
.el-input__inner {
  width: 80px;
  text-align: right;
}

/* 排序下拉菜单样式 */
.el-select {
  margin-right: 10px;
}
</style>