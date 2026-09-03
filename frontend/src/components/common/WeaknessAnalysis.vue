<script setup>
import BaseCard from './BaseCard.vue'

defineProps({
  weaknesses: {
    type: Array,
    required: true,
  },
})
</script>

<template>
  <BaseCard class="weakness-analysis-card">
    <h3 class="card-title">
      분별해야 하는 부분
    </h3>

    <p class="card-description">
      QUIZ 정답률이 낮은 수준과 추가 학습이 필요한 항목들을 확인합니다.
    </p>

    <div class="weakness-list">
      <article
        v-for="item in weaknesses"
        :key="item.title"
        class="weakness-item"
      >
        <div>
          <strong>{{ item.title }}</strong>
          <p>{{ item.description }}</p>
        </div>

        <span class="score">
          {{ item.score }}%
        </span>
      </article>
    </div>

    <div class="card-footer">
      <span>집중 학습 권장</span>
      <strong>맞춤형 퀴즈 대기중</strong>
    </div>
  </BaseCard>
</template>

<style scoped>
.weakness-analysis-card {
  height: 100%;
}

.card-title {
  margin: 0;
  color: #4b5563;
  font-size: 14px;
  font-weight: 500;
}

.card-description {
  margin: 7px 0 15px;
  color: #8b919b;
  font-size: 10px;
}

.weakness-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.weakness-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 11px 10px;
  border: 1px solid #edf0f3;
  border-radius: 7px;
}

.weakness-item strong {
  color: #4b5563;
  font-size: 11px;
}

.weakness-item p {
  margin: 4px 0 0;
  color: #9ca3af;
  font-size: 9px;
}

.score {
  color: #d65427;
  font-size: 11px;
  font-weight: 600;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #edf0f3;
  color: #8b919b;
  font-size: 10px;
}

.card-footer strong {
  color: #d65427;
  font-weight: 600;
}
</style>