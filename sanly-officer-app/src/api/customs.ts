import { customsClient } from './client'
import type { CustomsDeclaration } from '../types'

export async function getDeclarationByCode(declarationCode: string): Promise<CustomsDeclaration> {
  const res = await customsClient.get(`/api/v1/declarations/code/${declarationCode}`)
  return res.data
}

export async function getDeclarationsByDeclarant(nationalId: string): Promise<CustomsDeclaration[]> {
  const res = await customsClient.get('/api/v1/declarations', { params: { declarantNin: nationalId, size: 20 } })
  return res.data?.content ?? res.data ?? []
}

export async function getPendingDeclarations(): Promise<CustomsDeclaration[]> {
  const res = await customsClient.get('/api/v1/declarations', { params: { status: 'PENDING', size: 50 } })
  return res.data?.content ?? res.data ?? []
}

export async function recordInspection(
  declarationId: string,
  inspectionNotes: string,
  action: 'CLEAR' | 'HOLD'
): Promise<CustomsDeclaration> {
  const endpoint = action === 'CLEAR'
    ? `/api/v1/declarations/${declarationId}/clear`
    : `/api/v1/declarations/${declarationId}/hold`
  const res = await customsClient.patch(endpoint, { inspectionNotes })
  return res.data
}
