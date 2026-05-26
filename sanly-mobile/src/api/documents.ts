import { documentsClient } from './client'
import type { ApiResponse } from '../types'

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
}

export interface TrackedItem {
  trackingId: string
  trackingCode: string
  itemType: string
  sourceService: string
  currentStatus: string
  title: string
  isCompleted: boolean
  createdAt: string
  lastUpdatedAt: string
}

export async function getMyCertificates(): Promise<CertificateRecord[]> {
  const res = await documentsClient.get<ApiResponse<CertificateRecord[]>>(
    '/api/v1/documents/certificates/my'
  )
  return res.data.data ?? []
}

export async function getMyTrackedItems(): Promise<TrackedItem[]> {
  const res = await documentsClient.get<ApiResponse<TrackedItem[]>>(
    '/api/v1/documents/tracking/my'
  )
  return res.data.data ?? []
}
