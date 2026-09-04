<script setup>
defineProps({
  /** rect(기본) | circle */
  shape: { type: String, default: 'rect' },
  width: { type: String, default: '100%' },
  height: { type: String, default: '1em' },
  radius: { type: String, default: '' },
})
</script>

<template>
  <span
    class="skeleton"
    :class="`skeleton--${shape}`"
    :style="{
      width,
      height,
      borderRadius: shape === 'circle' ? '50%' : (radius || 'var(--radius-sm)'),
    }"
    aria-hidden="true"
  />
</template>

<style scoped>
.skeleton {
  display: block;
  flex-shrink: 0;
  background: linear-gradient(
    100deg,
    var(--color-border) 30%,
    var(--color-canvas) 50%,
    var(--color-border) 70%
  );
  background-size: 200% 100%;
  animation: skeleton-shimmer 1.4s ease-in-out infinite;
}

@keyframes skeleton-shimmer {
  0% { background-position: 100% 0; }
  100% { background-position: -100% 0; }
}

@media (prefers-reduced-motion: reduce) {
  .skeleton {
    animation: none;
    background: var(--color-border);
  }
}
</style>
