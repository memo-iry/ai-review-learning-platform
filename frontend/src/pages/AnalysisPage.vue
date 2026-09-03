<script setup>
defineProps({ analysis: { type: Object, required: true } })
const emit = defineEmits(['open-review'])
</script>

<template>
  <section class="analysis-layout">
    <div class="page-heading"><div><p class="eyebrow">AI 이해도 분석</p><h1>회고록을 바탕으로 학습 상태를 분석했습니다.</h1></div></div>
    <div class="score-card"><div class="score-circle">{{ analysis.understandingScore }}%</div><div><h2>현재 이해도</h2><p>{{ analysis.analysisReason }}</p></div></div>
    <div class="result-grid">
      <article class="panel"><h2>이해한 부분</h2><p>{{ analysis.understoodSummary }}</p></article>
      <article class="panel warning-panel"><h2>보완이 필요한 부분</h2><p>{{ analysis.weaknessSummary }}</p></article>
    </div>
    <button class="primary-button" @click="emit('open-review')">맞춤형 복습자료 보기</button>
  </section>
</template>
