<script setup>
import { useRouter } from 'vue-router'
import booksstack from '../assets/books-stack.png'
import logoMark from '../assets/logo-mark.png'
import arrowIcon from '../assets/arrow-icon.png'

const router = useRouter()

const loginhandler = () => {
  router.push({ name: 'login' })
}
</script>

<template>
  <section class="landing">
    <!-- 1열: 인트로 -->
    <div class="landing__col landing__col--intro">
      <div class="landing__top">
        <div class="brand">
          <img :src="logoMark" alt="MEMO:IRY 로고" class="brand__mark" />
          <span class="brand__name">MEMO:IRY</span>
        </div>

        <h1 class="landing__headline">
          복습,<br />이제<br />어렵지<br />않습니다
        </h1>

        <button class="btn btn--cta" type="button" @click="loginhandler">
          회고록 작성하기
        </button>
      </div>

      <button class="btn btn--login" type="button" @click="loginhandler">
         <span class="btn--login__text">LOGIN</span>
        <img :src="arrowIcon" alt="" class="btn__icon" aria-hidden="true" />
      </button>
    </div>

    <!-- 2열: 이미지 -->
    <div class="landing__col landing__col--image">
      <img :src="booksstack" alt="쌓여 있는 책" class="landing__image" />
    </div>

    <!-- 3, 4열: 기능 소개 -->
    <div class="landing__col landing__col--features">
      <div class="features__body">
        <div class="features__eyebrow">이런<br/>서비스를<br/>제공합니다 </div>
        <div class="feature">
  <h2 class="feature__title">01. 복습</h2>
  <hr class="feature__divider" />
  <p class="feature__desc">
    강의 자료와 나의 회고록을 바탕으로<br />
    AI가 나에게 꼭 필요한<br />
    복습 노트를 만들어줍니다.
  </p>
    <hr class="feature__divider" />
</div>

        <div class="feature">
          <h2 class="feature__title">02. QUIZ</h2>
           <hr class="feature__divider" />
          <p class="feature__desc">
            복습을 마치면 자동 생성된<br />
            QUIZ로 이해도를<br />
            바로 확인할 수 있습니다.
          </p>
            <hr class="feature__divider" />
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.landing {
  --border-color: #e4e1db;
  --text-strong: #1c1b19;
  --text-muted: #1c1b19;
  --panel-bg: #ffffff;

  display: grid;
  grid-template-columns: 1fr 1fr 2fr;
  width: 70rem;   /* 1400px */
  height: 47rem;    /* 800px */
  margin: 0 auto;
  border: 1px solid var(--border-color);
  background: #ffffff;
  color: var(--text-strong);
  font-family: 'Noto Serif KR', sans-serif;
  overflow: hidden;
}

.landing__col {
  border-right: 1px solid var(--border-color);
  padding: 1.75rem 1.625rem;   /* 28px 26px */
  display: flex;
  flex-direction: column;
}

.landing__col:last-child {
  border-right: none;
}

/* 1열: 인트로 */
.landing__col--intro {
  justify-content: space-between;
  min-height: 26.25rem;   /* 420px */
}

.landing__top {
  display: flex;
  flex-direction: column;
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 2.5rem;   /* 로고 ↔ 헤드라인 간격 (기존 gap 값) */
}

.brand__mark {
  width: 1.25rem;    /* 20px */
  height: 1.25rem;   /* 20px */
  border-radius: 50%;
  background: var(--color-primary, #cc5126);
  color: #fff;
  font-size: 0.6875rem;   /* 11px */
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand__name {
  font-size: 0.75rem;   /* 12px */
  font-weight: 700;
  letter-spacing: 0.06em;
}

.landing__headline {
  font-size: 1.875rem;
  font-weight: 400;
  line-height: 1.8;
  margin: 0;
  margin-top: 1rem;          /* 이전에 추가하신 값 */
  margin-bottom: 1rem;    /* 헤드라인 ↔ 버튼 간격, 원하는 만큼 줄이기 */
}

.btn {
  font-family: inherit;
  cursor: pointer;
  background: none;
}

.btn--cta {
  align-self: flex-start;
  border: 1px solid #1a1a1a;
  color: #1a1a1a;
  border-radius: 999px;
  padding: 0.5625rem 1.125rem;   /* 9px 18px */
  font-size: 0.75rem;   /* 12px */
  font-weight: 500;
  transition: background-color 0.15s ease, color 0.15s ease;
}

.btn--cta:hover {
  background: #1a1a1a;
  color: #fff;
}

.btn--login {
  align-self: flex-end;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;   /* 8px */
  border: none;
  padding: 0.5625rem 0.625rem 0.5625rem 1.25rem;   /* 9px 10px 9px 20px */
  font-size: 0.75rem;   /* 12px */
  font-weight: 600;
  letter-spacing: 0.04em;
  color: var(--text-strong);
}

.btn--login__text {
  position: relative;
}

.btn--login__text::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -0.2rem;
  width: 100%;
  height: 1px;
  background: var(--text-strong);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.2s ease;
}

.btn--login:hover .btn--login__text::after {
  transform: scaleX(1);
}

.btn__icon {
  width: 1rem;      /* 16px */
  height: 1rem;     /* 16px */
  border: none;
  padding: 0.125rem;   /* 2px */
}

/* 2열: 이미지 */
.landing__col--image {
  padding: 0;
}

.landing__image {
  width: 100%;
  height: 100%;
  min-height: 26.25rem;   /* 420px */
  object-fit: cover;
  display: block;
}

/* 3, 4열: 기능 소개 */
.landing__col--features {
  background: var(--panel-bg);
  padding: 0;
}

.features__eyebrow {
  position: absolute;
  top: 29%;
  left: 40%;
  transform: translate(-50%, -50%);
  z-index: 10;
  margin: 0;
  padding: 0;
  letter-spacing: 0.04em;
  line-height: 0.98em;
  font-size: 5rem;   /* 57px */
  font-weight: 400;
  color: #b4b3b398;
  pointer-events: none;
  word-break: keep-all;
}

.features__body {
  flex: 1;
  display: flex;
  position: relative;
}

.feature {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1rem;   /* 16px */
  padding: 1.25rem 1.625rem 1.75rem;   /* 20px 26px 28px */
  border-right: 1px solid var(--border-color);
}

.feature:last-child {
  border-right: none;
}

.feature__title {
  font-size: 0.875rem;   /* 14px */
  font-weight: 700;
  margin: 0;
  margin-top: 27rem;   /* 원하는 만큼 값 조절 */
}

.feature__desc {
  font-size: 0.78125rem;   /* 12.5px */
  line-height: 1.7;
  color: var(--text-muted);
  margin: 0;
  margin-top: 3.3rem;
  margin-bottom: 3.3rem;
}

.feature__divider {
  width: 4rem;   /* 선 길이 - 원하는 만큼 조절 */
  height: 1px;
  border: none;
  background: var(--border-color);
  margin: 0.3rem 0 0;   /* 위아래 간격 조절 */
}

/* 반응형 */
@media (max-width: 860px) {
  .landing__col--features {
    grid-column: 1 / -1;
    position: relative;
  }
}

@media (max-width: 480px) {
  .features__body {
    flex-direction: column;
  }
  .feature {
    border-right: none;
    border-bottom: 1px solid var(--border-color);
  }
  .feature:last-child {
    border-bottom: none;
  }
}
</style>