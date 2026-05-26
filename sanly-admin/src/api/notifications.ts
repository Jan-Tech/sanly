import axios from 'axios'
import { useAuthStore } from '../store/authStore'

const client = axios.create({ baseURL: '/proxy/notifications' })

client.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export interface NotificationStats {
  totalSentToday: number
  totalFailedToday: number
  totalUnread: number
  byEventType: Record<string, number>
  byChannel: Record<string, number>
}

export interface AdminNotification {
  notificationId: string
  citizenNationalId: string
  eventType: string
  title: string
  body: string
  channel: string
  status: string
  createdAt: string
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export function getNotificationStats(): Promise<NotificationStats> {
  return client.get('/api/v1/notifications/admin/stats').then(r => r.data)
}

export function getAllNotifications(params?: {
  nationalId?: string
  eventType?: string
  status?: string
  page?: number
  size?: number
}): Promise<PageResponse<AdminNotification>> {
  return client.get('/api/v1/notifications/admin/all', { params }).then(r => r.data)
}
