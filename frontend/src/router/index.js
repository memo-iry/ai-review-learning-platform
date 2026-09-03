import { createRouter, createWebHistory } from 'vue-router'
import { learningState } from '../stores/learning.js'

const router = createRouter({
  history: createWebHistory(),

  routes: [
    {
      path: '/',
      name: 'login',
      component: () => import('../pages/LoginPage.vue'),
      meta: {
        step: '로그인',
      },
    },
    {
      path: '/home',
      name: 'home',
      component: () => import('../pages/HomePage.vue'),
      redirect: { name: 'dashboard' },
      meta: {
        step: '메인페이지',
      },

      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('../pages/DashboardPage.vue'),
          meta: {
            step: '대시보드',
          },
        },
        {
          // 실제 주소: /home/reflection
          path: 'reflection',
          name: 'reflection',
          component: () => import('../pages/ReflectionPage.vue'),
          meta: {
            step: '회고록',
            requiresLecture: true,
          },
        },
        {
          // 실제 주소: /home/analysis
          path: 'analysis',
          name: 'analysis',
          component: () => import('../pages/AnalysisPage.vue'),
          meta: {
            step: 'AI 분석',
            requiresAnalysis: true,
          },
        },
        {
          // 실제 주소: /home/review
          path: 'review',
          name: 'review',
          component: () => import('../pages/ReviewPage.vue'),
          meta: {
            step: '복습자료',
            requiresAnalysis: true,
          },
        },
        {
          // 실제 주소: /home/growth
          path: 'growth',
          name: 'growth',
          component: () => import('../pages/GrowthPage.vue'),
          meta: {
            step: '내 성장',
            requiresAnalysis: true,
          },
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],

  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to) => {
  if (to.meta.requiresLecture && !learningState.lecture) {
    return {
      name: 'dashboard',
    }
  }

  if (to.meta.requiresAnalysis && !learningState.analysis) {
    return {
      name: learningState.lecture ? 'reflection' : 'dashboard',
    }
  }

  return true
})

export default router