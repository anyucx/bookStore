import { computed, ref } from 'vue';
import { defineStore } from 'pinia';

import { authApi, type LoginPayload, type RegisterPayload } from '@/api/auth';
import type { LoginResult, UserInfo } from '@/types';
import { clearStoredUser, clearToken, getStoredUser, getToken, setStoredUser, setToken } from '@/utils/storage';

export const useAuthStore = defineStore('auth', () => {
  const token = ref(getToken());
  const user = ref<UserInfo | null>(getStoredUser());
  const initialized = ref(false);

  const isAuthenticated = computed(() => Boolean(token.value && user.value));
  const isAdmin = computed(() => user.value?.role?.code === 'ADMIN');

  function setSession(payload: LoginResult | { token: string; user: UserInfo }) {
    token.value = payload.token;
    user.value = payload.user;
    setToken(payload.token);
    setStoredUser(payload.user);
  }

  function clearSession() {
    token.value = '';
    user.value = null;
    clearToken();
    clearStoredUser();
  }

  async function bootstrap() {
    if (!token.value) {
      initialized.value = true;
      return;
    }
    try {
      user.value = await authApi.me();
      setStoredUser(user.value);
    } catch {
      clearSession();
    } finally {
      initialized.value = true;
    }
  }

  async function login(payload: LoginPayload) {
    const result = await authApi.login(payload);
    setSession(result);
    return result;
  }

  async function register(payload: RegisterPayload) {
    const result = await authApi.register(payload);
    setSession(result);
    return result;
  }

  async function adminLogin(payload: LoginPayload) {
    const result = await authApi.adminLogin(payload);
    setSession(result);
    return result;
  }

  async function logout() {
    try {
      await authApi.logout();
    } finally {
      clearSession();
    }
  }

  return {
    token,
    user,
    initialized,
    isAuthenticated,
    isAdmin,
    bootstrap,
    login,
    register,
    adminLogin,
    logout,
    setSession,
    clearSession,
  };
});
