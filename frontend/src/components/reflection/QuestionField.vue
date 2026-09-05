<script setup>
import BaseCard from '@/components/common/BaseCard.vue'
import BaseTextarea from '@/components/common/BaseTextarea.vue'

defineProps({
  number: { type: String, required: true },
  label: { type: String, required: true },
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  required: { type: Boolean, default: false },
})

defineEmits(['update:modelValue'])
</script>

<template>
  <BaseCard class="question-field" padding="lg">
    <h3 class="question-field__label">{{ number }}. {{ label }}</h3>

    <BaseTextarea :model-value="modelValue" :placeholder="placeholder" :required="required" :rows="6"
      @update:model-value="$emit('update:modelValue', $event)" />
  </BaseCard>
</template>

<style scoped>
.question-field+.question-field {
  margin-top: var(--space-6);
}

.question-field__label {
  margin: 0 0 var(--space-4);
  font-family: var(--font-body);
  font-size: var(--text-md);
  font-weight: 600;
}
</style>
