<script setup lang="ts">
defineProps<{
  title: string;
  description?: string;
}>();
</script>

<template>
  <section class="section-card" style="padding: 24px">
    <div style="display: flex; justify-content: space-between; gap: 12px; align-items: center; margin-bottom: 20px">
      <div>
        <h3 style="margin: 0; font-size: 20px">{{ title }}</h3>
        <p v-if="description" style="margin: 8px 0 0; color: var(--text-secondary)">{{ description }}</p>
      </div>
      <slot name="extra" />
    </div>
    <slot />
  </section>
</template>
