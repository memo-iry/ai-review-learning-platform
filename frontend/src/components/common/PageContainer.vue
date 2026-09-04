<script setup>
defineProps({
  /** md(860) | lg(960, 기본값) | xl(1200 — 2단 레이아웃처럼 더 넓은 폭이 필요할 때) */
  size: { type: String, default: 'lg' },
})
</script>

<template>
  <div class="page-container" :class="`page-container--${size}`">
    <slot />
  </div>
</template>

<style scoped>
.page-container {
  width: 100%;
  margin: 0 auto;
  padding: var(--space-8) var(--space-6);
}

.page-container--md { max-width: 860px; }
.page-container--lg { max-width: 960px; }
.page-container--xl { max-width: 1200px; }

@media (max-width: 720px) {
  .page-container {
    padding: var(--space-6) var(--space-4);
  }
}
</style>
