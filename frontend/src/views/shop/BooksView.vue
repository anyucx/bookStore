<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';

import { catalogApi } from '@/api/catalog';
import EmptyState from '@/components/common/EmptyState.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import SectionCard from '@/components/common/SectionCard.vue';
import type { BookSummary, CategoryTreeNode } from '@/types';
import { flattenCategories, formatMoney } from '@/utils/format';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const submitting = ref(false);
const categories = ref<CategoryTreeNode[]>([]);
const list = ref<BookSummary[]>([]);
const total = ref(0);
const pageNo = ref(Number(route.query.pageNo || 1));
const pageSize = ref(12);
const form = ref({
  keyword: String(route.query.keyword || ''),
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : undefined as number | undefined,
});

const flatCategories = computed(() => flattenCategories(categories.value));

async function loadCategories() {
  categories.value = await catalogApi.categoriesTree();
}

async function loadBooks() {
  loading.value = true;
  try {
    const data = await catalogApi.books({
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      keyword: form.value.keyword || undefined,
      categoryId: form.value.categoryId,
    });
    list.value = data.records;
    total.value = data.total;
  } finally {
    loading.value = false;
  }
}

async function updateRoute(resetPage = false) {
  if (resetPage) {
    pageNo.value = 1;
  }
  await router.replace({
    path: '/books',
    query: {
      keyword: form.value.keyword || undefined,
      categoryId: form.value.categoryId || undefined,
      pageNo: pageNo.value,
    },
  });
}

async function search() {
  await updateRoute(true);
}

function resetFilters() {
  form.value.keyword = '';
  form.value.categoryId = undefined;
  search();
}

async function handlePageChange(page: number) {
  pageNo.value = page;
  await updateRoute(false);
}

async function addToCart(book: BookSummary) {
  const { tradeApi } = await import('@/api/trade');
  submitting.value = true;
  try {
    await tradeApi.addCartItem({ bookId: book.id, quantity: 1 });
    ElMessage.success('已加入购物车');
  } finally {
    submitting.value = false;
  }
}

watch(
  () => route.query,
  async (query) => {
    form.value.keyword = String(query.keyword || '');
    form.value.categoryId = query.categoryId ? Number(query.categoryId) : undefined;
    pageNo.value = Number(query.pageNo || 1);
    await loadBooks();
  },
  { deep: true },
);

onMounted(async () => {
  await loadCategories();
  await loadBooks();
});
</script>

<template>
  <div style="display: grid; gap: 24px">
    <PageHeader title="分类筛选" description="按分类、关键词快速检索图书，支持直达详情与购物车。" extra="Catalog Explorer" />

    <div class="responsive-grid" style="grid-template-columns: 300px minmax(0, 1fr)">
      <SectionCard title="筛选条件" description="组合分类与搜索词定位目标图书。">
        <el-form label-position="top">
          <el-form-item label="关键词">
            <el-input v-model="form.keyword" placeholder="书名 / 作者 / ISBN" clearable @keyup.enter="search" />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="form.categoryId" placeholder="全部分类" clearable style="width: 100%">
              <el-option v-for="item in flatCategories" :key="item.id" :label="`${'　'.repeat(item.level)}${item.name}`" :value="item.id" />
            </el-select>
          </el-form-item>
          <div style="display: grid; gap: 12px">
            <el-button type="primary" round @click="search">应用筛选</el-button>
            <el-button round @click="resetFilters">重置</el-button>
          </div>
        </el-form>
      </SectionCard>

      <SectionCard :title="`图书列表（${total}）`" description="现代化卡片视图，适配前台商品浏览。">
        <div v-if="list.length" v-loading="loading" class="book-grid">
          <article v-for="book in list" :key="book.id" class="section-card" style="padding: 16px; display: grid; gap: 14px">
            <div class="cover-placeholder" style="aspect-ratio: 4/5; border-radius: 20px; overflow: hidden">
              <img v-if="book.coverUrl" :src="book.coverUrl" :alt="book.name" style="width: 100%; height: 100%; object-fit: cover" />
              <span v-else>BOOK</span>
            </div>
            <div>
              <div style="font-size: 18px; font-weight: 700">{{ book.name }}</div>
              <div style="margin-top: 6px; color: var(--text-secondary)">{{ book.author }}</div>
              <div style="margin-top: 8px; color: var(--text-secondary)">{{ book.category?.name || '未分类' }}</div>
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px">
              <span class="price-text" style="font-size: 22px">{{ formatMoney(book.price) }}</span>
              <div style="display: flex; gap: 8px">
                <el-button round @click="router.push(`/books/${book.id}`)">详情</el-button>
                <el-button type="primary" round :loading="submitting" @click="addToCart(book)">加入购物车</el-button>
              </div>
            </div>
          </article>
        </div>
        <EmptyState v-else title="没有匹配结果" description="尝试更换关键词或切换分类重新筛选。" button-text="查看全部" @action="resetFilters" />
        <div style="display: flex; justify-content: flex-end; margin-top: 20px">
          <el-pagination
            v-model:current-page="pageNo"
            v-model:page-size="pageSize"
            layout="prev, pager, next"
            :total="total"
            @current-change="handlePageChange"
          />
        </div>
      </SectionCard>
    </div>
  </div>
</template>
