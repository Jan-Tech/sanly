import { taxClient } from './client'
import type { TaxpayerRecord, TaxFiling } from '../types'

export async function getTaxpayerByNin(nationalId: string): Promise<TaxpayerRecord> {
  const res = await taxClient.get(`/api/v1/taxpayers/nin/${nationalId}`)
  return res.data
}

export async function getFilingsByTaxId(taxId: string): Promise<TaxFiling[]> {
  const res = await taxClient.get(`/api/v1/tax-filings/taxpayer/${taxId}`)
  return res.data?.content ?? res.data ?? []
}

export async function getFilingById(filingId: string): Promise<TaxFiling> {
  const res = await taxClient.get(`/api/v1/tax-filings/${filingId}`)
  return res.data
}

export async function processFiling(
  filingId: string,
  status: 'ACCEPTED' | 'REJECTED',
  notes?: string
): Promise<TaxFiling> {
  const res = await taxClient.patch(`/api/v1/tax-filings/${filingId}/process`, { status, notes })
  return res.data
}

export async function getPendingFilings(): Promise<TaxFiling[]> {
  const res = await taxClient.get('/api/v1/tax-filings', { params: { status: 'SUBMITTED', size: 50 } })
  return res.data?.content ?? res.data ?? []
}
