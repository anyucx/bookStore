<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';

import { adminApi } from '@/api/admin';
import PageHeader from '@/components/common/PageHeader.vue';
import SectionCard from '@/components/common/SectionCard.vue';
import StatusTag from '@/components/common/StatusTag.vue';
import type { OrderInfo } from '@/types';
import { formatDate, formatMoney } from '@/utils/format';

const loading = ref(false);
const submittingId = ref<number | null>(null);
const orders = ref<OrderInfo[]>([]);
const filters = reactive({
  keyword: '',
  status: '',
});

async function loadData() {
  loading.value = true;
  try {
    orders.value = await adminApi.orders({ ...filters, status: filters.status || undefined });
  } finally {
    loading.value = false;
  }
}

async function updateOrderStatus(id: number, status: string) {
  submittingId.value = id;
  try {
    await adminApi.updateOrder({ id, status });
    ElMessage.success('订单状态已更新');
    await loadData();
  } finally {
    submittingId.value = null;
  }
}

onMounted(loadData);
</script>

<template>
  <div class="admin-page-gap">
    <PageHeader title="订单管理" description="后台查看订单、用户与收货信息，并支持必要的状态维护。" extra="Order Management" />

    <SectionCard title="订单列表" description="对接 /api/admin/orders，支持状态筛选、检索与状态维护。">
      <div class="toolbar-row toolbar-mb">
        <div class="flex-row-wrap-sm">
          <el-input v-model="filters.keyword" placeholder="订单号 / 收货人" clearable style="width: 280px" />
          <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 180px">
            <el-option label="待支付" value="CREATED" />
            <el-option label="待收货" value="PAID" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="已完成" value="CONFIRMED" />
          </el-select>
        </div>
        <div class="flex-row-sm">
          <el-button round @click="loadData">筛选</el-button>
          <el-button round @click="filters.keyword = ''; filters.status = ''; loadData()">重置</el-button>
        </div>
      </div>

      <el-table :data="orders" stripe v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column label="状态" width="120">
          <template #default="scope"><StatusTag :status="scope.row.status" /></template>
        </el-table-column>
        <el-table-column label="用户" min-width="180">
          <template #default="scope">{{ scope.row.user?.displayName || '--' }} / {{ scope.row.user?.username || '--' }}</template>
        </el-table-column>
        <el-table-column label="收货信息" min-width="200">
          <template #default="scope">{{ scope.row.receiverName }} / {{ scope.row.receiverPhone }}</template>
        </el-table-column>
        <el-table-column label="订单金额" width="120">
          <template #default="scope">{{ formatMoney(scope.row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="下单时间" min-width="170">
          <template #default="scope">{{ formatDate(scope.row.createdTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="220" fixed="right">
          <template #default="scope">
            <div class="flex-row-xs" style="flex-wrap: wrap">
              <el-button
                v-if="scope.row.status === 'CREATED'"
                round
                type="warning"
                :loading="submittingId === scope.row.id"
                @click="updateOrderStatus(scope.row.id, 'PAID')"
              >
                标记已支付
              </el-button>
              <el-button
                v-if="scope.row.status === 'PAID'"
                round
                type="success"
                :loading="submittingId === scope.row.id"
                @click="updateOrderStatus(scope.row.id, 'CONFIRMED')"
              >
                标记已完成
              </el-button>
              <el-button
                v-if="scope.row.status === 'CREATED'"
                round
                type="danger"
                plain
                :loading="submittingId === scope.row.id"
                @click="updateOrderStatus(scope.row.id, 'CANCELLED')"
              >
                取消订单
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </SectionCard>
  </div>
</template>
