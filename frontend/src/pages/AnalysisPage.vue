<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { learningState } from '../stores/learning.js'

const router = useRouter()

const analysis = computed(() => learningState.analysis)

function moveToReview() {
  router.push({ name: 'review' })
}
</script>

<template>
  <section v-if="analysis" class="analysis-layout">
    <div class="page-heading">
      <div>
        <p class="eyebrow">AI 이해도 분석</p>
        <h1>회고록을 바탕으로 학습 상태를 분석했습니다.</h1>
      </div>
    </div>

    <div class="score-card">
      <div class="score-circle">
        {{ analysis.understandingScore }}%
      </div>

      <div>
        <h2>현재 이해도</h2>
        <p>{{ analysis.analysisReason }}</p>
      </div>
    </div>

    <div class="result-grid">
      <article class="panel">
        <h2>이해한 부분</h2>
        <p>{{ analysis.understoodSummary }}</p>
      </article>

      <article class="panel warning-panel">
        <h2>보완이 필요한 부분</h2>
        <p>{{ analysis.weaknessSummary }}</p>
      </article>
    </div>

    <button
      class="primary-button"
      type="button"
      @click="moveToReview"
    >
      맞춤형 복습자료 보기
    </button>
  </section>

  <section v-else class="empty-state">
    <h1>분석 결과가 없습니다.</h1>
    <p>회고록을 먼저 작성하고 AI 분석을 진행해 주세요.</p>

    <button
      class="primary-button"
      type="button"
      @click="router.push({ name: 'reflection' })"
    >
      회고록 작성하기
    </button>
  </section>
</template>