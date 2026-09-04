<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/client.js'
import { learningState, selectLecture } from '../stores/learning.js'
import AppLayout from '../components/AppLayout.vue'
import TodayStudy from '../components/common/TodayStudy.vue'
import WrittenReflection from '../components/common/WrittenReflection.vue'
import WeeklyReflectionChart from '../components/common/WeeklyReflectionChart.vue'
import WeaknessAnalysis from '../components/common/WeaknessAnalysis.vue'
import LearningHistory from '../components/common/LearningHistory.vue'
import { currentUserId } from '../stores/auth.js'
import WeeklyClassCalendar from '../components/common/WeeklyClassCalendar.vue'

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
      api.getMastery(currentUserId()),
    ])
    lectures.value = lectureList
    mastery.value = masteryResult
  } catch (requestError) {
    error.value = requestError.message
  }
})

const weeklyRecords = [
  { day: '월', count: 4, completed: true },
  { day: '화', count: 6, completed: true },
  { day: '수', count: 2, completed: false },
  { day: '목', count: 5, completed: true },
  { day: '금', count: 0, completed: false },
  { day: '토', count: 1, completed: false },
  { day: '일', count: 3, completed: false },
]

const weaknesses = [
  {
    title: 'UX 리서치 방법론',
    description: '실습 인터뷰 분석 보조 자료로 보강',
    score: 72,
  },
  {
    title: '알고리즘 기초',
    description: '자료구조 개념 학습 필요',
    score: 68,
  },
]
const dateParts = new Intl.DateTimeFormat('ko-KR', {
  month: '2-digit',
  day: '2-digit',
  weekday: 'long',
}).formatToParts(new Date())

const getDatePart = (type) => {
  return dateParts.find((part) => part.type === type)?.value
}

const currentDate = `${getDatePart('month')}/${getDatePart('day')}`
const currentWeekday = getDatePart('weekday')

const quizRecords = [
  {
    title: '데이터 분석을 위한 Python 이해',
    score: 88,
  },
  {
    title: 'Java, SpringBoot, Rest API 구현',
    score: 76,
  },
  {
    title: 'Agile 방법론 및 MSA 개발',
    score: 100,
  },
]
</script>

<template>
  <section>
    <AppLayout />
  </section>
  <section class="dashboard-grid">
    <div class="dashboard-date">
      {{ currentDate }} {{ currentWeekday }}
      <hr />
    </div>

    <div class="today-lesson">
      <TodayStudy />
    </div>

    <div class="reflection-card">
      <WrittenReflection />
    </div>

    <div class="recent-record">
      <WeeklyClassCalendar
        :lectures="lectures"
      />
    </div>

    <div class="emotion-analysis">
      <WeeklyReflectionChart
        :records="weeklyRecords"
        :completion-rate="15"
      />
    </div>

    <div class="weakness-analysis">
      <WeaknessAnalysis
        :weaknesses="weaknesses"
      />
    </div>
  </section>
</template>

<style scoped>
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  grid-template-rows: auto 120px 220px 280px;
  grid-template-areas:
    "date date"
    "today reflection"
    "record record"
    "emotion weakness";
  gap: 10px;
  align-content: start;
}

.dashboard-date {
  grid-area: date;
}

.today-lesson {
  grid-area: today;
}

.reflection-card {
  grid-area: reflection;
}

.recent-record {
  grid-area: record;
}

.learning-progress {
  grid-area: progress;
}

.emotion-analysis {
  grid-area: emotion;
}

.weakness-analysis {
  grid-area: weakness;
}
</style>