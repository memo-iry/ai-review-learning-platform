<script setup>
defineProps({ currentStep: { type: String, required: true } })

const steps = ['대시보드', '강의자료', '회고록', 'AI 분석', '복습자료', '내 성장']
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <div class="brand">AI 맞춤 복습</div>
      <nav class="steps" aria-label="학습 단계">
        <span
          v-for="step in steps"
          :key="step"
          class="step"
          :class="{ active: step === currentStep }"
        >{{ step }}</span>
      </nav>
    </header>
    <main class="page"><slot /></main>
  </div>
</template>
