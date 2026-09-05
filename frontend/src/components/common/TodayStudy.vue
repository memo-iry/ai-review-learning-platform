<script setup>
import { useRouter } from 'vue-router'
import BaseCard from './BaseCard.vue'
import { learningState } from '@/stores/learning.js'

const router = useRouter()

const todayLecture = {
  lectureId: 3,
  title: 'Spring AI',
}

function moveToReflection() {
  learningState.lecture = todayLecture

  router.push(`/reflection/${todayLecture.lectureId}`)
}
</script>

<template>
  <BaseCard class="today-lesson-card">
    <div class="card-header">
      <span class="card-label">오늘의 수업</span>

      <button
        type="button"
        class="reflection-button"
        @click="moveToReflection"
      >
        회고록 작성하러 가기
      </button>
    </div>

    <p class="lecture-title">
      {{ todayLecture.title }}
    </p>
  </BaseCard>
</template>

<style scoped>
.today-lesson-card {
  height: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-label {
  color: #d65427;
  font-size: 11px;
  font-weight: 600;
}

.reflection-button {
  border: 0;
  border-radius: 14px;
  padding: 7px 13px;
  background: #d65427;
  color: #ffffff;
  font-size: 10px;
  cursor: pointer;
}

.lecture-title {
  margin: 16px 0 0;
  color: #4b5563;
  font-family: "AppleMyungjo", "Noto Serif KR", serif;
  font-size: 15px;
}
</style>
