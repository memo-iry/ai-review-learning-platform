<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '@/api/client.js'
import { selectLecture } from '@/stores/learning.js'
import Skeleton from '@/components/common/Skeleton.vue'
import PageHeading from '@/components/common/PageHeading.vue'

const router = useRouter()

const lectures = ref([])
const sortType = ref('week')
const isLoading = ref(true)
const errorMessage = ref('')

function getLectureId(lecture) {
  return lecture.lectureId ?? lecture.id
}

function getLectureTitle(lecture) {
  return (
    lecture.title ??
    lecture.name ??
    lecture.lectureName ??
    '강의 제목 없음'
  )
}

function getLectureDescription(lecture) {
  return (
    lecture.description ??
    lecture.lectureDescription ??
    '강의 자료를 확인하고 회고록을 작성해 보세요.'
  )
}

function getLectureDate(lecture) {
  return (
    lecture.lectureDate ??
    lecture.scheduledAt ??
    lecture.date ??
    lecture.createdAt ??
    lecture.createdDate ??
    null
  )
}

function getDateTimestamp(lecture) {
  const date = getLectureDate(lecture)

  if (!date) {
    return 0
  }

  const timestamp = Date.parse(date)

  return Number.isNaN(timestamp) ? 0 : timestamp
}

function formatLectureDate(lecture) {
  const date = getLectureDate(lecture)

  if (!date) {
    return '날짜 미정'
  }

  const parsedDate = new Date(date)

  if (Number.isNaN(parsedDate.getTime())) {
    return '날짜 미정'
  }

  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(parsedDate)
}

async function loadLectures() {
  try {
    const response = await api.getLectures()

    const lectureList = Array.isArray(response)
      ? response
      : response.data ?? []

    // 날짜가 오래된 강의부터 정렬
    const chronologicalLectures = [...lectureList].sort(
      (first, second) => {
        const firstDate = getDateTimestamp(first)
        const secondDate = getDateTimestamp(second)

        if (
          firstDate > 0 &&
          secondDate > 0 &&
          firstDate !== secondDate
        ) {
          return firstDate - secondDate
        }

        // 날짜가 없거나 같은 날짜면 ID가 작은 강의를 먼저 배치
        return (
          Number(getLectureId(first)) -
          Number(getLectureId(second))
        )
      },
    )

    // 가장 오래된 강의부터 1주차 부여
    lectures.value = chronologicalLectures.map(
      (lecture, index) => ({
        ...lecture,
        week: index + 1,
      }),
    )
  } catch (error) {
    console.error('강의 목록 조회 실패', error)
    errorMessage.value = '강의 목록을 불러오지 못했습니다.'
  } finally {
    isLoading.value = false
  }
}

const sortedLectures = computed(() => {
  const copiedLectures = [...lectures.value]

  if (sortType.value === 'latest') {
    // 최신 날짜, 높은 주차부터 출력
    return copiedLectures.sort(
      (first, second) => second.week - first.week,
    )
  }

  // 오래된 날짜, 1주차부터 출력
  return copiedLectures.sort(
    (first, second) => first.week - second.week,
  )
})

function moveToReflection(lecture) {
  selectLecture(lecture)

  router.push(
    `/reflection/${getLectureId(lecture)}`,
  )
}

onMounted(loadLectures)
</script>

<template>
  <section class="course-list-page">
    <header class="page-header">
      <PageHeading title="강의 목록" />

      <div class="sort-toggle" :class="{ week: sortType === 'week' }">
        <button type="button" :class="{ active: sortType === 'latest' }" @click="sortType = 'latest'">
          최신순
        </button>

        <button type="button" :class="{ active: sortType === 'week' }" @click="sortType = 'week'">
          주차순
        </button>
      </div>
    </header>

    <p class="section-label">
      전체 강의 목록
    </p>

    <!-- 로딩 중: 실제 강의 카드와 같은 크기·배치로 자리를 잡아둡니다. -->
    <div v-if="isLoading" class="lecture-list">
      <article v-for="placeholder in 4" :key="placeholder" class="lecture-item">
        <div class="lecture-content">
          <Skeleton width="48px" height="17px" radius="10px" />
          <Skeleton width="230px" height="16px" style="margin: 9px 0 6px" />
          <Skeleton width="320px" height="11px" />
        </div>

        <div class="lecture-side">
          <Skeleton width="70px" height="10px" />
          <Skeleton width="78px" height="10px" />
        </div>
      </article>
    </div>

    <p v-else-if="errorMessage" class="message">
      {{ errorMessage }}
    </p>

    <p v-else-if="sortedLectures.length === 0" class="message">
      등록된 강의가 없습니다.
    </p>

    <div v-else class="lecture-list">
      <article v-for="lecture in sortedLectures" :key="getLectureId(lecture)" class="lecture-item">
        <div class="lecture-content">
          <span class="week-badge">
            {{ lecture.week }}주차
          </span>

          <h2>
            {{ getLectureTitle(lecture) }}
          </h2>

          <p>
            {{ getLectureDescription(lecture) }}
          </p>
        </div>

        <div class="lecture-side">
          <time class="lecture-date">
            {{ formatLectureDate(lecture) }}
          </time>

          <button type="button" class="reflection-button" @click="moveToReflection(lecture)">
            회고록 작성 →
          </button>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.course-list-page {
  width: 100%;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-header h1 {
  margin: 0;
  color: #434b5a;
  font-family: "AppleMyungjo", "Noto Serif KR", serif;
  font-size: 25px;
  font-weight: 400;
}

.sort-toggle {
  position: relative;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  width: 106px;
  padding: 3px;
  border: 1px solid #e5e7eb;
  border-radius: 18px;
  background: #f6f7f9;
}

.sort-toggle::before {
  position: absolute;
  top: 3px;
  left: 3px;
  width: calc(50% - 3px);
  height: calc(100% - 6px);
  border-radius: 14px;
  background: #d65427;
  content: "";
  transition: transform 0.2s ease;
}

.sort-toggle.week::before {
  transform: translateX(100%);
}

.sort-toggle button {
  position: relative;
  z-index: 1;
  border: 0;
  padding: 6px 0;
  background: transparent;
  color: #9ca3af;
  font-size: 10px;
  cursor: pointer;
}

.sort-toggle button.active {
  color: #ffffff;
}

.section-label {
  margin: 25px 0 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #edf0f3;
  color: #9ca3af;
  font-size: 11px;
}

.lecture-list {
  display: flex;
  flex-direction: column;
  gap: 13px;
}

.lecture-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  min-height: 96px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 7px;
  background: #ffffff;
}

.lecture-content {
  min-width: 0;
}

.week-badge {
  display: inline-block;
  padding: 2px 8px;
  border: 1px solid #f2d7ce;
  border-radius: 10px;
  color: #d65427;
  font-size: 10px;
}

.lecture-content h2 {
  margin: 9px 0 6px;
  overflow: hidden;
  color: #4b5563;
  font-family: "AppleMyungjo", "Noto Serif KR", serif;
  font-size: 16px;
  font-weight: 400;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.lecture-content p {
  margin: 0;
  overflow: hidden;
  color: #9ca3af;
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.lecture-side {
  display: flex;
  flex-direction: column;
  align-items: end;
  justify-content: space-between;
  align-self: stretch;
  flex-shrink: 0;
}

.lecture-date {
  color: #9ca3af;
  font-size: 10px;
}

.reflection-button {
  border: 0;
  padding: 0;
  background: transparent;
  color: #4b5563;
  font-size: 10px;
  font-weight: 600;
  cursor: pointer;
}

.message {
  margin: 30px 0;
  color: #9ca3af;
  font-size: 13px;
  text-align: center;
}

.page-heading__title {
  margin: 0;
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: 400;
  line-height: 1.35;
  letter-spacing: -0.01em;
}
</style>