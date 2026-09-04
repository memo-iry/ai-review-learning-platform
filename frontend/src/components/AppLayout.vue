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
        <RouterLink to="/courses" class="step">강의 목록</RouterLink>
        <RouterLink to="/history" class="step">회고 기록</RouterLink>
        <RouterLink to="/quiz" class="step">QUIZ</RouterLink>
      </nav>

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
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.app-shell {
  display: flex;
  min-height: 100vh;
  background: #ffffff;
}

.sidebar {
  display: flex;
  flex-direction: column;
  width: 128px;
  min-height: 100vh;
  flex-shrink: 0;
  padding: 16px 14px;
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