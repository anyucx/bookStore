<script setup lang="ts">
defineProps<{
  title: string;
  description?: string;
}>();
</script>

<template>
  <section class="section-card section-card-padded">
    <div class="section-header">
      <div>
        <h3 class="section-title">{{ title }}</h3>
        <p v-if="description" class="section-desc">{{ description }}</p>
      </div>
      <slot name="extra" />
    </div>
    <slot />
  </section>
</template>
