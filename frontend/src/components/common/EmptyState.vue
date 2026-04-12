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
  <div class="section-card" style="padding: 48px 24px; text-align: center">
    <el-empty :description="description || title">
      <template #image>
        <div class="cover-placeholder" style="width: 92px; height: 92px; margin: 0 auto; border-radius: 28px">
          <el-icon :size="42"><Box /></el-icon>
        </div>
      </template>
      <template #description>
        <div>
          <div style="font-size: 18px; font-weight: 700; color: var(--text)">{{ title }}</div>
          <p class="empty-copy">{{ description }}</p>
        </div>
      </template>
      <el-button v-if="buttonText" type="primary" round @click="emit('action')">{{ buttonText }}</el-button>
    </el-empty>
  </div>
</template>
