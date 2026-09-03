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
      <RouterLink to="/">Lading</RouterLink>
      <RouterLink to="/login">login</RouterLink>
      <RouterLink to="/home/dashboard">Dashboard</RouterLink>
      <RouterLink to="/home/analysis">Analysis</RouterLink>
      <RouterLink to="/home/review">Review</RouterLink>
      <RouterLink to="/home/growth">Growth</RouterLink>
    </nav>
    <RouterView />
</template>