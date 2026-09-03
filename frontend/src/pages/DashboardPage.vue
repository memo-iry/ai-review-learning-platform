<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/client.js'
import { learningState } from '../stores/learning.js'

const router = useRouter()
const lectures = ref([])
const error = ref('')

function startLecture(lecture) {
  learningState.lecture = lecture
  learningState.analysis = null
  router.push({ name: 'reflection', params: { lectureId: lecture.lectureId } })
}

onMounted(async () => {
  try { lectures.value = await api.getLectures() }
  catch (requestError) { error.value = requestError.message }
})
</script>

<template>
  <section class="page-heading">
    <div><p class="eyebrow">학습 대시보드</p><h1>오늘 배운 내용을 회고하고 복습해 보세요.</h1></div>
  </section>
  <section class="summary-grid">
    <article class="summary-card"><span>현재 차원</span><strong>2</strong></article>
    <article class="summary-card"><span>학습 진도</span><strong>68%</strong></article>
    <article class="summary-card"><span>최근 이해도</span><strong>74%</strong></article>
  </section>
  <section class="content-section">
    <h2>강의 목록</h2>
    <p v-if="error" class="error-message">{{ error }}</p>
    <div class="course-list">
      <article v-for="item in lectures" :key="item.lectureId" class="course-card">
        <div>
          <span class="tag">진행 중</span><h3>{{ item.title }}</h3><p>{{ item.description }}</p>
          <small>{{ item.lectureDate }} {{ item.startTime }}~{{ item.endTime }}</small>
        </div>
        <button class="primary-button" @click="startLecture(item)">강의자료 확인</button>
      </article>
    </div>
  </section>
</template>
