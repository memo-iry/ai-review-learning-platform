<script setup>
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import AppLayout from './components/AppLayout.vue'

const route = useRoute()


// 부모 컴포넌트
async function movePage(path) {
  // path 대신 name을 객체 형태로 전달
  await router.push({ path: path })

  console.log('현재 주소:', router.currentRoute.value.fullPath)
}

const currentStep = computed(() => {
  return route.meta.step ?? '대시보드'
})
</script>

<template>
    <RouterView />
</template>