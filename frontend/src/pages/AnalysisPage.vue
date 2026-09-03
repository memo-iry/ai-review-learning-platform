<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { learningState } from '../stores/learning.js'

import BaseButton from '@/components/common/BaseButton.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import PageHeading from '@/components/common/PageHeading.vue'
import AppSection from '@/components/common/AppSection.vue'

import AnalysisScoreCard from '@/components/analysis/AnalysisScoreCard.vue'
import AnalysisResultGrid from '@/components/analysis/AnalysisResultGrid.vue'

const router = useRouter()

const analysis = computed(() => learningState.analysis)

function moveToReview() {
  router.push({ name: 'review' })
}

function moveToReflection() {
  router.push({ name: 'reflection' })
}
</script>

<template>
  <div class="analysis-page">
    <template v-if="analysis">
      <PageHeading eyebrow="AI 이해도 분석" title="회고록을 바탕으로 학습 상태를 분석했습니다." />

      <AppSection title="현재 이해도">
        <AnalysisScoreCard :score="analysis.understandingScore" :reason="analysis.analysisReason" />
      </AppSection>

      <AppSection title="분석 결과">
        <AnalysisResultGrid :understood-summary="analysis.understoodSummary"
          :weakness-summary="analysis.weaknessSummary" />
      </AppSection>

      <div class="analysis-page__footer">
        <BaseButton variant="primary" @click="moveToReview">
          맞춤형 복습자료 보기
        </BaseButton>
      </div>
    </template>

    <EmptyState v-else title="아직 분석 결과가 없습니다" description="회고록을 작성하면 AI가 이해도를 분석해 드립니다.">
      <template #action>
        <BaseButton variant="pill" @click="moveToReflection">
          회고록 작성하기
        </BaseButton>
      </template>
    </EmptyState>
  </div>
</template>

<style scoped>
.analysis-page {
  max-width: 860px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-6);
}

.analysis-page__footer {
  margin-top: var(--space-8);
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 720px) {
  .analysis-page {
    padding: var(--space-6) var(--space-4);
  }

  .analysis-page__footer {
    justify-content: stretch;
  }
}
</style>
