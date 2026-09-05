<script setup>
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import AppLayout from './components/AppLayout.vue'

const route = useRoute()

async function movePage(path) {
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
