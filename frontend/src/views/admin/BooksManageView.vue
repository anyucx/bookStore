<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { UploadProps } from 'element-plus';

import { adminApi } from '@/api/admin';
import PageHeader from '@/components/common/PageHeader.vue';
import SectionCard from '@/components/common/SectionCard.vue';
import type { BookSummary, CategoryTreeNode } from '@/types';
import { flattenCategories, formatMoney } from '@/utils/format';

const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const uploadLoading = ref(false);
const categories = ref<CategoryTreeNode[]>([]);
const books = ref<BookSummary[]>([]);
const total = ref(0);
const query = reactive({
  keyword: '',
  categoryId: undefined as number | undefined,
  pageNo: 1,
  pageSize: 10,
});
const form = reactive<Partial<BookSummary>>({
  id: undefined,
  categoryId: undefined,
  name: '',
  author: '',
  isbn: '',
  price: 0,
  stock: 0,
  coverUrl: '',
  description: '',
  status: 1,
});

const flatCategories = computed(() => flattenCategories(categories.value));

async function loadCategories() {
  categories.value = await adminApi.categories();
}

async function loadBooks() {
  loading.value = true;
  try {
    const data = await adminApi.books(query);
    books.value = data.records;
    total.value = data.total;
  } finally {
    loading.value = false;
  }
}

function openDialog(row?: BookSummary) {
  if (row) {
    Object.assign(form, { ...row });
  } else {
    Object.assign(form, {
      id: undefined,
      categoryId: undefined,
      name: '',
      author: '',
      isbn: '',
      price: 0,
      stock: 0,
      coverUrl: '',
      description: '',
      status: 1,
    });
  }
  dialogVisible.value = true;
}

async function submit() {
  saving.value = true;
  try {
    if (form.id) {
      await adminApi.updateBook(form);
    } else {
      await adminApi.createBook(form);
    }
    ElMessage.success('图书已保存');
    dialogVisible.value = false;
    await loadBooks();
  } finally {
    saving.value = false;
  }
}

async function remove(row: BookSummary) {
  await adminApi.deleteBook(row.id);
  ElMessage.success('图书已删除');
  await loadBooks();
}

const handleUpload: UploadProps['httpRequest'] = async (options) => {
  uploadLoading.value = true;
  try {
    const result = await adminApi.upload(options.file as File);
    form.coverUrl = result.accessUrl;
    ElMessage.success('封面上传成功');
    options.onSuccess?.(result);
  } catch (error) {
    options.onError?.(error as Error);
  } finally {
    uploadLoading.value = false;
  }
};

onMounted(async () => {
  await loadCategories();
  await loadBooks();
});
</script>

<template>
  <div style="display: grid; gap: 20px">
    <PageHeader title="图书管理" description="分页管理图书信息，并支持后台封面上传。" extra="Book Management">
      <template #actions>
        <el-button type="primary" round @click="openDialog()">新增图书</el-button>
      </template>
    </PageHeader>

    <SectionCard title="图书列表" description="覆盖 /api/admin/books 与 /api/admin/files/upload。">
      <div class="toolbar-row" style="margin-bottom: 18px">
        <div style="display: flex; gap: 12px; flex-wrap: wrap">
          <el-input v-model="query.keyword" placeholder="搜索书名 / 作者 / ISBN" clearable style="width: 280px" />
          <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 220px">
            <el-option v-for="item in flatCategories" :key="item.id" :value="item.id" :label="`${'　'.repeat(item.level)}${item.name}`" />
          </el-select>
        </div>
        <div style="display: flex; gap: 12px">
          <el-button round @click="query.pageNo = 1; loadBooks()">筛选</el-button>
          <el-button round @click="query.keyword = ''; query.categoryId = undefined; query.pageNo = 1; loadBooks()">重置</el-button>
        </div>
      </div>

      <el-table :data="books" stripe v-loading="loading">
        <el-table-column label="封面" width="96">
          <template #default="scope">
            <div class="cover-placeholder" style="width: 52px; height: 68px; border-radius: 12px; overflow: hidden">
              <img v-if="scope.row.coverUrl" :src="scope.row.coverUrl" :alt="scope.row.name" style="width: 100%; height: 100%; object-fit: cover" />
              <span>图</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="图书名称" min-width="220" />
        <el-table-column prop="author" label="作者" min-width="140" />
        <el-table-column label="分类" min-width="140"><template #default="scope">{{ scope.row.category?.name || '--' }}</template></el-table-column>
        <el-table-column label="价格" width="120"><template #default="scope">{{ formatMoney(scope.row.price) }}</template></el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="scope"><el-tag :type="scope.row.status === 1 ? 'success' : 'info'" round>{{ scope.row.status === 1 ? '上架' : '下架' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button text type="primary" @click="openDialog(scope.row)">编辑</el-button>
            <el-button text type="danger" @click="remove(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display: flex; justify-content: flex-end; margin-top: 18px">
        <el-pagination v-model:current-page="query.pageNo" v-model:page-size="query.pageSize" layout="prev, pager, next" :total="total" @current-change="loadBooks" />
      </div>
    </SectionCard>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑图书' : '新增图书'" width="720px">
      <el-form label-position="top">
        <div class="responsive-grid" style="grid-template-columns: repeat(2, minmax(0, 1fr))">
          <el-form-item label="图书名称"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="作者"><el-input v-model="form.author" /></el-form-item>
          <el-form-item label="分类">
            <el-select v-model="form.categoryId" style="width: 100%" placeholder="请选择分类">
              <el-option v-for="item in flatCategories" :key="item.id" :value="item.id" :label="`${'　'.repeat(item.level)}${item.name}`" />
            </el-select>
          </el-form-item>
          <el-form-item label="ISBN"><el-input v-model="form.isbn" /></el-form-item>
          <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" /></el-form-item>
          <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" style="width: 100%" /></el-form-item>
        </div>
        <el-form-item label="封面地址"><el-input v-model="form.coverUrl" placeholder="可手动填写或上传文件" /></el-form-item>
        <el-upload :show-file-list="false" :http-request="handleUpload">
          <el-button round :loading="uploadLoading">上传封面</el-button>
        </el-upload>
        <el-form-item label="图书简介" style="margin-top: 18px"><el-input v-model="form.description" type="textarea" :rows="5" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" round :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
