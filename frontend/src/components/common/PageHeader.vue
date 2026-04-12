<script setup lang="ts">
defineProps<{
  title: string;
  description?: string;
  extra?: string;
}>();
</script>

<template>
  <section class="glass-card" style="padding: 28px 28px 24px">
    <div class="toolbar-row">
      <div>
        <p style="margin: 0; color: var(--primary); font-weight: 600">{{ extra }}</p>
        <h1 class="page-title">{{ title }}</h1>
        <p v-if="description" class="page-desc">{{ description }}</p>
      </div>
      <slot name="actions" />
    </div>
  </section>
</template>
