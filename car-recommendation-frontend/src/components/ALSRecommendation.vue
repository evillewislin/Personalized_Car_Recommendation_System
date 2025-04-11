<template>
  <div class="als-container" v-if="dataLoaded">
    <el-tabs
        v-model="activeSubTab"
        class="sub-tabs"
        @tab-change="handleTabChange"
    >
      <!-- 显式反馈ALS -->
      <el-tab-pane label="显式推荐" name="Explicit" key="Explicit">
        <ExplicitFeedback v-if="activeSubTab === 'Explicit'" />
      </el-tab-pane>

      <!-- 隐式反馈ALS -->
      <el-tab-pane label="隐式推荐" name="Implicit" key="Implicit">
        <ImplicitFeedback v-if="activeSubTab === 'Implicit'" />
      </el-tab-pane>

      <!-- 混合反馈ALS -->
      <el-tab-pane label="混合推荐" name="Fixed" key="Fixed">
        <FixedFeedback
            :timeRange="timeRange"
            v-if="activeSubTab === 'Fixed'"
        />
      </el-tab-pane>
    </el-tabs>
  </div>
  <div v-else class="loading-container">
    数据加载中...
  </div>
</template>

<script>
import { defineComponent, ref, onMounted } from 'vue';
import ExplicitFeedback from './ALSSub/ExplicitFeedback.vue';
import ImplicitFeedback from './ALSSub/ImplicitFeedback.vue';
import FixedFeedback from './ALSSub/FixedFeedback.vue';

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
.als-container {
  padding: 20px;
  min-height: 300px; /* 防止布局跳动 */
}

.sub-tabs {
  margin-top: 20px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  color: #666;
  font-size: 16px;
}
</style>