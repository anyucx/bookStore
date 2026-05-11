<script setup lang="ts">
defineProps<{
  title: string;
  description: string;
  badge: string;
}>();
</script>

<template>
  <div style="min-height: 100vh; display: grid; place-items: center; padding: 24px">
    <div class="page-shell" style="display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 24px; align-items: stretch">
      <section class="glass-card hero-gradient" style="padding: 40px; display: flex; flex-direction: column; justify-content: space-between; min-height: 620px">
        <div>
          <el-tag round type="primary" effect="dark" style="border: none; background: rgba(255,255,255,0.18)">{{ badge }}</el-tag>
          <h1 style="margin: 22px 0 0; font-size: 42px; line-height: 1.15">{{ title }}</h1>
          <p style="margin: 18px 0 0; line-height: 1.8; color: rgba(255,255,255,0.82)">{{ description }}</p>
        </div>
        <div class="section-card hero-feature-card">
          <div class="grid-stack-sm" style="gap: 14px">
            <div>统一对接图书、分类、购物车、订单与后台运营接口</div>
            <div>内置现代化导航、表单反馈、空态与状态提示</div>
            <div>基于 Vue 3 + TypeScript + Element Plus 构建</div>
          </div>
        </div>
      </section>
      <section class="glass-card" style="padding: 28px; min-height: 620px">
        <slot />
      </section>
    </div>
  </div>
</template>

<style scoped>
@media (max-width: 980px) {
  .page-shell {
    grid-template-columns: 1fr !important;
  }
}
</style>
