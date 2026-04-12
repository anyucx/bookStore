import http, { unwrap } from './http';
import type { BookListData, BookSummary, CategoryTreeNode, DashboardData, OrderInfo, UploadResult, UserInfo } from '@/types';
import { buildCategoryTree } from '@/utils/format';

export const adminApi = {
  dashboard() {
    return unwrap<DashboardData>(http.get('/api/admin/dashboard'));
  },
  async categories() {
    const data = await unwrap<Array<Partial<CategoryTreeNode> & { id: number; parentId?: number; name: string }>>(http.get('/api/admin/categories'));
    return buildCategoryTree(data);
  },
  createCategory(payload: { parentId?: number; name: string; sort?: number; status?: number }) {
    return unwrap(http.post('/api/admin/categories', payload));
  },
  updateCategory(payload: { id: number; parentId?: number; name: string; sort?: number; status?: number }) {
    return unwrap(http.put('/api/admin/categories', payload));
  },
  deleteCategory(id: number) {
    return unwrap<void>(http.delete('/api/admin/categories', { params: { id } }));
  },
  books(params: { categoryId?: number; keyword?: string; pageNo?: number; pageSize?: number }) {
    return unwrap<BookListData>(http.get('/api/admin/books', { params }));
  },
  createBook(payload: Partial<BookSummary>) {
    return unwrap(http.post('/api/admin/books', payload));
  },
  updateBook(payload: Partial<BookSummary>) {
    return unwrap(http.put('/api/admin/books', payload));
  },
  deleteBook(id: number) {
    return unwrap<void>(http.delete('/api/admin/books', { params: { id } }));
  },
  orders(params: { status?: string; keyword?: string }) {
    return unwrap<OrderInfo[]>(http.get('/api/admin/orders', { params }));
  },
  updateOrder(payload: { id: number; status: string }) {
    return unwrap<OrderInfo>(http.put('/api/admin/orders', payload));
  },
  users(params: { keyword?: string; status?: number }) {
    return unwrap<UserInfo[]>(http.get('/api/admin/users', { params }));
  },
  updateUser(payload: { userId: number; displayName?: string; status?: number; roleId?: number }) {
    return unwrap<UserInfo>(http.put('/api/admin/users', payload));
  },
  upload(file: File) {
    const formData = new FormData();
    formData.append('file', file);
    return unwrap<UploadResult>(http.post('/api/admin/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }));
  },
};
