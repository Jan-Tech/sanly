import axios from 'axios'

const customsClient = axios.create({ baseURL: '/proxy/customs', timeout: 10_000 })
customsClient.interceptors.request.use((config) => {
  const token = import.meta.env.VITE_CUSTOMS_TOKEN || null
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export interface CustomsDeclaration {
  declarationId: string
  declarationCode: string
  declarantType: 'CITIZEN' | 'BUSINESS'
  declarantNationalId: string | null
  declarantBusinessNumber: string | null
  declarationType: 'IMPORT' | 'EXPORT' | 'TRANSIT'
  portCode: string
  cargoDescription: string | null
  hsCode: string | null
  countryOfOrigin: string | null
  countryOfDestination: string | null
  quantity: string | null
  unit: string | null
  declaredValue: string | null
  currency: string
  dutiesOwed: string | null
  dutiesPaid: string | null
  declarationDate: string
  status: 'DRAFT' | 'SUBMITTED' | 'UNDER_REVIEW' | 'CLEARED' | 'REJECTED' | 'HELD'
  rejectionReason: string | null
  processedAt: string | null
  createdAt: string
}

export async function getDeclarationsByDeclarant(id: string): Promise<CustomsDeclaration[]> {
  const res = await customsClient.get<CustomsDeclaration[]>(
    `/api/v1/customs/declarations/declarant/${id}`
  )
  return res.data ?? []
}

export async function verifyDeclaration(declarationCode: string): Promise<CustomsDeclaration> {
  const res = await customsClient.get<CustomsDeclaration>(
    `/api/v1/customs/declarations/verify/${declarationCode}`
  )
  return res.data
}
