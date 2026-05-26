import axios from 'axios'

const customsClient = axios.create({ baseURL: '/proxy/customs', timeout: 10_000 })
customsClient.interceptors.request.use((config) => {
  const token = import.meta.env.VITE_CUSTOMS_TOKEN || null
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export interface AdminDeclaration {
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

export interface AdminPort {
  portId: string
  portCode: string
  name: string
  portType: string
  region: string | null
  status: 'ACTIVE' | 'CLOSED'
}

export async function getAllDeclarationsByStatus(status: string): Promise<AdminDeclaration[]> {
  const res = await customsClient.get<AdminDeclaration[]>(`/api/v1/customs/declarations/port/ALL?status=${status}`)
    .catch(async () => {
      // Fall back to getting all ports then querying each
      const portsRes = await customsClient.get<AdminPort[]>('/api/v1/customs/ports')
      const ports = portsRes.data ?? []
      const all: AdminDeclaration[] = []
      for (const port of ports) {
        try {
          const dr = await customsClient.get<AdminDeclaration[]>(
            `/api/v1/customs/declarations/port/${port.portCode}?status=${status}`
          )
          all.push(...(dr.data ?? []))
        } catch {}
      }
      return { data: all }
    })
  return res.data ?? []
}

export async function getDeclarationsByPort(portCode: string, status?: string): Promise<AdminDeclaration[]> {
  const url = status
    ? `/api/v1/customs/declarations/port/${portCode}?status=${status}`
    : `/api/v1/customs/declarations/port/${portCode}`
  const res = await customsClient.get<AdminDeclaration[]>(url)
  return res.data ?? []
}

export async function getDeclaration(declarationCode: string): Promise<AdminDeclaration> {
  const res = await customsClient.get<AdminDeclaration>(`/api/v1/customs/declarations/${declarationCode}`)
  return res.data
}

export async function clearDeclaration(declarationCode: string): Promise<AdminDeclaration> {
  const res = await customsClient.patch<AdminDeclaration>(`/api/v1/customs/declarations/${declarationCode}/clear`)
  return res.data
}

export async function rejectDeclaration(declarationCode: string, rejectionReason: string): Promise<AdminDeclaration> {
  const res = await customsClient.patch<AdminDeclaration>(
    `/api/v1/customs/declarations/${declarationCode}/reject`,
    { rejectionReason }
  )
  return res.data
}

export async function holdDeclaration(declarationCode: string): Promise<AdminDeclaration> {
  const res = await customsClient.patch<AdminDeclaration>(
    `/api/v1/customs/declarations/${declarationCode}/hold`
  )
  return res.data
}

export async function calculateDuties(declarationCode: string): Promise<unknown> {
  const res = await customsClient.post(`/api/v1/customs/declarations/${declarationCode}/calculate-duties`, {})
  return res.data
}

export async function getPorts(): Promise<AdminPort[]> {
  const res = await customsClient.get<AdminPort[]>('/api/v1/customs/ports')
  return res.data ?? []
}
