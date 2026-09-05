<script setup>
import BaseCard from '@/components/common/BaseCard.vue'

defineProps({
  title: { type: String, required: true },
  body: { type: String, default: '' },

  tone: { type: String, default: 'default' },
})
</script>

<template>
  <BaseCard :tone="tone" padding="md" class="info-panel">
    <h3 class="info-panel__title">{{ title }}</h3>
    <slot>
      <p class="info-panel__body">{{ body }}</p>
    </slot>
  </BaseCard>
</template>

<style scoped>
.info-panel__title {
  margin: 0 0 var(--space-3);
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: 400;
}

.info-panel__body {
  margin: 0;
  color: var(--color-text-muted);
  font-size: var(--text-md);
  line-height: 1.75;
  white-space: pre-line;
}
</style>
