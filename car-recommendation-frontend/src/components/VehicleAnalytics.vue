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