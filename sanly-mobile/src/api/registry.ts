import { registryClient } from './client'
import type { ApiResponse, Citizen, LoginResponse, PageResponse, SessionInfo } from '../types'

export async function login(username: string, password: string, language = 'EN'): Promise<LoginResponse> {
  const res = await registryClient.post<ApiResponse<LoginResponse>>(
    '/api/v1/auth/login', { username, password, language }
  )
  return res.data.data
}

export async function verifyOtp(sessionToken: string, otpCode: string): Promise<LoginResponse> {
  const res = await registryClient.post<ApiResponse<LoginResponse>>(
    '/api/v1/auth/verify-otp', { sessionToken, otpCode }
  )
  return res.data.data
}

export async function resendOtp(sessionToken: string, language = 'EN'): Promise<void> {
  await registryClient.post('/api/v1/auth/resend-otp', { sessionToken, language })
}

export async function getCitizen(nationalId: string): Promise<Citizen> {
  const res = await registryClient.get<ApiResponse<Citizen>>(`/api/v1/citizens/${nationalId}`)
  return res.data.data
}

export async function listSessions(currentSessionId?: string): Promise<SessionInfo[]> {
  const params = currentSessionId ? { currentSessionId } : {}
  const res = await registryClient.get<ApiResponse<SessionInfo[]>>('/api/v1/auth/sessions', { params })
  return res.data.data ?? []
}

export async function revokeSession(sessionId: string): Promise<void> {
  await registryClient.delete(`/api/v1/auth/sessions/${sessionId}`)
}

export async function revokeOtherSessions(currentSessionId: string): Promise<void> {
  await registryClient.delete('/api/v1/auth/sessions/other', { params: { currentSessionId } })
}
