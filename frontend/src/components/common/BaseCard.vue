<script setup>
defineProps({
  as: { type: String, default: 'article' },

  tone: { type: String, default: 'default' },

  padding: { type: String, default: 'md' },

  interactive: { type: Boolean, default: false },
})
</script>

<template>
  <component
    :is="as"
    class="base-card"
    :class="[`base-card--${tone}`, `base-card--pad-${padding}`, { 'is-interactive': interactive }]"
  >
    <slot />
  </component>
</template>

<style scoped>
.base-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
}

.base-card--pad-sm { padding: var(--space-4); }
.base-card--pad-md { padding: var(--space-5); }
.base-card--pad-lg { padding: var(--space-6); }

.base-card--accent {
  border-color: var(--color-border);
  box-shadow: inset 3px 0 0 var(--color-accent);
}

.base-card--warning {
  background: var(--color-warning-soft);
  border-color: var(--color-warning-border);
}

.is-interactive {
  cursor: pointer;
  transition: border-color 0.15s ease;
}

.is-interactive:hover {
  border-color: var(--color-border-strong);
}
</style>
