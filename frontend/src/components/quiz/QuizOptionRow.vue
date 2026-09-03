<script setup>
defineProps({
  number: { type: Number, required: true },
  label: { type: String, required: true },
  selected: { type: Boolean, default: false },
  name: { type: String, default: 'quiz-option' },
})

defineEmits(['select'])
</script>

<template>
  <label class="quiz-option-row" :class="{ 'is-selected': selected }">
    <input
      class="quiz-option-row__input"
      type="radio"
      :name="name"
      :checked="selected"
      @change="$emit('select')"
    />
    <span class="quiz-option-row__radio" aria-hidden="true" />
    <span class="quiz-option-row__text">{{ number }}. {{ label }}</span>
    <span class="quiz-option-row__number">{{ number }}</span>
  </label>
</template>

<style scoped>
.quiz-option-row {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: border-color 0.15s ease;
}

.quiz-option-row + .quiz-option-row {
  margin-top: var(--space-3);
}

.quiz-option-row:hover {
  border-color: var(--color-border-strong);
}

.quiz-option-row.is-selected {
  border: 2px solid var(--color-text);
  padding: calc(var(--space-4) - 1px) calc(var(--space-5) - 1px);
}

.quiz-option-row__input {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
}

.quiz-option-row__radio {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid var(--color-border-strong);
  position: relative;
}

.is-selected .quiz-option-row__radio {
  border-color: var(--color-accent);
}

.is-selected .quiz-option-row__radio::after {
  content: '';
  position: absolute;
  inset: 3px;
  border-radius: 50%;
  background: var(--color-accent);
}

.quiz-option-row__text {
  flex: 1 1 auto;
  font-size: var(--text-md);
  color: var(--color-text);
}

.quiz-option-row__number {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  border: 1px solid var(--color-border-strong);
  color: var(--color-text-subtle);
  font-size: var(--text-xs);
}

.is-selected .quiz-option-row__number {
  background: var(--color-accent);
  border-color: var(--color-accent);
  color: #fff;
}

.quiz-option-row__input:focus-visible ~ .quiz-option-row__radio {
  outline: 2px solid var(--color-accent);
  outline-offset: 2px;
}
</style>
