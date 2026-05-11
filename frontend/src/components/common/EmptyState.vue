<script setup lang="ts">
defineProps<{
  title: string;
  description?: string;
  buttonText?: string;
}>();

const emit = defineEmits<{
  action: [];
}>();
</script>

<template>
  <div class="section-card empty-card">
    <el-empty :description="description || title">
      <template #image>
        <div class="cover-placeholder cover-empty">
          <el-icon :size="42"><Box /></el-icon>
        </div>
      </template>
      <template #description>
        <div>
          <div class="text-empty-title">{{ title }}</div>
          <p class="empty-copy">{{ description }}</p>
        </div>
      </template>
      <el-button v-if="buttonText" type="primary" round @click="emit('action')">{{ buttonText }}</el-button>
    </el-empty>
  </div>
</template>
