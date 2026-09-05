<script setup>
defineProps({
  label: { type: String, default: '' },

  size: { type: String, default: 'md' },
})
</script>

<template>
  <div class="loading-spinner" role="status" :aria-label="label || '불러오는 중'">
    <span class="loading-spinner__ring" :class="`loading-spinner__ring--${size}`" aria-hidden="true" />
    <p v-if="label" class="loading-spinner__label">{{ label }}</p>
  </div>
</template>

<style scoped>
.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  padding: var(--space-8) 0;
}

.loading-spinner__ring {
  border-radius: 50%;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-accent);
  animation: loading-spinner-rotate 0.8s linear infinite;
}

.loading-spinner__ring--sm { width: 22px; height: 22px; border-width: 2px; }
.loading-spinner__ring--md { width: 36px; height: 36px; border-width: 3px; }

.loading-spinner__label {
  margin: 0;
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

@keyframes loading-spinner-rotate {
  to { transform: rotate(360deg); }
}

@media (prefers-reduced-motion: reduce) {
  .loading-spinner__ring {
    animation: none;
    border-top-color: var(--color-border-strong);
  }
}
</style>
