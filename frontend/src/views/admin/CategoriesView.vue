<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';

import { adminApi } from '@/api/admin';
import PageHeader from '@/components/common/PageHeader.vue';
import SectionCard from '@/components/common/SectionCard.vue';
import StatusTag from '@/components/common/StatusTag.vue';
import type { CategoryTreeNode } from '@/types';
import { flattenCategories } from '@/utils/format';

const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const categories = ref<CategoryTreeNode[]>([]);
const form = reactive({
  id: undefined as number | undefined,
  parentId: 0,
  name: '',
  sort: 0,
  status: 1,
});

const flatCategories = computed(() => flattenCategories(categories.value));

async function loadData() {
  loading.value = true;
  try {
    categories.value = await adminApi.categories();
  } finally {
    loading.value = false;
  }
}

function openDialog(row?: CategoryTreeNode) {
  if (row) {
    Object.assign(form, {
      id: row.id,
      parentId: row.parentId,
      name: row.name,
      sort: row.sort,
      status: row.status,
    });
  } else {
    Object.assign(form, { id: undefined, parentId: 0, name: '', sort: 0, status: 1 });
  }
  dialogVisible.value = true;
}

async function submit() {
  saving.value = true;
  try {
    if (form.id) {
      await adminApi.updateCategory(form);
    } else {
      await adminApi.createCategory(form);
    }
    ElMessage.success('分类已保存');
    dialogVisible.value = false;
    await loadData();
  } finally {
    saving.value = false;
  }
}

async function remove(row: CategoryTreeNode) {
  await adminApi.deleteCategory(row.id);
  ElMessage.success('分类已删除');
  await loadData();
}

onMounted(loadData);
</script>

<template>
  <div class="admin-page-gap">
    <PageHeader title="分类管理" description="树形分类列表与新增、编辑、删除操作。" extra="Category Management">
      <template #actions>
        <el-button type="primary" round @click="openDialog()">新增分类</el-button>
      </template>
    </PageHeader>

    <SectionCard title="分类树" description="对接 /api/admin/categories 与前台分类层级。">
      <el-table :data="flatCategories" row-key="id" stripe v-loading="loading">
        <el-table-column label="分类名称" min-width="240">
          <template #default="scope">
            <span>{{ ' '.repeat(scope.row.level) }}{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <StatusTag kind="category" :status="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button text type="primary" @click="openDialog(scope.row)">编辑</el-button>
            <el-button text type="danger" @click="remove(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </SectionCard>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="520px">
      <el-form label-position="top">
        <el-form-item label="父分类">
          <el-select v-model="form.parentId" class="w-full">
            <el-option :value="0" label="顶级分类" />
            <el-option v-for="item in flatCategories" :key="item.id" :value="item.id" :label="`${'　'.repeat(item.level)}${item.name}`" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类名称"><el-input v-model="form.name" placeholder="请输入分类名称" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" class="w-full" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
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
