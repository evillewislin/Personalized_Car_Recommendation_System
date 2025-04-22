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

      <div class="filter-group" style="flex: 1; display: flex; align-items: center; gap: 10px;">
        <label>地区筛选：</label>
        <el-select
            v-model="selectedRegions"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择地区"
            filterable
            @change="handleFilterChange"
        >
          <el-option
              v-for="region in availableRegions"
              :key="region"
              :label="region"
              :value="region"
          />
        </el-select>
        <el-button
            type="info"
            size="small"
            @click="toggleAllRegions"
            class="el-button"
            style="width: 115px; padding: 8px 15px;font-size: 14px"
        >
          {{ isAllSelected ? '全不选' : '全选' }}
        </el-button>
      </div>

      <el-button
          type="primary"
          @click="resetFilters"
          class="el-button"
      >重置筛选</el-button>
    </div>

    <!-- 统计卡片（基于筛选后的用户数据） -->
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
      <div class="chart-container" style="height: 400px; width: 100%;">
        <v-chart
            :option="ageChartOption"
            style="height: 100%; width: 100%;"
            @rendered="handleChartRendered('ageChart')"
            autoresize
        />
      </div>
      <div class="chart-container" style="height: 400px; width: 100%;">
        <v-chart
            :option="regionChartOption"
            style="height: 100%; width: 100%;"
            @rendered="handleChartRendered('regionChart')"
            autoresize
        />
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-container" style="height: 400px; width: 100%;">
        <v-chart
            :option="activityChartOption"
            style="height: 100%; width: 100%;"
            @rendered="handleChartRendered('activityChart')"
            autoresize
        />
      </div>
      <div class="chart-container" style="height: 400px; width: 100%;">
        <v-chart
            :option="brandChartOption"
            style="height: 100%; width: 100%;"
            @rendered="handleChartRendered('brandChart')"
            autoresize
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
import 'echarts-wordcloud';

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

// 数据状态（假设用户和推荐数据通过userId关联）
const rawUsers = ref([]); // 用户数据（包含userId和region）
const rawRecommendations = ref([]); // 推荐数据（包含userId和timestamp）

// 筛选状态
const dateRange = ref([]);
const selectedRegions = ref([]);
const availableRegions = ref([]);

// 图表实例引用
const chartInstances = ref({
  ageChart: null,
  regionChart: null,
  activityChart: null,
  brandChart: null
});

// 设置默认最近30天
const setDefaultDateRange = () => {
  const end = new Date();
  const start = new Date();
  start.setDate(end.getDate() - 30);

  dateRange.value = [
    start.toISOString().split('T')[0],
    end.toISOString().split('T')[0]
  ];
};

// 获取TOP10地区（基于全部用户）
const getTopRegions = () => {
  if (rawUsers.value.length === 0) return [];
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
      axios.get('/api/history/users'), // 假设返回包含userId的用户数据
      axios.get('/api/history/recommendation-history') // 假设返回包含userId的推荐数据
    ]);

    // 处理用户数据（确保包含userId和region）
    rawUsers.value = (usersRes.data || []).map(user => ({
      ...user,
      userId: user.userId, // 关键关联字段
      age: Number(user.age) || 0,
      region: user.region || '未知地区',
      registerDate: user.registerDate?.split('T')[0] || '2023-01-01'
    }));

    // 处理推荐数据（确保包含userId和timestamp）
    rawRecommendations.value = (recRes.data || []).map(rec => ({
      ...rec,
      userId: rec.userId, // 关键关联字段
      score: Math.min(Math.max(Number(rec.score) || 0, 0), 10),
      timestamp: rec.timestamp?.split('T')[0] || '2023-01-01'
    }));

    // 初始化地区选项和默认选中TOP10
    availableRegions.value = [...new Set(rawUsers.value.map(u => u.region))].sort();
    selectedRegions.value = getTopRegions();

    // 设置默认日期范围
    setDefaultDateRange();
  } catch (error) {
    console.error('数据加载失败:', error);
  }
};

// 图表渲染回调
const handleChartRendered = (chartName) => (instance) => {
  if (instance && !instance.isDisposed()) {
    chartInstances.value[chartName] = instance;
  }
};

// 地区筛选后的用户数据（用于用户相关图表和统计）
const regionFilteredUsers = computed(() => {
  return rawUsers.value.filter(user => selectedRegions.value.includes(user.region));
});

// 日期+地区筛选后的推荐数据（通过userId关联用户地区）
const dateAndRegionFilteredRecommendations = computed(() => {
  if (rawUsers.value.length === 0 || rawRecommendations.value.length === 0) return [];

  return rawRecommendations.value.filter(rec => {
    // 找到推荐记录对应的用户
    const user = rawUsers.value.find(u => u.userId === rec.userId);
    if (!user) return false; // 确保用户存在

    // 地区匹配 + 日期匹配
    const regionMatch = selectedRegions.value.includes(user.region);
    const dateMatch = dateRange.value.length
        ? rec.timestamp >= dateRange.value[0] && rec.timestamp <= dateRange.value[1]
        : true; // 无日期筛选时默认匹配

    return regionMatch && dateMatch;
  });
});

// 用户统计信息（基于筛选后的用户数据）
const userStats = computed(() => {
  const validUsers = regionFilteredUsers.value;
  const validAges = validUsers.filter(u => !isNaN(u.age));
  const totalAge = validAges.reduce((sum, u) => sum + u.age, 0);

  return {
    total: validUsers.length, // 筛选后的用户总数
    avgAge: validAges.length
        ? `${(totalAge / validAges.length).toFixed(1)}岁`
        : '0.0岁',
    regions: new Set(validUsers.map(u => u.region)).size // 筛选后的地区数量
  };
});

// 全选状态计算
const isAllSelected = computed(() => {
  if (!availableRegions.value.length) return false;
  return availableRegions.value.length === selectedRegions.value.length;
});

// 全选/全不选处理
const toggleAllRegions = () => {
  if (isAllSelected.value) {
    selectedRegions.value = [];
  } else {
    selectedRegions.value = [...availableRegions.value];
  }
  handleFilterChange(); // 触发筛选更新
};

// 图表数据 - 年龄分布（地区筛选）
const ageChartOption = computed(() => {
  if (regionFilteredUsers.value.length === 0) {
    return {};
  }
  return {
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
  };
});

// 图表数据 - 地区分布（地区筛选）
const regionChartOption = computed(() => ({
  title: { text: '用户地区分布' },
  tooltip: { formatter: '{b}: {c} ({d}%)' },
  series: [{
    type: 'pie',
    radius: '60%',
    data: getRegionData(regionFilteredUsers.value)
  }]
}));

// 图表数据 - 每日用户活跃度
const activityChartOption = computed(() => {
  const activityData = getActivityData();
  return {
    title: { text: '每日用户活跃度' },
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const data = params[0].data;
        return `日期: ${data[0]}<br/>活跃用户数: ${data[1]}`;
      }
    },
    xAxis: {
      type: 'category',
      data: activityData.map(d => d.date),
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '活跃用户数'
    },
    series: [{
      data: activityData.map(d => [d.date, d.count]),
      type: 'bar',
      barWidth: '60%',
      itemStyle: {
        color: '#91CC75'
      },
      emphasis: {
        itemStyle: {
          color: '#5470C6'
        }
      }
    }],
    dataZoom: [
      {
        type: 'inside',
        start: 0,
        end: 100
      },
      {
        start: 0,
        end: 100
      }
    ]
  };
});

// 图表数据 - 品牌词云
const brandChartOption = computed(() => {
  const brandData = getBrandData();
  const brandFrequency = {};
  brandData.forEach(item => {
    brandFrequency[item] = (brandFrequency[item] || 0) + 1;
  });
  const maxFrequency = Math.max(...Object.values(brandFrequency));
  const wordCloudData = Object.keys(brandFrequency).map(brand => ({
    name: brand,
    value: brandFrequency[brand]
  }));
  return {
    title: { text: '品牌出现频率词云' },
    tooltip: {},
    series: [{
      type: 'wordCloud',
      shape: 'circle',
      left: 'center',
      top: 'center',
      width: '90%',
      height: '90%',
      sizeRange: [20, 60],
      rotationRange: [0, 45],
      rotationStep: 45,
      gridSize: 8,
      drawOutOfBound: false,
      textStyle: {
        normal: {
          color: (params) => {
            const value = brandFrequency[params.name];
            const hue = (value / maxFrequency) * 180;
            return `hsl(${hue}, 70%, 50%)`;
          }
        },
        emphasis: {
          shadowBlur: 10,
          shadowColor: '#333'
        }
      },
      data: wordCloudData
    }]
  };
});

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

const getActivityData = () => {
  // 按日期分组统计活跃用户数
  const activityByDate = dateAndRegionFilteredRecommendations.value.reduce((acc, rec) => {
    if (!acc[rec.timestamp]) {
      acc[rec.timestamp] = new Set();
    }
    acc[rec.timestamp].add(rec.userId); // 使用Set去重
    return acc;
  }, {});

  return Object.entries(activityByDate)
      .map(([date, userIds]) => ({
        date,
        count: userIds.size
      }))
      .sort((a, b) => a.date.localeCompare(b.date));
};

const getBrandData = () => {
  return rawRecommendations.value.map(rec => {
    return rec.carName?.split(' ')[0] || '其他';
  });
};

// 筛选操作（触发计算属性更新）
const handleFilterChange = () => {};

const resetFilters = () => {
  setDefaultDateRange();
  selectedRegions.value = getTopRegions();
  handleFilterChange(); // 触发筛选更新
};

// 禁用未来日期
const disabledDate = (time) => {
  return time.getTime() > Date.now();
};

onMounted(() => {
  fetchData();
});

onBeforeUnmount(() => {
  Object.values(chartInstances.value).forEach(instance => {
    if (instance && !instance.isDisposed()) {
      instance.dispose();
    }
  });
});
</script>

<style scoped>
.el-button {
  background-color: #1a73e8;
  color: white;
  border-radius: 4px;
}
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
}

.filter-group label {
  width: 100px;
  font-weight: 500;
  color: #606266;
  white-space: nowrap;
}

.el-button {
  background-color: #1a73e8;
  color: white;
  border-radius: 4px;
  min-width: 60px; /* 确保按钮文字显示空间 */
  height: 36px;

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
}

.v-chart {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
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