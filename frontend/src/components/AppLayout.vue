<script setup>
import { RouterLink } from 'vue-router'

const username = 'STUDENT USER'
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        {{ username }}
      </div>

      <nav class="step-list">
        <RouterLink to="/" class="step">Landing</RouterLink>
        <RouterLink to="/login" class="step">Login</RouterLink>
        <RouterLink to="/dashboard" class="step">Dashboard</RouterLink>
        <RouterLink to="/analysis" class="step">Analysis</RouterLink>
        <RouterLink to="/review" class="step">Review</RouterLink>
        <RouterLink to="/growth" class="step">Growth</RouterLink>
      </nav>
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
  width: 128px;
  min-height: 100vh;
  flex-shrink: 0;
  padding: 16px 14px;
  background: #ffffff;
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