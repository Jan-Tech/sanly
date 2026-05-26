import { medicalClient } from './client'
import type { MedicalRecord, TestType } from '../types'

export async function getMedicalRecords(nationalId: string): Promise<MedicalRecord[]> {
  const res = await medicalClient.get('/api/v1/medical-records', { params: { nationalId, size: 20 } })
  return res.data?.content ?? res.data ?? []
}

export async function submitTestResult(data: {
  nationalId: string
  testType: TestType
  result: string
  testDate: string
  notes?: string
}): Promise<MedicalRecord> {
  const res = await medicalClient.post('/api/v1/medical-records', data)
  return res.data
}

export async function getTodayRecords(): Promise<MedicalRecord[]> {
  const today = new Date().toISOString().slice(0, 10)
  const res = await medicalClient.get('/api/v1/medical-records', { params: { date: today, size: 50 } })
  return res.data?.content ?? res.data ?? []
}
