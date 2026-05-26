import { landClient } from './client'
import type { Property, TransferApplication } from '../types'

export async function verifyProperty(cadastralCode: string): Promise<Property> {
  const res = await landClient.get(`/api/v1/land/properties/verify/${cadastralCode}`)
  return res.data
}

export async function getPropertiesByOwner(nationalId: string): Promise<Property[]> {
  const res = await landClient.get(`/api/v1/land/properties/owner/${nationalId}`)
  return res.data?.content ?? res.data ?? []
}

export async function getPendingTransfers(): Promise<TransferApplication[]> {
  const res = await landClient.get('/api/v1/land/transfer-applications', { params: { status: 'PENDING', size: 50 } })
  return res.data?.content ?? res.data ?? []
}

export async function getTransferById(applicationId: string): Promise<TransferApplication> {
  const res = await landClient.get(`/api/v1/land/transfer-applications/${applicationId}`)
  return res.data
}

export async function approveTransfer(applicationId: string, notes?: string): Promise<TransferApplication> {
  const res = await landClient.patch(`/api/v1/land/transfer-applications/${applicationId}/approve`, { notes })
  return res.data
}

export async function rejectTransfer(applicationId: string, reason: string): Promise<TransferApplication> {
  const res = await landClient.patch(`/api/v1/land/transfer-applications/${applicationId}/reject`, { reason })
  return res.data
}
