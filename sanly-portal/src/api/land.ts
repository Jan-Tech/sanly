import axios from 'axios'

const landClient = axios.create({ baseURL: '/proxy/land', timeout: 10_000 })
landClient.interceptors.request.use((config) => {
  const token = import.meta.env.VITE_LAND_TOKEN || null
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export interface PropertyDetail {
  property: {
    propertyId: string
    cadastralNumber: string
    propertyType: string
    address: string
    region: string
    area: number | null
    description: string | null
    status: 'REGISTERED' | 'UNDER_TRANSFER' | 'DISPUTED' | 'DEREGISTERED'
    registeredAt: string
  }
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

export interface TransferApplication {
  applicationId: string
  cadastralNumber: string
  fromNationalId: string
  toNationalId: string
  transferType: 'SALE' | 'GIFT' | 'INHERITANCE' | 'COURT_ORDER'
  agreedPrice: string | null
  applicationDate: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'
  processedAt: string | null
  rejectionReason: string | null
  createdAt: string
}

export async function getPropertiesByOwner(nationalId: string): Promise<PropertyDetail[]> {
  const res = await landClient.get<PropertyDetail[]>(
    `/api/v1/land/properties/owner/${nationalId}`
  )
  return res.data ?? []
}

export async function getTransfersByCitizen(nationalId: string): Promise<TransferApplication[]> {
  const res = await landClient.get<TransferApplication[]>(
    `/api/v1/land/transfers/citizen/${nationalId}`
  )
  return res.data ?? []
}
