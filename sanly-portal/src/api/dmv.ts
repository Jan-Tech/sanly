import { dmvClient } from './client'
import type { ApiResponse, DrivingLicense, PageResponse } from '../types'

export async function getLicenses(ownerNationalId: string): Promise<DrivingLicense[]> {
  const res = await dmvClient.get<ApiResponse<PageResponse<DrivingLicense>>>(
    `/api/v1/licenses`,
    { params: { ownerNationalId, size: 50 } }
  )
  return res.data.data?.content ?? []
}
