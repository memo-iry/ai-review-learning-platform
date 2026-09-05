import { createRouter, createWebHistory } from "vue-router";
import { learningState } from "../stores/learning.js";
import { authState, restoreSession } from "../stores/auth.js";

const router = createRouter({
  history: createWebHistory(),

  routes: [
    {
      path: "/",
      name: "landing",
      component: () => import("../pages/LandingPage.vue"),
    },
    {
      path: "/login",
      name: "login",
      component: () => import("../pages/LoginPage.vue"),
      meta: {
        step: "로그인",
      },
    },
    {
      path: "/dashboard",
      name: "dashboard",
      component: () => import("../pages/DashboardPage.vue"),
      meta: {
        step: "대시보드",
      },
    },
    {
      path: "/courses",
      name: "courses",
      component: () => import("../pages/CourselistPage.vue"),
      meta: {
        step: "강의 목록",
      },
    },
    {
      path: "/reflection/:lectureId",
      name: "reflection",
      component: () => import("../pages/ReflectionPage.vue"),
      meta: {
        step: "회고록",
        requiresLecture: true,
      },
    },
    {
      path: '/analysis/:lectureId',
      name: 'analysis',
      component: () => import('../pages/AnalysisPage.vue'),
      meta: {
        step: 'AI 분석',
      },
    },
    {
      path: "/growth",
      name: "growth",
      component: () => import("../pages/GrowthPage.vue"),
      meta: {
        step: "내 성장",
        requiresAnalysis: true,
      },
    },
    {
      path: "/history",
      name: "history",
      component: () => import("../pages/HistoryPage.vue"),
      meta: {
        step: "회고 기록",
        requiresAnalysis: false,
      },
    },
    {
      path: "/quiz/:quizId",
      name: "quiz-attempt",
      component: () => import("../pages/QuizAttemptPage.vue"),
      meta: {
        step: "퀴즈 응시",
        requiresAnalysis: false,
      },
    },
    {
      path: "/quiz",
      name: "quiz",
      component: () => import("../pages/QuizPage.vue"),
      meta: {
        step: "회고 기록",
        requiresAnalysis: false,
      },
    },
    {
      path: "/:pathMatch(.*)*",
      redirect: "/",
    },
  ],
  scrollBehavior() {
    return { top: 0 };
  },
});

const PUBLIC = ["landing", "login"];

router.beforeEach(async (to) => {
  await restoreSession();

  if (!authState.user && !PUBLIC.includes(to.name)) {
    return { name: "login" };
  }

  if (authState.user && to.name === "login") {
    return { name: "dashboard" };
  }

  if (to.meta.requiresLecture && !learningState.lecture) {
    return {
      name: "dashboard",
    };
  }

  if (to.meta.requiresAnalysis && !learningState.analysis) {
    return {
      name: learningState.lecture ? "reflection" : "dashboard",
    };
  }

  return true;
});

export default router;
