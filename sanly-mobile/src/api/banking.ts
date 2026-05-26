import { bankingClient } from './client'
import type { ApiResponse } from '../types'

export interface ConsentRequest {
  consentId: string
  bankCode: string
  bankName: string
  purpose: string
  requestedScopes: string[]
  expiresAt: string
  status: string
  createdAt: string
}

export interface ConsentHistory {
  consentId: string
  bankCode: string
  bankName: string
  purpose: string
  approvedScopes: string[] | null
  status: string
  createdAt: string
  resolvedAt: string | null
}

export async function getPendingConsents(): Promise<ConsentRequest[]> {
  const res = await bankingClient.get<ApiResponse<ConsentRequest[]>>('/api/v1/banking/consent/my/pending')
  return res.data.data ?? []
}

export async function getConsentHistory(): Promise<ConsentHistory[]> {
  const res = await bankingClient.get<ApiResponse<ConsentHistory[]>>('/api/v1/banking/consent/my/history')
  return res.data.data ?? []
}
