import axios from 'axios'
import { useAuthStore } from '../store/authStore'

function makeClient(baseURL: string, getToken: () => string | null) {
  const instance = axios.create({ baseURL, timeout: 10_000 })
  instance.interceptors.request.use((config) => {
    const token = getToken()
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
  })
  instance.interceptors.response.use(
    (res) => res,
    (err) => {
      if (err.response?.status === 401) {
        useAuthStore.getState().clearAuth()
        window.location.href = '/login'
      }
      return Promise.reject(err)
    }
  )
  return instance
}

// Admin's own citizen-registry JWT (set after login)
export const registryClient = makeClient('/proxy/registry', () => useAuthStore.getState().token)

// Pre-configured tokens for other services (set in .env)
export const bridgeClient = makeClient('/proxy/bridge', () => import.meta.env.VITE_BRIDGE_ADMIN_TOKEN || null)
export const medicalClient = makeClient('/proxy/medical', () => import.meta.env.VITE_MEDICAL_TOKEN || null)
export const dmvClient = makeClient('/proxy/dmv', () => import.meta.env.VITE_DMV_TOKEN || null)
export const policeClient = makeClient('/proxy/police', () => import.meta.env.VITE_POLICE_TOKEN || null)
export const taxClient = makeClient('/proxy/tax', () => import.meta.env.VITE_TAX_TOKEN || null)
export const businessClient = makeClient('/proxy/business', () => import.meta.env.VITE_BUSINESS_TOKEN || null)
export const civilClient = makeClient('/proxy/civil', () => import.meta.env.VITE_CIVIL_TOKEN || null)
export const educationClient = makeClient('/proxy/education', () => import.meta.env.VITE_EDUCATION_TOKEN || null)

// Unauthenticated clients for health checks
export const healthClient = (proxyPath: string) => axios.create({ baseURL: proxyPath, timeout: 5_000 })
