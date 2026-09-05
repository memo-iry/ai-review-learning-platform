<script setup>
import {
  computed,
  onMounted,
  reactive,
  ref,
} from 'vue'

import {
  useRoute,
  useRouter,
} from 'vue-router'

import { api } from '@/api/client.js'
import { learningState } from '@/stores/learning.js'
import { currentUserId } from '@/stores/auth.js'

import PageHeading from '@/components/common/PageHeading.vue'
import BaseSelect from '@/components/common/BaseSelect.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import QuestionField from '@/components/reflection/QuestionField.vue'
import AppLayout from '@/components/AppLayout.vue'
import PageContainer from '@/components/common/PageContainer.vue'

const route = useRoute()
const router = useRouter()

const materials = ref([])
const loading = ref(false)
const error = ref('')

const form = reactive({
  understood: '',
  difficult: '',
  wantsToLearn: '',
})

const lectureId = computed(() => {
  return Number(route.params.lectureId)
})

const lecture = computed(() => {
  return learningState.lecture ?? null
})

const selectedLectureId = ref(
  String(route.params.lectureId),
)

const lectureOptions = computed(() => {
  return [
    {
      value: String(lectureId.value),

      label:
        lecture.value?.title ??
        lecture.value?.lectureName ??
        lecture.value?.courseName ??
        lecture.value?.name ??
        `강의 #${lectureId.value}`,
    },
  ]
})

async function loadMaterials() {
  try {
    materials.value =
      await api.getMaterials(
        lectureId.value,
      )
  } catch (requestError) {
    console.error(
      '강의자료 조회 실패',
      requestError,
    )

    error.value =
      requestError.message ??
      '강의자료를 불러오지 못했습니다.'
  }
}

async function submit() {
  loading.value = true
  error.value = ''

  try {
    if (
      Number.isNaN(lectureId.value) ||
      lectureId.value <= 0
    ) {
      throw new Error(
        '올바른 강의 ID가 아닙니다.',
      )
    }

    const reflectionResponse =
      await api.createReflection({
        userId: currentUserId(),
        lectureId: lectureId.value,
        ...form,
      })

    const reflection =
      reflectionResponse?.data ??
      reflectionResponse

    const reflectionId =
      reflection?.reflectionId ??
      reflection?.id

    if (!reflectionId) {
      throw new Error(
        '저장된 회고록 ID를 확인할 수 없습니다.',
      )
    }

    const analysisResponse =
      await api.analyzeReflection(
        reflectionId,
      )

    const analysis =
      analysisResponse?.data ??
      analysisResponse

    if (!analysis) {
      throw new Error(
        '분석 결과를 받지 못했습니다.',
      )
    }

    if (!analysis.reviewMaterial) {
      error.value =
        analysis.weaknessSummary ??
        '회고록 내용을 조금 더 자세히 작성해 주세요.'

      return
    }

    learningState.analysis = analysis

    await router.push({
      name: 'analysis',

      params: {
        lectureId: String(
          lectureId.value,
        ),
      },

      query: {
        reflectionId: String(
          reflectionId,
        ),
      },
    })
  } catch (requestError) {
    console.error(
      '회고 저장 및 분석 실패',
      requestError,
    )

    error.value =
      requestError.message ??
      '회고 저장 및 분석에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(loadMaterials)
</script>

<template>
  <AppLayout>
    <PageContainer>
      <main class="reflection-page">
        <PageHeading title="회고록 작성" description="학습한 내용을 회고록으로 입력하고 AI 분석을 통해 복습해보세요." description-tone="accent">
          <template #actions>
            <BaseSelect v-model="selectedLectureId" :options="lectureOptions" />
          </template>
        </PageHeading>

        <form class="reflection-page__form" @submit.prevent="submit">
          <QuestionField v-model="form.understood" number="01" label="잘 이해한 내용"
            placeholder="학습한 내용 중 명확하게 이해하고 내 것으로 만든 부분을 작성해 주세요." required />

          <QuestionField v-model="form.difficult" number="02" label="아직 어려운 내용"
            placeholder="강의나 실습 중 헷갈렸거나 추가적인 설명이 필요한 부분을 작성해 주세요." required />

          <QuestionField v-model="form.wantsToLearn" number="03" label="추가로 공부하고 싶은 내용"
            placeholder="더 깊이 찾아보거나 공부해보고 싶은 주제를 적어주세요." />

          <p v-if="error" class="reflection-page__error">
            {{ error }}
          </p>

          <div class="reflection-page__footer">
            <BaseButton variant="primary" type="submit" rounded :disabled="loading">
              {{
                loading
                  ? '회고록과 강의자료 분석 중...'
                  : '회고 저장하고 AI 분석하기'
              }}
            </BaseButton>
          </div>
        </form>
      </main>
    </PageContainer>
  </AppLayout>
</template>

<style scoped>
.reflection-page {
  width: 100%;
}

.reflection-page__error {
  margin: var(--space-5) 0 0;
  color: var(--color-warning);
  font-size: var(--text-sm);
}

.reflection-page__footer {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-6);
}

@media (max-width: 720px) {
  .reflection-page__footer {
    justify-content: stretch;
  }

  .reflection-page__footer .base-button {
    width: 100%;
  }
}
</style>
