<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: { type: Number, required: true },
  max: { type: Number, default: 100 },
  size: { type: Number, default: 72 },
  thickness: { type: Number, default: 4 },
  /** accent | muted */
  tone: { type: String, default: 'accent' },
  /** 가운데 표시 문자열. 없으면 value 를 그대로 표시 */
  label: { type: String, default: '' },
  suffix: { type: String, default: '' },
  showLabel: { type: Boolean, default: true },
})

const radius = computed(() => (props.size - props.thickness) / 2)
const circumference = computed(() => 2 * Math.PI * radius.value)

const ratio = computed(() => {
  if (!props.max) return 0
  return Math.min(Math.max(props.value / props.max, 0), 1)
})

const dashOffset = computed(() => circumference.value * (1 - ratio.value))
const centerText = computed(() => (props.label || `${Math.round(props.value)}`) + props.suffix)
const fontSize = computed(() => `${Math.max(props.size * 0.2, 11)}px`)
</script>

<template>
  <div
    class="progress-ring"
    :class="`progress-ring--${tone}`"
    :style="{ width: `${size}px`, height: `${size}px` }"
    role="img"
    :aria-label="centerText"
  >
    <svg :width="size" :height="size" :viewBox="`0 0 ${size} ${size}`" aria-hidden="true">
      <circle
        class="progress-ring__track"
        :cx="size / 2"
        :cy="size / 2"
        :r="radius"
        fill="none"
        :stroke-width="thickness"
      />
      <circle
        class="progress-ring__value"
        :cx="size / 2"
        :cy="size / 2"
        :r="radius"
        fill="none"
        stroke-linecap="round"
        :stroke-width="thickness"
        :stroke-dasharray="circumference"
        :stroke-dashoffset="dashOffset"
        :transform="`rotate(-90 ${size / 2} ${size / 2})`"
      />
    </svg>

    <span v-if="showLabel" class="progress-ring__label" :style="{ fontSize }">
      {{ centerText }}
    </span>
  </div>
</template>

<style scoped>
.progress-ring {
  position: relative;
  display: inline-grid;
  place-items: center;
  flex-shrink: 0;
}

.progress-ring svg { position: absolute; inset: 0; }

.progress-ring__track { stroke: var(--color-track); }

.progress-ring--accent .progress-ring__value { stroke: var(--color-accent); }
.progress-ring--muted .progress-ring__value { stroke: var(--color-border-strong); }

.progress-ring__label {
  position: relative;
  font-weight: 600;
  letter-spacing: -0.02em;
}

.progress-ring--accent .progress-ring__label { color: var(--color-accent); }
.progress-ring--muted .progress-ring__label { color: var(--color-text-muted); }

@media (prefers-reduced-motion: no-preference) {
  .progress-ring__value { transition: stroke-dashoffset 0.6s ease; }
}
</style>
