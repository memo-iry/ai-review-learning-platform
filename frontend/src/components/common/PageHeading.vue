<script setup>
defineProps({
  eyebrow: { type: String, default: '' },
  title: { type: String, required: true },
  description: { type: String, default: '' },
  /** 제목 아래 강조색 구분선을 보여줄지 */
  divider: { type: Boolean, default: false },
  /** description 텍스트 색상: muted | accent */
  descriptionTone: { type: String, default: 'muted' },
})
</script>

<template>
  <header class="page-heading">
    <p v-if="eyebrow" class="page-heading__eyebrow">{{ eyebrow }}</p>
    <h1 class="page-heading__title">{{ title }}</h1>

    <hr v-if="divider" class="page-heading__divider" />

    <div v-if="description || $slots.actions" class="page-heading__meta">
      <p v-if="description" class="page-heading__description" :class="`page-heading__description--${descriptionTone}`">
        {{ description }}
      </p>

      <div v-if="$slots.actions" class="page-heading__actions">
        <slot name="actions" />
      </div>
    </div>
  </header>
</template>

<style scoped>
.page-heading {
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

.page-heading__divider {
  margin: var(--space-4) 0 0;
  border: none;
  border-top: 2px solid var(--color-accent);
}

.page-heading__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  margin-top: var(--space-4);
}

.page-heading__description {
  margin: 0;
  max-width: var(--content-max);
  font-size: var(--text-md);
  line-height: 1.7;
}

.page-heading__description--muted {
  color: var(--color-text-muted);
}

.page-heading__description--accent {
  color: var(--color-accent);
  font-weight: 500;
}

@media (max-width: 640px) {
  .page-heading__meta {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>