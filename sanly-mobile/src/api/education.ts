import { educationClient } from './client'

export interface Diploma {
  diplomaId: string
  diplomaCode: string
  studentNationalId: string
  institutionCode: string
  institutionName: string
  programName: string
  issuedAt: string
  status: 'VALID' | 'REVOKED'
}

export interface Enrollment {
  enrollmentId: string
  studentNationalId: string
  institutionCode: string
  institutionName: string
  programName: string
  enrolledAt: string
  status: 'PENDING' | 'ACTIVE' | 'GRADUATED' | 'DROPPED' | 'TRANSFERRED'
  gpa: number | null
}

export async function getDiplomas(nationalId: string): Promise<Diploma[]> {
  const res = await educationClient.get<Diploma[]>(`/api/v1/education/diplomas/student/${nationalId}`)
  return res.data ?? []
}

export async function getEnrollments(nationalId: string): Promise<Enrollment[]> {
  const res = await educationClient.get<Enrollment[]>(`/api/v1/education/enrollments/student/${nationalId}`)
  return res.data ?? []
}
