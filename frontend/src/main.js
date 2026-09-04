import { createApp } from 'vue'
import App from './App.vue'
import router from './router/index.js'
import { setSessionExpiredHandler } from './api/http.js'
import { authState } from './stores/auth.js'
import { resetLearning } from './stores/learning.js'
import './styles.css'

setSessionExpiredHandler(() => {
  if (!authState.user) {
    return
  }
  authState.user = null
  authState.restored = true
  resetLearning()
  router.push({ name: 'login' })
})

createApp(App).use(router).mount('#app')
