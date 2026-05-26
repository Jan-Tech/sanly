import { courtClient } from './client'
import type { CourtFine, CourtCase } from '../types'

export async function getFineByCode(fineCode: string): Promise<CourtFine> {
  const res = await courtClient.get(`/api/v1/fines/code/${fineCode}`)
  return res.data
}

export async function getFinesByNin(nationalId: string): Promise<CourtFine[]> {
  const res = await courtClient.get('/api/v1/fines', { params: { nationalId, size: 20 } })
  return res.data?.content ?? res.data ?? []
}

export async function confirmFinePayment(fineId: string, paymentRef?: string): Promise<CourtFine> {
  const res = await courtClient.patch(`/api/v1/fines/${fineId}/pay`, { paymentRef })
  return res.data
}

export async function getCaseByNumber(caseNumber: string): Promise<CourtCase> {
  const res = await courtClient.get(`/api/v1/cases/number/${caseNumber}`)
  return res.data
}

export async function getCasesByNin(nationalId: string): Promise<CourtCase[]> {
  const res = await courtClient.get('/api/v1/cases', { params: { nationalId, size: 20 } })
  return res.data?.content ?? res.data ?? []
}

export async function scheduleHearing(caseId: string, hearingDate: string, notes?: string): Promise<CourtCase> {
  const res = await courtClient.patch(`/api/v1/cases/${caseId}/schedule`, { hearingDate, notes })
  return res.data
}
