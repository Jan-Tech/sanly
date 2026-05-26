import { policeClient } from './client'
import type { CitizenCheck, CriminalRecord } from '../types'

export async function runCitizenCheck(
  nationalId: string,
  checkType: 'DRIVING_LICENSE' | 'TAX_STATUS' | 'FULL_CHECK',
  purposeCode = 'ROUTINE_CHECK',
  caseReference?: string
): Promise<CitizenCheck> {
  const res = await policeClient.post('/api/v1/citizen-checks', {
    nationalId,
    checkType,
    purposeCode,
    caseReference,
  })
  return res.data
}

export async function getTodayChecks(): Promise<CitizenCheck[]> {
  const today = new Date().toISOString().slice(0, 10)
  const res = await policeClient.get('/api/v1/citizen-checks', {
    params: { from: today, size: 50 },
  })
  return res.data?.content ?? res.data ?? []
}

export async function getCriminalRecords(nationalId: string): Promise<CriminalRecord[]> {
  const res = await policeClient.get('/api/v1/criminal-records', {
    params: { nationalId, size: 20 },
  })
  return res.data?.content ?? res.data ?? []
}

export async function createCriminalRecord(data: {
  nationalId: string
  offenseType: string
  offenseDate: string
  verdict: string
  sentenceDescription?: string
  courtName: string
  caseNumber: string
}): Promise<CriminalRecord> {
  const res = await policeClient.post('/api/v1/criminal-records', data)
  return res.data
}
