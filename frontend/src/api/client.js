import { http } from "./http.js";

export const api = {
  login: (body) => http.post("/auth/login", body),

  me: () => http.get("/auth/me", { skipAuthHandler: true }),

  logout: () => http.post("/auth/logout"),

  getLectures: () => http.get("/lectures"),
  getLecture: (lectureId) => http.get(`/lectures/${lectureId}`),
  getMaterials: (lectureId) => http.get(`/lectures/${lectureId}/materials`),

  createReflection: (body) => http.post("/reflections", body),
  getReflections: (userId) => http.get("/reflections", { params: { userId } }),
  analyzeReflection: (reflectionId) =>
    http.post(`/reflections/${reflectionId}/analyze`),

  getMastery: (userId) => http.get(`/users/${userId}/mastery`),

  getReviews: (userId) => http.get(`/users/${userId}/reviews`),
  getReview: (reviewId) => http.get(`/reviews/${reviewId}`),

  getQuiz: (quizId) => http.get(`/quizzes/${quizId}`),
  getQuizzes: (userId) => http.get("/quizzes", { params: { userId } }),
  getQuizAttempts: (userId) =>
    http.get("/quizzes/attempts", { params: { userId } }),
  submitQuiz: (quizId, body) => http.post(`/quizzes/${quizId}/attempts`, body),

  getAnalysis: (userId, lectureId) =>
    request(
      `/analyses?userId=${encodeURIComponent(userId)}&lectureId=${encodeURIComponent(lectureId)}`,
    ),

  admin: {
    getOverview: () => http.get("/admin/overview"),
  },
};

export { ApiError, setSessionExpiredHandler } from "./http.js";
