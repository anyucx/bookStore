<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';

import AuthLayout from '@/layouts/AuthLayout.vue';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const activeTab = ref('login');
const loading = ref(false);

const loginForm = reactive({
  username: '',
  password: '',
});

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  displayName: '',
  phone: '',
  email: '',
});

async function submitLogin() {
  loading.value = true;
  try {
    await authStore.login(loginForm);
    ElMessage.success('登录成功');
    router.push(String(route.query.redirect || '/'));
  } finally {
    loading.value = false;
  }
}

async function submitRegister() {
  loading.value = true;
  try {
    await authStore.register(registerForm);
    ElMessage.success('注册成功');
    router.push('/');
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <AuthLayout title="连接读者与好书的现代化商城" description="登录后可查看个人购物车、创建订单并持续跟踪订单状态。" badge="Customer Portal">
    <div class="auth-form-gap">
      <div>
        <el-button text @click="router.push('/')">返回商城</el-button>
        <h2 class="auth-title">登录 / 注册</h2>
        <p class="page-desc">统一对接 /api/auth/register、/api/auth/login、/api/auth/logout、/api/auth/me。</p>
      </div>

      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form label-position="top" @submit.prevent="submitLogin">
            <el-form-item label="用户名"><el-input v-model="loginForm.username" placeholder="请输入用户名" /></el-form-item>
            <el-form-item label="密码"><el-input v-model="loginForm.password" show-password placeholder="请输入密码" /></el-form-item>
            <el-button type="primary" round size="large" class="w-full" :loading="loading" @click="submitLogin">立即登录</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form label-position="top" @submit.prevent="submitRegister">
            <el-form-item label="用户名"><el-input v-model="registerForm.username" placeholder="请输入用户名" /></el-form-item>
            <el-form-item label="昵称"><el-input v-model="registerForm.displayName" placeholder="请输入昵称" /></el-form-item>
            <el-form-item label="手机号"><el-input v-model="registerForm.phone" placeholder="选填" /></el-form-item>
            <el-form-item label="邮箱"><el-input v-model="registerForm.email" placeholder="选填" /></el-form-item>
            <el-form-item label="密码"><el-input v-model="registerForm.password" show-password placeholder="请输入密码" /></el-form-item>
            <el-form-item label="确认密码"><el-input v-model="registerForm.confirmPassword" show-password placeholder="请再次输入密码" /></el-form-item>
            <el-button type="primary" round size="large" class="w-full" :loading="loading" @click="submitRegister">创建账号</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </AuthLayout>
</template>
