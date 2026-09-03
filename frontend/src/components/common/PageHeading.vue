<script setup>
defineProps({
  eyebrow: { type: String, default: '' },
  title: { type: String, required: true },
  description: { type: String, default: '' },
})
</script>

<template>
  <header class="page-heading">
    <div class="page-heading__text">
      <p v-if="eyebrow" class="page-heading__eyebrow">{{ eyebrow }}</p>
      <h1 class="page-heading__title">{{ title }}</h1>
      <p v-if="description" class="page-heading__description">{{ description }}</p>
    </div>

    <div v-if="$slots.actions" class="page-heading__actions">
      <slot name="actions" />
    </div>
  </header>
</template>

<style scoped>
.page-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-5);
  margin-bottom: var(--space-7);
}

.page-heading__eyebrow {
  margin: 0 0 var(--space-2);
  color: var(--color-accent);
  font-size: var(--text-xs);
  font-weight: 700;
}

.page-heading__title {
  margin: 0;
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: 400;
  line-height: 1.35;
  letter-spacing: -0.01em;
}

.page-heading__description {
  margin: var(--space-3) 0 0;
  max-width: var(--content-max);
  color: var(--color-text-muted);
  font-size: var(--text-md);
  line-height: 1.7;
}

@media (max-width: 640px) {
  .page-heading { flex-direction: column; align-items: flex-start; }
}
</style>
