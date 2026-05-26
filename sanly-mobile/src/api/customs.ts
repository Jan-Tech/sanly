import { customsClient } from './client'

export interface CustomsDeclaration {
  declarationId: string
  declarationCode: string
  declarantNationalId: string
  declarationType: string
  status: string
  portCode: string
  submittedAt: string | null
  dutiesOwed: string | null
  dutiesPaid: string | null
  hsCode: string | null
  goodsDescription: string | null
  totalValueTmt: string | null
}

export async function getDeclarationsByDeclarant(nationalId: string): Promise<CustomsDeclaration[]> {
  const res = await customsClient.get<CustomsDeclaration[]>(`/api/v1/customs/declarations/declarant/${nationalId}`)
  return res.data ?? []
}
