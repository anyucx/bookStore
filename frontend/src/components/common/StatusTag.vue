<script setup lang="ts">
import { computed } from 'vue';

import { entityStatusLabel, entityStatusType, statusLabel, statusType } from '@/utils/format';

const props = withDefaults(defineProps<{
  status: string | number;
  kind?: 'order' | 'book' | 'category' | 'user';
}>(), {
  kind: 'order',
});

const effect = computed(() => {
  if (props.kind !== 'order') return entityStatusType(props.status, props.kind);
  return statusType(String(props.status));
});

const label = computed(() => {
  if (props.kind !== 'order') return entityStatusLabel(props.status, props.kind);
  return statusLabel(String(props.status));
});
</script>

<template>
  <el-tag :type="effect" round effect="light">{{ label }}</el-tag>
</template>
