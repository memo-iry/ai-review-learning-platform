<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '@/api/client.js'

import PageHeading from '@/components/common/PageHeading.vue'
import AppSection from '@/components/common/AppSection.vue'
import BaseCard from '@/components/common/BaseCard.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import QuizHistoryCard from '@/components/quiz/QuizHistoryCard.vue'
import AppLayout from '@/components/AppLayout.vue'
import { currentUserId } from '../stores/auth.js'

const router = useRouter()

// TODO: 로그인 사용자 식별자는 인증 상태가 생기면 그쪽에서 받아오도록 교체
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

        // 같은 퀴즈를 여러 번 응시했을 수 있어 가장 최근 점수만 남긴다
        // (attempts 는 완료 시각 최신순으로 내려온다)
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
    <section>
        <AppLayout />
    </section>
    <div class="quiz-page">
        <PageHeading title="퀴즈 및 복습" />

        <LoadingSpinner v-if="loading" label="퀴즈를 불러오는 중이에요" />

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
</template>

<style scoped>
.quiz-page {
    /* max-width: 960px; */
    margin: 0 auto;
    padding: var(--space-8) var(--space-6);
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

/* 패딩이 있으면 원이 찌그러지므로 버튼 기본 padding을 걷어내고
   정사각형 박스 + border-radius: 50% 로 진짜 원을 만듭니다. */
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

/* "지난 퀴즈 불러오기" 목록만 이 영역 안에서 스크롤됩니다. */
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

.quiz-page__error {
    margin-top: var(--space-5);
    color: var(--color-warning);
    font-size: var(--text-sm);
}

@media (max-width: 640px) {
    .quiz-page {
        padding: var(--space-6) var(--space-4);
    }

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