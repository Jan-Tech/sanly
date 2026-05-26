import { appointmentsClient } from './client'
import type { ApiResponse, PageResponse } from '../types'

export interface Appointment {
  appointmentId: string
  appointmentCode: string
  citizenNationalId: string
  officeCode: string
  serviceTypeId: string
  officeName: string
  serviceName: string
  appointmentDate: string
  slotTime: string
  status: string
  bookedAt: string
  notes?: string
  cancelledAt?: string
  cancelReason?: string
  completedAt?: string
}

export interface Office {
  officeId: string
  officeCode: string
  institutionType: string
  name: string
  region: string
  address: string
  phone?: string
  status: string
}

export interface ServiceType {
  serviceTypeId: string
  officeCode: string
  serviceName: string
  description?: string
  durationMinutes: number
  status: string
}

export interface SlotResponse {
  slotTime: string
  available: boolean
  spotsRemaining: number
}

export async function getMyAppointments(page = 0, size = 20): Promise<PageResponse<Appointment>> {
  const res = await appointmentsClient.get<ApiResponse<PageResponse<Appointment>>>(
    '/api/v1/appointments/my', { params: { page, size } }
  )
  return res.data.data
}

export async function cancelAppointment(appointmentCode: string, reason: string): Promise<void> {
  await appointmentsClient.patch(
    `/api/v1/appointments/my/${appointmentCode}/cancel`, { reason }
  )
}

export async function getOffices(params?: { institutionType?: string; region?: string }): Promise<Office[]> {
  const res = await appointmentsClient.get<ApiResponse<Office[]>>(
    '/api/v1/appointments/offices', { params }
  )
  return res.data.data ?? []
}

export async function getServiceTypes(officeCode: string): Promise<ServiceType[]> {
  const res = await appointmentsClient.get<ApiResponse<ServiceType[]>>(
    `/api/v1/appointments/services/office/${officeCode}`
  )
  return (res.data.data ?? []).filter((s: ServiceType) => s.status === 'ACTIVE')
}

export async function getAvailableSlots(officeCode: string, date: string, serviceTypeId: string): Promise<SlotResponse[]> {
  const res = await appointmentsClient.get<ApiResponse<SlotResponse[]>>(
    `/api/v1/appointments/slots/${officeCode}`, { params: { date, serviceTypeId } }
  )
  return res.data.data ?? []
}

export async function bookAppointment(payload: {
  officeCode: string; serviceTypeId: string; appointmentDate: string; slotTime: string; notes?: string
}): Promise<Appointment> {
  const res = await appointmentsClient.post<ApiResponse<Appointment>>(
    '/api/v1/appointments/book', payload
  )
  return res.data.data
}
