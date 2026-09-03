<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { learningState, resetLearning } from '../stores/learning.js'
import AppLayout from '../components/AppLayout.vue'

const LEVEL_NAMES = ['', '인지', '이해', '적용', '구현']

const router = useRouter()

const analysis = computed(() => learningState.analysis)
const before = computed(() => analysis.value?.levelBefore ?? 0)
const after = computed(() => analysis.value?.levelAfter ?? 0)
const raised = computed(() => after.value > before.value)

function backToDashboard() {
  resetLearning()
  router.push({ name: 'dashboard' })
}
</script>

<template>
  <section>
    <AppLayout />
  </section>
  <section class="growth-page">
    <p class="eyebrow">복습 완료</p>
    <h1>학습 결과가 성장 기록에 반영되었습니다.</h1>

    <div class="dimension-change">
      <div>
        <span>이전 수준</span>
        <strong>{{ before }} {{ LEVEL_NAMES[before] }}</strong>
      </div>

      <span class="growth-arrow">→</span>

      <div>
        <span>현재 수준</span>
        <strong>{{ after }} {{ LEVEL_NAMES[after] }}</strong>
      </div>
    </div>

    <p v-if="raised">
      부족했던 개념을 보완하여 다음 학습 단계로 이동했습니다.
    </p>

    <p v-else>
      이번 회고는 취약 개념을 새로 기록했습니다.
      복습과 확인 문제를 반복하면 수준이 올라갑니다.
    </p>

    <p v-if="analysis" class="growth-detail">
      이번에 보완할 개념: {{ analysis.weakTopics.join(', ') }}
    </p>

    <button class="primary-button" type="button" @click="backToDashboard">
      대시보드로 이동
    </button>
  </section>
</template>
