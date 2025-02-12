<template>
  <div class="user-analytics">
    <h2>用户行为分析</h2>
    <div class="chart-container">
      <canvas ref="userChart"></canvas>
    </div>

    <div class="stats-grid">
      <div class="stat-item">
        <h3>日活跃用户</h3>
        <p>2,340</p>
      </div>
      <div class="stat-item">
        <h3>月留存率</h3>
        <p>78%</p>
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
      new Chart(this.$refs.userChart, {
        type: 'line',
        data: {
          labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May'],
          datasets: [{
            label: '用户增长趋势',
            data: [65, 59, 80, 81, 56],
            borderColor: '#3498db'
          }]
        }
      })
    }
  }
}
</script>