<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/client.js'
import { learningState, selectLecture } from '../stores/learning.js'
import AppLayout from '../components/AppLayout.vue'

const LEVEL_NAMES = ['', '인지', '이해', '적용', '구현']

const router = useRouter()
const lectures = ref([])
const mastery = ref(null)
const error = ref('')

const levelLabel = computed(() => {
  if (!mastery.value) return '-'
  return `${mastery.value.currentLevel} ${LEVEL_NAMES[mastery.value.currentLevel] ?? ''}`
})

function startLecture(lecture) {
  selectLecture(lecture)
  router.push({ name: 'reflection', params: { lectureId: lecture.lectureId } })
}

onMounted(async () => {
  try {
    const [lectureList, masteryResult] = await Promise.all([
      api.getLectures(),
      api.getMastery(learningState.userId),
    ])
    lectures.value = lectureList
    mastery.value = masteryResult
  } catch (requestError) {
    error.value = requestError.message
  }
})

</script>

<template>
  <section>
    <AppLayout />
  </section>
  <main class="main-content">
    <section class="page-heading">
      <div>
        <p class="eyebrow">학습 대시보드</p>
        <h1>오늘 배운 내용을 회고하고 복습해 보세요.</h1>
      </div>
    </section>
  
    <section class="summary-grid">
      <article class="summary-card">
        <span>현재 학습 수준</span>
        <strong>{{ levelLabel }}</strong>
      </article>
  
      <article class="summary-card">
        <span>학습 진도</span>
        <strong>{{ mastery ? mastery.progressRate + '%' : '-' }}</strong>
        <small v-if="mastery">회고 {{ mastery.reviewedLectures }} / 강의 {{ mastery.totalLectures }}</small>
      </article>
  
      <article class="summary-card">
        <span>평균 이해도</span>
        <strong>{{ mastery ? mastery.averageScore + '%' : '-' }}</strong>
        <small v-if="mastery">개념 {{ mastery.conceptCount }}개 기준</small>
      </article>
    </section>
  
    <section v-if="mastery && mastery.weakest.length" class="content-section">
      <h2>복습이 필요한 개념</h2>
  
      <div class="weak-list">
        <article
          v-for="concept in mastery.weakest"
          :key="concept.conceptName"
          class="weak-card"
        >
          <strong>{{ concept.conceptName }}</strong>
          <span>{{ concept.score }}%</span>
        </article>
      </div>
    </section>
  
    <section class="content-section">
      <h2>강의 목록</h2>
  
      <p v-if="error" class="error-message">{{ error }}</p>
  
      <div class="course-list">
        <article
          v-for="item in lectures"
          :key="item.lectureId"
          class="course-card"
        >
          <div>
            <span class="tag">진행 중</span>
            <h3>{{ item.title }}</h3>
            <p>{{ item.description }}</p>
            <small>{{ item.lectureDate }} {{ item.startTime }}~{{ item.endTime }}</small>
          </div>
  
          <button class="primary-button" @click="startLecture(item)">
            강의자료 확인
          </button>
        </article>
      </div>
    </section>
  </main>
</template>
