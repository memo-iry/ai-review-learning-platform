<script setup>
defineProps({
  title: { type: String, required: true },
  description: { type: String, default: '' },
})
</script>

<template>
  <div class="empty-state">
    <h1 class="empty-state__title">{{ title }}</h1>
    <p v-if="description" class="empty-state__description">{{ description }}</p>

    <div v-if="$slots.action" class="empty-state__action">
      <slot name="action" />
    </div>
  </div>
</template>

<style scoped>
.empty-state {
  padding: var(--space-8) var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  text-align: center;
}

.empty-state__title {
  margin: 0;
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: 400;
}

.empty-state__description {
  margin: var(--space-3) 0 0;
  color: var(--color-text-muted);
  font-size: var(--text-md);
}

.empty-state__action { margin-top: var(--space-6); }
</style>
