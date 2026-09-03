<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { learningState } from '../stores/learning.js'

const router = useRouter()

const review = computed(() => {
  return learningState.analysis?.reviewMaterial ?? null
})

function completeReview() {
  router.push({ name: 'growth' })
}
</script>

<template>
  <div v-if="review">
    <section class="page-heading">
      <div>
        <p class="eyebrow">RAG 맞춤 복습</p>
        <h1>{{ review.title }}</h1>
      </div>
    </section>

    <section class="review-stack">
      <article class="panel">
        <h2>핵심 개념</h2>

        <ol>
          <li
            v-for="concept in review.coreConcepts"
            :key="concept"
          >
            {{ concept }}
          </li>
        </ol>
      </article>

      <article class="panel">
        <h2>예제 코드</h2>

        <pre><code>{{ review.exampleCode }}</code></pre>
      </article>

      <article class="panel">
        <h2>확인 문제</h2>

        <details
          v-for="(item, index) in review.quiz"
          :key="item.question"
        >
          <summary>
            {{ index + 1 }}. {{ item.question }}
          </summary>

          <p>정답: {{ item.answer }}</p>
        </details>
      </article>
    </section>

    <button
      class="primary-button"
      type="button"
      @click="completeReview"
    >
      복습 완료 기록
    </button>
  </div>

  <section v-else class="empty-state">
    <h1>복습자료가 없습니다.</h1>
    <p>AI 분석을 완료하면 맞춤형 복습자료가 생성됩니다.</p>

    <button
      class="primary-button"
      type="button"
      @click="router.push({ name: 'reflection' })"
    >
      회고록 작성하기
    </button>
  </section>
</template>