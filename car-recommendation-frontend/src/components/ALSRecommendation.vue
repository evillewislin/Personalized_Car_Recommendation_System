<template>
  <div class="p-6 bg-white rounded-lg shadow-md space-y-6" v-if="dataLoaded">
    <h2 class="text-2xl font-bold text-gray-800">以下是三种不同的推荐方法，选择您喜欢的进行推荐吧</h2>
    <el-tabs
        v-model="activeSubTab"
        class="sub-tabs"
        @tab-change="handleTabChange"
        type="card"
        v-if="dataLoaded"
        style="--el-tabs-header-padding: 0; --el-tabs-card-header-border-bottom: none; --el-tabs-card-active-color: #1e40af; --el-tabs-card-border: none;"
    >
      <el-tab-pane label="① 基于内容推荐" name="Explicit" key="Explicit">
        <ExplicitFeedback />
      </el-tab-pane>
      <el-tab-pane label="② 基于协同过滤推荐" name="Implicit" key="Implicit">
        <ImplicitFeedback  :timeRange="timeRange"/>
      </el-tab-pane>
      <el-tab-pane label="③ 基于混合推荐" name="Fixed" key="Fixed">
        <FixedFeedback :timeRange="timeRange" />
      </el-tab-pane>
    </el-tabs>
  </div>
  <div v-else class="flex justify-center items-center h-64 text-gray-600 text-lg">
    <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
    <span class="ml-4">数据加载中...</span>
  </div>
</template>

<script>
import { defineComponent, ref, onMounted } from 'vue';
import ExplicitFeedback from './ALSSub/ExplicitFeedback.vue';
import ImplicitFeedback from './ALSSub/ImplicitFeedback.vue';
import FixedFeedback from './ALSSub/FixedFeedback.vue';

const handleTabChange = (newTab) => {
  if (changingTab.value) return;
  changingTab.value = true;
  requestAnimationFrame(() => {
    activeSubTab.value = newTab;
    setTimeout(() => {
      changingTab.value = false;
    }, 100);
  });
};
export default defineComponent({
  name: 'ALSRecommendation',
  components: {
    ExplicitFeedback,
    ImplicitFeedback,
    FixedFeedback
  },
  setup() {
    const activeSubTab = ref('Explicit');
    const dataLoaded = ref(false);
    const timeRange = ref(['2023-01-01', '2023-06-30']);
    const changingTab = ref(false);

    const handleTabChange = (newTab) => {
      if (changingTab.value) return;
      changingTab.value = true;
      activeSubTab.value = newTab;
      setTimeout(() => {
        changingTab.value = false;
      }, 100);
    };

    onMounted(async () => {
      try {
        // 模拟异步数据加载
        await new Promise((resolve) => setTimeout(resolve, 1000));
        dataLoaded.value = true;
      } catch (error) {
        console.error('数据加载失败:', error);
      }
    });

    return {
      activeSubTab,
      dataLoaded,
      timeRange,
      handleTabChange
    };
  }
});
</script>

<style scoped>
.sub-tabs .el-tabs__item {
  transition: all 0.3s ease;
}

.sub-tabs .el-tabs__item:hover {
  background-color: #f3f4f6;
}

.sub-tabs .el-tabs__item.is-active {
  color: #1e40af;
  font-weight: bold;
}
</style>