<script setup>
defineProps({
  title: { type: String, required: true },
})
</script>

<template>
  <section class="app-section">
    <div class="app-section__head">
      <h2 class="app-section__title">{{ title }}</h2>
      <slot name="action" />
    </div>

    <div class="app-section__body">
      <slot />
    </div>
  </section>
</template>

<style scoped>
.app-section + .app-section { margin-top: var(--space-8); }

.app-section__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--space-4);
  padding-bottom: var(--space-2);
  border-bottom: 1px solid var(--color-border);
}

.app-section__title {
  margin: 0;
  color: var(--color-text-muted);
  font-size: var(--text-xs);
  font-weight: 500;
}

.app-section__body { margin-top: var(--space-5); }
</style>
