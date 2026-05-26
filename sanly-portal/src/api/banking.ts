import axios from 'axios'
import type { ApiResponse } from '../types'

const bankingClient = axios.create({
  baseURL: import.meta.env.VITE_GATEWAY_URL ?? 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
})

bankingClient.interceptors.request.use((config) => {
  const raw = localStorage.getItem('sanly-auth')
  if (raw) {
    try {
      const { state } = JSON.parse(raw)
      if (state?.token) config.headers.Authorization = `Bearer ${state.token}`
    } catch { /* ignore */ }
  }
  return config
})

// ── Types ─────────────────────────────────────────────────────────────────────

export interface ScopeDescription {
  scope: string
  name: string
  description: string
}

export interface ConsentRequest {
  consentId: string
  consentCode: string
  bankCode: string
  bankName: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'EXPIRED'
  requestedAt: string
  expiresAt: string
  approvedAt?: string
  rejectedAt?: string
  purpose: string
  requestedScopes: ScopeDescription[]
}

export interface BankDataAccess {
  accessId: string
  bankCode: string
  bankName: string
  citizenNationalId: string
  consentCode: string
  scopesAccessed: string[]
  accessedAt: string
  responseStatus: 'SUCCESS' | 'PARTIAL' | 'FAILED'
}

// ── API calls ─────────────────────────────────────────────────────────────────

export async function getPendingConsents(): Promise<ConsentRequest[]> {
  const res = await bankingClient.get<ApiResponse<ConsentRequest[]>>(
    '/api/v1/banking/consent/pending'
  )
  return res.data.data ?? []
}

export async function getConsentDetail(consentCode: string): Promise<ConsentRequest> {
  const res = await bankingClient.get<ApiResponse<ConsentRequest>>(
    `/api/v1/banking/consent/${consentCode}`
  )
  return res.data.data!
}

export async function approveConsent(consentCode: string, approvedScopes: string[], otpCode: string): Promise<void> {
  await bankingClient.post(`/api/v1/banking/consent/${consentCode}/approve`, {
    approvedScopes,
    otpCode,
  })
}

export async function rejectConsent(consentCode: string): Promise<void> {
  await bankingClient.post(`/api/v1/banking/consent/${consentCode}/reject`)
}

export async function getMyConsentHistory(): Promise<ConsentRequest[]> {
  const res = await bankingClient.get<ApiResponse<ConsentRequest[]>>(
    '/api/v1/banking/consent/pending'  // Will also include history from same endpoint with status param
  )
  // The backend returns pending + historical depending on query params
  return res.data.data ?? []
}

export async function getAccessHistory(): Promise<BankDataAccess[]> {
  const res = await bankingClient.get<ApiResponse<BankDataAccess[]>>(
    '/api/v1/banking/my/access-history'
  )
  return res.data.data ?? []
}
