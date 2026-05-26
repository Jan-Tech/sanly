import axios from 'axios'
import type { ApiResponse, PageResponse } from '../types'

const client = axios.create({
  baseURL: import.meta.env.VITE_GATEWAY_URL ?? 'http://localhost:8080',
  headers: { Authorization: `Bearer ${import.meta.env.VITE_APPOINTMENTS_TOKEN ?? ''}` },
})

client.interceptors.request.use((config) => {
  const raw = localStorage.getItem('sanly-admin-auth')
  if (raw) {
    try {
      const { state } = JSON.parse(raw)
      if (state?.token) config.headers.Authorization = `Bearer ${state.token}`
    } catch { /* ignore */ }
  }
  return config
})

export interface Office {
  officeId: string; officeCode: string; institutionType: string; name: string
  region: string; address: string; phone?: string; status: string
}

export interface Appointment {
  appointmentId: string; appointmentCode: string; citizenNationalId: string
  officeCode: string; officeName: string; serviceName: string
  appointmentDate: string; slotTime: string; status: string; bookedAt: string
  citizenRating?: number; completedAt?: string; cancelledAt?: string
}

export interface AppointmentStats {
  totalBooked: number; totalCompleted: number; totalCancelled: number
  totalNoShow: number; completionRate: number; noShowRate: number; averageRating: number
}

export async function getAdminAllAppointments(params: { page?: number; size?: number; officeCode?: string; status?: string }): Promise<PageResponse<Appointment>> {
  const res = await client.get<ApiResponse<PageResponse<Appointment>>>('/appointments/api/v1/appointments/admin/all', { params: {
    page: params.page ?? 0, size: params.size ?? 20,
    ...(params.officeCode && { officeCode: params.officeCode }),
    ...(params.status && { status: params.status }),
  }})
  return res.data.data
}

export async function getAdminStats(): Promise<AppointmentStats> {
  const res = await client.get<ApiResponse<AppointmentStats>>('/appointments/api/v1/appointments/admin/stats')
  return res.data.data
}

export async function getOfficeStats(officeCode: string): Promise<AppointmentStats> {
  const res = await client.get<ApiResponse<AppointmentStats>>(`/appointments/api/v1/appointments/stats/${officeCode}`)
  return res.data.data
}

export async function getAllOffices(): Promise<Office[]> {
  const res = await client.get<ApiResponse<Office[]>>('/appointments/api/v1/appointments/offices')
  return res.data.data ?? []
}
