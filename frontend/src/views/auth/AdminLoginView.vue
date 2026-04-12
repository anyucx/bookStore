<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';

import AuthLayout from '@/layouts/AuthLayout.vue';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const loading = ref(false);
const form = reactive({
  username: '',
  password: '',
});

async function submit() {
  loading.value = true;
  try {
    await authStore.adminLogin(form);
    ElMessage.success('后台登录成功');
    router.push(String(route.query.redirect || '/admin/dashboard'));
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <AuthLayout title="书城后台运营中心" description="覆盖仪表盘、分类管理、图书管理、订单管理与用户管理。" badge="Admin Portal">
    <div style="display: grid; gap: 20px">
      <div>
        <el-button text @click="router.push('/')">返回商城首页</el-button>
        <h2 style="margin: 8px 0 0; font-size: 30px">后台登录</h2>
        <p class="page-desc">管理员账号将通过 /api/admin/auth/login 验证身份。</p>
      </div>

      <el-form label-position="top">
        <el-form-item label="管理员用户名"><el-input v-model="form.username" placeholder="请输入管理员用户名" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" show-password placeholder="请输入密码" /></el-form-item>
        <el-button type="primary" round size="large" style="width: 100%" :loading="loading" @click="submit">进入后台</el-button>
      </el-form>
    </div>
  </AuthLayout>
</template>
