<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '@/api/client.js'

import BaseCard from '@/components/common/BaseCard.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import TagBadge from '@/components/common/TagBadge.vue'
import ProgressBar from '@/components/common/ProgressBar.vue'
import Skeleton from '@/components/common/Skeleton.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import QuizOptionRow from '@/components/quiz/QuizOptionRow.vue'
import QuizTutorPanel from '@/components/quiz/QuizTutorPanel.vue'
import QuizStatusCard from '@/components/quiz/QuizStatusCard.vue'
import AppLayout from '@/components/AppLayout.vue'
import PageContainer from '@/components/common/PageContainer.vue'
import { currentUserId } from '../stores/auth.js'

const route = useRoute()
const router = useRouter()

const quiz = ref(null)
const answers = ref([])
const currentIndex = ref(0)
const loading = ref(true)
const error = ref('')

const currentQuestion = computed(() => quiz.value?.questions[currentIndex.value] ?? null)
const totalCount = computed(() => quiz.value?.questions.length ?? 0)
const progressPercent = computed(() =>
  totalCount.value ? ((currentIndex.value + 1) / totalCount.value) * 100 : 0,
)

const estimatedTimeLeftLabel = computed(() => {
  const remaining = totalCount.value - (currentIndex.value + 1)
  return remaining <= 0 ? '곧 종료' : `${remaining}분 남음`
})

const isLastQuestion = computed(() => currentIndex.value === totalCount.value - 1)

onMounted(async () => {
  try {
    const quizId = route.params.quizId
    const response = await api.getQuiz(quizId)
    quiz.value = response
    answers.value = Array(response.questions.length).fill(-1)
  } catch (requestError) {
    error.value = requestError.message
  } finally {
    loading.value = false
  }
})

function selectAnswer(optionIndex) {
  answers.value[currentIndex.value] = optionIndex
}

function goPrev() {
  if (currentIndex.value > 0) currentIndex.value -= 1
}

function goNext() {
  if (!isLastQuestion.value) {
    currentIndex.value += 1
    return
  }

  submitAnswers()
}

async function submitAnswers() {
  try {
    const userId = currentUserId()
    await api.submitQuiz(quiz.value.quizId, { userId, answers: answers.value })
    router.push({ name: 'quiz' })
  } catch (requestError) {
    error.value = requestError.message
  }
}
</script>

<template>
  <AppLayout>
    <PageContainer size="xl">
      <div class="quiz-attempt-page">
        <div class="quiz-attempt-page__topbar">
          <TagBadge>이해도 확인 Quiz</TagBadge>
          <p class="quiz-attempt-page__topbar-text">방금 복습한 내용을 바탕으로 핵심 개념을 확인해보세요.</p>
        </div>

            <div v-if="loading" class="quiz-attempt-page__body">
        <div class="quiz-attempt-page__main">
          <div class="quiz-attempt-page__meta">
            <Skeleton width="70px" height="0.8em" />
            <Skeleton width="40px" height="0.9em" />
          </div>

          <Skeleton width="100%" height="6px" radius="var(--radius-pill)" />

          <BaseCard padding="lg" class="quiz-attempt-page__question">
            <Skeleton width="120px" height="0.85em" />
            <Skeleton width="85%" height="1.6em" style="margin-top: 14px; margin-bottom: 28px" />
            <div class="quiz-attempt-page__options">
              <Skeleton v-for="n in 4" :key="n" width="100%" height="58px" style="margin-top: 12px" />
            </div>
          </BaseCard>

          <div class="quiz-attempt-page__nav">
            <Skeleton width="110px" height="44px" />
            <Skeleton width="130px" height="44px" />
          </div>
        </div>

        <aside class="quiz-attempt-page__side">
          <BaseCard padding="lg">
            <div class="quiz-attempt-page__tutor-head">
              <Skeleton shape="circle" width="36px" height="36px" />
              <div>
                <Skeleton width="90px" height="0.85em" />
                <Skeleton width="110px" height="0.75em" style="margin-top: 6px" />
              </div>
            </div>
            <Skeleton width="100%" height="1em" style="margin-top: 20px" />
            <Skeleton width="90%" height="1em" style="margin-top: 8px" />
            <Skeleton width="60%" height="1em" style="margin-top: 8px" />
            <Skeleton width="100%" height="40px" style="margin-top: 20px" />
            <Skeleton width="100%" height="40px" style="margin-top: 12px" />
          </BaseCard>

          <BaseCard padding="lg">
            <Skeleton width="110px" height="0.85em" style="margin-bottom: 16px" />
            <div class="quiz-attempt-page__status-skeleton">
              <Skeleton width="100%" height="60px" />
              <Skeleton width="100%" height="60px" />
            </div>
          </BaseCard>
        </aside>
      </div>

        <EmptyState v-else-if="!quiz || !currentQuestion" title="문항을 찾을 수 없습니다" description="퀴즈 목록으로 돌아가 다시 시도해 주세요." />

        <div v-else class="quiz-attempt-page__body">
          <div class="quiz-attempt-page__main">
            <div class="quiz-attempt-page__meta">
              <span class="quiz-attempt-page__meta-label">QUESTION</span>
              <span class="quiz-attempt-page__meta-count">
                <strong>{{ currentIndex + 1 }}</strong> / {{ totalCount }}
              </span>
            </div>

            <ProgressBar :value="progressPercent" />

            <BaseCard padding="lg" class="quiz-attempt-page__question">
              <p class="quiz-attempt-page__concept">ⓘ 핵심 개념 점검</p>
              <h2 class="quiz-attempt-page__prompt">{{ currentQuestion.question }}</h2>

              <div class="quiz-attempt-page__options">
                <QuizOptionRow v-for="(option, optionIndex) in currentQuestion.options" :key="optionIndex"
                  :number="optionIndex + 1" :label="option" :selected="answers[currentIndex] === optionIndex"
                  :name="`question-${currentIndex}`" @select="selectAnswer(optionIndex)" />
              </div>
            </BaseCard>

            <div class="quiz-attempt-page__nav">
              <BaseButton variant="outline" :disabled="currentIndex === 0" @click="goPrev">
                이전 문제
              </BaseButton>
              <BaseButton variant="primary" @click="goNext">
                {{ isLastQuestion ? '제출하기' : '다음 문제' }} →
              </BaseButton>
            </div>
          </div>

          <aside class="quiz-attempt-page__side">
            <QuizTutorPanel :concept-name="currentQuestion.conceptName" />
            <QuizStatusCard :progress="progressPercent" :time-left-label="estimatedTimeLeftLabel" />
          </aside>
        </div>

        <p v-if="error" class="quiz-attempt-page__error">{{ error }}</p>
      </div>
    </PageContainer>
  </AppLayout>
</template>

<style scoped>
.quiz-attempt-page {
}

.quiz-attempt-page__topbar {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding-bottom: var(--space-5);
  margin-bottom: var(--space-6);
  border-bottom: 1px solid var(--color-border);
}

.quiz-attempt-page__topbar-text {
  margin: 0;
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.quiz-attempt-page__body {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: var(--space-6);
  align-items: start;
}

.quiz-attempt-page__meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: var(--space-3);
}

.quiz-attempt-page__meta-label {
  color: var(--color-text-subtle);
  font-size: var(--text-xs);
  font-weight: 700;
  letter-spacing: 0.04em;
}

.quiz-attempt-page__meta-count {
  color: var(--color-text-subtle);
  font-size: var(--text-sm);
}

.quiz-attempt-page__meta-count strong {
  color: var(--color-accent);
  font-size: var(--text-md);
}

.quiz-attempt-page__question {
  margin-top: var(--space-5);
}

.quiz-attempt-page__concept {
  margin: 0 0 var(--space-3);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.quiz-attempt-page__prompt {
  margin: 0 0 var(--space-6);
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: 400;
}

.quiz-attempt-page__nav {
  display: flex;
  justify-content: space-between;
  margin-top: var(--space-7);
}

.quiz-attempt-page__side {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.quiz-attempt-page__tutor-head {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.quiz-attempt-page__status-skeleton {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-3);
}

.quiz-attempt-page__error {
  margin-top: var(--space-5);
  color: var(--color-warning);
  font-size: var(--text-sm);
}

@media (max-width: 900px) {
  .quiz-attempt-page__body {
    grid-template-columns: 1fr;
  }
}
</style>
