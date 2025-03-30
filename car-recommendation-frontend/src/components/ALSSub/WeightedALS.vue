<!-- WeightedALS.vue -->
<template>
  <div class="sub-page">
    <h3>权重配置</h3>
    <div class="weight-config">
      <div class="config-item">
        <label>时间衰减因子</label>
        <el-slider
            v-model="localWeights.timeDecay"
            :min="0"
            :max="1"
            :step="0.1"
            @change="update"
        />
      </div>
      <div class="config-item">
        <label>用户可信度</label>
        <el-rate
            v-model="localWeights.userTrust"
            :max="5"
            @change="update"
        />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: ['weights'],
  data() {
    return {
      localWeights: {...this.weights}
    }
  },
  methods: {
    update() {
      this.$emit('weight-change', this.localWeights)
    }
  }
}
</script>