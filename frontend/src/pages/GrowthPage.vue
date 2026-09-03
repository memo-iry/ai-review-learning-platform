<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <section class="growth-page">
    <p class="eyebrow">복습 완료</p><h1>학습 결과가 성장 기록에 반영되었습니다.</h1>
    <div class="dimension-change"><div><span>이전 차원</span><strong>2</strong></div><span class="growth-arrow">→</span><div><span>현재 차원</span><strong>3</strong></div></div>
    <p>부족했던 개념을 보완하여 다음 학습 단계로 이동했습니다.</p>
    <button class="primary-button" @click="router.push({ name: 'dashboard' })">대시보드로 이동</button>
  </section>
</template>
