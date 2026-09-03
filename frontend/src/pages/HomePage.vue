<script setup>
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
    <div>
      <nav>
          <RouterLink to="/home/dashboard">Dashboard</RouterLink>
          <RouterLink to="/home/analysis">Analysis</RouterLink>
          <RouterLink to="/home/review">Review</RouterLink>
          <RouterLink to="/home/growth">Growth</RouterLink>
      </nav>
      <RouterView />
  </div>
</template>
