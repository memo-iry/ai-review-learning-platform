<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'

const props = defineProps({
  text: { type: String, default: '' },
  /** 글자당 간격(ms) */
  speed: { type: Number, default: 26 },
})

const displayed = ref('')
let timerId = null

const prefersReducedMotion =
  typeof window !== 'undefined' && window.matchMedia
    ? window.matchMedia('(prefers-reduced-motion: reduce)').matches
    : false

const done = computed(() => displayed.value.length >= props.text.length)

function clearTimer() {
  if (timerId) {
    clearTimeout(timerId)
    timerId = null
  }
}

function typeFrom(index) {
  clearTimer()
  if (prefersReducedMotion) {
    displayed.value = props.text
    return
  }
  if (index >= props.text.length) return
  displayed.value = props.text.slice(0, index + 1)
  timerId = setTimeout(() => typeFrom(index + 1), props.speed)
}

watch(
  () => props.text,
  () => {
    displayed.value = ''
    typeFrom(0)
  },
  { immediate: true },
)

onBeforeUnmount(clearTimer)
</script>

<template>
  <span class="typewriter">
    {{ displayed }}<span v-if="!done" class="typewriter__cursor" aria-hidden="true" />
  </span>
</template>

<style scoped>
.typewriter__cursor {
  display: inline-block;
  width: 2px;
  height: 1em;
  margin-left: 1px;
  background: currentColor;
  vertical-align: -0.15em;
  animation: typewriter-blink 0.9s step-end infinite;
}

@keyframes typewriter-blink {
  50% { opacity: 0; }
}

@media (prefers-reduced-motion: reduce) {
  .typewriter__cursor { display: none; }
}
</style>
