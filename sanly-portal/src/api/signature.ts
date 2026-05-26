import axios from 'axios'
import type { ApiResponse, PageResponse } from '../types'

const signatureClient = axios.create({
  baseURL: import.meta.env.VITE_GATEWAY_URL ?? 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
})

signatureClient.interceptors.request.use((config) => {
  const raw = localStorage.getItem('sanly-auth')
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

export interface VerifyResponse {
  valid: boolean
  signatureCode: string
  signerName?: string
  signerNationalId?: string
  signedAt?: string
  purpose?: string
  documentHash?: string
  status?: string
  documentResubmitted?: boolean
  message?: string
}

export async function generateActionOtp(language = 'EN'): Promise<{ sent: boolean; phoneMasked?: string }> {
  const res = await signatureClient.post('/registry/api/v1/auth/generate-otp-for-action', null, {
    params: { language },
  })
  return res.data.data
}

export async function getMySignatures(page = 0, size = 20): Promise<PageResponse<SignatureRecord>> {
  const res = await signatureClient.get<ApiResponse<PageResponse<SignatureRecord>>>(
    '/signature/api/v1/signature/my', { params: { page, size } }
  )
  return res.data.data
}

export async function signDocument(
  file: File,
  purpose: string,
  otpCode: string
): Promise<SignatureRecord> {
  const form = new FormData()
  form.append('file', file)
  form.append('purpose', purpose)
  form.append('otpCode', otpCode)
  const res = await signatureClient.post<ApiResponse<SignatureRecord>>(
    '/signature/api/v1/signature/sign', form,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  )
  return res.data.data
}

export async function revokeSignature(signatureCode: string, reason: string): Promise<SignatureRecord> {
  const res = await signatureClient.patch<ApiResponse<SignatureRecord>>(
    `/signature/api/v1/signature/${signatureCode}/revoke`, { reason }
  )
  return res.data.data
}

export async function getQrCode(signatureCode: string): Promise<string> {
  const res = await signatureClient.get(`/signature/api/v1/signature/${signatureCode}/qr`, {
    responseType: 'blob',
  })
  return URL.createObjectURL(res.data)
}

export async function verifyDocument(file: File, signatureCode: string): Promise<VerifyResponse> {
  const form = new FormData()
  form.append('file', file)
  form.append('signatureCode', signatureCode)
  const res = await signatureClient.post<ApiResponse<VerifyResponse>>(
    '/signature/api/v1/signature/verify', form,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  )
  return res.data.data
}

export async function verifyByCode(signatureCode: string): Promise<VerifyResponse> {
  const res = await signatureClient.get<ApiResponse<VerifyResponse>>(
    `/signature/api/v1/signature/verify/${signatureCode}`
  )
  return res.data.data
}
