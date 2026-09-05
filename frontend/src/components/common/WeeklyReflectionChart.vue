<script setup>
import { computed } from 'vue'
import BaseCard from './BaseCard.vue'

const props = defineProps({
  records: {
    type: Array,
    required: true,
  },

  completionRate: {
    type: Number,
    default: 15,
  },
})

const maxCount = computed(() => {
  return Math.max(
    ...props.records.map((record) => record.count),
    1,
  )
})

function getBarHeight(count) {
  return `${(count / maxCount.value) * 80}px`
}
</script>

<template>
  <BaseCard class="weekly-chart-card">
    <h3 class="card-title">
      요일별 회고 작성 빈도 분석
    </h3>

    <div class="chart">
      <div
        v-for="record in records"
        :key="record.day"
        class="bar-item"
      >
        <div class="bar-area">
          <span class="count-label">
            {{ record.count }}
          </span>

          <span
            class="bar"
            :class="{ muted: !record.completed }"
            :style="{ height: getBarHeight(record.count) }"
          />
        </div>

        <span class="day-label">
          {{ record.day }}
        </span>
      </div>
    </div>

    <div class="card-footer">
      <span>주간 회고 작성 횟수</span>
    </div>
  </BaseCard>
</template>

<style scoped>
.weekly-chart-card {
  height: 100%;
}

.card-title {
  margin: 0;
  color: #4b5563;
  font-size: 14px;
  font-weight: 500;
}

.chart {
  display: flex;
  align-items: end;
  justify-content: space-around;
  height: 130px;
  margin-top: 10px;
}

.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 7px;
}

.bar-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  height: 100px;
}

.count-label {
  color: #7d8490;
  font-size: 9px;
  line-height: 1;
}

.bar {
  display: block;
  width: 9px;
  min-height: 4px;
  border-radius: 2px 2px 0 0;
  background: #d65427;
}

.bar.muted {
  background: #e1e5eb;
}

.day-label {
  color: #a3a8b1;
  font-size: 10px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  padding-top: 13px;
  border-top: 1px solid #edf0f3;
  color: #8b919b;
  font-size: 10px;
}

.card-footer strong {
  color: #d65427;
  font-weight: 600;
}
</style>
