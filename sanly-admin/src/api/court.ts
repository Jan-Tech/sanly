import axios from 'axios'

const courtClient = axios.create({ baseURL: '/proxy/court', timeout: 10_000 })
courtClient.interceptors.request.use((config) => {
  const token = import.meta.env.VITE_COURT_TOKEN || null
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export interface AdminCourtCase {
  caseId: string
  caseNumber: string
  courtCode: string
  caseType: string
  status: string
  plaintiffNationalId: string
  defendantNationalId: string
  assignedJudgeOfficerId: string | null
  filedAt: string
  hearingDate: string | null
  closedAt: string | null
}

export interface AdminCourtFine {
  fineId: string
  fineCode: string
  caseNumber: string | null
  citizenNationalId: string
  reason: string
  amount: string
  dueDate: string
  status: string
  issuedAt: string
  paidAt: string | null
  paymentProofNote: string | null
}

export interface AdminVerdict {
  verdictId: string
  caseNumber: string
  verdictType: string
  summary: string | null
  issuedAt: string
  appealDeadline: string | null
  judgeOfficerId: string
}

export interface AdminCourt {
  courtId: string
  courtCode: string
  name: string
  courtType: string
  region: string | null
  status: string
}

export async function getCasesByStatus(status: string): Promise<AdminCourtCase[]> {
  const res = await courtClient.get<AdminCourtCase[]>(`/api/v1/court/cases?status=${status}`)
  return res.data ?? []
}

export async function getCaseByNumber(caseNumber: string): Promise<AdminCourtCase> {
  const res = await courtClient.get<AdminCourtCase>(`/api/v1/court/cases/${caseNumber}`)
  return res.data
}

export async function assignJudge(caseNumber: string, judgeOfficerId: string): Promise<AdminCourtCase> {
  const res = await courtClient.patch<AdminCourtCase>(
    `/api/v1/court/cases/${caseNumber}/assign-judge`,
    { judgeOfficerId }
  )
  return res.data
}

export async function scheduleHearing(caseNumber: string, hearingDate: string): Promise<AdminCourtCase> {
  const res = await courtClient.patch<AdminCourtCase>(
    `/api/v1/court/cases/${caseNumber}/schedule-hearing`,
    { hearingDate }
  )
  return res.data
}

export async function getFinesByStatus(status: string): Promise<AdminCourtFine[]> {
  const res = await courtClient.get<AdminCourtFine[]>(`/api/v1/court/fines?status=${status}`)
  return res.data ?? []
}

export async function verifyPayment(fineCode: string): Promise<AdminCourtFine> {
  const res = await courtClient.patch<AdminCourtFine>(`/api/v1/court/fines/${fineCode}/verify-payment`)
  return res.data
}

export async function markFinePaid(fineCode: string): Promise<AdminCourtFine> {
  const res = await courtClient.patch<AdminCourtFine>(`/api/v1/court/fines/${fineCode}/mark-paid`)
  return res.data
}

export async function waiveFine(fineCode: string, reason: string): Promise<AdminCourtFine> {
  const res = await courtClient.patch<AdminCourtFine>(`/api/v1/court/fines/${fineCode}/waive`, { reason })
  return res.data
}

export async function getCourts(): Promise<AdminCourt[]> {
  const res = await courtClient.get<AdminCourt[]>('/api/v1/court/courts')
  return res.data ?? []
}
