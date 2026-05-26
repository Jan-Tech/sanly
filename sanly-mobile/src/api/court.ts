import { courtClient } from './client'

export interface CourtFine {
  fineId: string
  fineCode: string
  citizenNationalId: string
  fineType: string
  amount: string
  status: string
  issuedAt: string
  dueDate: string | null
  paidAt: string | null
  description: string | null
}

export interface CourtCase {
  caseId: string
  caseNumber: string
  citizenNationalId: string
  caseType: string
  status: string
  filedAt: string
  hearingDate: string | null
  verdictType: string | null
  summary: string | null
}

export async function getFinesByCitizen(nationalId: string): Promise<CourtFine[]> {
  const res = await courtClient.get<CourtFine[]>(`/api/v1/court/fines/citizen/${nationalId}`)
  return res.data ?? []
}

export async function getCasesByCitizen(nationalId: string): Promise<CourtCase[]> {
  const res = await courtClient.get<CourtCase[]>(`/api/v1/court/cases/citizen/${nationalId}`)
  return res.data ?? []
}
