import { dmvClient } from './client'
import type { ApiResponse, DrivingLicense } from '../types'

export async function getLicenses(nationalId: string): Promise<DrivingLicense[]> {
  const res = await dmvClient.get<ApiResponse<DrivingLicense[]>>(
    '/api/v1/licenses', { params: { nationalId } }
  )
  return res.data.data ?? []
}
