<template>
  <div class="als-container" v-if="dataLoaded">
    <el-tabs v-model="activeSubTab" class="sub-tabs">
      <!-- 显式反馈ALS -->
      <el-tab-pane label="显式推荐" name="Explicit">
        <ExplicitFeedback />
      </el-tab-pane>

      <!-- 隐式反馈ALS -->
      <el-tab-pane label="隐式推荐" name="Implicit">
        <ImplicitFeedback />
      </el-tab-pane>

      <!-- 混合反馈ALS -->
      <el-tab-pane label="混合推荐" name="Fixed">
        <!-- 确保传递的 timeRange 是可迭代对象 -->
        <FixedFeedback :timeRange="timeRange" />
      </el-tab-pane>
    </el-tabs>
  </div>
  <div v-else>
    数据加载中...
  </div>
</template>

<script>
import { defineComponent, ref, onMounted } from 'vue';
import ExplicitFeedback from './ALSSub/ExplicitFeedback.vue';
import ImplicitFeedback from './ALSSub/ImplicitFeedback.vue';
import FixedFeedback from './ALSSub/FixedFeedback.vue';

export default defineComponent({
  components: {
    ExplicitFeedback,
    ImplicitFeedback,
    FixedFeedback
  },
  setup() {
    // 当前激活的子标签页
    const activeSubTab = ref('Explicit');
    const dataLoaded = ref(false);
    // 初始化 timeRange 为数组
    const timeRange = ref(['2023-01-01', '2023-06-30']);

    onMounted(async () => {
      // 模拟异步数据加载
      await new Promise((resolve) => setTimeout(resolve, 1000));
      dataLoaded.value = true;
    });

    return {
      activeSubTab,
      dataLoaded,
      timeRange
    };
  }
});
</script>

<style scoped>
.als-container {
  padding: 20px;
}

.sub-tabs {
  margin-top: 20px;
}
</style>