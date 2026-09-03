<script setup>
import { onMounted, ref, watch } from 'vue'
import BaseCard from '@/components/common/BaseCard.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import TypewriterText from '@/components/common/TypewriterText.vue'
import tutorMessages from '@/data/tutorMessages.json'

const props = defineProps({
  /** 현재 문항의 개념명. 힌트 매칭에 사용 */
  conceptName: { type: String, default: '' },
})

// 실제 백엔드 AI 엔드포인트가 아직 없어서, 프론트엔드에 내장된 JSON을
// "AI가 응답해준 것"처럼 흉내 내어 사용합니다. 나중에 실제 튜터 API가
// 생기면 이 함수 내부만 fetch 호출로 바꾸면 됩니다.
function fakeAiFetch(payload) {
  return new Promise((resolve) => {
    setTimeout(() => resolve(payload), 350 + Math.random() * 350)
  })
}

const message = ref('')
const messageRevision = ref(0)
const loadingMessage = ref(true)

const hintVisible = ref(false)
const hintText = ref('')
const hintRevision = ref(0)
const loadingHint = ref(false)

const reviewRequested = ref(false)

let lastGreetingIndex = -1

function pickGreeting() {
  const { greetings } = tutorMessages
  if (!greetings.length) return ''
  let index = Math.floor(Math.random() * greetings.length)
  if (greetings.length > 1 && index === lastGreetingIndex) {
    index = (index + 1) % greetings.length
  }
  lastGreetingIndex = index
  return greetings[index]
}

async function loadGreeting() {
  loadingMessage.value = true
  const greeting = pickGreeting()
  const response = await fakeAiFetch(greeting)
  message.value = response
  messageRevision.value += 1
  loadingMessage.value = false
}

async function loadHint() {
  loadingHint.value = true
  hintVisible.value = true
  const text =
    tutorMessages.hints[props.conceptName] ?? tutorMessages.hints.default
  const response = await fakeAiFetch(text)
  hintText.value = response
  hintRevision.value += 1
  loadingHint.value = false
}

function toggleReview() {
  reviewRequested.value = !reviewRequested.value
}

onMounted(loadGreeting)

// 문항이 바뀌면 새 문항에 맞는 응원 메시지를 다시 받아오고,
// 힌트/복습 표시는 초기화합니다.
watch(
  () => props.conceptName,
  () => {
    loadGreeting()
    hintVisible.value = false
    reviewRequested.value = false
  },
)
</script>

<template>
  <BaseCard padding="lg" class="quiz-tutor-panel">
    <div class="quiz-tutor-panel__header">
      <span class="quiz-tutor-panel__avatar" aria-hidden="true">🎓</span>
      <div>
        <p class="quiz-tutor-panel__name">Memo:iry 튜터</p>
        <p class="quiz-tutor-panel__role">실시간 학습 도우미</p>
      </div>
    </div>

    <p class="quiz-tutor-panel__message">
      "<TypewriterText :key="messageRevision" :text="message" />"
    </p>

    <p v-if="hintVisible" class="quiz-tutor-panel__hint">
      <TypewriterText :key="`hint-${hintRevision}`" :text="hintText" />
    </p>

    <BaseButton variant="outline" block class="quiz-tutor-panel__action" @click="loadHint">
      힌트 보기
    </BaseButton>

    <BaseButton
      variant="outline"
      block
      class="quiz-tutor-panel__action"
      :class="{ 'is-active': reviewRequested }"
      @click="toggleReview"
    >
      <span class="quiz-tutor-panel__checkbox" aria-hidden="true">{{ reviewRequested ? '☑' : '☐' }}</span>
      이 개념 다시 복습하기
    </BaseButton>
  </BaseCard>
</template>

<style scoped>
.quiz-tutor-panel__header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-5);
}

.quiz-tutor-panel__avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--color-accent-soft);
  font-size: var(--text-lg);
}

.quiz-tutor-panel__name {
  margin: 0;
  font-weight: 700;
  font-size: var(--text-sm);
}

.quiz-tutor-panel__role {
  margin: 2px 0 0;
  color: var(--color-text-subtle);
  font-size: var(--text-xs);
}

.quiz-tutor-panel__message {
  margin: 0 0 var(--space-5);
  min-height: 4.6em;
  color: var(--color-text);
  font-size: var(--text-sm);
  line-height: 1.7;
}

.quiz-tutor-panel__hint {
  margin: calc(var(--space-5) * -1) 0 var(--space-5);
  padding: var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-canvas);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  line-height: 1.7;
}

.quiz-tutor-panel__action + .quiz-tutor-panel__action {
  margin-top: var(--space-3);
}

.quiz-tutor-panel__action.is-active {
  border-color: var(--color-accent);
  color: var(--color-accent);
}

.quiz-tutor-panel__checkbox {
  margin-right: var(--space-2);
}
</style>
