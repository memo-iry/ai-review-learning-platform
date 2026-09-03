<script setup>
defineProps({ review: { type: Object, required: true } })
const emit = defineEmits(['complete'])
</script>

<template>
  <section class="page-heading"><div><p class="eyebrow">RAG 맞춤 복습</p><h1>{{ review.title }}</h1></div></section>
  <section class="review-stack">
    <article class="panel"><h2>핵심 개념</h2><ol><li v-for="concept in review.coreConcepts" :key="concept">{{ concept }}</li></ol></article>
    <article class="panel"><h2>예제 코드</h2><pre><code>{{ review.exampleCode }}</code></pre></article>
    <article class="panel"><h2>확인 문제</h2><details v-for="(item, index) in review.quiz" :key="item.question"><summary>{{ index + 1 }}. {{ item.question }}</summary><p>정답: {{ item.answer }}</p></details></article>
  </section>
  <button class="primary-button" @click="emit('complete')">복습 완료 기록</button>
</template>
