import { landClient } from './client'

export interface PropertyOwnership {
  ownershipId: string
  propertyCode: string
  ownerNationalId: string
  sharePercent: number
  ownershipType: string
  acquiredVia: string
  acquiredAt: string
  status: string
}

export interface Property {
  propertyId: string
  cadastralCode: string
  propertyType: string
  area: number
  address: string
  description: string | null
  status: string
  registeredAt: string
}

export async function getPropertiesByOwner(nationalId: string): Promise<Property[]> {
  const res = await landClient.get<Property[]>(`/api/v1/land/properties/owner/${nationalId}`)
  return res.data ?? []
}
