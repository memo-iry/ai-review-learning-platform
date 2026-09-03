<script setup>
import BaseCard from './BaseCard.vue'
import TagBadge from './TagBadge.vue'

defineProps({
  category: { type: String, default: '' },
  title: { type: String, required: true },
  date: { type: String, default: '' },
  actionLabel: { type: String, default: '' },
})

defineEmits(['action'])
</script>

<template>
  <BaseCard as="li" class="record-row" padding="md" interactive @click="$emit('action')">
    <div class="record-row__main">
      <TagBadge v-if="category">{{ category }}</TagBadge>
      <h3 class="record-row__title">{{ title }}</h3>
    </div>

    <div class="record-row__meta">
      <time v-if="date" class="record-row__date">{{ date }}</time>
      <span v-if="actionLabel" class="record-row__action">{{ actionLabel }}</span>
    </div>
  </BaseCard>
</template>

<style scoped>
.record-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
  list-style: none;
}

.record-row__title {
  margin: var(--space-3) 0 0;
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: 400;
}

.record-row__meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: var(--space-4);
  white-space: nowrap;
}

.record-row__date {
  color: var(--color-text-subtle);
  font-size: var(--text-xs);
}

.record-row__action {
  color: var(--color-accent);
  font-size: var(--text-sm);
  font-weight: 700;
}
</style>
