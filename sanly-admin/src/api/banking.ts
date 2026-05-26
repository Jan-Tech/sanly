import axios from 'axios'
import type { ApiResponse, PageResponse } from '../types'

const client = axios.create({
  baseURL: import.meta.env.VITE_GATEWAY_URL ?? 'http://localhost:8080',
})

client.interceptors.request.use((config) => {
  const raw = localStorage.getItem('sanly-admin-auth')
  if (raw) {
    try {
      const { state } = JSON.parse(raw)
      if (state?.token) config.headers.Authorization = `Bearer ${state.token}`
    } catch { /* ignore */ }
  }
  return config
})

// ── Types ─────────────────────────────────────────────────────────────────────

export interface RegisteredBank {
  bankId: string
  bankCode: string
  bankName: string
  licenseNumber: string
  contactEmail: string
  status: 'PENDING_APPROVAL' | 'ACTIVE' | 'SUSPENDED' | 'REVOKED'
  registeredAt: string
  approvedAt?: string
}

export interface AdminConsentRequest {
  consentId: string
  consentCode: string
  bankCode: string
  bankName: string
  citizenNationalId: string
  status: string
  requestedAt: string
  expiresAt: string
  approvedAt?: string
  rejectedAt?: string
  requestedScopes: string[]
}

export interface AdminDataAccess {
  accessId: string
  bankCode: string
  bankName: string
  citizenNationalId: string
  consentCode: string
  scopesAccessed: string[]
  accessedAt: string
  responseStatus: string
}

export interface BankingStats {
  consentRequestsToday: number
  approvedToday: number
  rejectedToday: number
  expiredToday: number
  totalActiveBanks: number
}

// ── API calls ─────────────────────────────────────────────────────────────────

export const listBanks = () =>
  client.get<ApiResponse<RegisteredBank[]>>('/api/v1/banking/banks').then(r => r.data.data ?? [])

export const approveBank = (bankCode: string) =>
  client.patch<ApiResponse<RegisteredBank>>(`/api/v1/banking/banks/${bankCode}/approve`).then(r => r.data.data!)

export const updateBankStatus = (bankCode: string, status: string) =>
  client.patch<ApiResponse<RegisteredBank>>(`/api/v1/banking/banks/${bankCode}/status`, { status }).then(r => r.data.data!)

export const rotateApiKey = (bankCode: string) =>
  client.post<ApiResponse<{ bankCode: string; apiKey: string }>>(`/api/v1/banking/banks/${bankCode}/rotate-key`).then(r => r.data.data!)

export const getAdminConsents = (params: { status?: string; bankCode?: string; page?: number; size?: number }) =>
  client.get<ApiResponse<PageResponse<AdminConsentRequest>>>('/api/v1/banking/admin/consents', { params }).then(r => r.data.data!)

export const getAdminAccessLog = (params: { bankCode?: string; from?: string; to?: string; page?: number; size?: number }) =>
  client.get<ApiResponse<PageResponse<AdminDataAccess>>>('/api/v1/banking/admin/access-log', { params }).then(r => r.data.data!)

export const getBankingStats = () =>
  client.get<ApiResponse<BankingStats>>('/api/v1/banking/admin/stats').then(r => r.data.data!)
