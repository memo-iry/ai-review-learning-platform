const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api'

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: '요청에 실패했습니다.' }))
    throw new Error(error.message ?? '요청에 실패했습니다.')
  }

  return response.json()
}

export const api = {
  getLectures: () => request('/lectures'),
  getMaterials: (lectureId) => request(`/lectures/${lectureId}/materials`),
  createReflection: (body) => request('/reflections', {
    method: 'POST',
    body: JSON.stringify(body),
  }),
  analyzeReflection: (reflectionId) => request(`/reflections/${reflectionId}/analyze`, {
    method: 'POST',
  }),
  getMastery: (userId) => request(`/users/${userId}/mastery`),
}
