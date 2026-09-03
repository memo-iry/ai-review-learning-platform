<script setup>
import BaseCard from '@/components/common/BaseCard.vue'
import TagBadge from '@/components/common/TagBadge.vue'
import ProgressRing from '@/components/common/ProgressRing.vue'

defineProps({
    week: { type: [String, Number], default: '' },
    title: { type: String, required: true },
    /** 0~100. 아직 응시 기록이 없으면 null */
    accuracy: { type: Number, default: null },
})

defineEmits(['open'])
</script>

<template>
    <BaseCard as="li" class="quiz-history-card" padding="lg" interactive @click="$emit('open')">
        <div class="quiz-history-card__main">
            <TagBadge v-if="week !== ''">{{ week }}주차</TagBadge>
            <h3 class="quiz-history-card__title">{{ title }}</h3>
        </div>

        <div v-if="accuracy !== null" class="quiz-history-card__score">
            <span class="quiz-history-card__score-label">전체 정답률</span>
            <ProgressRing :value="accuracy" :size="88" :thickness="6" suffix="%" />
        </div>
    </BaseCard>
</template>

<style scoped>
.quiz-history-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: var(--space-5);
    list-style: none;
}

.quiz-history-card__title {
    margin: var(--space-3) 0 0;
    font-family: var(--font-display);
    font-size: var(--text-lg);
    font-weight: 400;
}

.quiz-history-card__score {
    display: flex;
    flex-direction: column;
    align-items: center;
    flex-shrink: 0;
    gap: var(--space-2);
}

.quiz-history-card__score-label {
    color: var(--color-accent);
    font-size: var(--text-xs);
    font-weight: 700;
    white-space: nowrap;
}

@media (max-width: 560px) {
    .quiz-history-card {
        flex-direction: column;
        align-items: flex-start;
    }

    .quiz-history-card__score {
        align-self: flex-end;
    }
}
</style>