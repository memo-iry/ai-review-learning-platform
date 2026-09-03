import { reactive } from 'vue'

export const learningState = reactive({
  lecture: {
    lectureId: 1,
    courseId: 1,
    title: 'Vue Router와 상태 관리',
    courseName: 'IT Full Stack',
    instructor: '이현준 강사',
    learningDate: '2026-09-03',
  },

  reflection: {
    understoodContent:
      'Vue Router를 이용해 페이지를 이동하고 라우트 이름을 지정하는 방법을 이해했습니다.',

    difficultContent:
      '중첩 라우팅과 RouterView의 관계, 상태 데이터를 다른 페이지로 전달하는 부분이 어렵습니다.',
  },

  analysis: {
    understandingScore: 72,

    analysisReason:
      '기본적인 Vue Router 사용법과 페이지 이동 방식은 이해하고 있습니다. 다만 중첩 라우팅에서 부모 RouterView와 자식 RouterView가 각각 어떤 역할을 하는지에 대한 추가 복습이 필요합니다.',

    understoodSummary:
      'createRouter와 createWebHistory를 사용한 라우터 생성 방법, RouterLink를 통한 페이지 이동, router.push()를 이용한 프로그래밍 방식의 페이지 이동을 이해했습니다.',

    weaknessSummary:
      'children에 등록하는 자식 경로의 작성 방법과 부모 컴포넌트 내부에 RouterView를 배치해야 하는 이유를 보완해야 합니다.',

    reviewMaterial: {
      title: 'Vue Router 중첩 라우팅 맞춤 복습',

      coreConcepts: [
        '부모 라우트의 children 배열에 자식 라우트를 등록합니다.',
        '자식 라우트의 path 앞에는 슬래시를 붙이지 않습니다.',
        '자식 컴포넌트는 부모 컴포넌트의 RouterView 위치에 표시됩니다.',
        '페이지 간 공용 데이터는 reactive 상태 또는 Pinia로 관리합니다.',
        'router.push()에 라우트 이름을 사용하면 주소 변경에 유연하게 대응할 수 있습니다.',
      ],

      exampleCode: `const routes = [
  {
    path: '/home',
    name: 'home',
    component: () => import('../pages/HomePage.vue'),

    children: [
      {
        path: 'analysis',
        name: 'analysis',
        component: () => import('../pages/AnalysisPage.vue'),
      },
      {
        path: 'review',
        name: 'review',
        component: () => import('../pages/ReviewPage.vue'),
      },
    ],
  },
]

// /home/analysis로 이동
router.push({ name: 'analysis' })

// /home/review로 이동
router.push({ name: 'review' })`,

      quiz: [
        {
          question: 'children에 작성하는 path 앞에 슬래시를 붙여야 할까요?',
          answer:
            '아니요. 상대 경로로 연결하려면 analysis처럼 슬래시 없이 작성해야 합니다.',
        },
        {
          question: '자식 페이지는 어느 위치에 표시될까요?',
          answer:
            '부모 컴포넌트에 작성된 RouterView 위치에 표시됩니다.',
        },
        {
          question: '화면 이동과 함께 분석 결과를 보관하려면 어떻게 해야 할까요?',
          answer:
            'reactive 상태 또는 Pinia 같은 전역 상태 관리 도구에 분석 결과를 저장합니다.',
        },
      ],
    },
  },
})