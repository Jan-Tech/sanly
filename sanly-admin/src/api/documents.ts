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

export interface AdminCertificate {
  certificateId: string
  certificateCode: string
  citizenNationalId: string
  holderName?: string
  documentType: string
  sourceRecordCode?: string
  status: 'ACTIVE' | 'EXPIRED' | 'REVOKED'
  issuedAt: string
  expiresAt: string
  downloadCount?: number
  verificationCount?: number
}

export interface AdminTrackedItem {
  trackingId: string
  trackingCode: string
  citizenNationalId: string
  itemType: string
  sourceService: string
  sourceItemCode: string
  currentStatus: string
  title: string
  isCompleted: boolean
  createdAt: string
  lastUpdatedAt: string
}

export interface DocumentStats {
  certsGeneratedToday: number
  verificationsToday: number
  activeCerts: number
  topDocumentType?: string
}

export async function getAdminDocumentStats(): Promise<DocumentStats> {
  const res = await client.get<ApiResponse<DocumentStats>>(`/documents/api/v1/documents/admin/stats`)
  return res.data.data!
}

export async function getAdminCertificates(params: {
  page?: number; size?: number
}): Promise<PageResponse<AdminCertificate>> {
  const res = await client.get<ApiResponse<PageResponse<AdminCertificate>>>(
    `/documents/api/v1/documents/admin/all`,
    { params }
  )
  return res.data.data!
}

export async function adminRevokeCertificate(certCode: string, reason: string): Promise<void> {
  await client.patch(
    `/documents/api/v1/documents/admin/${certCode}/revoke`,
    null,
    { params: { reason } }
  )
}

export async function getAdminTrackedItems(params: {
  page?: number; size?: number
}): Promise<PageResponse<AdminTrackedItem>> {
  const res = await client.get<ApiResponse<PageResponse<AdminTrackedItem>>>(
    `/documents/api/v1/documents/admin/tracking`,
    { params }
  )
  return res.data.data!
}
