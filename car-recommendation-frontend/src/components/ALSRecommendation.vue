<!-- ALSRecommendation.vue -->
<template>
  <div class="als-container">
    <el-tabs v-model="activeSubTab" class="sub-tabs">
      <!-- 显式反馈ALS -->
      <el-tab-pane label="显式反馈推荐" name="Explicit">
        <ExplicitFeedback
            :params="explicitParams"
            @recommend="handleExplicitRecommend"
        />
      </el-tab-pane>

      <!-- 隐式反馈ALS -->
      <el-tab-pane label="隐式反馈推荐" name="Implicit">
        <ImplicitFeedback
            :data="implicitData"
            @refresh="loadImplicitData"
        />
      </el-tab-pane>

      <!-- 加权ALS -->
      <el-tab-pane label="加权推荐" name="Weighted">
        <WeightedALS
            :weights="weightSettings"
            @weight-change="updateWeights"
        />
      </el-tab-pane>

      <!-- 时间感知ALS -->
      <el-tab-pane label="时序推荐" name="Temporal">
        <TemporalALS
            :time-range="timeRange"
            @time-select="handleTimeSelect"
        />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { defineComponent, ref } from 'vue'
import ExplicitFeedback from './ALSSub/ExplicitFeedback.vue'
import ImplicitFeedback from './ALSSub/ImplicitFeedback.vue'
import WeightedALS from './ALSSub/WeightedALS.vue'
import TemporalALS from './ALSSub/TemporalALS.vue'

export default defineComponent({
  components: {
    ExplicitFeedback,
    ImplicitFeedback,
    WeightedALS,
    TemporalALS
  },
  setup() {
    const activeSubTab = ref('Explicit')

    // 显式反馈参数
    const explicitParams = ref({
      rank: 10,
      iterations: 15,
      lambda: 0.01
    })

    // 隐式反馈数据
    const implicitData = ref([
      { userId: 1, itemId: 101, interactions: 5 },
      { userId: 2, itemId: 102, interactions: 3 }
    ])

    // 加权设置
    const weightSettings = ref({
      timeDecay: 0.7,
      userTrust: 0.8
    })

    // 时间范围
    const timeRange = ref(['2023-01-01', '2023-06-30'])

    const handleExplicitRecommend = (params) => {
      console.log('执行显式反馈推荐:', params)
      // 调用API获取推荐结果
    }

    const loadImplicitData = async () => {
      console.log('刷新隐式数据...')
      // 模拟API调用
    }

    const updateWeights = (newWeights) => {
      weightSettings.value = newWeights
      console.log('更新权重:', newWeights)
    }

    const handleTimeSelect = (range) => {
      timeRange.value = range
      console.log('选择时间范围:', range)
    }

    return {
      activeSubTab,
      explicitParams,
      implicitData,
      weightSettings,
      timeRange,
      handleExplicitRecommend,
      loadImplicitData,
      updateWeights,
      handleTimeSelect
    }
  }
})
</script>

<style scoped>
/* 保持原有样式 */
</style>