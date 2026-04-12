<script setup lang="ts">
defineProps<{
  title: string;
  value: string | number;
  description?: string;
  icon?: string;
}>();
</script>

<template>
  <div class="section-card surface-muted" style="padding: 24px; min-height: 148px">
    <div style="display: flex; justify-content: space-between; gap: 12px; align-items: flex-start">
      <div>
        <p style="margin: 0; color: var(--text-secondary)">{{ title }}</p>
        <div style="margin-top: 12px; font-size: 34px; font-weight: 700; line-height: 1.1">{{ value }}</div>
        <p style="margin: 12px 0 0; color: var(--text-secondary)">{{ description }}</p>
      </div>
      <div class="cover-placeholder" style="width: 52px; height: 52px; border-radius: 18px; font-size: 22px; font-weight: 700">
        {{ icon }}
      </div>
    </div>
  </div>
</template>
