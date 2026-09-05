import { reactive } from 'vue'
import { api } from '../api/client.js'

export const authState = reactive({
  user: null,
  restored: false,
})

export function currentUserId() {
  return authState.user?.userId ?? null
}

export function isAdmin() {
  return authState.user?.role === 'ADMIN'
}

export async function login(email, password) {
  const user = await api.login({ email, password })
  authState.user = user
  authState.restored = true
  return user
}

export async function logout() {
  try {
    await api.logout()
  } finally {
    authState.user = null
  }
}

export async function restoreSession() {
  if (authState.restored) {
    return authState.user
  }
  try {
    authState.user = await api.me()
  } catch {
    authState.user = null
  } finally {
    authState.restored = true
  }
  return authState.user
}
