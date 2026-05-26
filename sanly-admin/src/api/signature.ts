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

export interface SignatureRecord {
  signatureId: string
  signatureCode: string
  signerNationalId: string
  documentHash: string
  documentName: string
  documentSizeBytes: number
  purpose: string
  signedAt: string
  status: 'VALID' | 'REVOKED'
  revokedAt?: string
  revokedReason?: string
}

export interface SignatureStats {
  signedToday: number
  verifiedToday: number
  revokedTotal: number
  totalSignatures: number
}

export async function adminGetAllSignatures(page = 0, size = 20): Promise<PageResponse<SignatureRecord>> {
  const res = await client.get<ApiResponse<PageResponse<SignatureRecord>>>(
    '/signature/api/v1/signature/admin/all', { params: { page, size } }
  )
  return res.data.data
}

export async function adminGetSignatureStats(): Promise<SignatureStats> {
  const res = await client.get<ApiResponse<SignatureStats>>('/signature/api/v1/signature/admin/stats')
  return res.data.data
}

export async function adminRevokeSignature(signatureCode: string, reason: string): Promise<SignatureRecord> {
  const res = await client.patch<ApiResponse<SignatureRecord>>(
    `/signature/api/v1/signature/${signatureCode}/revoke`, { reason }
  )
  return res.data.data
}
