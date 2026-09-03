<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../api/client.js'
import { learningState } from '../stores/learning.js'

const route = useRoute()
const router = useRouter()
const lecture = learningState.lecture
const materials = ref([])
const loading = ref(false)
const error = ref('')
const form = reactive({ understood: '', difficult: '', wantsToLearn: '' })

onMounted(async () => {
  try { materials.value = await api.getMaterials(route.params.lectureId) }
  catch (requestError) { error.value = requestError.message }
})

async function submit() {
  loading.value = true
  error.value = ''
  try {
    const reflection = await api.createReflection({ userId: 2, lectureId: Number(route.params.lectureId), ...form })
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
  <button class="text-button" @click="router.push({ name: 'dashboard' })">← 대시보드</button>
  <section class="page-heading"><div><p class="eyebrow">{{ lecture.title }}</p><h1>강의자료를 확인하고 회고록을 작성하세요.</h1></div></section>
  <div class="two-column">
    <section class="panel">
      <h2>강의자료</h2>
      <div class="document-list">
        <article v-for="material in materials" :key="material.materialId" class="document-card">
          <h3>{{ material.title }}</h3><p>{{ material.materialType }}</p><small>{{ material.fileUrl }}</small>
        </article>
      </div>
    </section>
    <form class="panel reflection-form" @submit.prevent="submit">
      <h2>오늘의 회고록</h2>
      <label>잘 이해한 내용<textarea v-model="form.understood" placeholder="개념과 실습 내용을 구체적으로 작성해 주세요." required /></label>
      <label>어려웠던 내용<textarea v-model="form.difficult" placeholder="헷갈리거나 다시 보고 싶은 내용을 작성해 주세요." required /></label>
      <label>추가 학습 내용<textarea v-model="form.wantsToLearn" placeholder="추가로 공부하고 싶은 내용을 작성해 주세요." /></label>
      <p v-if="error" class="error-message">{{ error }}</p>
      <button class="primary-button full" type="submit" :disabled="loading">
        {{ loading ? '회고록과 강의자료 분석 중...' : '이해도 분석 및 복습자료 생성' }}
      </button>
    </form>
  </div>
</template>
