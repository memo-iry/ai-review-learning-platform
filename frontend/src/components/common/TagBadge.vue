<script setup>
defineProps({
  tone: { type: String, default: 'accent' },
})
</script>

<template>
  <span class="tag-badge" :class="`tag-badge--${tone}`">
    <slot />
  </span>
</template>

<style scoped>
.tag-badge {
  display: inline-block;
  padding: 3px var(--space-3);
  border: 1px solid currentColor;
  border-radius: var(--radius-pill);
  font-size: var(--text-xs);
  font-weight: 700;
  line-height: 1.5;
}

.tag-badge--accent { color: var(--color-accent); }
.tag-badge--neutral { color: var(--color-text-subtle); }
.tag-badge--warning { color: var(--color-warning); }
</style>
