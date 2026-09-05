<script setup>
defineProps({
  size: {
    type: String,
    default: 'lg',
  },
})
</script>

<template>
  <div
    class="page-container"
    :class="`page-container--${size}`"
  >
    <slot />
  </div>
</template>

<style scoped>
.page-container {
  width: 100%;
  margin: 0 auto;
  padding:
    var(--space-8)
    var(--space-4);
  box-sizing: border-box;
}

.page-container--md {
  max-width: 960px;
}

.page-container--lg {
  max-width: 1100px;
}

.page-container--xl {
  max-width: 1280px;
}

@media (max-width: 720px) {
  .page-container {
    padding:
      var(--space-6)
      var(--space-4);
  }
}
</style>
