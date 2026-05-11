import { createRouter, createWebHistory } from 'vue-router';

import { useAuthStore } from '@/stores/auth';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/layouts/ShopLayout.vue'),
      children: [
        { path: '', name: 'home', component: () => import('@/views/shop/HomeView.vue') },
        { path: 'books', name: 'books', component: () => import('@/views/shop/BooksView.vue') },
        { path: 'books/:id', name: 'book-detail', component: () => import('@/views/shop/BookDetailView.vue'), props: true },
        { path: 'cart', name: 'cart', component: () => import('@/views/shop/CartView.vue'), meta: { requiresAuth: true } },
        { path: 'orders', name: 'orders', component: () => import('@/views/shop/OrdersView.vue'), meta: { requiresAuth: true } },
      ],
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/CustomerAuthView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/admin/login',
      name: 'admin-login',
      component: () => import('@/views/auth/AdminLoginView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true, adminOnly: true },
      children: [
        { path: '', redirect: '/admin/dashboard' },
        { path: 'dashboard', name: 'admin-dashboard', component: () => import('@/views/admin/DashboardView.vue') },
        { path: 'categories', name: 'admin-categories', component: () => import('@/views/admin/CategoriesView.vue') },
        { path: 'books', name: 'admin-books', component: () => import('@/views/admin/BooksManageView.vue') },
        { path: 'orders', name: 'admin-orders', component: () => import('@/views/admin/OrdersManageView.vue') },
        { path: 'users', name: 'admin-users', component: () => import('@/views/admin/UsersManageView.vue') },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],
  scrollBehavior () {
    return new Promise((resolve) => {
      setTimeout(() => resolve({ top: 0, behavior: 'smooth' }), 0);
    });
  },
});

router.beforeEach(async (to) => {
  const authStore = useAuthStore();
  if (!authStore.initialized) {
    await authStore.bootstrap();
  }
  const requiresAuth = to.matched.some((item) => item.meta.requiresAuth);
  const adminOnly = to.matched.some((item) => item.meta.adminOnly);
  const guestOnly = to.matched.some((item) => item.meta.guestOnly);

  if (requiresAuth && !authStore.isAuthenticated) {
    return {
      path: to.path.startsWith('/admin') ? '/admin/login' : '/login',
      query: { redirect: to.fullPath },
    };
  }

  if (adminOnly && !authStore.isAdmin) {
    return '/';
  }

  if (guestOnly && authStore.isAuthenticated) {
    if (to.path.startsWith('/admin') && authStore.isAdmin) {
      return '/admin/dashboard';
    }
    return '/';
  }

  return true;
});

export default router;
