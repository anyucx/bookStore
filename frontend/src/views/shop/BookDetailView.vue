<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';

import { catalogApi } from '@/api/catalog';
import { tradeApi } from '@/api/trade';
import EmptyState from '@/components/common/EmptyState.vue';
import SectionCard from '@/components/common/SectionCard.vue';
import type { BookSummary } from '@/types';
import { formatMoney } from '@/utils/format';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const submitting = ref(false);
const quantity = ref(1);
const book = ref<BookSummary | null>(null);

async function loadDetail() {
  loading.value = true;
  try {
    book.value = await catalogApi.bookDetail(Number(route.params.id));
  } finally {
    loading.value = false;
  }
}

async function addToCart() {
  if (!book.value) {
    return;
  }
  submitting.value = true;
  try {
    await tradeApi.addCartItem({ bookId: book.value.id, quantity: quantity.value });
    ElMessage.success('图书已加入购物车');
  } finally {
    submitting.value = false;
  }
}

onMounted(loadDetail);
</script>

<template>
  <div v-loading="loading" style="display: grid; gap: 24px">
    <div v-if="book" class="responsive-grid" style="grid-template-columns: minmax(320px, 420px) minmax(0, 1fr)">
      <section class="section-card" style="padding: 24px">
        <div class="cover-placeholder" style="aspect-ratio: 4/5; border-radius: 28px; overflow: hidden">
          <img v-if="book.coverUrl" :src="book.coverUrl" :alt="book.name" style="width: 100%; height: 100%; object-fit: cover" />
          <span v-else style="font-size: 28px; font-weight: 700">BOOK</span>
        </div>
      </section>

      <section class="glass-card" style="padding: 28px; display: grid; gap: 20px">
        <div>
          <el-breadcrumb separator=">">
            <el-breadcrumb-item @click="router.push('/')">首页</el-breadcrumb-item>
            <el-breadcrumb-item @click="router.push('/books')">图书列表</el-breadcrumb-item>
            <el-breadcrumb-item>{{ book.name }}</el-breadcrumb-item>
          </el-breadcrumb>
          <h1 class="page-title" style="margin-top: 20px">{{ book.name }}</h1>
          <p class="page-desc">{{ book.author }}  {{ book.category?.name || '未分类' }}  ISBN {{ book.isbn || '--' }}</p>
        </div>

        <div class="price-text">{{ formatMoney(book.price) }}</div>

        <div class="info-list">
          <div class="info-row"><span>库存</span><strong>{{ book.stock }}</strong></div>
          <div class="info-row"><span>销量</span><strong>{{ book.sales }}</strong></div>
          <div class="info-row"><span>状态</span><strong>{{ book.status === 1 ? '上架中' : '已下架' }}</strong></div>
        </div>

        <SectionCard title="图书简介" description="展示后端详情接口返回的完整描述。">
          <div style="white-space: pre-wrap; line-height: 1.85; color: var(--text-secondary)">{{ book.description || '暂无图书简介。' }}</div>
        </SectionCard>

        <div style="display: flex; align-items: center; gap: 14px; flex-wrap: wrap">
          <el-input-number v-model="quantity" :min="1" :max="book.stock || 1" />
          <el-button type="primary" size="large" round :loading="submitting" @click="addToCart">加入购物车</el-button>
          <el-button size="large" round @click="router.push('/cart')">前往购物车</el-button>
        </div>
      </section>
    </div>

    <EmptyState v-else title="图书不存在" description="请返回列表重新选择图书。" button-text="返回列表" @action="router.push('/books')" />
  </div>
</template>
