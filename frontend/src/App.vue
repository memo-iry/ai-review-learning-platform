<script setup>
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import AppLayout from './components/AppLayout.vue'

const route = useRoute()

const currentStep = computed(() => {
  return route.meta.step ?? '대시보드'
})
</script>

<template>
    <nav>
      <RouterLink to="/">login</RouterLink>
      <RouterLink to="/home">Home</RouterLink>
    </nav>

    <RouterView />
</template>