<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { catalogApi } from '@/api/catalog';
import EmptyState from '@/components/common/EmptyState.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import SectionCard from '@/components/common/SectionCard.vue';
import type { BookSummary, CategoryTreeNode } from '@/types';
import { formatMoney } from '@/utils/format';

const router = useRouter();
const loading = ref(false);
const books = ref<BookSummary[]>([]);
const categories = ref<CategoryTreeNode[]>([]);

async function loadData() {
  loading.value = true;
  try {
    const [bookData, categoryData] = await Promise.all([
      catalogApi.books({ pageNo: 1, pageSize: 8 }),
      catalogApi.categoriesTree(),
    ]);
    books.value = bookData.records;
    categories.value = categoryData;
  } finally {
    loading.value = false;
  }
}

onMounted(loadData);
</script>

<template>
  <div class="page-gap">
    <PageHeader title="让好书更快抵达读者" description="商城端覆盖首页、分类筛选、详情、购物车与订单体验，统一接入后端 API。" extra="Book Commerce Experience">
      <template #actions>
        <div class="flex-row-sm" style="flex-wrap: wrap">
          <el-button round size="large" @click="router.push('/books')">浏览图书</el-button>
          <el-button type="primary" round size="large" @click="router.push('/cart')">立即下单</el-button>
        </div>
      </template>
    </PageHeader>

    <section class="hero-gradient glass-card hero-section">
      <div class="responsive-grid" style="grid-template-columns: 1.2fr 0.8fr; align-items: center">
        <div>
          <el-tag round effect="dark" style="background: rgba(255,255,255,0.16); border: none">现代化商城前端</el-tag>
          <h2 style="margin: 18px 0 0; font-size: 40px; line-height: 1.15">图书浏览、购物车结算、后台运营统一一体化</h2>
          <p style="margin: 16px 0 0; line-height: 1.8; color: rgba(255,255,255,0.8)">
            前台聚焦选书与下单，后台覆盖分类、图书、订单和用户视图，提供一致的视觉语言、反馈机制与空状态表现。
          </p>
          <div class="flex-row-sm" style="flex-wrap: wrap; margin-top: 22px">
            <el-tag v-for="item in ['Vue 3', 'TypeScript', 'Pinia', 'Element Plus']" :key="item" round effect="dark" style="background: rgba(255,255,255,0.12); border: none">{{ item }}</el-tag>
          </div>
        </div>
        <div class="section-card hero-stat-card">
          <div class="grid-stack-lg">
            <div>
              <div style="font-size: 14px; opacity: 0.72">分类数量</div>
              <div style="margin-top: 8px; font-size: 32px; font-weight: 700">{{ categories.length }}</div>
            </div>
            <div>
              <div style="font-size: 14px; opacity: 0.72">精选图书</div>
              <div style="margin-top: 8px; font-size: 32px; font-weight: 700">{{ books.length }}</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <SectionCard title="热门分类" description="快速进入重点图书品类。">
      <div v-loading="loading" class="book-grid" style="grid-template-columns: repeat(auto-fill, minmax(180px, 1fr))">
        <div v-for="category in categories" :key="category.id" class="section-card section-card-padded" style="cursor: pointer" @click="router.push({ path: '/books', query: { categoryId: category.id } })">
          <div class="cover-placeholder cover-icon" style="margin-bottom: 18px">分</div>
          <div class="text-lg-bold">{{ category.name }}</div>
          <div class="text-muted" style="margin-top: 8px">{{ category.children?.length || 0 }} 个子分类</div>
        </div>
      </div>
    </SectionCard>

    <SectionCard title="精选图书" description="直接进入详情或加入购物车。">
      <div v-if="books.length" class="book-grid" v-loading="loading">
        <article v-for="book in books" :key="book.id" class="section-card book-card">
          <div class="cover-placeholder cover-book">
            <img v-if="book.coverUrl" :src="book.coverUrl" :alt="book.name" style="width: 100%; height: 100%; object-fit: cover" />
            <span v-else>BOOK</span>
          </div>
          <div>
            <div class="text-lg-bold" style="line-height: 1.4">{{ book.name }}</div>
            <div class="text-muted" style="margin-top: 6px">{{ book.author }}</div>
          </div>
          <div class="flex-between-sm">
            <span class="price-text price-sm">{{ formatMoney(book.price) }}</span>
            <el-button type="primary" round @click="router.push(`/books/${book.id}`)">查看详情</el-button>
          </div>
        </article>
      </div>
      <EmptyState v-else title="暂无精选图书" description="当前没有可展示图书，请稍后刷新。" />
    </SectionCard>
  </div>
</template>
