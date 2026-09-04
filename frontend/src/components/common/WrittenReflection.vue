<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import BaseCard from './BaseCard.vue'
import Skeleton from './Skeleton.vue'
import { api } from '@/api/client.js'
import { learningState } from '@/stores/learning.js'

const router = useRouter()

const lectures = ref([])
const selectedLectureId = ref(null)
const isLoading = ref(true)

function getLectureId(lecture) {
  return lecture.id ?? lecture.lectureId
}

async function loadLectures() {
  try {
    const response = await api.getLectures()

    lectures.value = Array.isArray(response)
      ? response
      : response.data ?? []

    if (lectures.value.length > 0) {
      selectedLectureId.value = getLectureId(lectures.value[0])
    }
  } catch (error) {
    console.error('강의 목록 조회 실패', error)
  } finally {
    isLoading.value = false
  }
}

const selectedLecture = computed(() => {
  return lectures.value.find(
    (lecture) => getLectureId(lecture) === selectedLectureId.value,
  )
})

function moveToReflection() {
  if (!selectedLecture.value) {
    return
  }

  const lectureId = getLectureId(selectedLecture.value)

  learningState.lecture = selectedLecture.value

  router.push(`/reflection/${lectureId}`)
}

onMounted(loadLectures)
</script>

<template>
  <BaseCard class="reflection-select-card">
    <div class="card-header">
      <span class="card-label">작성한 회고록</span>

      <button
        type="button"
        class="review-button"
        :disabled="!selectedLecture"
        @click="moveToReflection"
      >
        복습하기
      </button>
    </div>

    <Skeleton
      v-if="isLoading"
      width="100%"
      height="34px"
    />

    <select
      v-else
      v-model="selectedLectureId"
      class="lecture-select"
    >
      <option
        v-for="lecture in lectures"
        :key="getLectureId(lecture)"
        :value="getLectureId(lecture)"
      >
        {{ lecture.title || lecture.name || lecture.lectureName }}
      </option>
    </select>
  </BaseCard>
</template>

<style scoped>
.reflection-select-card {
  height: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-label {
  color: #d65427;
  font-size: 11px;
  font-weight: 600;
}

.review-button {
  border: 0;
  border-radius: 14px;
  padding: 7px 13px;
  background: #d65427;
  color: #ffffff;
  font-size: 10px;
  cursor: pointer;
}

.review-button:disabled {
  cursor: default;
  opacity: 0.5;
}

.lecture-select {
  width: 100%;
  margin-top: 14px;
  padding: 3px 24px 3px 0;
  border: 0;
  outline: 0;
  background: transparent;
  color: #4b5563;
  font-family: "AppleMyungjo", "Noto Serif KR", serif;
  font-size: 15px;
  cursor: pointer;
}
</style>