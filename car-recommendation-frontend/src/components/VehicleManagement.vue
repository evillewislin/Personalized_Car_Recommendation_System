<template>
  <div class="vehicle-management">
    <div class="action-buttons">
      <button @click="openAddVehicle">新增车辆</button>
      <select v-model="filterBrand">
        <option value="">全部品牌</option>
        <option v-for="brand in brands" :value="brand">{{ brand }}</option>
      </select>
    </div>

    <div class="data-table">
      <table>
        <thead>
        <tr>
          <th>VIN</th>
          <th>品牌</th>
          <th>型号</th>
          <th>库存状态</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="vehicle in filteredVehicles" :key="vehicle.vin">
          <td>{{ vehicle.vin }}</td>
          <td>{{ vehicle.brand }}</td>
          <td>{{ vehicle.model }}</td>
          <td>{{ vehicle.status }}</td>
          <td>
            <button @click="editVehicle(vehicle)">编辑</button>
            <button @click="deleteVehicle(vehicle.vin)">删除</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>