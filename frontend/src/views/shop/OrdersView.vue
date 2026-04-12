<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

import { tradeApi } from '@/api/trade';
import EmptyState from '@/components/common/EmptyState.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import SectionCard from '@/components/common/SectionCard.vue';
import StatusTag from '@/components/common/StatusTag.vue';
import type { OrderInfo } from '@/types';
import { formatDate, formatMoney } from '@/utils/format';

const loading = ref(false);
const paying = ref<number | null>(null);
const callbacking = ref<number | null>(null);
const orders = ref<OrderInfo[]>([]);

async function loadOrders() {
  loading.value = true;
  try {
    orders.value = await tradeApi.orders();
  } finally {
    loading.value = false;
  }
}

async function cancelOrder(id: number) {
  await tradeApi.cancelOrder(id);
  ElMessage.success('订单已取消');
  await loadOrders();
}

async function confirmOrder(id: number) {
  await tradeApi.confirmOrder(id);
  ElMessage.success('已确认收货');
  await loadOrders();
}

async function preparePayment(order: OrderInfo) {
  paying.value = order.id;
  try {
    const payload = await tradeApi.preparePayment({ orderId: order.id, payChannel: 'MOCK' });
    ElMessageBox.alert(
      `订单号：${payload.orderNo}\n支付金额：${formatMoney(payload.amount)}\n模拟支付链接：${payload.mockPayUrl}\n回调接口：${payload.callbackUrl}\n下一步可点击“模拟支付成功”完成闭环。`,
      '已生成支付参数',
      { confirmButtonText: '知道了' },
    );
  } finally {
    paying.value = null;
  }
}

async function finishPayment(order: OrderInfo) {
  callbacking.value = order.id;
  try {
    await tradeApi.callbackPayment({
      orderId: order.id,
      payChannel: 'MOCK',
      transactionNo: `MOCK-${order.orderNo}`,
    });
    ElMessage.success('模拟支付成功，订单已更新为已支付');
    await loadOrders();
  } finally {
    callbacking.value = null;
  }
}

onMounted(loadOrders);
</script>

<template>
  <div style="display: grid; gap: 24px">
    <PageHeader title="订单列表" description="查看订单状态，执行取消、支付预下单、模拟支付回调与确认收货。" extra="Order Center" />

    <SectionCard title="我的订单" description="订单接口已覆盖 /api/orders、订单详情、取消、确认、支付预下单与支付回调。">
      <div v-if="orders.length" v-loading="loading" style="display: grid; gap: 18px">
        <article v-for="order in orders" :key="order.id" class="section-card" style="padding: 20px; display: grid; gap: 18px">
          <div class="toolbar-row">
            <div>
              <div style="font-size: 18px; font-weight: 700">订单号 {{ order.orderNo }}</div>
              <div style="margin-top: 8px; color: var(--text-secondary)">下单时间 {{ formatDate(order.createdTime) }}</div>
            </div>
            <div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap">
              <StatusTag :status="order.status" />
              <span class="price-text" style="font-size: 22px">{{ formatMoney(order.totalAmount) }}</span>
            </div>
          </div>

          <div class="order-items">
            <div v-for="item in order.items" :key="item.id" class="order-item">
              <div class="cover-placeholder" style="width: 82px; min-width: 82px; height: 110px; border-radius: 16px; overflow: hidden">
                <img v-if="item.coverUrl" :src="item.coverUrl" :alt="item.bookName" style="width: 100%; height: 100%; object-fit: cover" />
                <span>BOOK</span>
              </div>
              <div style="display: grid; gap: 10px; flex: 1">
                <div style="font-size: 16px; font-weight: 700">{{ item.bookName }}</div>
                <div style="color: var(--text-secondary)">{{ item.bookAuthor }}</div>
                <div style="display: flex; justify-content: space-between; gap: 12px; flex-wrap: wrap">
                  <span>数量  {{ item.quantity }}</span>
                  <span>{{ formatMoney(item.amount) }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="info-list">
            <div class="info-row"><span>收货信息</span><strong>{{ order.receiverName }} / {{ order.receiverPhone }}</strong></div>
            <div class="info-row"><span>收货地址</span><strong>{{ order.receiverAddress }}</strong></div>
          </div>

          <div style="display: flex; justify-content: flex-end; gap: 12px; flex-wrap: wrap">
            <el-button v-if="order.status === 'CREATED'" round type="danger" plain @click="cancelOrder(order.id)">取消订单</el-button>
            <el-button v-if="order.status === 'CREATED'" round type="primary" :loading="paying === order.id" @click="preparePayment(order)">去支付</el-button>
            <el-button v-if="order.status === 'CREATED'" round type="warning" :loading="callbacking === order.id" @click="finishPayment(order)">模拟支付成功</el-button>
            <el-button v-if="order.status === 'PAID'" round type="success" @click="confirmOrder(order.id)">确认收货</el-button>
          </div>
        </article>
      </div>
      <EmptyState v-else title="暂无订单" description="购物车下单后将在此处展示订单进度。" button-text="前往购物车" @action="$router.push('/cart')" />
    </SectionCard>
  </div>
</template>
