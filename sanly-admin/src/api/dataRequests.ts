import axios from 'axios'
import type { ApiResponse, PageResponse } from '../types'

const client = axios.create({
  baseURL: import.meta.env.VITE_GATEWAY_URL ?? 'http://localhost:8080',
})

client.interceptors.request.use((config) => {
  const raw = localStorage.getItem('sanly-admin-auth')
  if (raw) {
    try {
      const { state } = JSON.parse(raw)
      if (state?.token) config.headers.Authorization = `Bearer ${state.token}`
    } catch { /* ignore */ }
  }
  return config
})

export interface DataRequest {
  requestId: string
  requestCode: string
  citizenNationalId: string
  requestType: string
  affectedService: string
  description: string
  status: string
  submittedAt: string
  reviewedAt?: string
  reviewedByOfficerId?: string
  reviewerNotes?: string
  resolutionDescription?: string
  hasExport?: boolean
}

export async function getAllDataRequests(params: {
  status?: string; requestType?: string; affectedService?: string; page?: number; size?: number
}): Promise<PageResponse<DataRequest>> {
  const res = await client.get<ApiResponse<PageResponse<DataRequest>>>('/registry/api/v1/data-requests', { params: {
    page: params.page ?? 0, size: params.size ?? 20,
    ...(params.status && { status: params.status }),
    ...(params.requestType && { requestType: params.requestType }),
    ...(params.affectedService && { affectedService: params.affectedService }),
  }})
  return res.data.data
}

export async function getDataRequestByCode(requestCode: string): Promise<DataRequest> {
  const res = await client.get<ApiResponse<DataRequest>>(`/registry/api/v1/data-requests/${requestCode}`)
  return res.data.data
}

export async function startReview(requestCode: string): Promise<DataRequest> {
  const res = await client.patch<ApiResponse<DataRequest>>(`/registry/api/v1/data-requests/${requestCode}/review`)
  return res.data.data
}

export async function approveRequest(requestCode: string, notes: string): Promise<DataRequest> {
  const res = await client.patch<ApiResponse<DataRequest>>(`/registry/api/v1/data-requests/${requestCode}/approve`, { notes })
  return res.data.data
}

export async function rejectRequest(requestCode: string, notes: string): Promise<DataRequest> {
  const res = await client.patch<ApiResponse<DataRequest>>(`/registry/api/v1/data-requests/${requestCode}/reject`, { notes })
  return res.data.data
}

export async function partialApproveRequest(requestCode: string, notes: string): Promise<DataRequest> {
  const res = await client.patch<ApiResponse<DataRequest>>(`/registry/api/v1/data-requests/${requestCode}/partial`, { notes })
  return res.data.data
}
