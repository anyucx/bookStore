<script setup lang="ts">
import { computed } from 'vue';

import { statusLabel, statusType } from '@/utils/format';

const props = defineProps<{
  status: string;
}>();

const effect = computed(() => statusType(props.status));
const label = computed(() => statusLabel(props.status));
</script>

<template>
  <el-tag :type="effect" round effect="light">{{ label }}</el-tag>
</template>
