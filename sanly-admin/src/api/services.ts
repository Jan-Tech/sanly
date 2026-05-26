import { medicalClient, dmvClient, taxClient, businessClient, civilClient } from './client'
import type { ApiResponse, BirthRecord, Business, DrivingLicense, MarriageRecord, MedicalRecord, PageResponse, TaxpayerRecord } from '../types'

export async function getMedicalRecords(nationalId: string): Promise<MedicalRecord[]> {
  const res = await medicalClient.get<ApiResponse<PageResponse<MedicalRecord>>>(
    '/api/v1/medical-records', { params: { nationalId, size: 50 } }
  )
  return res.data.data?.content ?? []
}

export async function getLicenses(ownerNationalId: string): Promise<DrivingLicense[]> {
  const res = await dmvClient.get<ApiResponse<PageResponse<DrivingLicense>>>(
    '/api/v1/licenses', { params: { ownerNationalId, size: 20 } }
  )
  return res.data.data?.content ?? []
}

export async function getTaxpayer(nationalId: string): Promise<TaxpayerRecord | null> {
  try {
    const res = await taxClient.get<ApiResponse<TaxpayerRecord>>(`/api/v1/taxpayers/${nationalId}`)
    return res.data.data
  } catch {
    return null
  }
}

export async function getBusinesses(ownerNationalId: string): Promise<Business[]> {
  const res = await businessClient.get<ApiResponse<PageResponse<Business>>>(
    '/api/v1/businesses', { params: { ownerNationalId, size: 20 } }
  )
  return res.data.data?.content ?? []
}

export async function getBirthRecord(nationalId: string): Promise<BirthRecord | null> {
  try {
    const res = await civilClient.get<ApiResponse<BirthRecord>>(
      `/api/v1/birth-records/by-national-id/${nationalId}`
    )
    return res.data.data
  } catch {
    return null
  }
}

export async function getMarriageRecords(nationalId: string): Promise<MarriageRecord[]> {
  try {
    const res = await civilClient.get<ApiResponse<PageResponse<MarriageRecord>>>(
      '/api/v1/marriage-records', { params: { spouseNationalId: nationalId, size: 10 } }
    )
    return res.data.data?.content ?? []
  } catch {
    return []
  }
}
