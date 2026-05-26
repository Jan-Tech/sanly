import axios, { type AxiosInstance } from 'axios'
import { useAuthStore } from '../store/authStore'

const GATEWAY = process.env.EXPO_PUBLIC_GATEWAY_URL ?? 'http://localhost:8080'

function makeClient(prefix: string, getToken: () => string | null): AxiosInstance {
  const instance = axios.create({ baseURL: `${GATEWAY}/${prefix}`, timeout: 12_000 })

  instance.interceptors.request.use((config) => {
    const token = getToken()
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
  })

  instance.interceptors.response.use(
    (res) => res,
    async (err) => {
      if (err.response?.status === 401) {
        await useAuthStore.getState().clearAuth()
      }
      return Promise.reject(err)
    }
  )

  return instance
}

// Uses the citizen's own JWT
export const registryClient = makeClient('registry', () => useAuthStore.getState().token)
export const notificationsClient = makeClient('notifications', () => useAuthStore.getState().token)
export const appointmentsClient = makeClient('appointments', () => useAuthStore.getState().token)
export const documentsClient = makeClient('documents', () => useAuthStore.getState().token)
export const bankingClient = makeClient('banking', () => useAuthStore.getState().token)
export const signaturesClient = makeClient('signature', () => useAuthStore.getState().token)

// Use service/institution tokens from env
export const medicalClient = makeClient('medical', () => process.env.EXPO_PUBLIC_MEDICAL_TOKEN ?? null)
export const dmvClient = makeClient('dmv', () => process.env.EXPO_PUBLIC_DMV_TOKEN ?? null)
export const taxClient = makeClient('tax', () => process.env.EXPO_PUBLIC_TAX_TOKEN ?? null)
export const businessClient = makeClient('business', () => process.env.EXPO_PUBLIC_BUSINESS_TOKEN ?? null)
export const educationClient = makeClient('education', () => process.env.EXPO_PUBLIC_EDUCATION_TOKEN ?? null)
export const landClient = makeClient('land', () => process.env.EXPO_PUBLIC_LAND_TOKEN ?? null)
export const socialClient = makeClient('social', () => process.env.EXPO_PUBLIC_SOCIAL_TOKEN ?? null)
export const pensionClient = makeClient('pension', () => process.env.EXPO_PUBLIC_PENSION_TOKEN ?? null)
export const vehicleClient = makeClient('vehicle', () => process.env.EXPO_PUBLIC_VEHICLE_TOKEN ?? null)
export const customsClient = makeClient('customs', () => process.env.EXPO_PUBLIC_CUSTOMS_TOKEN ?? null)
export const courtClient = makeClient('court', () => process.env.EXPO_PUBLIC_COURT_TOKEN ?? null)
export const signatureClient = makeClient('signature', () => process.env.EXPO_PUBLIC_SIGNATURE_TOKEN ?? null)

// Bridge — X-Institution-* headers
export const bridgeClient = axios.create({ baseURL: `${GATEWAY}/bridge`, timeout: 12_000 })
bridgeClient.interceptors.request.use((config) => {
  config.headers['X-Institution-Code'] = process.env.EXPO_PUBLIC_BRIDGE_INSTITUTION_CODE ?? 'INST_PORTAL'
  config.headers['X-Institution-Key'] = process.env.EXPO_PUBLIC_BRIDGE_INSTITUTION_KEY ?? ''
  return config
})
