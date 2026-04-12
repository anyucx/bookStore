<script setup lang="ts">
import { computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';

import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const activeMenu = computed(() => {
  if (route.path.startsWith('/books')) return '/books';
  if (route.path.startsWith('/cart')) return '/cart';
  if (route.path.startsWith('/orders')) return '/orders';
  return '/';
});

async function handleCommand(command: string) {
  if (command === 'logout') {
    await authStore.logout();
    ElMessage.success('已退出登录');
    router.push('/');
    return;
  }
  router.push(command);
}
</script>

<template>
  <div style="padding: 20px 0 40px">
    <header class="page-shell glass-card" style="padding: 16px 18px; position: sticky; top: 12px; z-index: 30">
      <div class="toolbar-row">
        <div style="display: flex; align-items: center; gap: 14px">
          <router-link to="/" style="display: flex; align-items: center; gap: 12px">
            <div class="cover-placeholder" style="width: 46px; height: 46px; border-radius: 16px; font-size: 20px; font-weight: 700">阅</div>
            <div>
              <div style="font-size: 18px; font-weight: 800">寥若晨星书城</div>
              <div style="font-size: 12px; color: var(--text-secondary)">现代化在线图书商城</div>
            </div>
          </router-link>
          <el-menu :default-active="activeMenu" mode="horizontal" router :ellipsis="false" style="border-bottom: none; background: transparent">
            <el-menu-item index="/">首页</el-menu-item>
            <el-menu-item index="/books">分类筛选</el-menu-item>
            <el-menu-item index="/cart">购物车</el-menu-item>
            <el-menu-item index="/orders">订单列表</el-menu-item>
          </el-menu>
        </div>
        <div style="display: flex; gap: 12px; align-items: center">
          <el-button round @click="router.push('/admin/login')">后台入口</el-button>
          <template v-if="authStore.isAuthenticated">
            <el-dropdown @command="handleCommand">
              <div style="display: flex; align-items: center; gap: 10px; cursor: pointer">
                <el-avatar :size="38">{{ authStore.user?.displayName?.slice(0, 1) || '读' }}</el-avatar>
                <div>
                  <div style="font-weight: 700">{{ authStore.user?.displayName }}</div>
                  <div style="font-size: 12px; color: var(--text-secondary)">@{{ authStore.user?.username }}</div>
                </div>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="/orders">我的订单</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" round @click="router.push('/login')">登录 / 注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <main class="page-shell" style="margin-top: 24px">
      <router-view />
    </main>
  </div>
</template>
