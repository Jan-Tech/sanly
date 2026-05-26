import axios from 'axios'

const landClient = axios.create({ baseURL: '/proxy/land', timeout: 10_000 })
landClient.interceptors.request.use((config) => {
  const token = import.meta.env.VITE_LAND_TOKEN || null
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export interface AdminProperty {
  propertyId: string
  cadastralNumber: string
  propertyType: string
  address: string
  region: string
  area: number | null
  status: 'REGISTERED' | 'UNDER_TRANSFER' | 'DISPUTED' | 'DEREGISTERED'
  registeredAt: string
}

export interface AdminPropertyDetail {
  property: AdminProperty
  owners: Array<{
    ownershipId: string
    ownerNationalId: string
    ownershipShare: number
    ownershipType: string
    acquiredAt: string
    acquiredVia: string
    status: string
  }>
}

export interface AdminTransfer {
  applicationId: string
  cadastralNumber: string
  fromNationalId: string
  toNationalId: string
  transferType: string
  agreedPrice: string | null
  applicationDate: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'
  processedAt: string | null
  rejectionReason: string | null
  createdAt: string
}

export interface AdminValuation {
  valuationId: string
  cadastralNumber: string
  valuationAmount: string
  valuationDate: string
  purpose: string
  createdAt: string
}

export async function getPendingTransfers(): Promise<AdminTransfer[]> {
  const res = await landClient.get<AdminTransfer[]>('/api/v1/land/transfers/pending')
  return res.data ?? []
}

export async function approveTransfer(applicationId: string): Promise<AdminTransfer> {
  const res = await landClient.post<AdminTransfer>(
    `/api/v1/land/transfers/${applicationId}/approve`
  )
  return res.data
}

export async function rejectTransfer(applicationId: string, reason: string): Promise<AdminTransfer> {
  const res = await landClient.post<AdminTransfer>(
    `/api/v1/land/transfers/${applicationId}/reject`,
    { reason }
  )
  return res.data
}

export async function getPropertyDetail(cadastralNumber: string): Promise<AdminPropertyDetail> {
  const res = await landClient.get<AdminPropertyDetail>(`/api/v1/land/properties/${cadastralNumber}`)
  return res.data
}

export async function getTransfersByProperty(cadastralNumber: string): Promise<AdminTransfer[]> {
  const res = await landClient.get<AdminTransfer[]>(
    `/api/v1/land/transfers/property/${cadastralNumber}`
  )
  return res.data ?? []
}

export async function getValuationsByProperty(cadastralNumber: string): Promise<AdminValuation[]> {
  const res = await landClient.get<AdminValuation[]>(
    `/api/v1/land/valuations/property/${cadastralNumber}`
  )
  return res.data ?? []
}
