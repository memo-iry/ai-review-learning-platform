<script setup>
import {
  computed,
  ref,
  watch,
} from 'vue'

import {
  useRoute,
  useRouter,
} from 'vue-router'

import { api } from '@/api/client.js'
import { learningState } from '@/stores/learning.js'

import AppLayout from '@/components/AppLayout.vue'
import PageContainer from '@/components/common/PageContainer.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import Skeleton from '@/components/common/Skeleton.vue'
import AnalysisScoreCard from '@/components/analysis/AnalysisScoreCard.vue'

const route = useRoute()
const router = useRouter()

const analysis = ref(null)
const loading = ref(true)
const error = ref('')

const lectureId = computed(() => {
  const value = Number(
    route.params.lectureId,
  )

  return Number.isNaN(value)
    ? null
    : value
})

const reflectionId = computed(() => {
  const value =
    route.query.reflectionId

  if (!value) {
    return null
  }

  const parsedValue = Number(value)

  return Number.isNaN(parsedValue)
    ? null
    : parsedValue
})

const review = computed(() => {
  return (
    analysis.value?.reviewMaterial ??
    null
  )
})

const lectureTitle = computed(() => {
  return (
    learningState.lecture?.title ??
    learningState.lecture?.lectureName ??
    learningState.lecture?.courseName ??
    learningState.lecture?.name ??
    review.value?.title ??
    `강의 #${lectureId.value}`
  )
})

const understandingScore = computed(() => {
  return Number(
    analysis.value?.understandingScore ??
    0,
  )
})

const understandingLevel = computed(() => {
  if (understandingScore.value < 40) {
    return 1
  }

  if (understandingScore.value < 80) {
    return 2
  }

  return 3
})

async function loadAnalysis() {
  loading.value = true
  error.value = ''

  try {
    const cachedAnalysis =
      learningState.analysis

    /*
     * ReflectionPage에서 방금 분석한 결과이고
     * reflectionId도 일치하면 API 재호출 없이 표시합니다.
     */
    if (
      cachedAnalysis &&
      reflectionId.value &&
      Number(
        cachedAnalysis.reflectionId,
      ) === reflectionId.value
    ) {
      analysis.value =
        cachedAnalysis

      return
    }

    /*
     * 기존 방식처럼 reflectionId 없이 이동했지만
     * 분석 결과가 메모리에 있으면 해당 결과를 표시합니다.
     */
    if (
      cachedAnalysis &&
      !reflectionId.value
    ) {
      analysis.value =
        cachedAnalysis

      return
    }

    /*
     * HistoryPage에서 이동한 경우에는
     * 선택한 reflectionId로 분석 결과를 조회합니다.
     */
    if (!reflectionId.value) {
      throw new Error(
        '선택한 회고록 ID가 없습니다.',
      )
    }

    const response =
      await api.analyzeReflection(
        reflectionId.value,
      )

    const loadedAnalysis =
      response?.data ??
      response

    if (!loadedAnalysis) {
      throw new Error(
        '분석 결과가 없습니다.',
      )
    }

    analysis.value =
      loadedAnalysis

    learningState.analysis =
      loadedAnalysis
  } catch (requestError) {
    console.error(
      '분석 결과 조회 실패',
      requestError,
    )

    analysis.value = null

    error.value =
      requestError.message ??
      '분석 결과를 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

function moveToDashboard() {
  router.push({
    name: 'dashboard',
  })
}

function moveToReflection() {
  if (!lectureId.value) {
    router.push({
      name: 'dashboard',
    })

    return
  }

  router.push({
    name: 'reflection',

    params: {
      lectureId:
        lectureId.value,
    },
  })
}

watch(
  () => [
    route.params.lectureId,
    route.query.reflectionId,
  ],
  loadAnalysis,
  {
    immediate: true,
  },
)
</script>

<template>
  <AppLayout>
    <PageContainer size="md">
      <main class="analysis-page">
        <!-- 로딩 화면 -->
        <template v-if="loading">
          <header class="page-heading">
            <Skeleton
              width="60%"
              height="30px"
            />
          </header>

          <section class="level-section">
            <Skeleton
              width="52px"
              height="11px"
            />

            <div class="level-result">
              <Skeleton
                width="60px"
                height="23px"
                radius="13px"
              />
            </div>
          </section>

          <section class="content-section">
            <Skeleton
              width="100%"
              height="150px"
              radius="5px"
            />
          </section>

          <section class="content-section">
            <div class="section-heading">
              <Skeleton
                width="56px"
                height="12px"
              />
            </div>

            <div class="stack-list">
              <div
                v-for="placeholder in 4"
                :key="placeholder"
                class="result-card"
              >
                <Skeleton
                  width="120px"
                  height="17px"
                />

                <Skeleton
                  width="100%"
                  height="13px"
                  style="margin-top: 14px"
                />

                <Skeleton
                  width="85%"
                  height="13px"
                  style="margin-top: 8px"
                />
              </div>
            </div>
          </section>
        </template>

        <!-- 분석 결과 -->
        <template v-else-if="analysis">
          <header class="page-heading">
            <h1>
              {{ lectureTitle }}
            </h1>
          </header>

          <section class="level-section">
            <p class="section-label">
              학습 단계
            </p>

            <div class="section-line"></div>

            <div class="level-result">
              <span class="level-badge">
                {{ understandingLevel }}단계
              </span>
            </div>
          </section>

          <section class="content-section">
            <AnalysisScoreCard
              :score="understandingScore"
              :reason="
                analysis.analysisReason
              "
            />
          </section>

          <section class="content-section">
            <div class="section-heading">
              <h2>분석 결과</h2>
            </div>

            <div class="stack-list">
              <article
                class="result-card understood-card"
              >
                <h3>이해한 부분</h3>

                <p>
                  {{
                    analysis.understoodSummary ||
                    '분석 내용이 없습니다.'
                  }}
                </p>
              </article>

              <article
                class="result-card weakness-card"
              >
                <h3>보완이 필요한 부분</h3>

                <p>
                  {{
                    analysis.weaknessSummary ||
                    '분석 내용이 없습니다.'
                  }}
                </p>
              </article>

              <article class="result-card">
                <p class="card-eyebrow">
                  맞춤 복습
                </p>

                <h3>핵심 개념</h3>

                <ol
                  v-if="
                    review?.coreConcepts?.length
                  "
                  class="concept-list"
                >
                  <li
                    v-for="(
                      concept,
                      index
                    ) in review.coreConcepts"
                    :key="index"
                  >
                    {{ concept }}
                  </li>
                </ol>

                <p v-else>
                  생성된 핵심 개념이 없습니다.
                </p>
              </article>

              <article class="result-card">
                <div class="card-title-area">
                  <h3>예제 코드</h3>
                </div>

                <pre
                  v-if="review?.exampleCode"
                  class="example-code"
                ><code>{{ review.exampleCode }}</code></pre>

                <p v-else>
                  생성된 예제 코드가 없습니다.
                </p>
              </article>
            </div>
          </section>

          <footer class="page-footer">
            <BaseButton
              variant="pill"
              class="dashboard-button"
              @click="moveToDashboard"
            >
              대시보드로 이동
            </BaseButton>
          </footer>
        </template>

        <!-- 오류 화면 -->
        <EmptyState
          v-else
          title="결과를 불러오지 못했습니다"
          :description="error"
        >
          <template #action>
            <BaseButton
              variant="pill"
              @click="moveToReflection"
            >
              회고록으로 이동
            </BaseButton>
          </template>
        </EmptyState>
      </main>
    </PageContainer>
  </AppLayout>
</template>

<style scoped>
.dashboard-button {
  border-color: #d65427;
  background: #d65427;
  color: #ffffff;
}

.dashboard-button:hover {
  border-color: #bd4620;
  background: #bd4620;
  color: #ffffff;
}

.analysis-page {
  width: 100%;
}

.page-heading {
  margin-bottom: 36px;
}

.card-eyebrow {
  margin: 0 0 10px;
  color: #d65427;
  font-size: 11px;
  font-weight: 600;
}

.page-heading h1 {
  margin: 0;
  color: #2f3339;
  font-size: 30px;
  font-weight: 400;
}

.level-section {
  margin-bottom: 34px;
}

.section-label {
  margin: 0;
  color: #8b919b;
  font-size: 11px;
}

.section-line {
  height: 1px;
  margin-top: 10px;
  background: #e4e6e9;
}

.level-result {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 14px;
}

.level-badge {
  padding: 5px 12px;
  border-radius: 13px;
  background: #272727;
  color: #ffffff;
  font-size: 10px;
}

.content-section {
  margin-top: 34px;
}

.section-heading {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #dedfe2;
}

.section-heading h2 {
  margin: 0;
  color: #777e88;
  font-size: 12px;
  font-weight: 400;
}

.stack-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.result-card {
  padding: 22px;
  border: 1px solid #e1e3e6;
  border-radius: 5px;
  background: #ffffff;
}

.result-card h3 {
  margin: 0 0 12px;
  color: #343a43;
  font-size: 17px;
  font-weight: 400;
}

.result-card p {
  margin: 0;
  color: #8b919b;
  font-size: 13px;
  line-height: 1.8;
}

.understood-card {
  border-left: 3px solid #d65427;
}

.weakness-card {
  border-color: #ecd2c8;
  background: #fffaf6;
}

.card-title-area {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.concept-list {
  margin: 0;
  padding-left: 22px;
  color: #707782;
  font-size: 13px;
  line-height: 1.9;
}

.example-code {
  margin: 4px 0 0;
  padding: 18px;
  overflow-x: auto;
  border-radius: 5px;
  background: #f5f6f7;
  color: #3f4652;
  font-size: 12px;
  line-height: 1.7;
}

.page-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
}

@media (max-width: 720px) {
  .page-heading h1 {
    font-size: 24px;
  }

  .result-card {
    padding: 18px;
  }

  .page-footer {
    justify-content: stretch;
  }
}
</style>