import { taxClient } from './client'
import type { ApiResponse, PageResponse, TaxFiling, TaxpayerRecord } from '../types'

export async function getTaxpayer(nationalId: string): Promise<TaxpayerRecord> {
  const res = await taxClient.get<ApiResponse<TaxpayerRecord>>(`/api/v1/taxpayers/${nationalId}`)
  return res.data.data
}

export async function getTaxFilings(taxId: string): Promise<TaxFiling[]> {
  const res = await taxClient.get<ApiResponse<PageResponse<TaxFiling>>>(
    `/api/v1/filings`,
    { params: { taxId, size: 50, sort: 'taxYear,desc' } }
  )
  return res.data.data?.content ?? []
}
