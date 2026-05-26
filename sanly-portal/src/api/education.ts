import axios from 'axios'

const educationClient = axios.create({ baseURL: '/proxy/education', timeout: 10_000 })
educationClient.interceptors.request.use((config) => {
  const token = import.meta.env.VITE_EDUCATION_TOKEN || null
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export interface Enrollment {
  enrollmentId: string
  citizenNationalId: string
  institutionCode: string
  enrollmentDate: string
  expectedGraduationYear: number | null
  programName: string | null
  status: 'PENDING' | 'ACTIVE' | 'GRADUATED' | 'DROPPED' | 'TRANSFERRED'
  createdAt: string
}

export interface AcademicRecord {
  recordId: string
  citizenNationalId: string
  institutionCode: string
  academicYear: string
  grade: string | null
  gpa: string | null
  notes: string | null
  createdAt: string
}

export interface Diploma {
  diplomaId: string
  diplomaCode: string
  citizenNationalId: string
  institutionCode: string
  programName: string
  programLevel: 'PRIMARY' | 'SECONDARY' | 'VOCATIONAL_CERTIFICATE' | 'BACHELORS' | 'MASTERS' | 'PHD' | 'POSTDOCTORAL'
  graduationDate: string
  honors: 'NONE' | 'CUM_LAUDE' | 'MAGNA_CUM_LAUDE' | 'SUMMA_CUM_LAUDE'
  issuedAt: string
  status: 'VALID' | 'REVOKED'
  revokedReason: string | null
}

export async function getEnrollments(nationalId: string): Promise<Enrollment[]> {
  const res = await educationClient.get<Enrollment[]>(
    `/api/v1/education/enrollments/citizen/${nationalId}`
  )
  return res.data ?? []
}

export async function getAcademicRecords(nationalId: string): Promise<AcademicRecord[]> {
  const res = await educationClient.get<AcademicRecord[]>(
    `/api/v1/education/records/citizen/${nationalId}`
  )
  return res.data ?? []
}

export async function getDiplomas(nationalId: string): Promise<Diploma[]> {
  const res = await educationClient.get<Diploma[]>(
    `/api/v1/education/diplomas/citizen/${nationalId}`
  )
  return res.data ?? []
}
