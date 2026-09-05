<script setup>
defineProps({
  modelValue: { type: [String, Number], default: '' },

  options: { type: Array, default: () => [] },
})

defineEmits(['update:modelValue'])
</script>

<template>
  <div class="base-select">
    <select
      class="base-select__control"
      :value="modelValue"
      @change="$emit('update:modelValue', $event.target.value)"
    >
      <option v-for="option in options" :key="option.value" :value="option.value">
        {{ option.label }}
      </option>
    </select>

    <svg class="base-select__icon" width="10" height="14" viewBox="0 0 10 14" aria-hidden="true">
      <path d="M5 0L9 4H1L5 0Z" fill="currentColor" />
      <path d="M5 14L1 10H9L5 14Z" fill="currentColor" />
    </svg>
  </div>
</template>

<style scoped>
.base-select {
  position: relative;
  display: inline-flex;
}

.base-select__control {
  appearance: none;
  min-width: 160px;
  padding: var(--space-3) var(--space-7) var(--space-3) var(--space-4);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font-family: inherit;
  font-size: var(--text-md);
  cursor: pointer;
}

.base-select__control:focus-visible {
  outline: 2px solid var(--color-accent);
  outline-offset: 2px;
  border-color: var(--color-accent);
}

.base-select__icon {
  position: absolute;
  top: 50%;
  right: var(--space-4);
  transform: translateY(-50%);
  color: var(--color-text-subtle);
  pointer-events: none;
}
</style>
