<script>
import {RouterLink, RouterView} from 'vue-router'
</script>

<template>
    <div>
        <nav>
            <RouterLink to="/home/">Home</RouterLink>
            <RouterLink to="/home/dashboard">Dashboard</RouterLink>
            <RouterLink to="/home/reflection">Reflection</RouterLink>
            <RouterLink to="/home/analysis">Analysis</RouterLink>
            <RouterLink to="/home/review">Review</RouterLink>
            <RouterLink to="/home/growth">Growth</RouterLink>
        </nav>
        <RouterView />
    </div>
</template>

<style scoped>
nav {
    display: flex;
    gap: 1rem;
    margin-bottom: 1rem;
}
nav a {
    text-decoration: none;
    color: #42b983;
}
nav a.router-link-exact-active {
    font-weight: bold;
}
</style>