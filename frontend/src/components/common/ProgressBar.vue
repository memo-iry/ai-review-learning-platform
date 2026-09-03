<script setup>
import { computed } from 'vue'

const props = defineProps({
  /** 0~100 */
  value: { type: Number, required: true },
})

const clamped = computed(() => Math.min(Math.max(props.value, 0), 100))
</script>

<template>
  <div
    class="progress-bar"
    role="progressbar"
    :aria-valuenow="Math.round(clamped)"
    aria-valuemin="0"
    aria-valuemax="100"
  >
    <div class="progress-bar__track">
      <div class="progress-bar__fill" :style="{ width: `${clamped}%` }" />
    </div>
  </div>
</template>

<style scoped>
.progress-bar__track {
  width: 100%;
  height: 6px;
  border-radius: var(--radius-pill);
  background: var(--color-track);
  overflow: hidden;
}

.progress-bar__fill {
  height: 100%;
  border-radius: var(--radius-pill);
  background: var(--color-accent);
  transition: width 0.3s ease;
}

@media (prefers-reduced-motion: reduce) {
  .progress-bar__fill { transition: none; }
}
</style>
