<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const activeMenu = computed(() => route.path);

const menuItems = [
  { path: '/admin/dashboard', title: '仪表盘', icon: 'DataBoard' },
  { path: '/admin/categories', title: '分类管理', icon: 'CollectionTag' },
  { path: '/admin/books', title: '图书管理', icon: 'Reading' },
  { path: '/admin/orders', title: '订单管理', icon: 'Tickets' },
  { path: '/admin/users', title: '用户管理', icon: 'User' },
];

async function logout() {
  await authStore.logout();
  ElMessage.success('管理员已退出');
  router.push('/admin/login');
}
</script>

<template>
  <div style="padding: 20px 0 28px">
    <div class="page-shell admin-two-col">
      <aside class="glass-card admin-sidebar" style="padding: 18px; min-height: calc(100vh - 40px)">
        <div class="flex-align-center-sm" style="padding: 6px 8px 18px">
          <div class="cover-placeholder cover-icon" style="font-size: 20px; font-weight: 700">管</div>
          <div>
            <div class="text-xl-bold">寥若晨星书城后台</div>
            <div class="text-xs text-muted">运营中心</div>
          </div>
        </div>
        <el-menu :default-active="activeMenu" router style="border-right: none; background: transparent">
          <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </el-menu>
        <div class="section-card" style="padding: 16px; margin-top: 18px">
          <div class="text-lg-bold">{{ authStore.user?.displayName }}</div>
          <div class="text-muted" style="margin-top: 6px">{{ authStore.user?.role?.name }}</div>
          <el-button class="w-full" round style="margin-top: 16px" @click="router.push('/')">访问前台</el-button>
          <el-button class="w-full" type="danger" plain round style="margin-top: 12px" @click="logout">退出登录</el-button>
        </div>
      </aside>

      <div class="admin-page-gap" style="align-content: start">
        <router-view />
      </div>
    </div>
  </div>
</template>
