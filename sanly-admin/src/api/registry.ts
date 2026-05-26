import { registryClient } from './client'
import type { ApiResponse, AuthResponse, Citizen, PageResponse } from '../types'

export async function adminLogin(username: string, password: string): Promise<AuthResponse> {
  const res = await registryClient.post<AuthResponse>('/api/v1/auth/login', { username, password })
  return res.data
}

export async function getCitizens(params: {
  page?: number; size?: number; search?: string; status?: string
}): Promise<PageResponse<Citizen>> {
  const res = await registryClient.get<ApiResponse<PageResponse<Citizen>>>('/api/v1/citizens', { params: {
    page: params.page ?? 0, size: params.size ?? 20,
    ...(params.search && { search: params.search }),
    ...(params.status && { status: params.status }),
  }})
  return res.data.data
}

export async function getCitizen(nationalId: string): Promise<Citizen> {
  const res = await registryClient.get<ApiResponse<Citizen>>(`/api/v1/citizens/${nationalId}`)
  return res.data.data
}

export async function updateCitizenStatus(nationalId: string, status: string): Promise<Citizen> {
  const res = await registryClient.patch<ApiResponse<Citizen>>(
    `/api/v1/citizens/${nationalId}/status`, { status }
  )
  return res.data.data
}

export interface ActiveSession {
  sessionId: string
  nationalId: string
  createdAt: string
  lastSeenAt: string
  expiresAt: string
  status: string
  ipAddress: string
  userAgent: string
  currentSession: boolean
}

export async function adminGetSessions(nationalId: string): Promise<ActiveSession[]> {
  const res = await registryClient.get<ApiResponse<ActiveSession[]>>(
    `/api/v1/auth/admin/citizens/${nationalId}/sessions`
  )
  return res.data.data
}

export async function adminRevokeAllSessions(nationalId: string): Promise<void> {
  await registryClient.delete(`/api/v1/auth/admin/citizens/${nationalId}/sessions`)
}
