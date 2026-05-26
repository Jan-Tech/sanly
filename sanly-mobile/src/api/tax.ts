import { taxClient } from './client'
import type { ApiResponse, TaxpayerRecord, TaxFiling } from '../types'

export async function getTaxpayer(nationalId: string): Promise<TaxpayerRecord | null> {
  try {
    const res = await taxClient.get<ApiResponse<TaxpayerRecord>>(
      `/api/v1/taxpayers/nin/${nationalId}`
    )
    return res.data.data
  } catch {
    return null
  }
}

export async function getFilings(taxId: string): Promise<TaxFiling[]> {
  const res = await taxClient.get<ApiResponse<TaxFiling[]>>(
    `/api/v1/tax-filings/taxpayer/${taxId}`
  )
  return res.data.data ?? []
}
