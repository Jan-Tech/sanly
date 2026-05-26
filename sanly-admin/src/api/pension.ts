import axios from 'axios'

const pensionClient = axios.create({ baseURL: '/proxy/pension', timeout: 10_000 })
pensionClient.interceptors.request.use((config) => {
  const token = import.meta.env.VITE_PENSION_TOKEN || null
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export interface AdminPensionAccount {
  accountId: string
  accountCode: string
  citizenNationalId: string
  openedAt: string
  eligibleAt: string
  totalContributions: string
  status: string
  retirementAgeTarget: number
}

export interface AdminEmployer {
  employerId: string
  employerCode: string
  businessName: string
  businessRegistrationNumber: string
  contactNationalId: string
  registeredAt: string
  status: string
}

export interface AdminContribution {
  contributionId: string
  accountCode: string
  employerCode: string | null
  contributionMonth: string
  totalAmount: string
  submittedAt: string
  status: string
  rejectionReason: string | null
}

export interface AdminRetirementApplication {
  applicationId: string
  accountCode: string
  citizenNationalId: string
  appliedAt: string
  requestedStartDate: string
  status: string
  monthlyPensionAmount: string | null
  rejectionReason: string | null
}

export interface AdminPayment {
  paymentId: string
  accountCode: string
  citizenNationalId: string
  paymentMonth: string
  amount: string
  status: string
  scheduledDate: string
  paidAt: string | null
}

export async function getAccountsByStatus(status: string): Promise<AdminPensionAccount[]> {
  const res = await pensionClient.get<AdminPensionAccount[]>(`/api/v1/pension/accounts?status=${status}`)
  return res.data ?? []
}

export async function getEmployers(): Promise<AdminEmployer[]> {
  const res = await pensionClient.get<AdminEmployer[]>('/api/v1/pension/employers')
  return res.data ?? []
}

export async function getRetirementApplications(status: string): Promise<AdminRetirementApplication[]> {
  const res = await pensionClient.get<AdminRetirementApplication[]>(`/api/v1/pension/retirement?status=${status}`)
  return res.data ?? []
}

export async function approveRetirement(applicationId: string): Promise<AdminRetirementApplication> {
  const res = await pensionClient.post<AdminRetirementApplication>(`/api/v1/pension/retirement/${applicationId}/approve`)
  return res.data
}

export async function rejectRetirement(applicationId: string, rejectionReason: string): Promise<AdminRetirementApplication> {
  const res = await pensionClient.post<AdminRetirementApplication>(
    `/api/v1/pension/retirement/${applicationId}/reject`,
    { rejectionReason }
  )
  return res.data
}

export async function getScheduledPayments(): Promise<AdminPayment[]> {
  const res = await pensionClient.get<AdminPayment[]>('/api/v1/pension/payments/scheduled')
  return res.data ?? []
}

export async function markPaymentPaid(paymentId: string): Promise<AdminPayment> {
  const res = await pensionClient.patch<AdminPayment>(`/api/v1/pension/payments/${paymentId}/mark-paid`)
  return res.data
}
