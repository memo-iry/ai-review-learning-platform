<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '@/api/client.js'

import PageHeading from '@/components/common/PageHeading.vue'
import AppSection from '@/components/common/AppSection.vue'
import BaseCard from '@/components/common/BaseCard.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import Skeleton from '@/components/common/Skeleton.vue'
import QuizHistoryCard from '@/components/quiz/QuizHistoryCard.vue'
import AppLayout from '@/components/AppLayout.vue'
import PageContainer from '@/components/common/PageContainer.vue'
import { currentUserId } from '../stores/auth.js'

const router = useRouter()

const userId = currentUserId()

const quizzes = ref([])
const lectureMap = ref({})
const accuracyByQuizId = ref({})
const todayQuiz = ref(null)
const loading = ref(true)
const error = ref('')

const historyItems = computed(() =>
    quizzes.value
        .filter((quiz) => accuracyByQuizId.value[quiz.quizId] !== undefined)
        .map((quiz) => {
            const lecture = lectureMap.value[quiz.lectureId] ?? {}
            return {
                quizId: quiz.quizId,
                week: lecture.week ?? lecture.weekNumber ?? lecture.order ?? '',
                title: quiz.title,
                accuracy: accuracyByQuizId.value[quiz.quizId],
            }
        }),
)

function pickTodayQuiz(list) {
    if (!list.length) return null
    const index = Math.floor(Math.random() * list.length)
    return list[index]
}

onMounted(async () => {
    try {
        const [quizList, lectureList, attempts] = await Promise.all([
            api.getQuizzes(userId),
            api.getLectures(),
            api.getQuizAttempts(userId),
        ])

        quizzes.value = quizList
        lectureMap.value = Object.fromEntries(
            lectureList.map((lecture) => [lecture.lectureId, lecture]),
        )

        const latestScoreByQuizId = {}
        attempts.forEach((attempt) => {
            if (latestScoreByQuizId[attempt.quizId] === undefined) {
                latestScoreByQuizId[attempt.quizId] = attempt.score
            }
        })
        accuracyByQuizId.value = latestScoreByQuizId

        todayQuiz.value = pickTodayQuiz(quizList)
    } catch (requestError) {
        error.value = requestError.message
    } finally {
        loading.value = false
    }
})

function goToQuiz(quizId) {
    router.push({ name: 'quiz-attempt', params: { quizId } })
}
</script>

<template>
  <AppLayout>
    <PageContainer>
        <div class="quiz-page">
            <PageHeading title="QUIZ" />

            <AppSection title="시작하기" v-if="loading">
                <BaseCard padding="lg" class="quiz-page__today">
                    <div class="quiz-page__today-text">
                        <Skeleton width="70px" height="0.85em" />
                        <Skeleton width="60%" height="1.4em" style="margin-top: 12px" />
                    </div>
                    <Skeleton shape="circle" width="108px" height="108px" />
                </BaseCard>
            </AppSection>

            <AppSection title="지난 퀴즈 불러오기" v-if="loading" class="quiz-page__history">
                <div class="quiz-page__list">
                    <BaseCard v-for="n in 3" :key="n" padding="lg" class="quiz-page__row-skeleton">
                        <div>
                            <Skeleton width="56px" height="20px" radius="var(--radius-pill)" />
                            <Skeleton width="260px" height="1.2em" style="margin-top: 12px" />
                        </div>
                        <Skeleton shape="circle" width="88px" height="88px" />
                    </BaseCard>
                </div>
            </AppSection>

            <template v-else>
                <AppSection title="시작하기">
                    <BaseCard v-if="todayQuiz" padding="lg" class="quiz-page__today">
                        <div class="quiz-page__today-text">
                            <p class="quiz-page__today-eyebrow">오늘의 퀴즈</p>
                            <h2 class="quiz-page__today-title">{{ todayQuiz.title }}</h2>
                        </div>

                        <BaseButton variant="primary" rounded class="quiz-page__today-cta"
                            @click="goToQuiz(todayQuiz.quizId)">
                            퀴즈 풀기
                        </BaseButton>
                    </BaseCard>

                    <EmptyState v-else title="아직 풀 수 있는 퀴즈가 없습니다" description="회고를 작성하고 AI 분석을 마치면 복습 퀴즈가 생성됩니다." />
                </AppSection>

                <AppSection title="지난 퀴즈 불러오기" class="quiz-page__history">
                    <div v-if="historyItems.length" class="quiz-page__scroll">
                        <ul class="quiz-page__list">
                            <QuizHistoryCard v-for="item in historyItems" :key="item.quizId" :week="item.week"
                                :title="item.title" :accuracy="item.accuracy" @open="goToQuiz(item.quizId)" />
                        </ul>
                    </div>

                    <EmptyState v-else title="지난 퀴즈가 없습니다" description="퀴즈를 한 번 풀고 나면 여기서 다시 확인할 수 있어요." />
                </AppSection>
            </template>

            <p v-if="error" class="quiz-page__error">{{ error }}</p>
        </div>
    </PageContainer>
  </AppLayout>
</template>

<style scoped>
.quiz-page {
    display: flex;
    flex-direction: column;
    min-height: 0;
}

.quiz-page__today {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: var(--space-5);
}

.quiz-page__today-eyebrow {
    margin: 0 0 var(--space-2);
    color: var(--color-accent);
    font-size: var(--text-xs);
    font-weight: 700;
}

.quiz-page__today-title {
    margin: 0;
    font-family: var(--font-display);
    font-size: var(--text-xl);
    font-weight: 400;
    line-height: 1.4;
}

.quiz-page__today-cta {
    flex-shrink: 0;

    display: flex;
    align-items: center;
    justify-content: center;
    text-align: center;
    line-height: 1.3;
    white-space: normal;
}

.quiz-page__history {
    flex: 1 1 auto;
    min-height: 0;
    display: flex;
    flex-direction: column;
}

.quiz-page__scroll {
    flex: 1 1 auto;
    min-height: 0;
    max-height: 520px;
    overflow-y: auto;
    padding-right: var(--space-2);
}

.quiz-page__list {
    display: flex;
    flex-direction: column;
    gap: var(--space-4);
    list-style: none;
    margin: 0;
    padding: 0;
}

.quiz-page__row-skeleton {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: var(--space-5);
}

.quiz-page__error {
    margin-top: var(--space-5);
    color: var(--color-warning);
    font-size: var(--text-sm);
}

@media (max-width: 640px) {
    .quiz-page__today {
        flex-direction: column;
        align-items: flex-start;
    }

    .quiz-page__today-cta {
        align-self: flex-end;
    }

    .quiz-page__scroll {
        max-height: 60vh;
    }
}
</style>
