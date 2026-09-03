<script setup>
import BaseCard from '@/components/common/BaseCard.vue'
import ProgressRing from '@/components/common/ProgressRing.vue'

defineProps({
  score: { type: Number, required: true },
  reason: { type: String, default: '' },
})
</script>

<template>
  <BaseCard class="analysis-score-card" padding="lg">
    <ProgressRing :value="score" :size="120" :thickness="7" suffix="%" />

    <div class="analysis-score-card__body">
      <h3 class="analysis-score-card__title">현재 이해도</h3>
      <p class="analysis-score-card__reason">{{ reason }}</p>
    </div>
  </BaseCard>
</template>

<style scoped>
.analysis-score-card {
  display: flex;
  align-items: center;
  gap: var(--space-6);
}

.analysis-score-card__title {
  margin: 0 0 var(--space-3);
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: 400;
}

.analysis-score-card__reason {
  margin: 0;
  max-width: var(--content-max);
  color: var(--color-text-muted);
  font-size: var(--text-md);
  line-height: 1.75;
}

@media (max-width: 720px) {
  .analysis-score-card {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
