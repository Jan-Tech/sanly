import { bridgeClient } from './client'
import type {
  AnomalyAlert, AnomalyAlertSummary, ApiResponse, ExchangeLog,
  Institution, InstitutionPermission, PageResponse
} from '../types'

// ── Institutions ──────────────────────────────────────────────────────────────

export async function getInstitutions(): Promise<Institution[]> {
  const res = await bridgeClient.get<ApiResponse<Institution[]>>('/api/v1/institutions')
  return res.data.data
}

export async function registerInstitution(data: {
  institutionCode: string; name: string; description?: string; publishableTypes?: string[]
}): Promise<{ institution: Institution; rawApiKey: string }> {
  const res = await bridgeClient.post<ApiResponse<{ institution: Institution; rawApiKey: string }>>(
    '/api/v1/institutions', data
  )
  return res.data.data
}

export async function updateInstitutionStatus(code: string, status: string): Promise<Institution> {
  const res = await bridgeClient.patch<ApiResponse<Institution>>(
    `/api/v1/institutions/${code}/status`, { status }
  )
  return res.data.data
}

export async function rotateInstitutionKey(code: string): Promise<{ rawApiKey: string }> {
  const res = await bridgeClient.post<ApiResponse<{ rawApiKey: string }>>(
    `/api/v1/institutions/${code}/rotate-key`
  )
  return res.data.data
}

// ── Permissions ───────────────────────────────────────────────────────────────

export async function getPermissions(): Promise<InstitutionPermission[]> {
  const res = await bridgeClient.get<ApiResponse<InstitutionPermission[]>>('/api/v1/permissions')
  return res.data.data
}

export async function grantPermission(data: {
  requestingCode: string; targetCode: string; dataType: string
}): Promise<InstitutionPermission> {
  const res = await bridgeClient.post<ApiResponse<InstitutionPermission>>('/api/v1/permissions', data)
  return res.data.data
}

export async function revokePermission(id: number): Promise<void> {
  await bridgeClient.delete(`/api/v1/permissions/${id}`)
}

// ── Audit Log ─────────────────────────────────────────────────────────────────

export async function getExchangeLogs(params: {
  page?: number; size?: number; institutionCode?: string; nationalId?: string
  dataType?: string; result?: string; purposeCode?: string; from?: string; to?: string
}): Promise<PageResponse<ExchangeLog>> {
  const res = await bridgeClient.get<ApiResponse<PageResponse<ExchangeLog>>>('/api/v1/audit/exchanges', { params: {
    page: params.page ?? 0,
    size: params.size ?? 50,
    ...params,
  }})
  return res.data.data
}

export async function getExchangeLogsByNationalId(
  nationalId: string, page = 0, size = 50
): Promise<PageResponse<ExchangeLog>> {
  const res = await bridgeClient.get<ApiResponse<PageResponse<ExchangeLog>>>(
    `/api/v1/audit/exchanges/${nationalId}`, { params: { page, size } }
  )
  return res.data.data
}

// ── Anomalies ─────────────────────────────────────────────────────────────────

export async function getAnomalySummary(): Promise<AnomalyAlertSummary> {
  const res = await bridgeClient.get<ApiResponse<AnomalyAlertSummary>>('/api/v1/anomalies/summary')
  return res.data.data
}

export async function getAnomalies(params: {
  page?: number; size?: number; status?: string; severity?: string; institutionCode?: string
}): Promise<PageResponse<AnomalyAlert>> {
  const res = await bridgeClient.get<ApiResponse<PageResponse<AnomalyAlert>>>('/api/v1/anomalies', { params: {
    page: params.page ?? 0, size: params.size ?? 50, ...params
  }})
  return res.data.data
}

export async function reviewAnomaly(alertId: string, data: {
  status: string; reviewedBy: string
}): Promise<AnomalyAlert> {
  const res = await bridgeClient.patch<ApiResponse<AnomalyAlert>>(
    `/api/v1/anomalies/${alertId}/review`, data
  )
  return res.data.data
}
