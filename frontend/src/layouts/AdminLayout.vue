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
      <aside class="glass-card" style="padding: 18px; min-height: calc(100vh - 40px); position: sticky; top: 20px">
        <div style="display: flex; align-items: center; gap: 12px; padding: 6px 8px 18px">
          <div class="cover-placeholder" style="width: 48px; height: 48px; border-radius: 18px; font-size: 20px; font-weight: 700">管</div>
          <div>
            <div style="font-size: 18px; font-weight: 800">寥若晨星书城后台</div>
            <div style="font-size: 12px; color: var(--text-secondary)">运营中心</div>
          </div>
        </div>
        <el-menu :default-active="activeMenu" router style="border-right: none; background: transparent">
          <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </el-menu>
        <div class="section-card" style="padding: 16px; margin-top: 18px">
          <div style="font-weight: 700">{{ authStore.user?.displayName }}</div>
          <div style="margin-top: 6px; color: var(--text-secondary)">{{ authStore.user?.role?.name }}</div>
          <el-button type="danger" plain round style="width: 100%; margin-top: 16px" @click="logout">退出登录</el-button>
        </div>
      </aside>

      <div style="display: grid; gap: 20px; align-content: start">
        <router-view />
      </div>
    </div>
  </div>
</template>
