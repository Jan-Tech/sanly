import { registryClient } from './client'
import type { Citizen } from '../types'

export async function loginOfficer(username: string, password: string) {
  const res = await registryClient.post('/api/v1/auth/login', { username, password })
  return res.data as { token: string; type: string; nationalId: string; roles: string[] }
}

export async function getCitizen(nationalId: string): Promise<Citizen> {
  const res = await registryClient.get(`/api/v1/citizens/${nationalId}`)
  return res.data
}

export async function searchCitizens(query: string): Promise<Citizen[]> {
  const res = await registryClient.get('/api/v1/citizens/search', { params: { q: query, size: 20 } })
  return res.data?.content ?? res.data ?? []
}

export async function verifyCitizen(nationalId: string): Promise<{ exists: boolean; status: string }> {
  const res = await registryClient.get(`/api/v1/citizens/${nationalId}/verify`)
  return res.data
}

export async function getBridgeAccessLogs(nationalId: string) {
  const res = await registryClient.get('/api/v1/audit-logs', { params: { subjectNationalId: nationalId, size: 20 } })
  return res.data?.content ?? res.data ?? []
}
