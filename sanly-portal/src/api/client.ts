import axios from 'axios'
import { useAuthStore } from '../store/authStore'

function makeClient(baseURL: string, getToken?: () => string | null) {
  const instance = axios.create({ baseURL, timeout: 10_000 })

  instance.interceptors.request.use((config) => {
    const token = getToken ? getToken() : null
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

// Citizen-registry: use the citizen's own JWT (from Zustand)
export const registryClient = makeClient(
  '/proxy/registry',
  () => useAuthStore.getState().token
)

// Downstream services: use pre-configured service tokens from .env
export const medicalClient = makeClient(
  '/proxy/medical',
  () => import.meta.env.VITE_MEDICAL_TOKEN || null
)

export const dmvClient = makeClient(
  '/proxy/dmv',
  () => import.meta.env.VITE_DMV_TOKEN || null
)

export const taxClient = makeClient(
  '/proxy/tax',
  () => import.meta.env.VITE_TAX_TOKEN || null
)

export const businessClient = makeClient(
  '/proxy/business',
  () => import.meta.env.VITE_BUSINESS_TOKEN || null
)

export const civilClient = makeClient(
  '/proxy/civil',
  () => import.meta.env.VITE_CIVIL_TOKEN || null
)

// Bridge: X-Institution-* headers
export const bridgeClient = axios.create({ baseURL: '/proxy/bridge', timeout: 10_000 })
bridgeClient.interceptors.request.use((config) => {
  config.headers['X-Institution-Code'] = import.meta.env.VITE_BRIDGE_INSTITUTION_CODE || 'INST_PORTAL'
  config.headers['X-Institution-Key'] = import.meta.env.VITE_BRIDGE_INSTITUTION_KEY || ''
  return config
})
