<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '@/api/client.js'
import { learningState } from '@/stores/learning.js'

import PageHeading from '@/components/common/PageHeading.vue'
import BaseSelect from '@/components/common/BaseSelect.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import QuestionField from '@/components/reflection/QuestionField.vue'
import AppLayout from '../components/AppLayout.vue'

const route = useRoute()
const router = useRouter()
const lecture = learningState.lecture

// 강의자료는 다른 화면(강의자료 확인)에서 쓰기 위해 계속 불러오되,
// 이 화면(스크린샷 기준)에는 목록을 표시하지 않습니다.
const materials = ref([])
const loading = ref(false)
const error = ref('')
const form = reactive({ understood: '', difficult: '', wantsToLearn: '' })

// 상단 드롭다운: 현재 회고 대상 강의를 보여줍니다.
const selectedLectureId = ref(String(route.params.lectureId))
const lectureOptions = computed(() => [
  { value: String(route.params.lectureId), label: lecture.title },
])

onMounted(async () => {
  try {
    materials.value = await api.getMaterials(route.params.lectureId)
  } catch (requestError) {
    error.value = requestError.message
  }
})

async function submit() {
  loading.value = true
  error.value = ''
  try {
    const reflection = await api.createReflection({
      userId: 2,
      lectureId: Number(route.params.lectureId),
      ...form,
    })
    const analysis = await api.analyzeReflection(reflection.reflectionId)
    if (!analysis.reviewMaterial) {
      error.value = analysis.weaknessSummary
      return
    }
    learningState.analysis = analysis
    router.push({ name: 'analysis' })
  } catch (requestError) {
    error.value = requestError.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section>
    <AppLayout />
  </section>
  <div class="reflection-page">
    <PageHeading title="새 회고 작성" description="오늘 학습한 내용을 회고록으로 입력하고 AI 분석을 통해 복습해보세요." description-tone="accent"
      divider>
      <template #actions>
        <BaseSelect v-model="selectedLectureId" :options="lectureOptions" />
      </template>
    </PageHeading>

    <form class="reflection-page__form" @submit.prevent="submit">
      <QuestionField number="01" label="오늘 잘 이해한 내용" v-model="form.understood"
        placeholder="오늘 학습한 내용 중 명확하게 이해하고 내 것으로 만든 부분을 작성해 주세요." required />

      <QuestionField number="02" label="아직 어려운 내용" v-model="form.difficult"
        placeholder="강의나 실습 중 헷갈렸거나 추가적인 설명이 필요한 부분을 작성해 주세요." required />

      <QuestionField number="03" label="추가로 공부하고 싶은 내용" v-model="form.wantsToLearn"
        placeholder="오늘 내용을 바탕으로 더 깊이 찾아보거나 공부해보고 싶은 주제를 적어주세요." />

      <p v-if="error" class="reflection-page__error">{{ error }}</p>

      <div class="reflection-page__footer">
        <BaseButton variant="primary" type="submit" rounded :disabled="loading">
          {{ loading ? '회고록과 강의자료 분석 중...' : '회고 저장하고 AI 분석하기' }}
        </BaseButton>
      </div>
    </form>
  </div>
</template>

<style scoped>
.reflection-page {
  max-width: 960px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-6);
}

.reflection-page__error {
  margin: var(--space-5) 0 0;
  color: var(--color-warning);
  font-size: var(--text-sm);
}

.reflection-page__footer {
  margin-top: var(--space-6);
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 720px) {
  .reflection-page {
    padding: var(--space-6) var(--space-4);
  }

  .reflection-page__footer {
    justify-content: stretch;
  }

  .reflection-page__footer .base-button {
    width: 100%;
  }
}
</style>