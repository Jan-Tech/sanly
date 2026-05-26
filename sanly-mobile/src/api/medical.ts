import { medicalClient } from './client'
import type { ApiResponse, MedicalRecord, PageResponse } from '../types'

export async function getMedicalRecords(nationalId: string): Promise<MedicalRecord[]> {
  const res = await medicalClient.get<ApiResponse<PageResponse<MedicalRecord>>>(
    '/api/v1/medical-records', { params: { nationalId, size: 100 } }
  )
  return res.data.data?.content ?? []
}
