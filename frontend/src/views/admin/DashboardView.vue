<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { adminApi } from '@/api/admin';
import PageHeader from '@/components/common/PageHeader.vue';
import SectionCard from '@/components/common/SectionCard.vue';
import StatCard from '@/components/common/StatCard.vue';
import type { DashboardData } from '@/types';
import { formatMoney } from '@/utils/format';

const loading = ref(false);
const dashboard = ref<DashboardData | null>(null);

async function loadData() {
  loading.value = true;
  try {
    dashboard.value = await adminApi.dashboard();
  } finally {
    loading.value = false;
  }
}

onMounted(loadData);
</script>

<template>
  <div style="display: grid; gap: 20px" v-loading="loading">
    <PageHeader title="仪表盘" description="总览图书、分类、用户、订单、支付与销售数据。" extra="Admin Overview" />

    <div class="book-grid" style="grid-template-columns: repeat(auto-fill, minmax(220px, 1fr))">
      <StatCard title="图书总数" :value="dashboard?.bookCount || 0" description="当前系统图书规模" icon="书" />
      <StatCard title="分类数量" :value="dashboard?.categoryCount || 0" description="含多级分类结构" icon="类" />
      <StatCard title="用户规模" :value="dashboard?.userCount || 0" description="累计注册用户" icon="人" />
      <StatCard title="订单总量" :value="dashboard?.orderCount || 0" description="商城全量订单数" icon="单" />
      <StatCard title="支付记录" :value="dashboard?.paymentCount || 0" description="已生成支付流程的订单" icon="付" />
      <StatCard title="销售总额" :value="formatMoney(dashboard?.totalSales || 0)" description="累计订单金额" icon="" />
    </div>

    <SectionCard title="运营说明" description="当前后端仪表盘接口返回基础统计字段，因此此页面聚焦核心 KPI 总览。">
      <div class="info-list">
        <div class="info-row"><span>接口路径</span><strong>/api/admin/dashboard</strong></div>
        <div class="info-row"><span>适用场景</span><strong>后台首页、经营概览、联调验证</strong></div>
        <div class="info-row"><span>下一步可扩展</span><strong>近 7 日趋势、最新订单、热销图书榜单</strong></div>
      </div>
    </SectionCard>
  </div>
</template>
