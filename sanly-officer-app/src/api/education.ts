import { educationClient } from './client'
import type { Diploma, Enrollment } from '../types'

export async function verifyDiploma(diplomaCode: string): Promise<Diploma> {
  const res = await educationClient.get(`/api/v1/education/diplomas/verify/${diplomaCode}`)
  return res.data
}

export async function getDiplomasByNin(nationalId: string): Promise<Diploma[]> {
  const res = await educationClient.get('/api/v1/education/diplomas', { params: { nationalId, size: 20 } })
  return res.data?.content ?? res.data ?? []
}

export async function issueDiploma(data: {
  enrollmentId: string
  degree: string
  field: string
  graduationYear: number
  gpa?: number
}): Promise<Diploma> {
  const res = await educationClient.post('/api/v1/education/diplomas', data)
  return res.data
}

export async function getPendingIntake(): Promise<Enrollment[]> {
  const res = await educationClient.get('/api/v1/education/enrollments/pending-intake', { params: { size: 50 } })
  return res.data?.content ?? res.data ?? []
}

export async function getEnrollmentsByNin(nationalId: string): Promise<Enrollment[]> {
  const res = await educationClient.get('/api/v1/education/enrollments', { params: { nationalId, size: 20 } })
  return res.data?.content ?? res.data ?? []
}

export async function updateEnrollmentStatus(
  enrollmentId: string,
  status: 'ACTIVE' | 'GRADUATED' | 'DROPPED'
): Promise<Enrollment> {
  const res = await educationClient.patch(`/api/v1/education/enrollments/${enrollmentId}`, { status })
  return res.data
}
