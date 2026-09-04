import { createApp } from 'vue'
import App from './App.vue'
import router from './router/index.js'
import { setSessionExpiredHandler } from './api/http.js'
import { authState } from './stores/auth.js'
import { resetLearning } from './stores/learning.js'
import './styles.css'

// 서버가 401 을 돌려주면 세션이 끊긴 것이다.
// http.js 가 라우터를 직접 import 하면 api 계층이 라우터에 묶이므로 여기서 주입한다.
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
