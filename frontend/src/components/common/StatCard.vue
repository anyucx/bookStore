<script setup lang="ts">
defineProps<{
  title: string;
  value: string | number;
  description?: string;
  icon?: string;
}>();
</script>

<template>
  <div class="section-card surface-muted stat-card-body">
    <div class="stat-card-layout">
      <div>
        <p class="text-stat-label">{{ title }}</p>
        <div class="text-stat-value">{{ value }}</div>
        <p class="text-stat-label" style="margin-top: 12px">{{ description }}</p>
      </div>
      <div class="cover-placeholder cover-icon" style="font-size: 22px; font-weight: 700">
        {{ icon }}
      </div>
    </div>
  </div>
</template>
