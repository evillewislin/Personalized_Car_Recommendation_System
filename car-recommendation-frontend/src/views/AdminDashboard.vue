<template>
  <div class="vehicle-analytics">
    <h2>车辆数据分析</h2>
    <div class="chart-container">
      <canvas ref="vehicleChart"></canvas>
    </div>

    <div class="stats-grid">
      <div class="stat-item">
        <h3>在库车辆</h3>
        <p>320</p>
      </div>
      <div class="stat-item">
        <h3>本月销量</h3>
        <p>85</p>
      </div>
    </div>
  </div>
</template>

<script>
import { Chart } from 'chart.js'

export default {
  mounted() {
    this.renderChart()
  },
  methods: {
    renderChart() {
      new Chart(this.$refs.vehicleChart, {
        type: 'bar',
        data: {
          labels: ['SUV', '轿车', 'MPV', '跑车'],
          datasets: [{
            label: '车型分布',
            data: [65, 59, 80, 81],
            backgroundColor: '#2ecc71'
          }]
        }
      })
    }
  }
}
</script>

<style scoped>
/* 全局布局样式 */
.admin-dashboard {
  display: flex;
  min-height: 100vh;
}

/* 侧边栏样式 */
.sidebar {
  width: 250px;
  background-color: #f8f9fa;
  border-right: 1px solid #ddd;
  padding: 20px;
  position: sticky;
  top: 0;
  height: 100vh;
}

.sidebar ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.sidebar li {
  padding: 12px 16px;
  margin: 8px 0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #333;
}

.sidebar li:hover {
  background-color: rgba(76, 175, 80, 0.1);
}

.sidebar li.active {
  background-color: #4CAF50;
  color: white;
  font-weight: 500;
}

/* 主内容区样式 */
.main-content {
  flex-grow: 1;
  padding: 2rem;
  background-color: #f5f5f5;
}

/* 统计卡片 */
.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  border-left: 4px solid #4CAF50;
}

.stat-card h3 {
  margin: 0 0 0.5rem;
  font-size: 16px;
  color: #666;
}

.stat-card p {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

/* 数据表格 */
.data-table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-top: 1.5rem;
}

.data-table table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.data-table th {
  background-color: #f8f9fa;
  font-weight: 500;
}

.data-table tr:hover {
  background-color: #f5f5f5;
}

/* 图表容器 */
.chart-container {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  margin-top: 1.5rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.action-buttons button {
  padding: 8px 16px;
  cursor: pointer;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.action-buttons button:hover {
  background-color: #45a049;
}

/* 搜索框 */
.search-container {
  margin: 1.5rem 0;
}

.search-container input {
  width: 100%;
  padding: 10px 20px;
  border: 2px solid #4CAF50;
  border-radius: 25px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s ease;
}

.search-container input:focus {
  border-color: #45a049;
  box-shadow: 0 0 8px rgba(76, 175, 80, 0.3);
}

/* 响应式布局 */
@media (max-width: 768px) {
  .admin-dashboard {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    height: auto;
    position: static;
  }

  .main-content {
    padding: 1rem;
  }

  .stats {
    grid-template-columns: 1fr;
  }
}

/* 页脚 */
.footer {
  background-color: #f8f9fa;
  padding: 1rem;
  text-align: center;
  border-top: 1px solid #ddd;
  margin-top: auto;
}
</style>
