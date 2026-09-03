<script setup>
import BaseCard from './BaseCard.vue'

defineProps({
  records: {
    type: Array,
    required: true,
  },
})
</script>

<template>
  <BaseCard class="learning-history-card">
    <h3 class="card-title">
      최근 퀴즈 기록
    </h3>

    <div class="history-list">
      <div
        v-for="record in records"
        :key="record.title"
        class="history-row"
      >
        <p class="record-title">
          {{ record.title }}
        </p>

        <div class="progress-group">
          <div class="progress-track">
            <span
              class="progress-value"
              :style="{ width: `${record.score}%` }"
            />
          </div>

          <span class="score-text">
            {{ record.score }}%
          </span>
        </div>
      </div>
    </div>
  </BaseCard>
</template>

<style scoped>
.learning-history-card {
  height: 100%;
}

.card-title {
  margin: 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #edf0f3;
  color: #7d8490;
  font-size: 11px;
  font-weight: 500;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding-top: 19px;
}

.history-row {
  display: grid;
  grid-template-columns: minmax(190px, 1fr) minmax(280px, 2fr);
  align-items: center;
  gap: 24px;
}

.record-title {
  margin: 0;
  color: #4b5563;
  font-family: "AppleMyungjo", "Noto Serif KR", serif;
  font-size: 14px;
}

.progress-group {
  display: flex;
  align-items: center;
  gap: 16px;
}

.progress-track {
  flex: 1;
  height: 4px;
  overflow: hidden;
  border-radius: 999px;
  background: #e4e8ed;
}

.progress-value {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: #d65427;
}

.score-text {
  width: 25px;
  color: #7d8490;
  font-size: 10px;
  text-align: right;
}
</style>