<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';

import { tradeApi, type CreateOrderPayload } from '@/api/trade';
import EmptyState from '@/components/common/EmptyState.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import SectionCard from '@/components/common/SectionCard.vue';
import type { CartItem } from '@/types';
import { formatMoney } from '@/utils/format';

const router = useRouter();
const loading = ref(false);
const submitting = ref(false);
const dialogVisible = ref(false);
const items = ref<CartItem[]>([]);
const orderForm = reactive<CreateOrderPayload>({
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  remark: '',
});

const totalAmount = computed(() => items.value.reduce((sum, item) => sum + Number(item.subtotal || 0), 0));
const totalQuantity = computed(() => items.value.reduce((sum, item) => sum + Number(item.quantity || 0), 0));

async function loadCart() {
  loading.value = true;
  try {
    items.value = await tradeApi.cartItems();
  } finally {
    loading.value = false;
  }
}

async function changeQuantity(item: CartItem, quantity: number | undefined) {
  if (!quantity || quantity < 1) {
    return;
  }
  await tradeApi.updateCartItem({ id: item.id, quantity });
  ElMessage.success('购物车已更新');
  await loadCart();
}

async function removeItem(item: CartItem) {
  await tradeApi.deleteCartItem(item.id);
  ElMessage.success('已移除图书');
  await loadCart();
}

async function submitOrder() {
  submitting.value = true;
  try {
    const order = await tradeApi.createOrder(orderForm);
    ElMessage.success(`下单成功，订单号 ${order.orderNo}`);
    dialogVisible.value = false;
    Object.assign(orderForm, { receiverName: '', receiverPhone: '', receiverAddress: '', remark: '' });
    await loadCart();
    router.push('/orders');
  } finally {
    submitting.value = false;
  }
}

onMounted(loadCart);
</script>

<template>
  <div class="page-gap">
    <PageHeader title="购物车" description="支持数量调整、删除商品和订单创建。" extra="Cart & Checkout">
      <template #actions>
        <el-button round @click="router.push('/books')">继续购书</el-button>
      </template>
    </PageHeader>

    <div class="responsive-grid" style="grid-template-columns: minmax(0, 1fr) 340px">
      <SectionCard title="待结算图书" description="已接入 /api/cart/items 增删改查。">
        <div v-if="items.length" v-loading="loading" class="grid-stack-md">
          <div v-for="item in items" :key="item.id" class="order-item">
            <div class="cover-placeholder cover-item">
              <img v-if="item.book.coverUrl" :src="item.book.coverUrl" :alt="item.book.name" style="width: 100%; height: 100%; object-fit: cover" />
              <span>BOOK</span>
            </div>
            <div class="grid-stack-sm" style="flex: 1">
              <div>
                <div class="text-lg-bold">{{ item.book.name }}</div>
                <div class="text-muted" style="margin-top: 6px">{{ item.book.author }}</div>
              </div>
              <div class="flex-between-wrap-sm">
                <el-input-number :model-value="item.quantity" :min="1" :max="item.book.stock || 1" @change="(val) => changeQuantity(item, Number(val))" />
                <span class="price-text price-sm">{{ formatMoney(item.subtotal) }}</span>
              </div>
              <div class="flex-between">
                <span class="text-muted">库存 {{ item.book.stock }}</span>
                <el-button text type="danger" @click="removeItem(item)">删除</el-button>
              </div>
            </div>
          </div>
        </div>
        <EmptyState v-else title="购物车还是空的" description="从图书列表加入想购买的图书后再来这里结算。" button-text="去选书" @action="router.push('/books')" />
      </SectionCard>

      <SectionCard title="结算摘要" description="创建订单时会提交收货信息。">
        <div class="info-list">
          <div class="info-row"><span>商品数量</span><strong>{{ totalQuantity }}</strong></div>
          <div class="info-row"><span>应付金额</span><strong>{{ formatMoney(totalAmount) }}</strong></div>
        </div>
        <el-divider />
        <el-button type="primary" size="large" round class="w-full" :disabled="!items.length" @click="dialogVisible = true">提交订单</el-button>
      </SectionCard>
    </div>

    <el-dialog v-model="dialogVisible" title="填写收货信息" width="520px">
      <el-form label-position="top">
        <el-form-item label="收货人"><el-input v-model="orderForm.receiverName" placeholder="请输入收货人" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="orderForm.receiverPhone" placeholder="请输入手机号" /></el-form-item>
        <el-form-item label="收货地址"><el-input v-model="orderForm.receiverAddress" type="textarea" :rows="3" placeholder="请输入详细地址" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="orderForm.remark" type="textarea" :rows="2" placeholder="选填" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" round :loading="submitting" @click="submitOrder">确认下单</el-button>
      </template>
    </el-dialog>
  </div>
</template>
