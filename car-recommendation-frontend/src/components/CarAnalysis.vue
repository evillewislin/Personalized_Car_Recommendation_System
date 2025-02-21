<template>
  <div class="car-analysis-container">
    <div class="filter-section">
      <label for="minPrice">最小价格:</label>
      <input type="number" id="minPrice" v-model="minPrice" placeholder="请输入最小价格">
      <label for="maxPrice">最大价格:</label>
      <input type="number" id="maxPrice" v-model="maxPrice" placeholder="请输入最大价格">
      <button @click="filterData">筛选</button>
    </div>
    <div class="chart-container">
      <canvas ref="scatterChart"></canvas>
    </div>
  </div>
</template>

<script>
import { Chart, registerables } from 'chart.js';
import axios from 'axios';

export default {
  name: 'CarAnalysis',
  data() {
    return {
      minPrice: null,
      maxPrice: null,
      originalData: [], // 存储原始数据
      scatterChart: null // 存储图表实例
    };
  },
  mounted() {
    Chart.register(...registerables);
    this.fetchDataAndRenderCharts();
  },
  methods: {
    async fetchDataAndRenderCharts() {
      try {
        const response = await axios.get('/api/car-analysis');
        let data = response.data;
        // 过滤掉价格超过一千万的数据
        data = data.filter(item => item.minprice < 10000000 && item.maxprice < 10000000);
        this.originalData = data; // 保存原始数据

        // 计算全部价格的最小和最大值
        const allPrices = data.flatMap(item => [item.minprice, item.maxprice]);
        this.minPrice = Math.min(...allPrices);
        this.maxPrice = Math.max(...allPrices);

        // 处理散点图数据
        const scatterData = this.processScatterData(data);
        this.scatterChart = this.renderScatterChart(scatterData.slice(0, 500)); // 先加载前 500 条
        await this.loadDataInBatches(scatterData, this.scatterChart, 500, 100);
      } catch (error) {
        console.error('获取数据失败:', error);
      }
    },
    processScatterData(data) {
      const brandPriceMap = new Map();
      data.forEach(item => {
        if (item.car_brand_name && typeof item.minprice === 'number' && typeof item.maxprice === 'number') {
          const brand = item.car_brand_name;
          const minPrice = item.minprice;
          const maxPrice = item.maxprice;
          const averagePrice = (minPrice + maxPrice) / 2;

          if (brandPriceMap.has(brand)) {
            const [totalPrice, count] = brandPriceMap.get(brand);
            brandPriceMap.set(brand, [totalPrice + averagePrice, count + 1]);
          } else {
            brandPriceMap.set(brand, [averagePrice, 1]);
          }
        }
      });

      const scatterData = [];
      brandPriceMap.forEach(([totalPrice, count], brand) => {
        const finalAveragePrice = totalPrice / count;
        scatterData.push({ x: brand, y: finalAveragePrice });
      });

      return scatterData;
    },
    renderScatterChart(data) {
      const plainData = data.map(item => ({ x: item.x, y: item.y }));
      const ctx = this.$refs.scatterChart.getContext('2d');
      const scatterChart = new Chart(ctx, {
        type: 'scatter',
        data: {
          datasets: [{
            label: '汽车品牌价格平均值',
            data: plainData,
            backgroundColor: 'rgba(75, 192, 192, 0.2)',
            borderColor: 'rgb(75, 192, 192)',
            borderWidth: 1
          }]
        },
        options: {
          scales: {
            x: {
              type: 'category',
              title: {
                display: true,
                text: '汽车品牌'
              },
              autoSkip: true,
              maxRotation: 45,
              minRotation: 0,
              maxTicksLimit: 20
            },
            y: {
              title: {
                display: true,
                text: '价格平均值'
              },
              beginAtZero: true
            }
          }
        }
      });
      return scatterChart;
    },
    async loadDataInBatches(data, chart, batchSize, delay) {
      let startIndex = batchSize;
      const updateInterval = 5; // 每添加 5 批数据后更新一次图表
      let batchCount = 0;

      while (startIndex < data.length) {
        const endIndex = Math.min(startIndex + batchSize, data.length);
        const newData = data.slice(startIndex, endIndex);
        const plainNewData = newData.map(item => ({ x: item.x, y: item.y }));
        chart.data.datasets[0].data.push(...plainNewData);
        batchCount++;

        if (batchCount % updateInterval === 0) {
          chart.update();
        }

        startIndex = endIndex;
        await new Promise(resolve => setTimeout(resolve, delay));
      }

      // 处理最后不足 updateInterval 批的数据
      if (batchCount % updateInterval !== 0) {
        chart.update();
      }
    },
    async filterData() {
      try {
        const response = await axios.get('/api/car-analysis', {
          params: {
            minPrice: this.minPrice,
            maxPrice: this.maxPrice
          }
        });
        let filteredData = response.data;
        if (filteredData.length === 0) {
          console.log('没有符合条件的数据');
          return;
        }

        // 处理筛选后的数据
        const scatterData = this.processScatterData(filteredData);
        if (this.scatterChart) {
          // 清空原有图表数据
          this.scatterChart.data.datasets[0].data = [];
          // 将处理后的数据转换为普通的 JavaScript 对象
          const plainScatterData = scatterData.map(item => ({ x: item.x, y: item.y }));
          // 更新图表数据
          this.scatterChart.data.datasets[0].data = plainScatterData;
          // 更新图表
          try {
            this.scatterChart.update();
          } catch (error) {
            console.error('更新图表时出错:', error);
          }
        } else {
          console.error('图表实例未正确初始化');
        }
      } catch (error) {
        console.error('筛选数据时出错:', error);
      }
    }
  }
};
</script>

<style scoped>
.car-analysis-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  height: 100vh; /* 占满整个可视区域高度 */
  box-sizing: border-box;
}

h2 {
  margin-bottom: 20px;
}

.filter-section {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.chart-container {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 800px;
  max-height: 450px;
  flex-grow: 1; /* 让图表容器占满剩余空间 */
}

canvas {
  padding: 1.5rem;
  width: 100% !important;
  height: auto !important;
  aspect-ratio: 16 / 9; /* 设置宽高比 */
}
</style>