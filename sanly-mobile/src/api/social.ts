import { socialClient } from './client'

export interface BenefitClaim {
  claimId: string
  claimCode: string
  citizenNationalId: string
  programCode: string
  claimType: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'ACTIVE' | 'SUSPENDED' | 'EXPIRED' | 'CANCELLED'
  appliedAt: string
  approvedAt: string | null
  expiresAt: string | null
  rejectionReason: string | null
}

export interface BenefitPayment {
  paymentId: string
  claimCode: string
  paymentPeriod: string
  amount: string
  status: 'SCHEDULED' | 'PAID' | 'FAILED' | 'CANCELLED'
  scheduledDate: string
  paidAt: string | null
}

export interface UnemploymentRecord {
  recordId: string
  citizenNationalId: string
  registeredAt: string
  reason: string
  status: 'REGISTERED' | 'EMPLOYED' | 'EXPIRED'
}

export async function getActiveClaims(nationalId: string): Promise<BenefitClaim[]> {
  const res = await socialClient.get<BenefitClaim[]>(`/api/v1/social/claims/citizen/${nationalId}`)
  return (res.data ?? []).filter(c => ['ACTIVE', 'PENDING'].includes(c.status))
}

export async function getAllClaims(nationalId: string): Promise<BenefitClaim[]> {
  const res = await socialClient.get<BenefitClaim[]>(`/api/v1/social/claims/citizen/${nationalId}`)
  return res.data ?? []
}

export async function getPayments(nationalId: string): Promise<BenefitPayment[]> {
  const res = await socialClient.get<BenefitPayment[]>(`/api/v1/social/payments/citizen/${nationalId}`)
  return res.data ?? []
}

export async function getUnemploymentStatus(nationalId: string): Promise<UnemploymentRecord | null> {
  try {
    const res = await socialClient.get<UnemploymentRecord>(`/api/v1/social/unemployment/${nationalId}`)
    return res.data
  } catch {
    return null
  }
}
