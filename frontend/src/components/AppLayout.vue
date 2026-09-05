<script setup>
import {
  computed,
  ref,
} from 'vue'

import {
  RouterLink,
  useRoute,
  useRouter,
} from 'vue-router'

import { api } from '@/api/client.js'
import {
  learningState,
  resetLearning,
} from '@/stores/learning.js'

import {
  authState,
  logout,
} from '@/stores/auth.js'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const error = ref('')

const username = computed(() => {
  return authState.user?.name ?? '게스트'
})

const roleLabel = computed(() => {
  return authState.user?.role === 'ADMIN'
    ? '운영자'
    : '교육생'
})

function normalizeList(response) {
  if (Array.isArray(response)) {
    return response
  }

  if (Array.isArray(response?.data)) {
    return response.data
  }

  return []
}

function getLectureId(lecture) {
  return (
    lecture?.lectureId ??
    lecture?.courseId ??
    lecture?.id
  )
}

function getLectureDate(lecture) {
  return (
    lecture?.lectureDate ??
    lecture?.scheduledAt ??
    lecture?.startDate ??
    lecture?.date ??
    null
  )
}

function getDateKey(value) {
  if (!value) {
    return null
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return null
  }

  const year = date.getFullYear()
  const month = String(
    date.getMonth() + 1,
  ).padStart(2, '0')
  const day = String(
    date.getDate(),
  ).padStart(2, '0')

  return `${year}-${month}-${day}`
}

async function moveToTodayReflection() {
  loading.value = true
  error.value = ''

  try {
    const loadLectureList =
      api.getLectures ??
      api.getCourses

    if (!loadLectureList) {
      throw new Error(
        '강의 목록 API가 정의되어 있지 않습니다.',
      )
    }

    const response =
      await loadLectureList()

    const lectures =
      normalizeList(response)

    if (lectures.length === 0) {
      throw new Error(
        '등록된 강의가 없습니다.',
      )
    }

    const todayKey =
      getDateKey(new Date())

    const todayLecture =
      lectures.find((lecture) => {
        return (
          getDateKey(
            getLectureDate(lecture),
          ) === todayKey
        )
      }) ?? lectures[0]

    const lectureId =
      getLectureId(todayLecture)

    if (
      lectureId === undefined ||
      lectureId === null
    ) {
      throw new Error(
        '오늘 수업의 lectureId가 없습니다.',
      )
    }

    learningState.lecture =
      todayLecture

    await router.push(
      `/reflection/${lectureId}`,
    )
  } catch (requestError) {
    console.error(
      '오늘 수업 이동 실패',
      requestError,
    )

    error.value =
      requestError.message ??
      '오늘 수업을 찾을 수 없습니다.'
  } finally {
    loading.value = false
  }
}

async function signOut() {
  await logout()
  resetLearning()

  router.push('/login')
}
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <p
          v-if="authState.user"
          class="role"
        >
          {{ username }} ({{ roleLabel }})
        </p>
      </div>

      <nav class="step-list">
        <RouterLink
          to="/dashboard"
          class="step"
        >
          대시보드
        </RouterLink>

        <button
          type="button"
          class="step step-button"
          :class="{
            active:
              route.name === 'reflection',
          }"
          :disabled="loading"
          @click="moveToTodayReflection"
        >
          {{
            loading
              ? '수업 확인 중...'
              : '회고록 작성'
          }}
        </button>

        <RouterLink
          to="/courses"
          class="step"
        >
          강의 목록
        </RouterLink>

        <RouterLink
          to="/history"
          class="step"
        >
          회고 기록
        </RouterLink>

        <RouterLink
          to="/quiz"
          class="step"
        >
          QUIZ
        </RouterLink>
      </nav>

      <p
        v-if="error"
        class="navigation-error"
      >
        {{ error }}
      </p>

      <button
        v-if="authState.user"
        type="button"
        class="signout"
        @click="signOut"
      >
        로그아웃
      </button>
    </aside>

    <main class="page">
      <slot>
        <router-view />
      </slot>
    </main>
  </div>
</template>

<style scoped>
.app-shell {
  display: flex;
  flex-direction: row;
  width: 100%;
  min-height: 100%;
  background: #ffffff;
}

.sidebar {
  display: flex;
  flex-direction: column;
  width: 210px;
  min-height: 100vh;
  flex-shrink: 0;
  box-sizing: border-box;
  padding: 28px 26px;
  border-right: 1px solid var(--color-border);
  background: #ffffff;
}

.role {
  margin: 0;
  color: #555b66;
  font-family:
    "AppleMyungjo",
    "Noto Serif KR",
    serif;
  font-size: 16px;
  font-weight: 700;
  line-height: 25px;
  letter-spacing: 0.2px;
  white-space: nowrap;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 42px;
  color: #242424;
  font-family: Arial, sans-serif;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.brand::before {
  display: block;
  width: 25px;
  height: 25px;
  flex-shrink: 0;
  border-radius: 50%;
  background:
    radial-gradient(
      circle at 50% 38%,
      #ffffff 0 2.5px,
      transparent 2.7px
    ),
    radial-gradient(
      ellipse at 50% 80%,
      #ffffff 0 5px,
      transparent 5.2px
    ),
    #292929;
  content: "";
}

.step-list {
  display: flex;
  flex-direction: column;
  gap: 34px;
}

.step {
  display: block;
  width: 100%;
  box-sizing: border-box;
  color: #9a9ca6;
  font-family: "AppleMyungjo", "Noto Serif KR", serif;
  font-size: 20px;
  line-height: 1.2;
  text-align: left;
  text-decoration: none;
  cursor: pointer;
}

.step-button {
  margin: 0;
  padding: 0;
  border: 0;
  background: transparent;
}

.step:hover,
.step.router-link-active,
.step-button.active {
  color: #5e626b;
  font-weight: 600;
}

.step-button:disabled {
  cursor: wait;
  opacity: 0.6;
}

.navigation-error {
  margin: 20px 0 0;
  color: #d65427;
  font-size: 11px;
  line-height: 1.5;
}

.signout {
  margin-top: auto;
  padding: 0;
  border: none;
  background: none;
  color: #9a9ca6;
  font-family: "AppleMyungjo", "Noto Serif KR", serif;
  font-size: 14px;
  text-align: left;
  cursor: pointer;
}

.signout:hover {
  color: #5e626b;
}

.page {
  flex: 1;
  min-width: 0;
}

@media (max-width: 720px) {
  .app-shell {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    min-height: auto;
    padding: 18px;
    border-right: 0;
    border-bottom: 1px solid var(--color-border);
  }

  .brand {
    margin-bottom: 20px;
  }

  .role {
    margin-bottom: 20px;
  }

  .step-list {
    flex-direction: row;
    gap: 22px;
    overflow-x: auto;
  }

  .step {
    width: auto;
    flex-shrink: 0;
    font-size: 13px;
  }

  .signout {
    margin-top: 20px;
    font-size: 12px;
  }
}
</style>
