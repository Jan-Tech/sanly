import { pensionClient } from './client'

export interface PensionAccount {
  accountId: string
  accountCode: string
  citizenNationalId: string
  openedAt: string
  eligibleAt: string
  totalContributions: string
  totalEmployerContributions: string
  totalCitizenContributions: string
  retirementAgeTarget: number
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
  contributionMonth: string
  totalAmount: string
  status: string
  submittedAt: string
}

export async function getPensionAccount(nationalId: string): Promise<PensionAccount | null> {
  try {
    const res = await pensionClient.get<PensionAccount>(`/api/v1/pension/accounts/citizen/${nationalId}`)
    return res.data
  } catch {
    return null
  }
}

export async function getPensionEligibility(nationalId: string): Promise<PensionEligibility | null> {
  try {
    const res = await pensionClient.get<PensionEligibility>(`/api/v1/pension/accounts/citizen/${nationalId}/eligibility`)
    return res.data
  } catch {
    return null
  }
}

export async function getPensionContributions(accountCode: string): Promise<PensionContribution[]> {
  const res = await pensionClient.get<PensionContribution[]>(`/api/v1/pension/contributions/account/${accountCode}`)
  return res.data ?? []
}
