<script setup>
import { computed, ref } from 'vue'
import BaseCard from './BaseCard.vue'

const mockReflections = ref([
  {
    reflectionId: 1,
    lectureTitle: 'Spring AI',
    writtenAt: '2026-08-31',
  },
  {
    reflectionId: 2,
    lectureTitle: 'AI 웹 서비스 설계',
    writtenAt: '2026-09-01',
  },
  {
    reflectionId: 3,
    lectureTitle: 'Java, SpringBoot, REST API',
    writtenAt: '2026-09-03',
  },
  {
    reflectionId: 4,
    lectureTitle: 'Vue.js 상태 관리',
    writtenAt: '2026-09-03',
  },
  {
    reflectionId: 5,
    lectureTitle: '컨테이너 이해',
    writtenAt: '2026-08-25',
  },
])

function parseDate(value) {
  if (!value) {
    return null
  }

  const dateText = String(value).slice(0, 10)
  const [year, month, day] = dateText
    .split('-')
    .map(Number)

  const date = new Date(year, month - 1, day)

  if (Number.isNaN(date.getTime())) {
    return null
  }

  date.setHours(0, 0, 0, 0)

  return date
}

function getWeekStart(date) {
  const result = new Date(date)

  result.setHours(0, 0, 0, 0)

  const currentDay = result.getDay()
  const distanceFromMonday =
    currentDay === 0 ? -6 : 1 - currentDay

  result.setDate(
    result.getDate() + distanceFromMonday,
  )

  return result
}

const currentWeekStart = ref(
  getWeekStart(new Date()),
)

const weekDays = computed(() => {
  return Array.from({ length: 7 }, (_, index) => {
    const date = new Date(currentWeekStart.value)

    date.setDate(date.getDate() + index)

    return date
  })
})

const weekRange = computed(() => {
  const startDate = weekDays.value[0]
  const endDate = weekDays.value[6]

  const formatter = new Intl.DateTimeFormat('ko-KR', {
    month: '2-digit',
    day: '2-digit',
  })

  return `${formatter.format(startDate)} - ${formatter.format(endDate)}`
})

function getDateKey(date) {
  const year = date.getFullYear()

  const month = String(
    date.getMonth() + 1,
  ).padStart(2, '0')

  const day = String(
    date.getDate(),
  ).padStart(2, '0')

  return `${year}-${month}-${day}`
}

const reflectionsByDate = computed(() => {
  const reflectionMap = new Map()

  mockReflections.value.forEach((reflection) => {
    const writtenDate = parseDate(reflection.writtenAt)

    if (!writtenDate) {
      return
    }

    const dateKey = getDateKey(writtenDate)

    if (!reflectionMap.has(dateKey)) {
      reflectionMap.set(dateKey, [])
    }

    reflectionMap.get(dateKey).push(reflection)
  })

  return reflectionMap
})

function getReflectionsByDate(date) {
  return reflectionsByDate.value.get(
    getDateKey(date),
  ) ?? []
}

function getDateLabel(date) {
  const weekday = new Intl.DateTimeFormat('ko-KR', {
    weekday: 'short',
  }).format(date)

  return `${weekday} ${date.getDate()}일`
}

function isToday(date) {
  return getDateKey(date) === getDateKey(new Date())
}

function moveWeek(amount) {
  const nextWeek = new Date(currentWeekStart.value)

  nextWeek.setDate(
    nextWeek.getDate() + amount * 7,
  )

  currentWeekStart.value = nextWeek
}

function moveToCurrentWeek() {
  currentWeekStart.value = getWeekStart(new Date())
}
</script>

<template>
  <BaseCard class="weekly-calendar-card">
    <div class="calendar-container">
      <header class="calendar-header">
        <div>
          <h3>주간 회고 작성 기록</h3>
          <p>{{ weekRange }}</p>
        </div>

        <div class="calendar-navigation">
          <button
            type="button"
            aria-label="이전 주"
            @click="moveWeek(-1)"
          >
            ‹
          </button>

          <button
            type="button"
            class="today-button"
            @click="moveToCurrentWeek"
          >
            이번 주
          </button>

          <button
            type="button"
            aria-label="다음 주"
            @click="moveWeek(1)"
          >
            ›
          </button>
        </div>
      </header>

      <div class="week-calendar">
        <article
          v-for="date in weekDays"
          :key="getDateKey(date)"
          class="day-column"
          :class="{
            today: isToday(date),
            completed:
              getReflectionsByDate(date).length > 0,
          }"
        >
          <div class="day-header">
            <span class="date-label">
              {{ getDateLabel(date) }}
            </span>
          </div>

          <div class="reflection-list">
            <div
              v-for="reflection in getReflectionsByDate(date)"
              :key="reflection.reflectionId"
              class="reflection-item"
              :title="reflection.lectureTitle"
            >
              <span class="completed-dot" />

              <span class="reflection-title">
                {{ reflection.lectureTitle }}
              </span>
            </div>

            <span
              v-if="getReflectionsByDate(date).length === 0"
              class="empty-reflection"
            >
              작성 없음
            </span>
          </div>
        </article>
      </div>
    </div>
  </BaseCard>
</template>

<style scoped>
:global(html) {
  scrollbar-gutter: stable;
}

.weekly-calendar-card {
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  min-height: 270px;
  max-height: none;
  overflow: visible;
}

.calendar-container {
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  min-width: 0;
  overflow: visible;
}

.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
  min-height: 58px;
  padding-bottom: 10px;
  border-bottom: 1px solid #edf0f3;
}

/* 주간 회고 작성 기록 */
.calendar-header h3 {
  margin: 0;
  color: #4b5563;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.5;
}

/* 날짜 범위 */
.calendar-header p {
  margin: 4px 0 0;
  color: #9ca3af;
  font-size: 13px;
  font-weight: 400;
  line-height: 1.4;
}

.calendar-navigation {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 6px;
}

.calendar-navigation button {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 30px;
  height: 30px;
  padding: 0;
  border: 1px solid #e5e7eb;
  border-radius: 15px;
  background: #ffffff;
  color: #6b7280;
  font-size: 15px;
  cursor: pointer;
}

.calendar-navigation button:hover {
  border-color: #d65427;
  color: #d65427;
}

.calendar-navigation .today-button {
  width: 60px;
  padding: 0;
  color: #d65427;
  font-size: 12px;
  font-weight: 600;
}

.week-calendar {
  display: grid;
  grid-template-columns:
    repeat(7, minmax(0, 1fr));
  box-sizing: border-box;
  width: 100%;
  min-height: 195px;
  padding-top: 10px;
  overflow: visible;
}

.day-column {
  box-sizing: border-box;
  min-width: 0;
  min-height: 185px;
  padding: 7px;
  overflow: visible;
  border-right: 1px solid #edf0f3;
}

.day-column:last-child {
  border-right: 0;
}

.day-column.today {
  border-radius: 6px;
  background: #fff7f3;
}

.day-header {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 34px;
  white-space: nowrap;
}

.date-label {
  color: #6b7280;
  font-size: 13px;
  font-weight: 500;
  line-height: 1.4;
  white-space: nowrap;
}

.today .date-label {
  padding: 6px 10px;
  border-radius: 15px;
  background: #d65427;
  color: #ffffff;
  font-weight: 600;
}

.reflection-list {
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  width: 100%;
  height: 135px;
  margin-top: 6px;
  gap: 6px;
  overflow-x: hidden;
  overflow-y: auto;
  scrollbar-gutter: stable;
}

.reflection-list::-webkit-scrollbar {
  width: 4px;
}

.reflection-list::-webkit-scrollbar-track {
  background: transparent;
}

.reflection-list::-webkit-scrollbar-thumb {
  border-radius: 4px;
  background: #d8dce2;
}

.reflection-item {
  display: flex;
  align-items: flex-start;
  box-sizing: border-box;
  width: 100%;
  min-width: 0;

  /*
   * 글자가 여러 줄이면 항목 높이도
   * 함께 늘어나도록 고정 높이를 제거
   */
  min-height: 32px;
  height: auto;
  flex-shrink: 0;

  gap: 5px;
  padding: 7px 6px;
  overflow: visible;
  border-radius: 4px;
  background: #fff1eb;
}

.completed-dot {
  width: 6px;
  height: 6px;
  margin-top: 6px;
  flex-shrink: 0;
  border-radius: 50%;
  background: #d65427;
}

/*
 * 강의명을 한 줄로 자르지 않고
 * 필요한 만큼 줄바꿈해서 표시
 */
.reflection-title {
  min-width: 0;
  flex: 1;
  overflow: visible;
  color: #5f6570;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.45;
  text-overflow: clip;
  white-space: normal;
  word-break: keep-all;
  overflow-wrap: anywhere;
}

.empty-reflection {
  color: #c5c9d0;
  font-size: 11px;
  line-height: 32px;
  text-align: center;
  white-space: nowrap;
}

@media (max-width: 900px) {
  .weekly-calendar-card {
    min-width: 760px;
    min-height: 270px;
  }

  .calendar-container {
    overflow-x: auto;
  }

  .week-calendar {
    min-width: 720px;
  }
}
</style>