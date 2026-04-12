<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';

import { adminApi } from '@/api/admin';
import PageHeader from '@/components/common/PageHeader.vue';
import SectionCard from '@/components/common/SectionCard.vue';
import type { UserInfo } from '@/types';
import { formatDate } from '@/utils/format';

const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const users = ref<UserInfo[]>([]);
const filters = reactive({
  keyword: '',
  status: undefined as number | undefined,
});
const form = reactive({
  userId: 0,
  displayName: '',
  status: 1,
  roleId: undefined as number | undefined,
});

async function loadData() {
  loading.value = true;
  try {
    users.value = await adminApi.users(filters);
  } finally {
    loading.value = false;
  }
}

function openDialog(row: UserInfo) {
  Object.assign(form, {
    userId: row.id,
    displayName: row.displayName,
    status: row.status,
    roleId: row.role?.id,
  });
  dialogVisible.value = true;
}

async function submit() {
  saving.value = true;
  try {
    await adminApi.updateUser(form);
    ElMessage.success('用户已更新');
    dialogVisible.value = false;
    await loadData();
  } finally {
    saving.value = false;
  }
}

onMounted(loadData);
</script>

<template>
  <div style="display: grid; gap: 20px">
    <PageHeader title="用户管理" description="覆盖后台用户查询与状态维护。" extra="User Management" />

    <SectionCard title="用户列表" description="对接 /api/admin/users 查询与更新接口。">
      <div class="toolbar-row" style="margin-bottom: 18px">
        <div style="display: flex; gap: 12px; flex-wrap: wrap">
          <el-input v-model="filters.keyword" placeholder="用户名 / 昵称" clearable style="width: 280px" />
          <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 180px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </div>
        <div style="display: flex; gap: 12px">
          <el-button round @click="loadData">筛选</el-button>
          <el-button round @click="filters.keyword = ''; filters.status = undefined; loadData()">重置</el-button>
        </div>
      </div>

      <el-table :data="users" stripe v-loading="loading">
        <el-table-column prop="username" label="用户名" min-width="160" />
        <el-table-column prop="displayName" label="昵称" min-width="160" />
        <el-table-column label="角色" min-width="120">
          <template #default="scope">{{ scope.row.role?.name || scope.row.role?.code || '--' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope"><el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" round>{{ scope.row.status === 1 ? '启用' : '禁用' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="注册时间" min-width="170">
          <template #default="scope">{{ formatDate(scope.row.createdTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope"><el-button text type="primary" @click="openDialog(scope.row)">编辑</el-button></template>
        </el-table-column>
      </el-table>
    </SectionCard>

    <el-dialog v-model="dialogVisible" title="编辑用户" width="520px">
      <el-form label-position="top">
        <el-form-item label="昵称"><el-input v-model="form.displayName" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色 ID"><el-input-number v-model="form.roleId" :min="1" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" round :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
