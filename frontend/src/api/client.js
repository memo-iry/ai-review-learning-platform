const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api";

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    // 세션 쿠키를 실어 보낸다. 없으면 로그인해도 서버가 사용자를 알 수 없다.
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...options.headers,
    },
    ...options,
  });

  if (!response.ok) {
    const error = await response
      .json()
      .catch(() => ({ message: "요청에 실패했습니다." }));
    throw new Error(error.message ?? "요청에 실패했습니다.");
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

export const api = {
  // 인증
  login: (body) =>
    request("/auth/login", {
      method: "POST",
      body: JSON.stringify(body),
    }),
  // 새로고침 후 로그인 상태 복원용. 401 이면 로그인하지 않은 상태다.
  me: () => request("/auth/me"),
  logout: () => request("/auth/logout", { method: "POST" }),

  // 강의
  getLectures: () => request("/lectures"),
  getLecture: (lectureId) => request(`/lectures/${lectureId}`),
  getMaterials: (lectureId) => request(`/lectures/${lectureId}/materials`),

  // 회고
  createReflection: (body) =>
    request("/reflections", {
      method: "POST",
      body: JSON.stringify(body),
    }),
  getReflections: (userId) => request(`/reflections?userId=${userId}`),
  analyzeReflection: (reflectionId) =>
    request(`/reflections/${reflectionId}/analyze`, {
      method: "POST",
    }),

  // 이해도
  getMastery: (userId) => request(`/users/${userId}/mastery`),

  // 복습자료
  getReviews: (userId) => request(`/users/${userId}/reviews`),
  getReview: (reviewId) => request(`/reviews/${reviewId}`),

  // Quiz
  getQuiz: (quizId) => request(`/quizzes/${quizId}`),
  getQuizzes: (userId) => request(`/quizzes?userId=${userId}`),
  getQuizAttempts: (userId) => request(`/quizzes/attempts?userId=${userId}`),
  submitQuiz: (quizId, body) =>
    request(`/quizzes/${quizId}/attempts`, {
      method: "POST",
      body: JSON.stringify(body),
    }),

  // 운영자 전용
  getAdminOverview: () => request("/admin/overview"),
};
