import axios, { type AxiosInstance } from 'axios'
import { useAuthStore } from '../store/authStore'

const GATEWAY = process.env.EXPO_PUBLIC_GATEWAY_URL ?? 'http://localhost:8080'

function makeClient(prefix: string, getToken: () => string | null): AxiosInstance {
  const client = axios.create({ baseURL: `${GATEWAY}/${prefix}` })

  client.interceptors.request.use(config => {
    const token = getToken()
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
  })

  client.interceptors.response.use(
    res => res,
    err => {
      if (err?.response?.status === 401) {
        useAuthStore.getState().clearAuth()
      }
      return Promise.reject(err)
    }
  )

  return client
}

// Officer JWT client — for citizen-registry calls (citizen lookups, bridge queries)
export const registryClient = makeClient('registry', () => useAuthStore.getState().token)

// Service token clients — for downstream service operations
const svcToken = (key: string) => () => process.env[key] ?? null

export const policeClient    = makeClient('police',      svcToken('EXPO_PUBLIC_POLICE_TOKEN'))
export const dmvClient       = makeClient('dmv',         svcToken('EXPO_PUBLIC_DMV_TOKEN'))
export const medicalClient   = makeClient('medical',     svcToken('EXPO_PUBLIC_MEDICAL_TOKEN'))
export const customsClient   = makeClient('customs',     svcToken('EXPO_PUBLIC_CUSTOMS_TOKEN'))
export const civilClient     = makeClient('civil',       svcToken('EXPO_PUBLIC_CIVIL_TOKEN'))
export const courtClient     = makeClient('court',       svcToken('EXPO_PUBLIC_COURT_TOKEN'))
export const educationClient = makeClient('education',   svcToken('EXPO_PUBLIC_EDUCATION_TOKEN'))
export const landClient      = makeClient('land',        svcToken('EXPO_PUBLIC_LAND_TOKEN'))
export const taxClient       = makeClient('tax',         svcToken('EXPO_PUBLIC_TAX_TOKEN'))
export const socialClient    = makeClient('social',      svcToken('EXPO_PUBLIC_SOCIAL_TOKEN'))
export const appointmentsClient = makeClient('appointments', svcToken('EXPO_PUBLIC_APPOINTMENTS_TOKEN'))

// Bridge client for cross-agency data queries
export const bridgeClient = axios.create({ baseURL: `${GATEWAY}/bridge` })
bridgeClient.interceptors.request.use(config => {
  config.headers['X-Institution-Code'] = process.env.EXPO_PUBLIC_BRIDGE_INSTITUTION_CODE ?? ''
  config.headers['X-Institution-Key']  = process.env.EXPO_PUBLIC_BRIDGE_INSTITUTION_KEY ?? ''
  return config
})
