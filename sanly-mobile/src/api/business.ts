import { businessClient } from './client'
import type { ApiResponse, Business } from '../types'

export async function getBusinesses(nationalId: string): Promise<Business[]> {
  const res = await businessClient.get<ApiResponse<Business[]>>(
    '/api/v1/businesses/owner', { params: { nationalId } }
  )
  return res.data.data ?? []
}
