import http, { unwrap } from './http';
import type { BookListData, BookSummary, CategoryTreeNode } from '@/types';

export interface BooksQuery {
  categoryId?: number;
  keyword?: string;
  pageNo?: number;
  pageSize?: number;
}

export const catalogApi = {
  categoriesTree() {
    return unwrap<CategoryTreeNode[]>(http.get('/api/categories/tree'));
  },
  books(params: BooksQuery) {
    return unwrap<BookListData>(http.get('/api/books', { params }));
  },
  bookDetail(id: number | string) {
    return unwrap<BookSummary>(http.get(`/api/books/${id}`));
  },
};
