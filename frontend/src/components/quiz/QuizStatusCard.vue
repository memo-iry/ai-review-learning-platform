<script setup>
defineProps({
  title: { type: String, default: '오늘의 퀴즈 현황' },
  progress: { type: Number, required: true },
  /** "2분 남음" 처럼 이미 포맷된 문자열 */
  timeLeftLabel: { type: String, required: true },
})
</script>

<template>
  <div class="quiz-status-card">
    <p class="quiz-status-card__title">{{ title }}</p>

    <div class="quiz-status-card__grid">
      <div class="quiz-status-card__item">
        <p class="quiz-status-card__label">진행률</p>
        <p class="quiz-status-card__value">{{ Math.round(progress) }}%</p>
      </div>
      <div class="quiz-status-card__item">
        <p class="quiz-status-card__label">예상 소요 시간</p>
        <p class="quiz-status-card__value">{{ timeLeftLabel }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.quiz-status-card__title {
  margin: 0 0 var(--space-4);
  font-size: var(--text-sm);
  font-weight: 700;
}

.quiz-status-card__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
}

.quiz-status-card__item {
  padding: var(--space-4);
  border: 1px solid var(--color-border);
}

.quiz-status-card__item:first-child {
  border-right: none;
  border-top-left-radius: var(--radius-sm);
  border-bottom-left-radius: var(--radius-sm);
}

.quiz-status-card__item:last-child {
  border-top-right-radius: var(--radius-sm);
  border-bottom-right-radius: var(--radius-sm);
}

.quiz-status-card__label {
  margin: 0 0 var(--space-2);
  color: var(--color-text-subtle);
  font-size: var(--text-xs);
}

.quiz-status-card__value {
  margin: 0;
  font-weight: 700;
  font-size: var(--text-lg);
}
</style>
