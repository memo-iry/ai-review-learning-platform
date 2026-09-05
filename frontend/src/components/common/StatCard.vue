<script setup>
import BaseCard from './BaseCard.vue'
import ProgressRing from './ProgressRing.vue'

defineProps({
  label: { type: String, required: true },
  value: { type: [String, Number], required: true },

  caption: { type: String, default: '' },

  ratio: { type: Number, default: null },
  ratioMax: { type: Number, default: 100 },
  ringLabel: { type: String, default: '' },
  tone: { type: String, default: 'accent' },

  badge: { type: String, default: '' },
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

    <ProgressRing v-if="ratio !== null" :value="ratio" :max="ratioMax" :label="ringLabel" :tone="tone" :size="76"
      :thickness="5" />
    <span v-else-if="badge" class="stat-card__badge">{{ badge }}</span>
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

.stat-card__badge {
  flex-shrink: 0;
  padding: var(--space-2) var(--space-4);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-pill);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  white-space: nowrap;
}
</style>
