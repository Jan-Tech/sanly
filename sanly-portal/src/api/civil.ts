import { civilClient } from './client'
import type { ApiResponse, BirthRecord, MarriageRecord, PageResponse } from '../types'

export async function getBirthRecord(nationalId: string): Promise<BirthRecord> {
  const res = await civilClient.get<ApiResponse<BirthRecord>>(
    `/api/v1/birth-records/by-national-id/${nationalId}`
  )
  return res.data.data
}

export async function getMarriageRecords(nationalId: string): Promise<MarriageRecord[]> {
  const res = await civilClient.get<ApiResponse<PageResponse<MarriageRecord>>>(
    `/api/v1/marriage-records`,
    { params: { spouseNationalId: nationalId, size: 10 } }
  )
  return res.data.data?.content ?? []
}
