<script setup>
defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  required: { type: Boolean, default: false },
  rows: { type: Number, default: 6 },
})

defineEmits(['update:modelValue'])
</script>

<template>
  <textarea
    class="base-textarea"
    :value="modelValue"
    :placeholder="placeholder"
    :required="required"
    :rows="rows"
    @input="$emit('update:modelValue', $event.target.value)"
  />
</template>

<style scoped>
.base-textarea {
  display: block;
  width: 100%;
  box-sizing: border-box;
  min-height: 140px;
  padding: var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font-family: var(--font-body);
  font-size: var(--text-md);
  line-height: 1.7;
  resize: vertical;
}

.base-textarea::placeholder {
  color: var(--color-text-subtle);
}

.base-textarea:focus-visible {
  outline: 2px solid var(--color-accent);
  outline-offset: 2px;
  border-color: var(--color-accent);
}
</style>
