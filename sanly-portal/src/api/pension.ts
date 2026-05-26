import axios from 'axios'

const pensionClient = axios.create({ baseURL: '/proxy/pension', timeout: 10_000 })
pensionClient.interceptors.request.use((config) => {
  const token = import.meta.env.VITE_PENSION_TOKEN || null
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export interface PensionAccount {
  accountId: string
  accountCode: string
  citizenNationalId: string
  openedAt: string
  employmentStartDate: string
  birthDate: string
  gender: string
  retirementAgeTarget: number
  eligibleAt: string
  totalContributions: string
  totalEmployerContributions: string
  totalCitizenContributions: string
  status: string
}

export interface PensionEligibility {
  eligible: boolean
  eligibleAt: string
  yearsRemaining: number
  monthsContributed: number
  projectedMonthlyAmount: string
}

export interface PensionContribution {
  contributionId: string
  accountCode: string
  employerCode: string | null
  contributionMonth: string
  employerAmount: string | null
  citizenAmount: string | null
  totalAmount: string
  submittedAt: string
  status: string
}

export interface PensionPayment {
  paymentId: string
  accountCode: string
  paymentMonth: string
  amount: string
  status: string
  scheduledDate: string
  paidAt: string | null
}

export async function getPensionAccount(nationalId: string): Promise<PensionAccount> {
  const res = await pensionClient.get<PensionAccount>(`/api/v1/pension/accounts/citizen/${nationalId}`)
  return res.data
}

export async function getPensionEligibility(nationalId: string): Promise<PensionEligibility> {
  const res = await pensionClient.get<PensionEligibility>(`/api/v1/pension/accounts/citizen/${nationalId}/eligibility`)
  return res.data
}

export async function getPensionContributions(accountCode: string): Promise<PensionContribution[]> {
  const res = await pensionClient.get<PensionContribution[]>(`/api/v1/pension/contributions/account/${accountCode}`)
  return res.data ?? []
}

export async function getPensionPayments(accountCode: string): Promise<PensionPayment[]> {
  const res = await pensionClient.get<PensionPayment[]>(`/api/v1/pension/payments/account/${accountCode}`)
  return res.data ?? []
}
