import type { CategoryTreeNode } from '@/types';

export function formatMoney(value?: number | string | null) {
  const amount = Number(value ?? 0);
  return `¥${amount.toFixed(2)}`;
}

export function formatDate(value?: string) {
  if (!value) {
    return '--';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  const y = date.getFullYear();
  const M = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  const H = String(date.getHours()).padStart(2, '0');
  const m = String(date.getMinutes()).padStart(2, '0');
  const s = String(date.getSeconds()).padStart(2, '0');
  return `${y}-${M}-${d} ${H}:${m}:${s}`;
}

export function buildCategoryTree(items: Array<Partial<CategoryTreeNode> & { id: number; parentId?: number; name: string }>) {
  const nodeMap = new Map<number, CategoryTreeNode>();
  const roots: CategoryTreeNode[] = [];

  items.forEach((item) => {
    nodeMap.set(item.id, {
      id: item.id,
      parentId: Number(item.parentId || 0),
      name: item.name,
      sort: Number(item.sort || 0),
      status: Number(item.status ?? 1),
      children: [],
    });
  });

  nodeMap.forEach((node) => {
    if (!node.parentId || !nodeMap.has(node.parentId)) {
      roots.push(node);
      return;
    }
    nodeMap.get(node.parentId)?.children.push(node);
  });

  return roots;
}

export function flattenCategories(nodes: CategoryTreeNode[], level = 0): Array<CategoryTreeNode & { level: number }> {
  return nodes.flatMap((node) => [
    { ...node, level },
    ...flattenCategories(node.children || [], level + 1),
  ]);
}

export function statusLabel(status: string) {
  const map: Record<string, string> = {
    CREATED: '待支付',
    PAID: '待收货',
    CANCELLED: '已取消',
    CONFIRMED: '已完成',
  };
  return map[status] || status;
}

export function statusType(status: string) {
  const map: Record<string, 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
    CREATED: 'warning',
    PAID: 'primary',
    CANCELLED: 'danger',
    CONFIRMED: 'success',
  };
  return map[status] || 'info';
}

/** 枚举型实体状态映射 (book / category / user)。 */
export function entityStatusLabel(status: number | string, kind: 'book' | 'category' | 'user') {
  const val = Number(status);
  if (kind === 'user') return val === 1 ? '启用' : '禁用';
  if (kind === 'book') return val === 1 ? '上架' : '下架';
  return val === 1 ? '启用' : '停用';
}

export function entityStatusType(status: number | string, kind: 'book' | 'category' | 'user'): 'success' | 'danger' | 'info' {
  const val = Number(status);
  if (kind === 'user') return val === 1 ? 'success' : 'danger';
  return val === 1 ? 'success' : 'info';
}
