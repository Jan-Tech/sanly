import { appointmentsClient } from './client'
import type { Appointment } from '../types'

export async function getAppointmentByCode(code: string): Promise<Appointment> {
  const res = await appointmentsClient.get(`/api/v1/appointments/code/${code}`)
  return res.data
}

export async function getTodayAppointments(officeCode?: string): Promise<Appointment[]> {
  const today = new Date().toISOString().slice(0, 10)
  const res = await appointmentsClient.get('/api/v1/appointments', {
    params: { date: today, officeCode, size: 100 },
  })
  return res.data?.content ?? res.data ?? []
}

export async function confirmArrival(appointmentId: string): Promise<Appointment> {
  const res = await appointmentsClient.patch(`/api/v1/appointments/${appointmentId}/confirm`)
  return res.data
}

export async function completeAppointment(appointmentId: string, notes?: string): Promise<Appointment> {
  const res = await appointmentsClient.patch(`/api/v1/appointments/${appointmentId}/complete`, { notes })
  return res.data
}

export async function markNoShow(appointmentId: string): Promise<Appointment> {
  const res = await appointmentsClient.patch(`/api/v1/appointments/${appointmentId}/no-show`)
  return res.data
}

export async function cancelAppointment(appointmentId: string, reason: string): Promise<Appointment> {
  const res = await appointmentsClient.patch(`/api/v1/appointments/${appointmentId}/cancel`, { reason })
  return res.data
}
