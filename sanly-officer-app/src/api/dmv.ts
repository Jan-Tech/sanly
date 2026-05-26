import { dmvClient } from './client'
import type { DrivingLicense, LicenseApplication } from '../types'

export async function verifyLicense(licenseNumber: string): Promise<DrivingLicense> {
  const res = await dmvClient.get(`/api/v1/licenses/verify/${licenseNumber}`)
  return res.data
}

export async function getLicensesByNin(nationalId: string): Promise<DrivingLicense[]> {
  const res = await dmvClient.get('/api/v1/licenses', { params: { nationalId } })
  return res.data?.content ?? res.data ?? []
}

export async function getPendingApplications(): Promise<LicenseApplication[]> {
  const res = await dmvClient.get('/api/v1/applications', { params: { status: 'PENDING', size: 50 } })
  return res.data?.content ?? res.data ?? []
}

export async function getApplicationById(applicationId: string): Promise<LicenseApplication> {
  const res = await dmvClient.get(`/api/v1/applications/${applicationId}`)
  return res.data
}

export async function approveApplication(applicationId: string, notes?: string): Promise<LicenseApplication> {
  const res = await dmvClient.patch(`/api/v1/applications/${applicationId}/approve`, { notes })
  return res.data
}

export async function rejectApplication(applicationId: string, reason: string): Promise<LicenseApplication> {
  const res = await dmvClient.patch(`/api/v1/applications/${applicationId}/reject`, { reason })
  return res.data
}

export async function getTodayAppointments() {
  const today = new Date().toISOString().slice(0, 10)
  const res = await dmvClient.get('/api/v1/appointments', { params: { date: today } })
  return res.data?.content ?? res.data ?? []
}
