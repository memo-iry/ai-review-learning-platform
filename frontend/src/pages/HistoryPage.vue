<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '@/api/client.js'

import PageHeading from '@/components/common/PageHeading.vue'
import AppSection from '@/components/common/AppSection.vue'
import StatCard from '@/components/common/StatCard.vue'
import RecordRow from '@/components/common/RecordRow.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import AppLayout from '@/components/AppLayout.vue'

const router = useRouter()

// TODO: 로그인 사용자 식별자는 인증 상태가 생기면 그쪽에서 받아오도록 교체
const userId = 2

const reflections = ref([])
const lectureMap = ref({})
const averageUnderstanding = ref(null)
const loading = ref(true)
const error = ref('')

const totalCount = computed(() => reflections.value.length)

const thisMonthCount = computed(() => {
  const now = new Date()
  return reflections.value.filter((reflection) => {
    const created = new Date(reflection.createdAt)
    return (
      created.getFullYear() === now.getFullYear() &&
      created.getMonth() === now.getMonth()
    )
  }).length
})

const rows = computed(() =>
  reflections.value.map((reflection) => {
    const lecture = lectureMap.value[reflection.lectureId] ?? {}
    return {
      id: reflection.reflectionId,
      lectureId: reflection.lectureId,
      week: lecture.week ?? lecture.weekNumber ?? lecture.order ?? '',
      title: lecture.title ?? `강의 #${reflection.lectureId}`,
      date: formatDate(reflection.createdAt),
    }
  }),
)

function formatDate(value) {
  const date = new Date(value)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}.${m}.${d}`
}

onMounted(async () => {
  try {
    const [reflectionList, lectureList] = await Promise.all([
      api.getReflections(userId),
      api.getLectures(),
    ])

    reflections.value = reflectionList
    lectureMap.value = Object.fromEntries(
      lectureList.map((lecture) => [lecture.lectureId, lecture]),
    )

    const mastery = await api.getMastery(userId)
    if (mastery.length) {
      const total = mastery.reduce((sum, item) => sum + item.score, 0)
      averageUnderstanding.value = Math.round(total / mastery.length)
    }
  } catch (requestError) {
    error.value = requestError.message
  } finally {
    loading.value = false
  }
})

function goToQuiz(lectureId) {
  // TODO: 회고 목록 API에 연결된 quizId가 없어 임시로 회고 작성 화면으로 이동합니다.
  // 실제 퀴즈 화면 라우트가 정해지면 여기만 바꾸면 됩니다.
  router.push({ name: 'quiz', params: { lectureId } })
}
</script>

<template>
  <section>
    <AppLayout />
  </section>
  <div class="my-reflection-page">
    <PageHeading title="나의 회고록" />

    <AppSection title="학습 및 회고 현황">
      <div class="my-reflection-page__stats">
        <StatCard label="총 회고 수" :value="`${totalCount}건`" :badge="`이번 달 ${thisMonthCount}건`" />
        <StatCard label="평균 이해도" :value="averageUnderstanding !== null ? `${averageUnderstanding}%` : '-'" />
      </div>
    </AppSection>

    <AppSection title="전체 회고 기록" class="my-reflection-page__records">
      <div v-if="rows.length" class="my-reflection-page__scroll">
        <ul class="my-reflection-page__list">
          <RecordRow v-for="row in rows" :key="row.id" :category="row.week ? `${row.week}주차` : ''" :title="row.title"
            :date="row.date" action-label="퀴즈 풀러가기 →" @action="goToQuiz(row.lectureId)" />
        </ul>
      </div>

      <EmptyState v-else-if="!loading" title="아직 작성한 회고가 없습니다" description="강의를 듣고 회고를 남기면 여기에 모아서 보여드려요." />
    </AppSection>

    <p v-if="error" class="my-reflection-page__error">{{ error }}</p>
  </div>
</template>

<style scoped>
.my-reflection-page {
  max-width: 960px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-6);
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.my-reflection-page__stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}

/* "전체 회고 기록" 섹션만 남은 공간을 채우고, 그 안에서 스크롤됩니다. */
.my-reflection-page__records {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.my-reflection-page__scroll {
  flex: 1 1 auto;
  min-height: 0;
  max-height: 520px;
  overflow-y: auto;
  padding-right: var(--space-2);
}

.my-reflection-page__list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  list-style: none;
  margin: 0;
  padding: 0;
}

.my-reflection-page__error {
  margin-top: var(--space-5);
  color: var(--color-warning);
  font-size: var(--text-sm);
}

@media (max-width: 640px) {
  .my-reflection-page {
    padding: var(--space-6) var(--space-4);
  }

  .my-reflection-page__stats {
    grid-template-columns: 1fr;
  }

  .my-reflection-page__scroll {
    max-height: 60vh;
  }
}
</style>