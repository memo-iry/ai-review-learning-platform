<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/client.js'
import { selectLecture } from '../stores/learning.js'
import AppLayout from '../components/AppLayout.vue'
import PageContainer from '../components/common/PageContainer.vue'
import TodayStudy from '../components/common/TodayStudy.vue'
import WrittenReflection from '../components/common/WrittenReflection.vue'
import WeeklyReflectionChart from '../components/common/WeeklyReflectionChart.vue'
import WeaknessAnalysis from '../components/common/WeaknessAnalysis.vue'
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

</script>

<template>
  <AppLayout>
    <PageContainer>
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
    </PageContainer>
  </AppLayout>
</template>

<style scoped>
.dashboard-grid {
  display: grid;
  width: 100%;
  min-width: 0;
  box-sizing: border-box;

  grid-template-columns:
    repeat(2, minmax(0, 1fr));

  grid-template-rows:
    auto
    150px
    290px
    320px;

  grid-template-areas:
    "date date"
    "today reflection"
    "record record"
    "emotion weakness";

  gap: 14px;
  align-content: start;
}

.dashboard-date {
  grid-area: date;
  color: #2f3339;
  font-size: 19px;
  font-weight: 600;
  line-height: 1.5;
}

.dashboard-date hr {
  margin: 5px 0 0;
  border: 0;
  border-top: 1px solid #343434;
}

.today-lesson {
  grid-area: today;
  min-width: 0;
}

.reflection-card {
  grid-area: reflection;
  min-width: 0;
}

.recent-record {
  grid-area: record;
  min-width: 0;
  min-height: 280px;
  overflow: visible;
}

.emotion-analysis {
  grid-area: emotion;
  min-width: 0;
}

.weakness-analysis {
  grid-area: weakness;
  min-width: 0;
}

.today-lesson :deep(*) {
  font-size: 17px !important;
  line-height: 1.5 !important;
}

.reflection-card :deep(*) {
  font-size: 17px !important;
  line-height: 1.5 !important;
}

.today-lesson :deep(h2),
.today-lesson :deep(h3),
.today-lesson :deep(.card-label),
.reflection-card :deep(h2),
.reflection-card :deep(h3),
.reflection-card :deep(.card-label) {
  font-size: 16px !important;
  font-weight: 700 !important;
}

.today-lesson :deep(p),
.today-lesson :deep(span),
.today-lesson :deep(.lecture-title),
.reflection-card :deep(select),
.reflection-card :deep(.lecture-select) {
  color: #4b5563;
  font-size: 18px !important;
  font-weight: 600 !important;
}

.today-lesson :deep(button),
.today-lesson :deep(button *),
.reflection-card :deep(button),
.reflection-card :deep(button *) {
  font-size: 14px !important;
  font-weight: 700 !important;
  line-height: 1.2 !important;
}

.recent-record :deep(*) {
  line-height: 1.5 !important;
}

.recent-record :deep(h2),
.recent-record :deep(h3),
.recent-record :deep(.card-title),
.recent-record :deep(.calendar-title) {
  margin-top: 0 !important;
  padding-top: 2px;
  overflow: visible !important;
  color: #4b5563;
  font-size: 18px !important;
  font-weight: 700 !important;
  line-height: 1.6 !important;
}

.recent-record :deep(p),
.recent-record :deep(span),
.recent-record :deep(li) {
  font-size: 14px !important;
  line-height: 1.5 !important;
}

.recent-record :deep(button),
.recent-record :deep(button *) {
  font-size: 13px !important;
  font-weight: 600 !important;
}

.recent-record :deep(> *) {
  width: 100%;
  min-height: 100%;
  box-sizing: border-box;
  overflow: visible !important;
}

.emotion-analysis :deep(h2),
.emotion-analysis :deep(h3),
.weakness-analysis :deep(h2),
.weakness-analysis :deep(h3) {
  font-size: 18px !important;
  font-weight: 700 !important;
  line-height: 1.5 !important;
}

.emotion-analysis :deep(p),
.emotion-analysis :deep(span),
.emotion-analysis :deep(li),
.weakness-analysis :deep(p),
.weakness-analysis :deep(span),
.weakness-analysis :deep(li) {
  font-size: 14px !important;
  line-height: 1.5 !important;
}

@media (max-width: 900px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
    grid-template-rows: auto;
    grid-template-areas:
      "date"
      "today"
      "reflection"
      "record"
      "emotion"
      "weakness";
  }

  .today-lesson,
  .reflection-card {
    min-height: 150px;
  }

  .recent-record {
    min-height: 300px;
  }

  .emotion-analysis,
  .weakness-analysis {
    min-height: 300px;
  }
}

@media (max-width: 640px) {
  .dashboard-grid {
    gap: 12px;
  }

  .dashboard-date {
    font-size: 17px;
  }

  .today-lesson :deep(*),
  .reflection-card :deep(*) {
    font-size: 15px !important;
  }

  .today-lesson :deep(p),
  .today-lesson :deep(span),
  .reflection-card :deep(select),
  .reflection-card :deep(.lecture-select) {
    font-size: 16px !important;
  }

  .recent-record {
    min-height: 340px;
    overflow-x: auto;
  }

  .recent-record :deep(h2),
  .recent-record :deep(h3),
  .recent-record :deep(.card-title),
  .recent-record :deep(.calendar-title) {
    font-size: 16px !important;
  }
}
</style>
