export interface ApiResponse<T> {
  success: boolean;
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export interface RoleInfo {
  id: number;
  name: string;
  code: string;
}

export interface UserInfo {
  id: number;
  username: string;
  displayName: string;
  phone?: string;
  email?: string;
  status?: number;
  role?: RoleInfo;
  createdTime?: string;
}

export interface LoginResult {
  token: string;
  tokenType: string;
  expiresInDays: number;
  user: UserInfo;
}

export interface CategoryTreeNode {
  id: number;
  parentId: number;
  name: string;
  sort: number;
  status: number;
  children: CategoryTreeNode[];
}

export interface BookSummary {
  id: number;
  name: string;
  author: string;
  isbn?: string;
  price: number;
  stock: number;
  coverUrl?: string;
  description?: string;
  status: number;
  sales: number;
  createdTime?: string;
  categoryId: number;
  category?: {
    id: number;
    name: string;
  };
}

export interface BookListData {
  pageNo: number;
  pageSize: number;
  total: number;
  records: BookSummary[];
}

export interface CartItem {
  id: number;
  quantity: number;
  selected: number;
  subtotal: number;
  book: {
    id: number;
    name: string;
    author: string;
    coverUrl?: string;
    price: number;
    stock: number;
  };
}

export interface OrderItem {
  id: number;
  bookId: number;
  bookName: string;
  bookAuthor: string;
  coverUrl?: string;
  quantity: number;
  price: number;
  amount: number;
}

export interface OrderInfo {
  id: number;
  orderNo: string;
  status: 'CREATED' | 'PAID' | 'CANCELLED' | 'CONFIRMED' | string;
  totalAmount: number;
  receiverName: string;
  receiverPhone: string;
  receiverAddress: string;
  remark?: string;
  createdTime?: string;
  updatedTime?: string;
  items: OrderItem[];
  user?: UserInfo;
}

export interface DashboardData {
  bookCount: number;
  categoryCount: number;
  userCount: number;
  orderCount: number;
  paymentCount: number;
  totalSales: number;
}

export interface UploadResult {
  id: number;
  originalName: string;
  accessUrl: string;
  sizeBytes?: number;
}
