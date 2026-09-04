<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/client.js'
import { learningState, selectLecture } from '../stores/learning.js'
import AppLayout from '../components/AppLayout.vue'
import PageContainer from '../components/common/PageContainer.vue'
import { currentUserId } from '../stores/auth.js'
import LectureList from '../components/common/LectureList.vue'

const LEVEL_NAMES = ['', '인지', '이해', '적용', '구현']

const router = useRouter()
const lectures = ref([])
const mastery = ref(null)
const error = ref('')

const levelLabel = computed(() => {
  if (!mastery.value) return '-'
  return `${mastery.value.currentLevel} ${LEVEL_NAMES[mastery.value.currentLevel] ?? ''}`
})

function startLecture(lecture) {
  selectLecture(lecture)
  router.push({ name: 'reflection', params: { lectureId: lecture.lectureId } })
}

onMounted(async () => {
  try {
    const [lectureList, masteryResult] = await Promise.all([
      api.getLectures(),
      api.getMastery(currentUserId()),
    ])
    lectures.value = lectureList
    mastery.value = masteryResult
  } catch (requestError) {
    error.value = requestError.message
  }
})

</script>

<template>
  <AppLayout>
    <PageContainer>
      <main class="main-content">
        <LectureList />
      </main>
    </PageContainer>
  </AppLayout>
</template>
