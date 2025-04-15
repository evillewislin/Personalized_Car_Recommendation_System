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
        <label>地区筛选：</label>
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
        </el-select>
      </div>

      <el-button type="primary" @click="resetFilters">重置筛选</el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-container">
      <div class="stat-card">
        <h3>总用户数</h3>
        <p>{{ userStats.total }}</p>
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
            @rendered="handleChartRendered('ageChart')"
        />
      </div>
      <div class="chart-container">
        <v-chart
            :option="regionChartOption"
            style="height: 400px;"
            @rendered="handleChartRendered('regionChart')"
        />
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-container">
        <v-chart
            :option="trendChartOption"
            style="height: 400px;"
            @rendered="handleChartRendered('trendChart')"
        />
      </div>
      <div class="chart-container">
        <v-chart
            :option="brandChartOption"
            style="height: 400px;"
            @rendered="handleChartRendered('brandChart')"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import axios from 'axios';
import VChart from 'vue-echarts';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { debounce } from 'lodash-es';
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

// 筛选状态
const dateRange = ref([]);
const selectedRegions = ref([]);
const availableRegions = ref([]);

// 图表实例引用
const chartInstances = ref({
  ageChart: null,
  regionChart: null,
  trendChart: null,
  brandChart: null
});

// 设置默认最近一周
const setDefaultDateRange = () => {
  const end = new Date();
  const start = new Date();
  start.setDate(end.getDate() - 7);

  dateRange.value = [
    start.toISOString().split('T')[0],
    end.toISOString().split('T')[0]
  ];
};

// 获取TOP10地区
const getTopRegions = () => {
  const regionCounts = rawUsers.value.reduce((acc, user) => {
    acc[user.region] = (acc[user.region] || 0) + 1;
    return acc;
  }, {});

  return Object.entries(regionCounts)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 10)
      .map(item => item[0]);
};

// 获取数据
const fetchData = async () => {
  try {
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

    // 初始化地区选项和默认选中TOP10
    availableRegions.value = [
      ...new Set(rawUsers.value.map(u => u.region))
    ].sort();
    selectedRegions.value = getTopRegions();

    // 设置默认日期范围
    setDefaultDateRange();

  } catch (error) {
    console.error('数据加载失败:', error);
  }
};

// 图表渲染回调
const handleChartRendered = (chartName) => (instance) => {
  chartInstances.value[chartName] = instance;
};

// 防抖的resize处理
const handleResize = debounce(() => {
  // 延迟图表重绘
  setTimeout(() => {
    Object.values(chartInstances.value).forEach(instance => {
      if (instance) {
        try {
          instance.resize();
        } catch (e) {
          console.warn('图表resize错误:', e);
        }
      }
    });
  }, 200);
}, 200);

// 地区筛选后的用户数据
const regionFilteredUsers = computed(() => {
  return rawUsers.value.filter(user =>
      selectedRegions.value.includes(user.region)
  );
});

// 日期筛选后的推荐数据
const dateFilteredRecommendations = computed(() => {
  if (!dateRange.value?.length) return rawRecommendations.value;

  return rawRecommendations.value.filter(rec =>
      rec.timestamp >= dateRange.value[0] &&
      rec.timestamp <= dateRange.value[1]
  );
});

// 用户统计信息（基于全部用户数据）
const userStats = computed(() => {
  const validAges = rawUsers.value.filter(u => !isNaN(u.age));
  const totalAge = validAges.reduce((sum, u) => sum + u.age, 0);

  return {
    total: rawUsers.value.length,
    avgAge: validAges.length
        ? `${(totalAge / validAges.length).toFixed(1)}岁`
        : '0.0岁',
    regions: new Set(rawUsers.value.map(u => u.region)).size
  };
});

// 图表数据 - 上面两个图表使用地区筛选
const ageChartOption = computed(() => ({
  title: { text: '用户年龄分布' },
  tooltip: {},
  xAxis: {
    type: 'category',
    data: getAgeGroups(regionFilteredUsers.value).map(g => `${g.name}岁`)
  },
  yAxis: { type: 'value' },
  series: [{
    data: getAgeGroups(regionFilteredUsers.value).map(g => g.value),
    type: 'bar',
    itemStyle: { color: '#5470C6' }
  }]
}));

const regionChartOption = computed(() => ({
  title: { text: '用户地区分布' },
  tooltip: { formatter: '{b}: {c} ({d}%)' },
  series: [{
    type: 'pie',
    radius: '60%',
    data: getRegionData(regionFilteredUsers.value)
  }]
}));

// 图表数据 - 下面两个图表使用日期筛选
const trendChartOption = computed(() => ({
  title: { text: '推荐评分趋势' },
  tooltip: {
    formatter: params => {
      const data = getTrendData()[params.dataIndex];
      return `日期: ${data.date}<br/>评分: ${data.score.toFixed(1)}`;
    }
  },
  xAxis: {
    type: 'category',
    data: getTrendData().map(d => d.date)
  },
  yAxis: {
    type: 'value',
    min: 0,
    max: 10
  },
  series: [{
    data: getTrendData().map(d => d.score),
    type: 'line',
    smooth: true,
    itemStyle: { color: '#EE6666' }
  }]
}));

const brandChartOption = computed(() => ({
  title: {
    text: '品牌平均评分',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis',
    formatter: params => {
      const data = getBrandData()[params[0].dataIndex];
      return `${data.brand}<br/>平均分: ${data.score.toFixed(1)}`;
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
      rotate: 45  // 如果品牌名称较长可以旋转45度
    }
  },
  yAxis: {
    type: 'value',
    min: 0,
    max: 10,
    axisLabel: {
      formatter: '{value} 分'  // 添加单位
    }
  },
  series: [{
    name: '平均分',
    data: getBrandData().map(d => d.score),
    type: 'line',
    symbol: 'circle',  // 数据点显示为圆形
    symbolSize: 8,     // 数据点大小
    smooth: true,      // 平滑曲线
    lineStyle: {
      width: 3,        // 线宽
      color: '#5470C6' // 线条颜色
    },
    itemStyle: {
      color: params => params.value > 5 ? '#91CC75' : '#EE6666'
    },
    label: {
      show: true,      // 显示数值标签
      position: 'top',
      formatter: params => params.value.toFixed(2) // 显示原始值
    },
    emphasis: {        // 高亮样式
      itemStyle: {
        color: '#FF0000'  // 高亮时变为红色
      }
    }
  }]
}));

// 数据处理方法
const getAgeGroups = (users) => {
  const groups = users.reduce((acc, user) => {
    const decade = Math.floor(user.age / 10) * 10;
    const key = `${decade}-${decade + 9}`;
    acc[key] = (acc[key] || 0) + 1;
    return acc;
  }, {});

  return Object.entries(groups)
      .map(([name, value]) => ({ name, value }))
      .sort((a, b) => parseInt(a.name) - parseInt(b.name));
};

const getRegionData = (users) => {
  const regions = users.reduce((acc, user) => {
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
  const byDate = dateFilteredRecommendations.value.reduce((acc, rec) => {
    if (!acc[rec.timestamp]) {
      acc[rec.timestamp] = { total: 0, count: 0 };
    }
    acc[rec.timestamp].total += rec.score;
    acc[rec.timestamp].count += 1;
    return acc;
  }, {});

  return Object.entries(byDate)
      .map(([date, { total, count }]) => ({
        date,
        score: total / count,
        count
      }))
      .sort((a, b) => a.date.localeCompare(b.date));
};

const getBrandData = () => {
  const byBrand = dateFilteredRecommendations.value.reduce((acc, rec) => {
    const brand = rec.carName?.split(' ')[0] || '其他';
    if (!acc[brand]) {
      acc[brand] = { total: 0, count: 0 };
    }
    acc[brand].total += rec.score;
    acc[brand].count += 1;
    return acc;
  }, {});

  return Object.entries(byBrand)
      .map(([brand, { total, count }]) => ({
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

const resetFilters = () => {
  setDefaultDateRange();
  selectedRegions.value = getTopRegions();
};

// 禁用未来日期
const disabledDate = (time) => {
  return time.getTime() > Date.now();
};

onMounted(() => {
  fetchData();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  // 清理图表实例
  Object.values(chartInstances.value).forEach(instance => {
    if (instance) {
      instance.dispose();
    }
  });
});
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

.filter-tip {
  font-size: 12px;
  color: #999;
  margin-left: 5px;
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
  position: relative;
  min-height: 400px;
}

.chart-note {
  position: absolute;
  bottom: 10px;
  right: 15px;
  font-size: 12px;
  color: #999;
  font-style: italic;
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