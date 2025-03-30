<!-- ImplicitFeedback.vue -->
<template>
  <div class="sub-page">
    <div class="header">
      <h3>用户行为数据</h3>
      <el-button @click="$emit('refresh')">刷新数据</el-button>
    </div>

    <el-table :data="data" height="400px">
      <el-table-column prop="userId" label="用户ID" />
      <el-table-column prop="itemId" label="物品ID" />
      <el-table-column prop="interactions" label="交互次数" />
    </el-table>
  </div>
</template>

<script>
export default {
  props: ['data']
}
</script>