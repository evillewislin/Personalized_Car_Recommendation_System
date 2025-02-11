<template>
  <div class="admin-dashboard">
    <div class="sidebar">
      <ul>
        <li v-for="item in menuItems" :key="item.title">{{ item.title }}</li>
      </ul>
    </div>

    <div class="main-content">
      <div class="stats">
        <div class="stat-card" v-for="stat in stats" :key="stat.title">
          <h3>{{ stat.title }}</h3>
          <p>{{ stat.value }}</p>
        </div>
      </div>

      <div class="recent-activities">
        <h2>最近活动</h2>
        <ul>
          <li v-for="activity in activities" :key="activity.id">
            <div class="activity-title">{{ activity.title }}</div>
            <div class="activity-time">{{ activity.timestamp }}</div>
          </li>
        </ul>
      </div>
    </div>

    <footer class="footer">
      <span>&copy; 2025 Admin Dashboard</span>
    </footer>
  </div>
</template>

<script>
export default {
  data() {
    return {
      menuItems: [
        { title: '仪表盘' },
        { title: '用户管理' },
        { title: '设置' },
        { title: '退出' },
      ],
      stats: [
        { title: '总用户数', value: '1200' },
        { title: '今天访问量', value: '800' },
        { title: '新注册用户', value: '50' },
        { title: '收入', value: '$5000' },
      ],
      activities: [
        { id: 1, title: '用户 A 更新了个人资料', timestamp: '2 分钟前' },
        { id: 2, title: '用户 B 登录', timestamp: '1 小时前' },
        { id: 3, title: '用户 C 创建了新文章', timestamp: '3 小时前' },
      ],
    };
  },
};
</script>

<style scoped>
.admin-dashboard {
  display: flex;
}

.sidebar {
  width: 250px;
  background-color: #2c3e50;
  color: white;
  padding: 20px;
}

.sidebar ul {
  list-style: none;
  padding: 0;
}

.sidebar li {
  margin: 15px 0;
  font-size: 18px;
  cursor: pointer;
}

.main-content {
  flex-grow: 1;
  padding: 20px;
}

.stats {
  display: flex;
  gap: 20px;
}

.stat-card {
  width: 200px;
  background-color: #ecf0f1;
  padding: 15px;
  border-radius: 8px;
}

.stat-card h3 {
  margin: 0;
  font-size: 20px;
}

.stat-card p {
  font-size: 24px;
  font-weight: bold;
}

.recent-activities h2 {
  margin-bottom: 15px;
}

.recent-activities ul {
  list-style: none;
  padding: 0;
}

.recent-activities li {
  margin-bottom: 10px;
}

.activity-title {
  font-weight: bold;
}

.activity-time {
  color: #7f8c8d;
}

.footer {
  width: 100%;
  text-align: center;
  padding: 10px;
  background-color: #34495e;
  color: white;
  position: fixed;
  bottom: 0;
}
</style>
