import { businessClient } from './client'
import type { ApiResponse, Business, PageResponse } from '../types'

export async function getBusinesses(ownerNationalId: string): Promise<Business[]> {
  const res = await businessClient.get<ApiResponse<PageResponse<Business>>>(
    `/api/v1/businesses`,
    { params: { ownerNationalId, size: 50 } }
  )
  return res.data.data?.content ?? []
}
