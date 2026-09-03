const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api";

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
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

  return response.json();
}

export const api = {
  getLectures: () => request("/lectures"),
  getMaterials: (lectureId) => request(`/lectures/${lectureId}/materials`),
  createReflection: (body) =>
    request("/reflections", {
      method: "POST",
      body: JSON.stringify(body),
    }),
  // 회고 전체 목록 조회 (GET /api/reflections?userId=)
  getReflections: (userId) => request(`/reflections?userId=${userId}`),
  analyzeReflection: (reflectionId) =>
    request(`/reflections/${reflectionId}/analyze`, {
      method: "POST",
    }),
  getMastery: (userId) => request(`/users/${userId}/mastery`),
  // Quiz 단건 조회 (문항만, 정답/해설 제외)
  getQuiz: (quizId) => request(`/quizzes/${quizId}`),
  // 사용자와 연결된 Quiz 전체 목록 조회
  getQuizzes: (userId) => request(`/quizzes?userId=${userId}`),
  // Quiz 응시 이력 조회 (정답률 등 표시용)
  getQuizAttempts: (userId) => request(`/quizzes/attempts?userId=${userId}`),
  submitQuiz: (quizId, body) =>
    request(`/quizzes/${quizId}/attempts`, {
      method: "POST",
      body: JSON.stringify(body),
    }),
};
