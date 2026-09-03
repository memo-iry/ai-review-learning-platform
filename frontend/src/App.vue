<script setup>
import { computed, ref } from 'vue'
import AppLayout from './components/AppLayout.vue'
import DashboardPage from './pages/DashboardPage.vue'
import ReflectionPage from './pages/ReflectionPage.vue'
import AnalysisPage from './pages/AnalysisPage.vue'
import ReviewPage from './pages/ReviewPage.vue'
import GrowthPage from './pages/GrowthPage.vue'

const screen = ref('dashboard')
const lecture = ref(null)
const analysis = ref(null)

const currentStep = computed(() => ({
  dashboard: '대시보드',
  reflection: '회고록',
  analysis: 'AI 분석',
  review: '복습자료',
  growth: '내 성장',
})[screen.value])

function startLecture(selectedLecture) {
  lecture.value = selectedLecture
  screen.value = 'reflection'
}

function completeAnalysis(result) {
  analysis.value = result
  screen.value = 'analysis'
}
</script>

<template>
  <AppLayout :current-step="currentStep">
    <DashboardPage v-if="screen === 'dashboard'" @start="startLecture" />
    <ReflectionPage
      v-else-if="screen === 'reflection' && lecture"
      :lecture="lecture"
      @analyzed="completeAnalysis"
      @back="screen = 'dashboard'"
    />
    <AnalysisPage
      v-else-if="screen === 'analysis' && analysis"
      :analysis="analysis"
      @open-review="screen = 'review'"
    />
    <ReviewPage
      v-else-if="screen === 'review' && analysis?.reviewMaterial"
      :review="analysis.reviewMaterial"
      @complete="screen = 'growth'"
    />
    <GrowthPage v-else-if="screen === 'growth'" @dashboard="screen = 'dashboard'" />
  </AppLayout>
</template>
