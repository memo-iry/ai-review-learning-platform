<script setup>
import {
  computed,
  onMounted,
  ref,
} from 'vue'

import { useRouter } from 'vue-router'
import { api } from '@/api/client.js'
import { selectLecture } from '@/stores/learning.js'

import PageHeading from '@/components/common/PageHeading.vue'
import AppSection from '@/components/common/AppSection.vue'
import StatCard from '@/components/common/StatCard.vue'
import RecordRow from '@/components/common/RecordRow.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import AppLayout from '@/components/AppLayout.vue'
import { currentUserId } from '../stores/auth.js'
import PageContainer from '@/components/common/PageContainer.vue'

const router = useRouter()


// TODO: 로그인 사용자 식별자는 인증 상태가 생기면 그쪽에서 받아오도록 교체
const userId = currentUserId()


const reflections = ref([])
const lectureMap = ref({})
const averageUnderstanding = ref(null)
const loading = ref(true)
const error = ref('')

const totalCount = computed(() => {
  return reflections.value.length
})

const thisMonthCount = computed(() => {
  const now = new Date()

  return reflections.value.filter((reflection) => {
    const created = new Date(
      reflection.createdAt,
    )

    if (Number.isNaN(created.getTime())) {
      return false
    }

    return (
      created.getFullYear() ===
      now.getFullYear() &&
      created.getMonth() === now.getMonth()
    )
  }).length
})

function getLectureId(data) {
  return (
    data?.lectureId ??
    data?.courseId ??
    data?.lecture?.lectureId ??
    data?.lecture?.id ??
    data?.course?.courseId ??
    data?.course?.id
  )
}

function formatDate(value) {
  if (!value) {
    return '-'
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return '-'
  }

  const year = date.getFullYear()
  const month = String(
    date.getMonth() + 1,
  ).padStart(2, '0')
  const day = String(
    date.getDate(),
  ).padStart(2, '0')

  return `${year}.${month}.${day}`
}

const rows = computed(() => {
  return reflections.value.map((reflection) => {
    const lectureId =
      getLectureId(reflection)

    const lecture =
      lectureMap.value[String(lectureId)] ??
      reflection.lecture ??
      reflection.course ??
      {}

    return {
      reflectionId:
        reflection.reflectionId ??
        reflection.id,

      lectureId,

      lecture,

      week:
        lecture.week ??
        lecture.weekNumber ??
        lecture.order ??
        '',

      title:
        lecture.title ??
        lecture.lectureName ??
        lecture.courseName ??
        lecture.name ??
        `강의 #${lectureId}`,

      date: formatDate(reflection.createdAt),
    }
  })
})

async function loadHistory() {
  loading.value = true
  error.value = ''

  try {
    const [
      reflectionResponse,
      lectureResponse,
      masteryResponse,
    ] = await Promise.all([
      api.getReflections(userId),
      api.getLectures(),
      api.getMastery(userId),
    ])

    reflections.value =
      Array.isArray(reflectionResponse)
        ? reflectionResponse
        : reflectionResponse?.data ?? []

    const lectures =
      Array.isArray(lectureResponse)
        ? lectureResponse
        : lectureResponse?.data ?? []

    lectureMap.value = Object.fromEntries(
      lectures.map((lecture) => [
        String(getLectureId(lecture)),
        lecture,
      ]),
    )

    const masteryList =
      Array.isArray(masteryResponse)
        ? masteryResponse
        : masteryResponse?.data ?? []

    if (masteryList.length > 0) {
      const total = masteryList.reduce(
        (sum, item) =>
          sum + Number(item.score ?? 0),
        0,
      )

      averageUnderstanding.value =
        Math.round(
          total / masteryList.length,
        )
    }
  } catch (requestError) {
    console.error(
      '회고 기록 조회 실패',
      requestError,
    )

    error.value =
      requestError.message ??
      '회고 기록을 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

function goToAnalysis(row) {
  if (!row.lectureId) {
    console.error(
      'lectureId가 없습니다.',
      row,
    )
    return
  }

  if (!row.reflectionId) {
    console.error(
      'reflectionId가 없습니다.',
      row,
    )
    return
  }

  if (row.lecture) {
    selectLecture(row.lecture)
  }

  router.push({
    name: 'analysis',

    params: {
      lectureId: String(row.lectureId),
    },

    query: {
      reflectionId: String(
        row.reflectionId,
      ),
    },
  })
}

onMounted(loadHistory)
</script>

<template>
  <AppLayout>
    <PageContainer>
      <main class="my-reflection-page">
        <PageHeading
          title="회고 기록"
        />

        <AppSection title="학습 및 회고 현황">
          <div class="my-reflection-page__stats">
            <StatCard
              label="총 회고 수"
              :value="`${totalCount}건`"
              :badge="`이번 달 ${thisMonthCount}건`"
            />

            <StatCard
              label="평균 이해도"
              :value="
                averageUnderstanding !== null
                  ? `${averageUnderstanding}%`
                  : '-'
              "
            />
          </div>
        </AppSection>

        <AppSection
          title="전체 회고 기록"
          class="my-reflection-page__records"
        >
          <p
            v-if="loading"
            class="my-reflection-page__message"
          >
            회고 기록을 불러오는 중입니다.
          </p>

          <div
            v-else-if="rows.length"
            class="my-reflection-page__scroll"
          >
            <ul class="my-reflection-page__list">
              <RecordRow
                v-for="row in rows"
                :key="row.reflectionId"
                :category="
                  row.week
                    ? `${row.week}주차`
                    : ''
                "
                :title="row.title"
                :date="row.date"
                action-label="결과창으로 가기 →"
                @action="goToAnalysis(row)"
              />
            </ul>
          </div>

          <EmptyState
            v-else
            title="아직 작성한 회고가 없습니다"
            description="강의를 듣고 회고를 남기면 여기에 모아서 보여드려요."
          />
        </AppSection>

        <p
          v-if="error"
          class="my-reflection-page__error"
        >
          {{ error }}
        </p>
      </main>
    </PageContainer>
  </AppLayout>
</template>

<style scoped>
.my-reflection-page {
  display: flex;
  flex-direction: column;
  width: 100%;
  min-width: 0;
  min-height: 0;
  box-sizing: border-box;
}

/*
 * max-width, margin, padding은
 * PageContainer가 공통으로 관리합니다.
 */
.my-reflection-page__stats {
  display: grid;
  grid-template-columns:
    repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}

.my-reflection-page__records {
  display: flex;
  flex: 1 1 auto;
  flex-direction: column;
  min-height: 0;
}

.my-reflection-page__scroll {
  flex: 1 1 auto;
  max-height: 520px;
  padding-right: var(--space-2);
  overflow-y: auto;
}

.my-reflection-page__list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  margin: 0;
  padding: 0;
  list-style: none;
}

.my-reflection-page__message {
  padding: 30px 0;
  color: #9ca3af;
  font-size: 13px;
  text-align: center;
}

.my-reflection-page__error {
  margin-top: var(--space-5);
  color: var(--color-warning);
  font-size: var(--text-sm);
}

@media (max-width: 640px) {
  .my-reflection-page__stats {
    grid-template-columns: 1fr;
  }
}
</style>