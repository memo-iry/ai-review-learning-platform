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
  height: 220px;
  min-height: 220px;
  max-height: 220px;
  overflow: hidden;
}

.calendar-container {
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  min-width: 0;
  overflow: hidden;
}

.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
  height: 42px;
  padding-bottom: 9px;
  border-bottom: 1px solid #edf0f3;
}

.calendar-header h3 {
  margin: 0;
  color: #4b5563;
  font-size: 14px;
  font-weight: 500;
}

.calendar-header p {
  margin: 4px 0 0;
  color: #9ca3af;
  font-size: 10px;
}

.calendar-navigation {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 5px;
}

.calendar-navigation button {
  min-width: 26px;
  height: 26px;
  border: 1px solid #e5e7eb;
  border-radius: 13px;
  background: #ffffff;
  color: #6b7280;
  cursor: pointer;
}

.calendar-navigation .today-button {
  width: 52px;
  padding: 0;
  color: #d65427;
  font-size: 10px;
}

.week-calendar {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  box-sizing: border-box;
  width: 100%;
  height: 150px;
  padding-top: 10px;
  overflow: hidden;
}

.day-column {
  box-sizing: border-box;
  min-width: 0;
  height: 140px;
  padding: 6px;
  overflow: hidden;
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
  height: 30px;
  white-space: nowrap;
}

.date-label {
  color: #6b7280;
  font-size: 10px;
  white-space: nowrap;
}

.today .date-label {
  padding: 5px 8px;
  border-radius: 13px;
  background: #d65427;
  color: #ffffff;
}

.reflection-list {
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  height: 92px;
  margin-top: 5px;
  gap: 5px;
  overflow-x: hidden;
  overflow-y: scroll;
  scrollbar-gutter: stable;
}

.reflection-list::-webkit-scrollbar {
  width: 3px;
}

.reflection-list::-webkit-scrollbar-thumb {
  border-radius: 3px;
  background: #d8dce2;
}

.reflection-item {
  display: flex;
  align-items: center;
  box-sizing: border-box;
  width: 100%;
  min-width: 0;
  min-height: 27px;
  flex-shrink: 0;
  gap: 4px;
  overflow: hidden;
  padding: 5px;
  border-radius: 3px;
  background: #fff1eb;
}

.completed-dot {
  width: 5px;
  height: 5px;
  flex-shrink: 0;
  border-radius: 50%;
  background: #d65427;
}

.reflection-title {
  min-width: 0;
  overflow: hidden;
  color: #5f6570;
  font-size: 9px;
  line-height: 17px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-reflection {
  color: #c5c9d0;
  font-size: 8px;
  line-height: 27px;
  text-align: center;
  white-space: nowrap;
}
</style>
