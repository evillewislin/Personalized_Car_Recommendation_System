<!-- TemporalALS.vue -->
<template>
  <div class="sub-page">
    <h3>时间趋势分析</h3>
    <div class="time-range-selector">
      <el-date-picker
          v-model="localRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="handleTimeChange"
      />
    </div>
    <!-- 添加图表容器 -->
    <div ref="trendChart" style="width: 100%; height: 400px;"></div>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  props: ['timeRange'],
  data() {
    return {
      localRange: [...this.timeRange],
      chart: null
    }
  },
  mounted() {
    this.initChart()
  },
  methods: {
    initChart() {
      this.$nextTick(() => {
        if (this.$refs.trendChart) {
          this.chart = echarts.init(this.$refs.trendChart)
          const option = {
            xAxis: {
              type: 'category',
              data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
            },
            yAxis: {
              type: 'value'
            },
            series: [{
              data: [150, 230, 224, 218, 135, 147, 260],
              type: 'line',
              smooth: true
            }]
          }
          this.chart.setOption(option)
        }
      })
    },
    handleTimeChange(range) {
      this.$emit('time-select', range)
    }
  }
}
</script>