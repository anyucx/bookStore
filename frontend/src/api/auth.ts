import http, { unwrap } from './http';
import type { LoginResult, UserInfo } from '@/types';

export interface RegisterPayload {
  username: string;
  password: string;
  confirmPassword: string;
  displayName: string;
  phone?: string;
  email?: string;
}

export interface LoginPayload {
  username: string;
  password: string;
}

export const authApi = {
  register(payload: RegisterPayload) {
    return unwrap<LoginResult>(http.post('/api/auth/register', payload));
  },
  login(payload: LoginPayload) {
    return unwrap<LoginResult>(http.post('/api/auth/login', payload));
  },
  logout() {
    return unwrap<void>(http.post('/api/auth/logout'));
  },
  me() {
    return unwrap<UserInfo>(http.get('/api/auth/me'));
  },
  adminLogin(payload: LoginPayload) {
    return unwrap<LoginResult>(http.post('/api/admin/auth/login', payload));
  },
};
