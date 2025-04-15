<template>
  <div class="dashboard-container">
    <h1>用户行为分析仪表盘</h1>

    <!-- 筛选条件区域 -->
    <div class="filters">
      <div class="filter-group">
        <label>日期范围：</label>
        <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledDate"
            @change="handleFilterChange"
        />
      </div>

      <div class="filter-group">
        <label>地区：</label>
        <el-select
            v-model="selectedRegions"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择地区"
            @change="handleFilterChange"
        >
          <el-option
              v-for="region in availableRegions"
              :key="region"
              :label="region"
              :value="region"
          />
          <template #footer>
            <div class="select-footer">
              <el-button link @click="selectAllRegions">全选</el-button>
              <el-button link @click="clearRegions">清空</el-button>
            </div>
          </template>
        </el-select>
      </div>

      <el-button type="primary" @click="resetFilters">重置筛选</el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-container">
      <div class="stat-card">
        <h3>总用户数</h3>
        <p>{{ userStats.total }}</p>
        <div class="stat-trend">
          <span :class="trendClass(userStats.trend)">
            {{ userStats.trend >= 0 ? '↑' : '↓' }} {{ Math.abs(userStats.trend) }}%
          </span>
          <span>较上月</span>
        </div>
      </div>
      <div class="stat-card">
        <h3>平均年龄</h3>
        <p>{{ userStats.avgAge }}</p>
      </div>
      <div class="stat-card">
        <h3>地区数量</h3>
        <p>{{ userStats.regions }}</p>
      </div>
    </div>

    <!-- 图表展示 -->
    <div class="charts-row">
      <div class="chart-container">
        <v-chart
            :option="ageChartOption"
            style="height: 400px;"
            :loading="loading"
            autoresize
        />
      </div>
      <div class="chart-container">
        <v-chart
            :option="regionChartOption"
            style="height: 400px;"
            :loading="loading"
            autoresize
        />
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-container">
        <v-chart
            :option="trendChartOption"
            style="height: 400px;"
            :loading="loading"
            autoresize
        />
      </div>
      <div class="chart-container">
        <v-chart
            :option="brandChartOption"
            style="height: 400px;"
            :loading="loading"
            autoresize
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import VChart from 'vue-echarts';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import {
  BarChart,
  LineChart,
  PieChart
} from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components';

use([
  CanvasRenderer,
  BarChart,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
]);

// 数据状态
const rawUsers = ref([]);
const rawRecommendations = ref([]);
const loading = ref(true);

// 筛选状态
const dateRange = ref([]);
const selectedRegions = ref([]);
const availableRegions = ref([]);

// 禁用未来日期
const disabledDate = (time) => {
  return time.getTime() > Date.now();
};

// 获取数据
const fetchData = async () => {
  try {
    loading.value = true;
    const [usersRes, recRes] = await Promise.all([
      axios.get('/api/history/users'),
      axios.get('/api/history/recommendation-history')
    ]);

    // 处理用户数据
    rawUsers.value = (usersRes.data || []).map(user => ({
      ...user,
      age: Number(user.age) || 0,
      region: user.region || '未知地区',
      registerDate: user.registerDate?.split('T')[0] || '2023-01-01'
    }));

    // 处理推荐数据
    rawRecommendations.value = (recRes.data || []).map(rec => ({
      ...rec,
      score: Math.min(Math.max(Number(rec.score) || 0, 0), 10),
      timestamp: rec.timestamp?.split('T')[0] || '2023-01-01'
    }));

    // 初始化地区选项
    availableRegions.value = [
      ...new Set(rawUsers.value.map(u => u.region))
    ].sort();
    selectedRegions.value = [...availableRegions.value];

  } catch (error) {
    console.error('数据加载失败:', error);
  } finally {
    loading.value = false;
  }
};

// 筛选后的数据
const filteredUsers = computed(() => {
  return rawUsers.value.filter(user => {
    // 日期筛选
    const datePass = !dateRange.value?.length ||
        (user.registerDate >= dateRange.value[0] &&
            user.registerDate <= dateRange.value[1]);

    // 地区筛选
    const regionPass = selectedRegions.value.includes(user.region);

    return datePass && regionPass;
  });
});

const filteredRecommendations = computed(() => {
  return rawRecommendations.value.filter(rec => {
    return !dateRange.value?.length ||
        (rec.timestamp >= dateRange.value[0] &&
            rec.timestamp <= dateRange.value[1]);
  });
});

// 用户统计信息
const userStats = computed(() => {
  const validAges = filteredUsers.value.filter(u => !isNaN(u.age));
  const totalAge = validAges.reduce((sum, u) => sum + u.age, 0);

  return {
    total: filteredUsers.value.length,
    avgAge: validAges.length
        ? `${(totalAge / validAges.length).toFixed(1)}岁`
        : '0.0岁',
    regions: new Set(filteredUsers.value.map(u => u.region)).size,
    trend: calculateTrend()
  };
});

// 计算趋势
const calculateTrend = () => {
  if (filteredUsers.value.length === 0) return 0;

  const now = new Date();
  const lastMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1);

  const currentCount = filteredUsers.value.length;
  const lastMonthCount = rawUsers.value.filter(u => {
    const date = new Date(u.registerDate);
    return date < now && date >= lastMonth;
  }).length;

  if (lastMonthCount === 0) return 100;
  return Math.round(((currentCount - lastMonthCount) / lastMonthCount) * 100);
};

// 图表数据
const ageChartOption = computed(() => ({
  title: {
    text: '用户年龄分布',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: getAgeGroups().map(g => `${g.name}岁`),
    axisLabel: {
      rotate: 45
    }
  },
  yAxis: {
    type: 'value',
    name: '用户数量'
  },
  series: [{
    name: '用户数',
    type: 'bar',
    data: getAgeGroups().map(g => g.value),
    itemStyle: {
      color: '#5470C6'
    },
    emphasis: {
      itemStyle: {
        color: '#3a56b4'
      }
    }
  }]
}));

const regionChartOption = computed(() => ({
  title: {
    text: '用户地区分布',
    left: 'center'
  },
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} ({d}%)'
  },
  series: [{
    name: '地区分布',
    type: 'pie',
    radius: ['40%', '70%'],
    avoidLabelOverlap: false,
    itemStyle: {
      borderRadius: 10,
      borderColor: '#fff',
      borderWidth: 2
    },
    label: {
      show: false,
      position: 'center'
    },
    emphasis: {
      label: {
        show: true,
        fontSize: '18',
        fontWeight: 'bold'
      }
    },
    labelLine: {
      show: false
    },
    data: getRegionData()
  }]
}));

const trendChartOption = computed(() => ({
  title: {
    text: '推荐评分趋势',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis',
    formatter: params => {
      const data = getTrendData()[params[0].dataIndex];
      return `日期: ${data.date}<br/>平均分: ${data.score.toFixed(1)}<br/>样本数: ${data.count}`;
    }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: getTrendData().map(d => d.date),
    axisLabel: {
      rotate: 45
    }
  },
  yAxis: {
    type: 'value',
    min: 0,
    max: 10,
    axisLabel: {
      formatter: '{value} 分'
    }
  },
  series: [{
    name: '平均分',
    type: 'line',
    symbol: 'circle',
    symbolSize: 8,
    data: getTrendData().map(d => d.score),
    lineStyle: {
      width: 3
    },
    itemStyle: {
      color: '#EE6666'
    },
    emphasis: {
      itemStyle: {
        color: '#d23333'
      }
    }
  }]
}));

const brandChartOption = computed(() => ({
  title: {
    text: '品牌平均评分',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    },
    formatter: params => {
      const data = getBrandData()[params[0].dataIndex];
      return `${data.brand}<br/>平均分: ${data.score.toFixed(1)}<br/>样本数: ${data.count}`;
    }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: getBrandData().map(d => d.brand),
    axisLabel: {
      interval: 0,
      rotate: 45
    }
  },
  yAxis: {
    type: 'value',
    min: 0,
    max: 10,
    axisLabel: {
      formatter: '{value} 分'
    }
  },
  series: [{
    name: '平均分',
    type: 'bar',
    data: getBrandData().map(d => ({
      value: d.score,
      itemStyle: {
        color: d.score > 5 ? '#91CC75' : '#EE6666'
      }
    })),
    emphasis: {
      itemStyle: {
        color: params => params.value > 5 ? '#6da754' : '#d23333'
      }
    },
    label: {
      show: true,
      position: 'top',
      formatter: '{c}'
    }
  }]
}));

// 数据处理方法
const getAgeGroups = () => {
  const groups = filteredUsers.value.reduce((acc, user) => {
    const decade = Math.floor(user.age / 10) * 10;
    const key = `${decade}-${decade + 9}`;
    acc[key] = (acc[key] || 0) + 1;
    return acc;
  }, {});

  return Object.entries(groups)
      .map(([name, value]) => ({name, value}))
      .sort((a, b) => parseInt(a.name) - parseInt(b.name));
};

const getRegionData = () => {
  const regions = filteredUsers.value.reduce((acc, user) => {
    acc[user.region] = (acc[user.region] || 0) + 1;
    return acc;
  }, {});

  return Object.entries(regions)
      .map(([name, value]) => ({
        name,
        value,
        itemStyle: {
          color: `hsl(${Math.random() * 360}, 70%, 60%)`
        }
      }))
      .sort((a, b) => b.value - a.value);
};

const getTrendData = () => {
  const byDate = filteredRecommendations.value.reduce((acc, rec) => {
    if (!acc[rec.timestamp]) {
      acc[rec.timestamp] = {total: 0, count: 0};
    }
    acc[rec.timestamp].total += rec.score;
    acc[rec.timestamp].count += 1;
    return acc;
  }, {});

  return Object.entries(byDate)
      .map(([date, {total, count}]) => ({
        date,
        score: total / count,
        count
      }))
      .sort((a, b) => a.date.localeCompare(b.date));
};

const getBrandData = () => {
  const byBrand = filteredRecommendations.value.reduce((acc, rec) => {
    const brand = rec.carName?.split(' ')[0] || '其他';
    if (!acc[brand]) {
      acc[brand] = {total: 0, count: 0};
    }
    acc[brand].total += rec.score;
    acc[brand].count += 1;
    return acc;
  }, {});

  return Object.entries(byBrand)
      .map(([brand, {total, count}]) => ({
        brand,
        score: total / count,
        count
      }))
      .sort((a, b) => b.score - a.score);
};

// 筛选操作
const handleFilterChange = () => {
  // 自动触发计算属性更新
};

const selectAllRegions = () => {
  selectedRegions.value = [...availableRegions.value];
};

const clearRegions = () => {
  selectedRegions.value = [];
};

const resetFilters = () => {
  dateRange.value = [];
  selectedRegions.value = [...availableRegions.value];
};

// 趋势样式
const trendClass = (trend) => {
  return trend >= 0 ? 'positive' : 'negative';
};

onMounted(fetchData);
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

h1 {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}

/* 筛选区域样式 */
.filters {
  margin: 20px 0;
  padding: 15px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-group label {
  font-weight: 500;
  color: #606266;
  white-space: nowrap;
}

.select-footer {
  padding: 8px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: space-between;
}

/* 统计卡片样式 */
.stats-container {
  display: flex;
  justify-content: space-between;
  margin: 20px 0;
  gap: 20px;
}

.stat-card {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-card h3 {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 16px;
}

.stat-card p {
  font-size: 28px;
  font-weight: bold;
  margin: 0;
  color: #333;
}

.stat-trend {
  margin-top: 10px;
  font-size: 14px;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.stat-trend .positive {
  color: #67c23a;
}

.stat-trend .negative {
  color: #f56c6c;
}

/* 图表区域样式 */
.charts-row {
  display: flex;
  margin-bottom: 20px;
  gap: 20px;
}

.chart-container {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.chart-container:hover {
  transform: translateY(-3px);
}

/* 响应式布局 */
@media (max-width: 992px) {
  .stats-container {
    flex-wrap: wrap;
  }

  .stat-card {
    min-width: calc(50% - 10px);
  }
}

@media (max-width: 768px) {
  .filters {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-group {
    width: 100%;
  }

  .stats-container {
    flex-direction: column;
  }

  .stat-card {
    width: 100%;
  }

  .charts-row {
    flex-direction: column;
  }
}
</style>