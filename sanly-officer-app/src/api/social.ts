import { socialClient } from './client'
import type { BenefitClaim, UnemploymentRecord, BenefitType } from '../types'

export async function getActiveClaimsByNin(nationalId: string): Promise<BenefitClaim[]> {
  const res = await socialClient.get('/api/v1/social/claims', { params: { nationalId, status: 'ACTIVE', size: 20 } })
  return res.data?.content ?? res.data ?? []
}

export async function getAllClaimsByNin(nationalId: string): Promise<BenefitClaim[]> {
  const res = await socialClient.get('/api/v1/social/claims', { params: { nationalId, size: 20 } })
  return res.data?.content ?? res.data ?? []
}

export async function getPendingClaims(): Promise<BenefitClaim[]> {
  const res = await socialClient.get('/api/v1/social/claims', { params: { status: 'PENDING', size: 50 } })
  return res.data?.content ?? res.data ?? []
}

export async function processClaim(
  claimId: string,
  status: 'ACTIVE' | 'REJECTED',
  notes?: string
): Promise<BenefitClaim> {
  const res = await socialClient.patch(`/api/v1/social/claims/${claimId}/process`, { status, notes })
  return res.data
}

export async function registerUnemployment(data: {
  nationalId: string
  lastEmployer?: string
  reason?: string
}): Promise<UnemploymentRecord> {
  const res = await socialClient.post('/api/v1/social/unemployment', data)
  return res.data
}

export async function getUnemploymentByNin(nationalId: string): Promise<UnemploymentRecord | null> {
  try {
    const res = await socialClient.get(`/api/v1/social/unemployment/citizen/${nationalId}`)
    return res.data
  } catch {
    return null
  }
}
