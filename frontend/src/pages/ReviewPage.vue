<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/client.js'
import { learningState } from '../stores/learning.js'
import AppLayout from '../components/AppLayout.vue'
import PageContainer from '../components/common/PageContainer.vue'
import { currentUserId } from '../stores/auth.js'

const router = useRouter()

const review = computed(() => learningState.analysis?.reviewMaterial ?? null)
const selected = reactive({})
const result = ref(null)
const loading = ref(false)
const error = ref('')

const allAnswered = computed(() => {
  if (!review.value) return false
  return review.value.quiz.every((_, index) => selected[index] !== undefined)
})

async function submitQuiz() {
  loading.value = true
  error.value = ''
  try {
    const answers = review.value.quiz.map((_, index) => selected[index])
    result.value = await api.submitQuiz(review.value.quizId, {
      userId: currentUserId(),
      answers,
    })
  } catch (requestError) {
    error.value = requestError.message
  } finally {
    loading.value = false
  }
}

function completeReview() {
  router.push({ name: 'growth' })
}
</script>

<template>
  <AppLayout>
    <PageContainer>
      <div v-if="review">
        <section class="page-heading">
          <div>
            <p class="eyebrow">맞춤 복습</p>
            <h1>{{ review.title }}</h1>
          </div>
        </section>

        <section class="review-stack">
          <article class="panel">
            <h2>핵심 개념</h2>

            <ol>
              <li v-for="concept in review.coreConcepts" :key="concept">
                {{ concept }}
              </li>
            </ol>
          </article>

          <article class="panel">
            <h2>예제 코드</h2>
            <pre><code>{{ review.exampleCode }}</code></pre>
          </article>

          <article class="panel">
            <h2>확인 문제</h2>

            <div v-if="!result" class="quiz-list">
              <fieldset
                v-for="(item, index) in review.quiz"
                :key="item.question"
                class="quiz-item"
              >
                <legend>{{ index + 1 }}. {{ item.question }}</legend>
                <span class="tag">{{ item.conceptName }}</span>

                <label
                  v-for="(option, optionIndex) in item.options"
                  :key="option"
                  class="quiz-option"
                >
                  <input
                    type="radio"
                    :name="'q' + index"
                    :value="optionIndex"
                    @change="selected[index] = optionIndex"
                  >
                  {{ option }}
                </label>
              </fieldset>

              <p v-if="error" class="error-message">{{ error }}</p>

              <button
                class="primary-button"
                type="button"
                :disabled="!allAnswered || loading"
                @click="submitQuiz"
              >
                {{ loading ? '채점 중...' : '제출하고 채점하기' }}
              </button>
            </div>

            <div v-else class="quiz-list">
              <p class="quiz-score">
                {{ result.correctCount }} / {{ result.totalCount }} 정답 · {{ result.score }}점
              </p>

              <div
                v-for="(item, index) in result.results"
                :key="item.question"
                class="quiz-item"
                :class="item.correct ? 'quiz-correct' : 'quiz-wrong'"
              >
                <strong>{{ index + 1 }}. {{ item.question }}</strong>
                <p>내 답: {{ item.options[item.selectedIndex] ?? '미선택' }}</p>
                <p v-if="!item.correct">정답: {{ item.options[item.answerIndex] }}</p>
                <p class="quiz-explanation">{{ item.explanation }}</p>
              </div>

              <div class="mastery-changes">
                <h3>이해도 반영</h3>
                <p
                  v-for="change in result.masteryChanges"
                  :key="change.conceptName"
                >
                  {{ change.conceptName }}
                  {{ change.scoreBefore }}% → {{ change.scoreAfter }}%
                </p>
              </div>
            </div>
          </article>
        </section>

        <button
          class="primary-button"
          type="button"
          :disabled="!result"
          @click="completeReview"
        >
          복습 완료 기록
        </button>
      </div>

      <section v-else class="empty-state">
        <h1>복습자료가 없습니다.</h1>
        <p>AI 분석을 완료하면 맞춤형 복습자료가 생성됩니다.</p>

        <button
          class="primary-button"
          type="button"
          @click="router.push({ name: 'reflection' })"
        >
          회고록 작성하기
        </button>
      </section>
    </PageContainer>
  </AppLayout>
</template>
