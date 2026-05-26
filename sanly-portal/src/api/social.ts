import axios from 'axios'

const socialClient = axios.create({ baseURL: '/proxy/social', timeout: 10_000 })
socialClient.interceptors.request.use((config) => {
  const token = import.meta.env.VITE_SOCIAL_TOKEN || null
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export interface BenefitClaim {
  claimId: string
  claimCode: string
  citizenNationalId: string
  programCode: string
  claimType: 'AUTO_TRIGGERED' | 'CITIZEN_APPLIED' | 'OFFICER_INITIATED'
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'ACTIVE' | 'SUSPENDED' | 'EXPIRED' | 'CANCELLED'
  appliedAt: string
  approvedAt: string | null
  expiresAt: string | null
  rejectionReason: string | null
  triggerReason: string | null
  createdAt: string
}

export interface BenefitPayment {
  paymentId: string
  claimCode: string
  citizenNationalId: string
  paymentPeriod: string
  amount: string
  status: 'SCHEDULED' | 'PAID' | 'FAILED' | 'CANCELLED'
  scheduledDate: string
  paidAt: string | null
  createdAt: string
}

export interface UnemploymentRecord {
  recordId: string
  citizenNationalId: string
  registeredAt: string
  lastEmployer: string | null
  lastEmploymentDate: string | null
  reason: string
  status: 'REGISTERED' | 'EMPLOYED' | 'EXPIRED'
  updatedAt: string
}

export interface PensionAccount {
  accountId: string
  citizenNationalId: string
  contributionStartDate: string
  totalContributions: string | null
  eligibleAt: string
  status: 'ACCUMULATING' | 'ELIGIBLE' | 'PAYING' | 'CLOSED'
  createdAt: string
}

export interface PensionEligibility {
  eligible: boolean
  eligibleAt: string
  yearsRemaining: number
  status: string
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

export async function getPensionAccount(nationalId: string): Promise<PensionAccount | null> {
  try {
    const res = await socialClient.get<PensionAccount>(`/api/v1/social/pensions/${nationalId}`)
    return res.data
  } catch {
    return null
  }
}

export async function getPensionEligibility(nationalId: string): Promise<PensionEligibility | null> {
  try {
    const res = await socialClient.get<PensionEligibility>(`/api/v1/social/pensions/${nationalId}/eligibility`)
    return res.data
  } catch {
    return null
  }
}
