import axios from 'axios'

const socialClient = axios.create({ baseURL: '/proxy/social', timeout: 10_000 })
socialClient.interceptors.request.use((config) => {
  const token = import.meta.env.VITE_SOCIAL_TOKEN || null
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export interface AdminBenefitProgram {
  programId: string
  programCode: string
  name: string
  description: string | null
  benefitType: string
  monthlyAmount: string
  eligibilityCriteria: string | null
  maxDurationMonths: number | null
  status: 'ACTIVE' | 'SUSPENDED' | 'DISCONTINUED'
  createdAt: string
}

export interface AdminBenefitClaim {
  claimId: string
  claimCode: string
  citizenNationalId: string
  programCode: string
  claimType: string
  status: string
  appliedAt: string
  approvedAt: string | null
  expiresAt: string | null
  rejectionReason: string | null
  triggerReason: string | null
  createdAt: string
}

export interface AdminPayment {
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

export interface AdminUnemployment {
  recordId: string
  citizenNationalId: string
  registeredAt: string
  lastEmployer: string | null
  lastEmploymentDate: string | null
  reason: string
  status: 'REGISTERED' | 'EMPLOYED' | 'EXPIRED'
  updatedAt: string
}

export interface AdminPension {
  accountId: string
  citizenNationalId: string
  contributionStartDate: string
  totalContributions: string | null
  eligibleAt: string
  status: 'ACCUMULATING' | 'ELIGIBLE' | 'PAYING' | 'CLOSED'
  createdAt: string
}

export async function getPrograms(): Promise<AdminBenefitProgram[]> {
  const res = await socialClient.get<AdminBenefitProgram[]>('/api/v1/social/programs')
  return res.data ?? []
}

export async function createProgram(data: {
  name: string; description: string; benefitType: string;
  monthlyAmount: string; eligibilityCriteria: string; maxDurationMonths: number | null
}): Promise<AdminBenefitProgram> {
  const res = await socialClient.post<AdminBenefitProgram>('/api/v1/social/programs', data)
  return res.data
}

export async function updateProgramStatus(programCode: string, status: string): Promise<AdminBenefitProgram> {
  const res = await socialClient.patch<AdminBenefitProgram>(`/api/v1/social/programs/${programCode}/status`, { status })
  return res.data
}

export async function getPendingClaims(): Promise<AdminBenefitClaim[]> {
  const res = await socialClient.get<AdminBenefitClaim[]>('/api/v1/social/claims/pending')
  return res.data ?? []
}

export async function getClaimsByCitizen(nationalId: string): Promise<AdminBenefitClaim[]> {
  const res = await socialClient.get<AdminBenefitClaim[]>(`/api/v1/social/claims/citizen/${nationalId}`)
  return res.data ?? []
}

export async function approveClaim(claimCode: string): Promise<AdminBenefitClaim> {
  const res = await socialClient.post<AdminBenefitClaim>(`/api/v1/social/claims/${claimCode}/approve`, {})
  return res.data
}

export async function rejectClaim(claimCode: string, rejectionReason: string): Promise<AdminBenefitClaim> {
  const res = await socialClient.post<AdminBenefitClaim>(`/api/v1/social/claims/${claimCode}/reject`, { rejectionReason })
  return res.data
}

export async function getScheduledPayments(): Promise<AdminPayment[]> {
  const res = await socialClient.get<AdminPayment[]>('/api/v1/social/payments/scheduled')
  return res.data ?? []
}

export async function markPaymentPaid(paymentId: string): Promise<AdminPayment> {
  const res = await socialClient.patch<AdminPayment>(`/api/v1/social/payments/${paymentId}/mark-paid`)
  return res.data
}

export async function getPensionByNin(nationalId: string): Promise<AdminPension> {
  const res = await socialClient.get<AdminPension>(`/api/v1/social/pensions/${nationalId}`)
  return res.data
}

export async function getUnemploymentByNin(nationalId: string): Promise<AdminUnemployment> {
  const res = await socialClient.get<AdminUnemployment>(`/api/v1/social/unemployment/${nationalId}`)
  return res.data
}
