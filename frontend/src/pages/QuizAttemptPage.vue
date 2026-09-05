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
const submitting = ref(false)
const error = ref('')

// 채점 결과는 제출 응답에만 들어 있다. 별도 화면으로 라우팅하면 넘길 방법이 없고
// 새로고침하면 사라지므로, 같은 페이지에서 문항 화면과 결과 화면을 바꿔 보여준다.
const result = ref(null)

const LEVEL_NAMES = ['', '인지', '이해', '적용', '구현']

const allAnswered = computed(() => answers.value.every((a) => a >= 0))

const currentQuestion = computed(() => quiz.value?.questions[currentIndex.value] ?? null)
const totalCount = computed(() => quiz.value?.questions.length ?? 0)
const progressPercent = computed(() =>
  totalCount.value ? ((currentIndex.value + 1) / totalCount.value) * 100 : 0,
)

// 문항당 대략 1분으로 어림잡은 값입니다. 실제 소요 시간 데이터가 없어
// 정확한 값은 아니고, 대략적인 감을 주는 용도입니다.
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
  submitting.value = true
  error.value = ''
  try {
    const userId = currentUserId()
    result.value = await api.submitQuiz(quiz.value.quizId, {
      userId,
      answers: answers.value,
    })
  } catch (requestError) {
    error.value = requestError.message
  } finally {
    submitting.value = false
  }
}

function goToQuizList() {
  router.push({ name: 'quiz' })
}

function goToDashboard() {
  router.push({ name: 'dashboard' })
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

      <!-- 로딩 중: 실제 2단 레이아웃과 같은 크기로 배치한 스켈레톤 -->
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


        <!-- 채점 결과 -->
        <div v-else-if="result" class="quiz-result">
          <BaseCard padding="lg" class="quiz-result__score">
            <p class="quiz-result__score-label">채점 결과</p>
            <p class="quiz-result__score-value">
              <strong>{{ result.correctCount }}</strong> / {{ result.totalCount }}
              <span class="quiz-result__score-point">{{ result.score }}점</span>
            </p>
            <ProgressBar :value="result.score" />
          </BaseCard>

          <section class="quiz-result__section">
            <h3 class="quiz-result__heading">문항별 결과</h3>
            <BaseCard
              v-for="(item, index) in result.results"
              :key="index"
              padding="lg"
              :tone="item.correct ? 'accent' : 'warning'"
              class="quiz-result__item"
            >
              <div class="quiz-result__item-head">
                <TagBadge :tone="item.correct ? 'accent' : 'warning'">
                  {{ item.correct ? '정답' : '오답' }}
                </TagBadge>
                <span class="quiz-result__item-concept">{{ item.conceptName }}</span>
              </div>

              <p class="quiz-result__question">{{ index + 1 }}. {{ item.question }}</p>

              <p class="quiz-result__answer">
                내 답 · {{ item.options[item.selectedIndex] ?? '미선택' }}
              </p>
              <p v-if="!item.correct" class="quiz-result__answer quiz-result__answer--correct">
                정답 · {{ item.options[item.answerIndex] }}
              </p>

              <p class="quiz-result__explanation">{{ item.explanation }}</p>
            </BaseCard>
          </section>

          <section v-if="result.masteryChanges?.length" class="quiz-result__section">
            <h3 class="quiz-result__heading">이해도 반영</h3>
            <BaseCard padding="lg">
              <p class="quiz-result__mastery-note">
                이번 채점 결과가 개념별 이해도에 반영되었습니다.
              </p>
              <ul class="quiz-result__mastery">
                <li
                  v-for="change in result.masteryChanges"
                  :key="change.conceptName"
                  class="quiz-result__mastery-row"
                >
                  <span class="quiz-result__mastery-name">{{ change.conceptName }}</span>
                  <span class="quiz-result__mastery-score">
                    {{ change.scoreBefore }}%
                    <span
                      class="quiz-result__arrow"
                      :class="change.scoreAfter >= change.scoreBefore
                        ? 'quiz-result__arrow--up'
                        : 'quiz-result__arrow--down'"
                    >{{ change.scoreAfter >= change.scoreBefore ? '↑' : '↓' }}</span>
                    {{ change.scoreAfter }}%
                  </span>
                  <span
                    v-if="change.levelBefore !== change.levelAfter"
                    class="quiz-result__mastery-level"
                  >
                    Level {{ change.levelBefore }} → {{ change.levelAfter }}
                    {{ LEVEL_NAMES[change.levelAfter] }}
                  </span>
                </li>
              </ul>
            </BaseCard>
          </section>

          <div class="quiz-result__nav">
            <BaseButton variant="outline" @click="goToQuizList">퀴즈 목록</BaseButton>
            <BaseButton variant="primary" @click="goToDashboard">대시보드로 →</BaseButton>
          </div>
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
              <BaseButton
                variant="primary"
                :disabled="submitting || (isLastQuestion && !allAnswered)"
                @click="goNext"
              >
                {{ submitting ? '채점 중...' : isLastQuestion ? '제출하기' : '다음 문제' }} →
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

/* ── 채점 결과 ───────────────────────────────── */
.quiz-result {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
  max-width: 720px;
}

.quiz-result__score-label {
  margin: 0 0 var(--space-2);
  color: var(--color-text-subtle);
  font-size: var(--text-xs);
  font-weight: 700;
  letter-spacing: 0.04em;
}

.quiz-result__score-value {
  display: flex;
  align-items: baseline;
  gap: var(--space-3);
  margin: 0 0 var(--space-4);
  font-family: var(--font-display);
  font-size: var(--text-lg);
}

.quiz-result__score-value strong {
  color: var(--color-accent);
  font-size: var(--text-2xl);
}

.quiz-result__score-point {
  margin-left: auto;
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.quiz-result__section {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.quiz-result__heading {
  margin: 0;
  font-size: var(--text-sm);
  font-weight: 700;
  color: var(--color-text-muted);
}

.quiz-result__item-head {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}

.quiz-result__item-concept {
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.quiz-result__question {
  margin: 0 0 var(--space-3);
  font-size: var(--text-md);
  font-weight: 600;
}

.quiz-result__answer {
  margin: 0 0 var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.quiz-result__answer--correct {
  color: var(--color-accent);
  font-weight: 600;
}

.quiz-result__explanation {
  margin: var(--space-3) 0 0;
  padding-top: var(--space-3);
  border-top: 1px solid var(--color-border);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.quiz-result__mastery-note {
  margin: 0 0 var(--space-4);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.quiz-result__mastery {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.quiz-result__mastery-row {
  display: flex;
  align-items: baseline;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.quiz-result__mastery-name {
  font-weight: 600;
  min-width: 8rem;
}

.quiz-result__mastery-score {
  font-variant-numeric: tabular-nums;
  color: var(--color-text-muted);
}

.quiz-result__arrow {
  margin: 0 var(--space-1);
  font-weight: 700;
}

.quiz-result__arrow--up {
  color: var(--color-accent);
}

.quiz-result__arrow--down {
  color: var(--color-warning);
}

.quiz-result__mastery-level {
  margin-left: auto;
  color: var(--color-accent);
  font-size: var(--text-sm);
  font-weight: 600;
}

.quiz-result__nav {
  display: flex;
  justify-content: space-between;
  gap: var(--space-4);
}

@media (max-width: 900px) {
  .quiz-attempt-page__body {
    grid-template-columns: 1fr;
  }
}
</style>
