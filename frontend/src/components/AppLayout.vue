<script setup>
import { computed } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { authState, logout } from '../stores/auth.js'
import { resetLearning } from '../stores/learning.js'

const router = useRouter()

const username = computed(() => authState.user?.name ?? '게스트')
const roleLabel = computed(() =>
  authState.user?.role === 'ADMIN' ? '운영자' : '교육생',
)

async function signOut() {
  await logout()
  resetLearning()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        {{ username }}
      </div>

      <p v-if="authState.user" class="role">{{ roleLabel }}</p>

      <nav class="step-list">
        <RouterLink to="/dashboard" class="step">대시보드</RouterLink>
        <RouterLink to="/courses" class="step">강의 목록</RouterLink>
        <RouterLink to="/history" class="step">회고 기록</RouterLink>
        <RouterLink to="/quiz" class="step">QUIZ</RouterLink>
      </nav>

      <button v-if="authState.user" type="button" class="signout" @click="signOut">
        로그아웃
      </button>
    </aside>

    <main class="page">
      <!-- 페이지가 <AppLayout> 안에 본문을 넣으면 그 내용이,
           넣지 않으면 기존처럼 router-view가 렌더링됩니다.
           (사이드바와 본문을 형제로 두면 세로로 쌓이기 때문에
            본문을 이 안쪽으로 받습니다.) -->
      <slot>
        <router-view />
      </slot>
    </main>
  </div>
</template>

<style scoped>
/* flex-direction을 명시적으로 선언합니다.
   생략하면 전역 스타일에 같은 클래스명이 생겼을 때 그 값을 그대로
   물려받아 사이드바와 페이지가 세로로 쌓이는 사고가 납니다. */
.app-shell {
  display: flex;
  flex-direction: row;
  width: 100%;
  background: #ffffff;
}

/* 높이는 min-height: 100vh 대신 부모(#app)의 stretch에 맡깁니다.
   #app 에 padding: 40px 16px 가 있어서, 여기서 100vh 를 다시 잡으면
   그 패딩만큼(80px) 세로 스크롤이 생깁니다. */
.sidebar {
  display: flex;
  flex-direction: column;
  width: 128px;
  flex-shrink: 0;
  padding: 16px 14px;
  border-right: 1px solid var(--color-border);
  background: #ffffff;
}

.role {
  margin: -22px 0 24px;
  color: #b3b5bd;
  font-family: "AppleMyungjo", "Noto Serif KR", serif;
  font-size: 10px;
}

.signout {
  margin-top: auto;
  padding: 0;
  border: none;
  background: none;
  color: #b3b5bd;
  font-family: "AppleMyungjo", "Noto Serif KR", serif;
  font-size: 11px;
  text-align: left;
  cursor: pointer;
}

.signout:hover {
  color: #5e626b;
}

.brand {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-bottom: 29px;
  color: #242424;
  font-family: Arial, sans-serif;
  font-size: 8px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.brand::before {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  border-radius: 50%;
  background:
    radial-gradient(circle at 50% 38%, #ffffff 0 2px, transparent 2.2px),
    radial-gradient(ellipse at 50% 80%, #ffffff 0 4px, transparent 4.2px),
    #292929;
  content: "";
}

.step-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.step {
  display: block;
  color: #9a9ca6;
  font-family: "AppleMyungjo", "Noto Serif KR", serif;
  font-size: 12px;
  line-height: 1;
  text-decoration: none;
  cursor: pointer;
}

/* Vue Router가 현재 URL과 일치하는 링크에 자동으로 부여하는 클래스 */
.step.router-link-active {
  color: #5e626b;
  font-weight: 600;
}

.page {
  flex: 1;
  min-width: 0;
}
</style>