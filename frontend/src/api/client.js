import { http } from "./http.js";

export const api = {
  // 인증
  login: (body) => http.post("/auth/login", body),

  // 새로고침 후 로그인 상태 복원용.
  // 401 은 "비로그인"이라는 정상 응답이므로 전역 세션 만료 처리를 건너뛴다.
  me: () => http.get("/auth/me", { skipAuthHandler: true }),

  logout: () => http.post("/auth/logout"),

  // 강의
  getLectures: () => http.get("/lectures"),
  getLecture: (lectureId) => http.get(`/lectures/${lectureId}`),
  getMaterials: (lectureId) => http.get(`/lectures/${lectureId}/materials`),

  // 회고
  createReflection: (body) => http.post("/reflections", body),
  getReflections: (userId) => http.get("/reflections", { params: { userId } }),
  analyzeReflection: (reflectionId) =>
    http.post(`/reflections/${reflectionId}/analyze`),

  // 이해도
  getMastery: (userId) => http.get(`/users/${userId}/mastery`),

  // 복습자료
  getReviews: (userId) => http.get(`/users/${userId}/reviews`),
  getReview: (reviewId) => http.get(`/reviews/${reviewId}`),

  // Quiz
  getQuiz: (quizId) => http.get(`/quizzes/${quizId}`),
  getQuizzes: (userId) => http.get("/quizzes", { params: { userId } }),
  getQuizAttempts: (userId) =>
    http.get("/quizzes/attempts", { params: { userId } }),
  submitQuiz: (quizId, body) => http.post(`/quizzes/${quizId}/attempts`, body),

  getAnalysis: (userId, lectureId) =>
    request(
      `/analyses?userId=${encodeURIComponent(userId)}&lectureId=${encodeURIComponent(lectureId)}`,
    ),

  // 운영자 전용
  // 별도 객체로 두되 파일은 나누지 않는다. baseURL 도 인증 방식도 같아서 axios
  // 인스턴스를 나눌 이유가 없고, 엔드포인트가 하나뿐이라 파일을 나눌 이유도 아직 없다.
  // 운영자 요구사항(REQ-ADMIN-*)이 늘어나면 그때 admin.js 로 분리한다.
  // 실제 접근 통제는 서버가 한다. 이 구분은 통제 수단이 아니라 호출부에서
  // "이건 권한이 필요한 요청"이라는 걸 드러내기 위한 것이다.
  admin: {
    getOverview: () => http.get("/admin/overview"),
  },
};

// 에러 타입과 세션 만료 훅도 여기서 함께 내보낸다.
// 호출부는 "@/api/client" 하나만 알면 된다.
export { ApiError, setSessionExpiredHandler } from "./http.js";
