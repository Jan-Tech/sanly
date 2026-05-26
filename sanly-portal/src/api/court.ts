import axios from 'axios'

const courtClient = axios.create({ baseURL: '/proxy/court', timeout: 10_000 })
courtClient.interceptors.request.use((config) => {
  const token = import.meta.env.VITE_COURT_TOKEN || null
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export interface CourtCase {
  caseId: string
  caseNumber: string
  courtCode: string
  caseType: string
  plaintiffNationalId: string
  defendantNationalId: string
  assignedJudgeOfficerId: string | null
  filedAt: string
  hearingDate: string | null
  closedAt: string | null
  status: 'FILED' | 'UNDER_REVIEW' | 'HEARING_SCHEDULED' | 'IN_PROGRESS' | 'DECIDED' | 'APPEALED' | 'CLOSED' | 'DISMISSED'
  summary: string | null
  createdAt: string
}

export interface CourtFine {
  fineId: string
  fineCode: string
  caseNumber: string
  citizenNationalId: string
  amount: string
  reason: string
  issuedAt: string
  dueDate: string
  status: 'OUTSTANDING' | 'PENDING_VERIFICATION' | 'PAID' | 'OVERDUE' | 'WAIVED' | 'APPEALING'
  paidAt: string | null
  notes: string | null
  createdAt: string
}

export interface CourtDocument {
  documentId: string
  documentCode: string
  caseNumber: string
  documentType: string
  title: string
  content: string
  submittedByNationalId: string
  submittedAt: string
  status: 'SUBMITTED' | 'ACCEPTED' | 'REJECTED'
  digitalSignature: string
  createdAt: string
}

export interface CourtVerdict {
  verdictId: string
  caseNumber: string
  verdictType: string
  summary: string | null
  issuedAt: string
  appealDeadline: string
  appealed: boolean
}

export async function getCasesByCitizen(nationalId: string): Promise<CourtCase[]> {
  const res = await courtClient.get<CourtCase[]>(`/api/v1/court/cases/citizen/${nationalId}`)
  return res.data ?? []
}

export async function getFinesByCitizen(nationalId: string): Promise<CourtFine[]> {
  const res = await courtClient.get<CourtFine[]>(`/api/v1/court/fines/citizen/${nationalId}`)
  return res.data ?? []
}

export async function getDocumentsByCase(caseNumber: string): Promise<CourtDocument[]> {
  const res = await courtClient.get<CourtDocument[]>(`/api/v1/court/cases/${caseNumber}/documents`)
  return res.data ?? []
}

export async function submitPaymentProof(fineCode: string, citizenNationalId: string, paymentProofNote: string): Promise<CourtFine> {
  const res = await courtClient.patch<CourtFine>(`/api/v1/court/fines/${fineCode}/citizen-pay`, {
    citizenNationalId,
    paymentProofNote,
  })
  return res.data
}
