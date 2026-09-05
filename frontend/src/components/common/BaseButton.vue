<script setup>
defineProps({
  variant: { type: String, default: 'primary' },

  size: { type: String, default: 'md' },
  type: { type: String, default: 'button' },
  disabled: { type: Boolean, default: false },
  block: { type: Boolean, default: false },

  rounded: { type: Boolean, default: false },
})
</script>

<template>
  <button class="base-button" :class="[
    `base-button--${variant}`,
    `base-button--${size}`,
    { 'is-block': block, 'is-rounded': rounded },
  ]" :type="type" :disabled="disabled">
    <slot />
  </button>
</template>

<style scoped>
.base-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  font-family: inherit;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.15s ease, border-color 0.15s ease, color 0.15s ease;
}

.base-button--sm {
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
}

.base-button--md {
  padding: var(--space-3) var(--space-6);
  font-size: var(--text-md);
}

.base-button--primary {
  background: var(--color-accent);
  color: #fff;
}

.base-button--primary:hover:not(:disabled) {
  background: var(--color-accent-strong);
}

.base-button--outline {
  background: var(--color-surface);
  border-color: var(--color-border-strong);
  color: var(--color-text);
}

.base-button--outline:hover:not(:disabled) {
  border-color: var(--color-accent);
  color: var(--color-accent);
}

.base-button--pill {
  background: var(--color-surface);
  border-color: var(--color-border-strong);
  border-radius: var(--radius-pill);
  color: var(--color-text);
}

.base-button--pill:hover:not(:disabled) {
  border-color: var(--color-accent);
  color: var(--color-accent);
}

.base-button--ghost {
  background: none;
  color: var(--color-accent);
  padding-inline: 0;
}

.is-block {
  width: 100%;
}

.is-rounded {
  border-radius: var(--radius-pill);
}

.base-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
