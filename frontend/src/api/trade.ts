import http, { unwrap } from './http';
import type { CartItem, OrderInfo } from '@/types';

export interface CreateOrderPayload {
  receiverName: string;
  receiverPhone: string;
  receiverAddress: string;
  remark?: string;
}

export const tradeApi = {
  cartItems() {
    return unwrap<CartItem[]>(http.get('/api/cart/items'));
  },
  addCartItem(payload: { bookId: number; quantity: number }) {
    return unwrap(http.post('/api/cart/items', payload));
  },
  updateCartItem(payload: { id: number; quantity: number }) {
    return unwrap(http.put('/api/cart/items', payload));
  },
  deleteCartItem(id: number) {
    return unwrap<void>(http.delete('/api/cart/items', { params: { id } }));
  },
  orders() {
    return unwrap<OrderInfo[]>(http.get('/api/orders'));
  },
  orderDetail(id: number) {
    return unwrap<OrderInfo>(http.get(`/api/orders/${id}`));
  },
  createOrder(payload: CreateOrderPayload) {
    return unwrap<OrderInfo>(http.post('/api/orders', payload));
  },
  cancelOrder(id: number) {
    return unwrap<OrderInfo>(http.post(`/api/orders/${id}/cancel`));
  },
  confirmOrder(id: number) {
    return unwrap<OrderInfo>(http.post(`/api/orders/${id}/confirm`));
  },
  preparePayment(payload: { orderId: number; payChannel: string }) {
    return unwrap<{
      paymentId: number;
      orderId: number;
      orderNo: string;
      amount: number;
      payChannel: string;
      mockPayUrl: string;
      callbackUrl: string;
    }>(http.post('/api/payments/prepare', payload));
  },
  callbackPayment(payload: { orderId: number; payChannel?: string; transactionNo?: string }) {
    return unwrap<{ orderId: number; orderNo: string; status: string; transactionNo: string }>(
      http.post('/api/payments/callback', payload),
    );
  },
};
