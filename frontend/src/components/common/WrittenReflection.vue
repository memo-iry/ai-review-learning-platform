<script setup>
import {
  computed,
  onMounted,
  ref,
} from 'vue'

import { useRouter } from 'vue-router'
import BaseCard from './BaseCard.vue'
import Skeleton from './Skeleton.vue'

import { api } from '@/api/client.js'
import { learningState } from '@/stores/learning.js'
import { currentUserId } from '@/stores/auth.js'

const router = useRouter()

const lectures = ref([])
const reflections = ref([])
const selectedLectureId = ref('')
const isLoading = ref(true)
const errorMessage = ref('')

function getLectureId(lecture) {
  return (
    lecture?.lectureId ??
    lecture?.courseId ??
    lecture?.id
  )
}

function getReflectionId(reflection) {
  return (
    reflection?.reflectionId ??
    reflection?.id
  )
}

function getReflectionLectureId(reflection) {
  return (
    reflection?.lectureId ??
    reflection?.courseId ??
    reflection?.lecture?.lectureId ??
    reflection?.lecture?.id ??
    reflection?.course?.courseId ??
    reflection?.course?.id
  )
}

function normalizeList(response) {
  if (Array.isArray(response)) {
    return response
  }

  if (Array.isArray(response?.data)) {
    return response.data
  }

  return []
}

function getReflectionTime(reflection) {
  const createdAt =
    reflection.createdAt ??
    reflection.updatedAt

  if (!createdAt) {
    return Number(
      getReflectionId(reflection) ?? 0,
    )
  }

  const timestamp =
    new Date(createdAt).getTime()

  return Number.isNaN(timestamp)
    ? Number(getReflectionId(reflection) ?? 0)
    : timestamp
}

async function loadData() {
  isLoading.value = true
  errorMessage.value = ''

  try {
    const [
      lectureResponse,
      reflectionResponse,
    ] = await Promise.all([
      api.getLectures(),
      api.getReflections(
        currentUserId(),
      ),
    ])

    lectures.value =
      normalizeList(lectureResponse)

    reflections.value =
      normalizeList(reflectionResponse)

    if (selectableLectures.value.length > 0) {
      selectedLectureId.value = String(
        getLectureId(
          selectableLectures.value[0],
        ),
      )
    }
  } catch (error) {
    console.error(
      '회고록 목록 조회 실패',
      error,
    )

    errorMessage.value =
      error.message ??
      '회고록 목록을 불러오지 못했습니다.'
  } finally {
    isLoading.value = false
  }
}

const selectableLectures = computed(() => {
  const reflectedLectureIds = new Set(
    reflections.value.map((reflection) =>
      String(
        getReflectionLectureId(
          reflection,
        ),
      ),
    ),
  )

  return lectures.value.filter((lecture) => {
    const lectureId =
      getLectureId(lecture)

    return reflectedLectureIds.has(
      String(lectureId),
    )
  })
})

const selectedLecture = computed(() => {
  return selectableLectures.value.find(
    (lecture) => {
      return (
        String(getLectureId(lecture)) ===
        String(selectedLectureId.value)
      )
    },
  )
})

const selectedReflection = computed(() => {
  const matchedReflections =
    reflections.value.filter(
      (reflection) => {
        return (
          String(
            getReflectionLectureId(
              reflection,
            ),
          ) ===
          String(selectedLectureId.value)
        )
      },
    )

  return matchedReflections.sort(
    (first, second) => {
      return (
        getReflectionTime(second) -
        getReflectionTime(first)
      )
    },
  )[0] ?? null
})

function moveToAnalysis() {
  if (
    !selectedLecture.value ||
    !selectedReflection.value
  ) {
    errorMessage.value =
      '선택한 과목의 회고 결과가 없습니다.'

    return
  }

  const lectureId = getLectureId(
    selectedLecture.value,
  )

  const reflectionId = getReflectionId(
    selectedReflection.value,
  )

  if (!lectureId || !reflectionId) {
    errorMessage.value =
      '강의 또는 회고록 ID가 없습니다.'

    return
  }

  learningState.lecture =
    selectedLecture.value

  router.push({
    name: 'analysis',

    params: {
      lectureId: String(lectureId),
    },

    query: {
      reflectionId: String(
        reflectionId,
      ),
    },
  })
}

onMounted(loadData)
</script>

<template>
  <BaseCard class="reflection-select-card">
    <div class="card-header">
      <span class="card-label">
        작성한 회고록
      </span>

      <button
        type="button"
        class="review-button"
        :disabled="
          isLoading ||
          !selectedLecture ||
          !selectedReflection
        "
        @click="moveToAnalysis"
      >
        복습하기
      </button>
    </div>

    <Skeleton
      v-if="isLoading"
      width="100%"
      height="34px"
    />

    <template v-else>
      <select
        v-if="selectableLectures.length"
        v-model="selectedLectureId"
        class="lecture-select"
      >
        <option
          v-for="lecture in selectableLectures"
          :key="getLectureId(lecture)"
          :value="
            String(getLectureId(lecture))
          "
        >
          {{
            lecture.title ||
            lecture.lectureName ||
            lecture.courseName ||
            lecture.name
          }}
        </option>
      </select>

      <p
        v-else
        class="empty-message"
      >
        작성한 회고록이 없습니다.
      </p>
    </template>

    <p
      v-if="errorMessage"
      class="error-message"
    >
      {{ errorMessage }}
    </p>
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
  margin-bottom: 14px;
}

.card-label {
  color: #d65427;
  font-size: 11px;
  font-weight: 600;
}

.review-button {
  padding: 7px 13px;
  border: 0;
  border-radius: 14px;
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
  padding: 3px 24px 3px 0;
  border: 0;
  outline: 0;
  background: transparent;
  color: #4b5563;
  font-family:
    "AppleMyungjo",
    "Noto Serif KR",
    serif;
  font-size: 15px;
  cursor: pointer;
}

.empty-message {
  margin: 8px 0 0;
  color: #9ca3af;
  font-size: 12px;
}

.error-message {
  margin: 10px 0 0;
  color: #d65427;
  font-size: 10px;
}
</style>
