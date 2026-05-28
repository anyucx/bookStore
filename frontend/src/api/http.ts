import axios from 'axios';
import { ElMessage } from 'element-plus';

import router from '@/router';
import { useAuthStore } from '@/stores/auth';
import type { ApiResponse } from '@/types';
import { getToken } from '@/utils/storage';

const http = axios.create({
  baseURL: '/',
  timeout: 15000,
});

http.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => {
    const data = response.data as ApiResponse<unknown>;
    if (data && data.success === false) {
      if (!document.hidden) {
        ElMessage.error(data.message || '请求失败');
      }
      return Promise.reject(data);
    }
    return response;
  },
  async (error) => {
    const status = error?.response?.status;
    const message = error?.response?.data?.message || error?.message || '网络请求异常';
    const authStore = useAuthStore();
    if (status === 401 || status === 403) {
      if (!document.hidden) {
        authStore.clearSession();
        const current = router.currentRoute.value;
        const target = current.path.startsWith('/admin') ? '/admin/login' : '/login';
        if (current.path !== target) {
          await router.push({ path: target, query: { redirect: current.fullPath } });
        }
      }
      return Promise.reject(error);
    }
    if (!document.hidden) {
      ElMessage.error(message);
    }
    return Promise.reject(error);
  },
);

export async function unwrap<T>(promise: Promise<{ data: ApiResponse<T> }>) {
  const response = await promise;
  return response.data.data;
}

export default http;
