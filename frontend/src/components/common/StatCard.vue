<script setup>
import BaseCard from './BaseCard.vue'
import ProgressRing from './ProgressRing.vue'

defineProps({
  label: { type: String, required: true },
  value: { type: [String, Number], required: true },
  /** 값 옆 작은 보조 텍스트 (예: "지난주 대비 +4%") */
  caption: { type: String, default: '' },
  /** 링에 넣을 수치. 없으면 링을 그리지 않음 */
  ratio: { type: Number, default: null },
  ratioMax: { type: Number, default: 100 },
  ringLabel: { type: String, default: '' },
  tone: { type: String, default: 'accent' },
})
</script>

<template>
  <BaseCard class="stat-card" padding="md">
    <div class="stat-card__text">
      <p class="stat-card__label">{{ label }}</p>
      <p class="stat-card__value">
        {{ value }}
        <span v-if="caption" class="stat-card__caption">{{ caption }}</span>
      </p>
    </div>

    <ProgressRing
      v-if="ratio !== null"
      :value="ratio"
      :max="ratioMax"
      :label="ringLabel"
      :tone="tone"
      :size="76"
      :thickness="5"
    />
  </BaseCard>
</template>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
}

.stat-card__label {
  margin: 0 0 var(--space-3);
  color: var(--color-accent);
  font-size: var(--text-xs);
  font-weight: 700;
}

.stat-card__value {
  margin: 0;
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  line-height: 1.1;
}

.stat-card__caption {
  margin-left: var(--space-2);
  color: var(--color-text-subtle);
  font-family: var(--font-body);
  font-size: var(--text-xs);
  font-weight: 400;
}
</style>
