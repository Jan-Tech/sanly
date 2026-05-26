import { educationClient } from './client'

export interface EducationInstitution {
  institutionId: string
  institutionCode: string
  name: string
  type: string
  region: string
  address: string
  licenseNumber: string | null
  accreditedUntil: string | null
  status: 'ACTIVE' | 'SUSPENDED' | 'CLOSED'
  createdAt: string
}

export interface Diploma {
  diplomaId: string
  diplomaCode: string
  citizenNationalId: string
  institutionCode: string
  programName: string
  programLevel: string
  graduationDate: string
  honors: 'NONE' | 'CUM_LAUDE' | 'MAGNA_CUM_LAUDE' | 'SUMMA_CUM_LAUDE'
  issuedAt: string
  status: 'VALID' | 'REVOKED'
  revokedReason: string | null
}

export interface PendingEnrollment {
  enrollmentId: string
  citizenNationalId: string
  institutionCode: string
  enrollmentDate: string
  expectedGraduationYear: number | null
  programName: string | null
  status: 'PENDING' | 'ACTIVE' | 'GRADUATED' | 'DROPPED' | 'TRANSFERRED'
  createdAt: string
}

export async function getInstitutions(): Promise<EducationInstitution[]> {
  const res = await educationClient.get<EducationInstitution[]>(
    '/api/v1/education/institutions'
  )
  return res.data ?? []
}

export async function updateInstitutionStatus(
  institutionCode: string,
  status: 'ACTIVE' | 'SUSPENDED' | 'CLOSED'
): Promise<EducationInstitution> {
  const res = await educationClient.patch<EducationInstitution>(
    `/api/v1/education/institutions/${institutionCode}/status`,
    { status }
  )
  return res.data
}

export async function getAllDiplomas(): Promise<Diploma[]> {
  const res = await educationClient.get<Diploma[]>('/api/v1/education/diplomas')
  return res.data ?? []
}

export async function revokeDiploma(diplomaCode: string, reason: string): Promise<Diploma> {
  const res = await educationClient.patch<Diploma>(
    `/api/v1/education/diplomas/${diplomaCode}/revoke`,
    { reason }
  )
  return res.data
}

export async function getPendingEnrollments(): Promise<PendingEnrollment[]> {
  const res = await educationClient.get<PendingEnrollment[]>(
    '/api/v1/education/enrollments/pending'
  )
  return res.data ?? []
}
