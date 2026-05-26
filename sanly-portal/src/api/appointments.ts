import axios from 'axios'
import type { ApiResponse, PageResponse } from '../types'

const client = axios.create({
  baseURL: import.meta.env.VITE_GATEWAY_URL ?? 'http://localhost:8080',
})

client.interceptors.request.use((config) => {
  const raw = localStorage.getItem('sanly-auth')
  if (raw) {
    try {
      const { state } = JSON.parse(raw)
      if (state?.token) config.headers.Authorization = `Bearer ${state.token}`
    } catch { /* ignore */ }
  }
  return config
})

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
  requiresDocuments?: string
  status: string
}

export interface SlotResponse {
  slotTime: string
  available: boolean
  spotsRemaining: number
}

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
  citizenRating?: number
  citizenFeedback?: string
}

export async function getOffices(params?: { institutionType?: string; region?: string }): Promise<Office[]> {
  const res = await client.get<ApiResponse<Office[]>>('/appointments/api/v1/appointments/offices', { params })
  return res.data.data ?? []
}

export async function getServiceTypes(officeCode: string): Promise<ServiceType[]> {
  const res = await client.get<ApiResponse<ServiceType[]>>(`/appointments/api/v1/appointments/services/office/${officeCode}`)
  return (res.data.data ?? []).filter((s: ServiceType) => s.status === 'ACTIVE')
}

export async function getAvailableSlots(officeCode: string, date: string, serviceTypeId: string): Promise<SlotResponse[]> {
  const res = await client.get<ApiResponse<SlotResponse[]>>(
    `/appointments/api/v1/appointments/slots/${officeCode}`,
    { params: { date, serviceTypeId } }
  )
  return res.data.data ?? []
}

export async function getNextAvailableDates(officeCode: string, serviceTypeId: string): Promise<string[]> {
  const res = await client.get<ApiResponse<string[]>>(
    `/appointments/api/v1/appointments/slots/${officeCode}/next-available`,
    { params: { serviceTypeId } }
  )
  return res.data.data ?? []
}

export async function bookAppointment(payload: {
  officeCode: string; serviceTypeId: string; appointmentDate: string; slotTime: string; notes?: string
}): Promise<Appointment> {
  const res = await client.post<ApiResponse<Appointment>>('/appointments/api/v1/appointments/book', payload)
  return res.data.data
}

export async function getMyAppointments(page = 0, size = 20): Promise<PageResponse<Appointment>> {
  const res = await client.get<ApiResponse<PageResponse<Appointment>>>('/appointments/api/v1/appointments/my', { params: { page, size } })
  return res.data.data
}

export async function cancelAppointment(appointmentCode: string, reason: string): Promise<Appointment> {
  const res = await client.patch<ApiResponse<Appointment>>(
    `/appointments/api/v1/appointments/my/${appointmentCode}/cancel`, { reason }
  )
  return res.data.data
}

export async function rateAppointment(appointmentCode: string, rating: number, feedback?: string): Promise<Appointment> {
  const res = await client.post<ApiResponse<Appointment>>(
    `/appointments/api/v1/appointments/my/${appointmentCode}/rate`, { rating, feedback }
  )
  return res.data.data
}
