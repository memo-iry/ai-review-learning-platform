<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import booksstack2 from '../assets/books-stack2.png'
import { login } from '../stores/auth.js'

const DEMO = {
  student: { email: 'learner@skala.com', password: 'demo' },
  admin: { email: 'admin@skala.com', password: 'demo' },
}

const router = useRouter()
const role = ref('student')
const email = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

function selectRole(next) {
  role.value = next
  email.value = DEMO[next].email
  password.value = DEMO[next].password
}

async function submit() {
  loading.value = true
  error.value = ''
  try {
    await login(email.value, password.value)
    router.push({ name: 'dashboard' })
  } catch (requestError) {
    error.value = requestError.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="login">
        <div class="login__card">
      <div class="login__brand">
        <h1 class="login__logo">Memo:iry</h1>
        <p class="login__tagline">복습, 이제 어렵지 않습니다</p>
      </div>

      <div class="login__field">
        <label class="login__label" for="login-email">이메일</label>
        <input
          id="login-email"
          v-model="email"
          type="email"
          class="login__input"
          placeholder="example@memo.iry"
        />
      </div>

      <div class="login__field">
        <label class="login__label" for="login-password">비밀번호</label>
        <input
          id="login-password"
          v-model="password"
          type="password"
          class="login__input"
          placeholder="비밀번호"
          autocomplete="current-password"
          @keyup.enter="submit"
        />
      </div>

      <div class="login__field">
        <span class="login__label">데모 계정 채우기</span>
        <div class="login__roles">
          <button
            type="button"
            class="login__role-btn"
            :class="{ 'is-active': role === 'student' }"
            @click="selectRole('student')"
          >
            학생
          </button>
          <button
            type="button"
            class="login__role-btn"
            :class="{ 'is-active': role === 'admin' }"
            @click="selectRole('admin')"
          >
            운영진
          </button>
        </div>
      </div>

      <p v-if="error" class="login__error">{{ error }}</p>

      <button
        type="button"
        class="login__submit"
        :disabled="loading"
        @click="submit"
      >
        {{ loading ? '확인 중...' : '로그인하기' }}
      </button>

      <div class="login__divider" />

      <p class="login__signup">
        계정이 없으신가요? <a href="#" class="login__signup-link">문의하기</a>
      </p>
    </div>

        <div class="login__image">
      <img :src="booksstack2" alt="쌓여 있는 책" />
    </div>
  </section>

</template>

<style scoped>
.login {
  --border-color: #e6d3c6;
  --divider-color: #ece9e4;
  --text-strong: #1c1b19;
  --text-muted: #9a958d;
  --text-placeholder: #b7b2aa;

  display: flex;
  align-items: center;
  justify-content: center;
   padding-left: 8rem;
  gap: 0.5rem;
  width: 100%;
  height: 100%;
  background: #ffffff;
  font-family: 'Noto Sans KR', sans-serif;
  color: var(--text-strong);
}

.login__card {
  width: 25rem;
  border: 1px solid var(--border-color);
  padding: 2.25rem 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  box-sizing: border-box;
}

.login__brand {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  text-align: center;
}

.login__logo {
  font-family: 'Noto Serif KR', serif;
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.login__tagline {
  font-size: 0.75rem;
  color: var(--text-muted);
  margin: 0;
}

.login__field {
  display: flex;
  flex-direction: column;
  gap: 0.625rem;
}

.login__label {
  font-size: 0.75rem;
  color: var(--text-strong);
}

.login__input {
  height: 2.5rem;
  border: 1px solid var(--divider-color);
  border-radius: 0.25rem;
  padding: 0 0.875rem;
  font-size: 0.8125rem;
  font-family: inherit;
  color: var(--text-strong);
  background: #fff;
}

.login__input::placeholder {
  color: var(--text-placeholder);
}

.login__input:focus {
  outline: none;
  border-color: var(--text-strong);
}

.login__roles {
  display: flex;
  gap: 0.5rem;
}

.login__role-btn {
  flex: 1;
  height: 2.5rem;
  border: 1px solid var(--divider-color);
  border-radius: 0.25rem;
  background: #fff;
  font-family: inherit;
  font-size: 0.8125rem;
  color: var(--text-muted);
  cursor: pointer;
  transition: border-color 0.15s ease, color 0.15s ease;
}

.login__role-btn.is-active {
  border-color: var(--text-strong);
  color: var(--text-strong);
  font-weight: 500;
}

.login__submit {
  height: 2.75rem;
  border: 1px solid var(--text-strong);
  border-radius: 0.25rem;
  background: #fff;
  font-family: inherit;
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--text-strong);
  cursor: pointer;
  transition: background-color 0.15s ease, color 0.15s ease;
}

.login__submit:hover {
  background: var(--text-strong);
  color: #fff;
}

.login__submit:disabled {
  opacity: 0.5;
  cursor: default;
}

.login__error {
  margin: 0;
  font-size: 0.75rem;
  color: #c0392b;
}

.login__divider {
  height: 1px;
  background: var(--divider-color);
}

.login__signup {
  margin: 0;
  font-size: 0.75rem;
  color: var(--text-muted);
  text-align: right;
}

.login__signup-link {
  color: var(--text-strong);
  font-weight: 500;
  text-decoration: none;
}

.login__signup-link:hover {
  text-decoration: underline;
}

.login__image {
  width: 50rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login__image img {
  width: 100%;
  height: auto;
  display: block;
}

@media (max-width: 860px) {
  .login {
    flex-direction: column;
    gap: 2.5rem;
    padding: 2.5rem 1.5rem;
    height: auto;
  }
  .login__image {
    width: 18rem;
  }
}
</style>
