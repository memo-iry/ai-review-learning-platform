<script setup>
import InfoPanel from '@/components/common/InfoPanel.vue'

defineProps({
  understoodSummary: { type: String, default: '' },
  weaknessSummary: { type: String, default: '' },
})
</script>

<template>
  <div class="analysis-result-grid">
    <InfoPanel tone="accent" title="이해한 부분" :body="understoodSummary" />
    <InfoPanel tone="warning" title="보완이 필요한 부분" :body="weaknessSummary" />
  </div>
</template>

<style scoped>
.analysis-result-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}

@media (max-width: 720px) {
  .analysis-result-grid {
    grid-template-columns: 1fr;
  }
}
</style>
