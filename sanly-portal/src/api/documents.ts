import axios from 'axios'
import type { ApiResponse } from '../types'

const docsClient = axios.create({
  baseURL: import.meta.env.VITE_GATEWAY_URL ?? 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
})

docsClient.interceptors.request.use((config) => {
  const raw = localStorage.getItem('sanly-auth')
  if (raw) {
    try {
      const { state } = JSON.parse(raw)
      if (state?.token) config.headers.Authorization = `Bearer ${state.token}`
    } catch { /* ignore */ }
  }
  return config
})

// ── Certificate types ─────────────────────────────────────────────────────────

export interface CertificateRecord {
  certificateId: string
  certificateCode: string
  citizenNationalId: string
  holderName?: string
  documentType: string
  sourceService?: string
  sourceRecordCode?: string
  title?: string
  issuedAt: string
  expiresAt: string
  status: 'ACTIVE' | 'EXPIRED' | 'REVOKED'
  verificationHash: string
  downloadCount?: number
}

export interface CertificateVerifyResponse {
  valid: boolean
  certificateCode?: string
  documentType?: string
  holderName?: string
  holderNationalId?: string
  issuedAt?: string
  expiresAt?: string
  status?: string
  message?: string
}

// ── Tracking types ────────────────────────────────────────────────────────────

export interface TrackingUpdate {
  updateId: string
  status: string
  description?: string
  updatedAt: string
  updatedByService: string
}

export interface TrackedItem {
  trackingId: string
  trackingCode: string
  citizenNationalId: string
  itemType: string
  sourceService: string
  sourceItemCode: string
  currentStatus: string
  statusDescription?: string
  title: string
  isCompleted: boolean
  createdAt: string
  lastUpdatedAt: string
  completedAt?: string
  updates: TrackingUpdate[]
}

// ── Certificate API ───────────────────────────────────────────────────────────

export async function getMyCertificates(): Promise<CertificateRecord[]> {
  const res = await docsClient.get<ApiResponse<CertificateRecord[]>>(
    `/documents/api/v1/documents/certificates/my`
  )
  return res.data.data ?? []
}

export async function generateCertificate(documentType: string, sourceRecordCode?: string): Promise<void> {
  const res = await docsClient.post(
    `/documents/api/v1/documents/certificates/generate`,
    { documentType, sourceRecordCode },
    { responseType: 'blob' }
  )
  const blob = new Blob([res.data], { type: 'application/pdf' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `certificate-${documentType}.pdf`
  a.click()
  URL.revokeObjectURL(url)
}

export async function downloadCertificatePdf(certCode: string): Promise<void> {
  const res = await docsClient.get(
    `/documents/api/v1/documents/certificates/${certCode}/download`,
    { responseType: 'blob' }
  )
  const blob = new Blob([res.data], { type: 'application/pdf' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `cert-${certCode}.pdf`
  a.click()
  URL.revokeObjectURL(url)
}

export async function verifyCertificate(certCode: string): Promise<CertificateVerifyResponse> {
  const res = await docsClient.get<ApiResponse<CertificateVerifyResponse>>(
    `/documents/api/v1/documents/verify/${certCode}`
  )
  return res.data.data!
}

// ── Tracking API ──────────────────────────────────────────────────────────────

export async function getMyTrackedItems(isCompleted?: boolean): Promise<TrackedItem[]> {
  const res = await docsClient.get<ApiResponse<TrackedItem[]>>(
    `/documents/api/v1/documents/tracking/my`,
    { params: isCompleted !== undefined ? { isCompleted } : {} }
  )
  return res.data.data ?? []
}

export async function getTrackedItem(trackingCode: string): Promise<TrackedItem> {
  const res = await docsClient.get<ApiResponse<TrackedItem>>(
    `/documents/api/v1/documents/tracking/${trackingCode}`
  )
  return res.data.data!
}
