<template>
  <div class="user-management">
    <div class="action-buttons">
      <button @click="openAddUser">新增用户</button>
      <input type="text" placeholder="搜索用户..." v-model="searchQuery">
    </div>

    <div class="data-table">
      <table>
        <thead>
        <tr>
          <th>ID</th>
          <th>用户名</th>
          <th>手机号</th>
          <th>注册时间</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="user in filteredUsers" :key="user.id">
          <td>{{ user.id }}</td>
          <td>{{ user.name }}</td>
          <td>{{ user.phone }}</td>
          <td>{{ user.regDate }}</td>
          <td>
            <button @click="editUser(user)">编辑</button>
            <button @click="deleteUser(user.id)">删除</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>