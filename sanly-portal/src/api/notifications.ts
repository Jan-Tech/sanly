import axios from 'axios'
import { useAuthStore } from '../store/authStore'

const client = axios.create({ baseURL: '/proxy/notifications' })

client.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export interface Notification {
  notificationId: string
  eventType: string
  title: string
  body: string
  channel: string
  language: string
  status: 'UNREAD' | 'READ' | 'DELIVERED' | 'FAILED'
  createdAt: string
  readAt: string | null
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

export function getUnreadCount(): Promise<{ count: number }> {
  return client.get('/api/v1/notifications/my/unread-count').then(r => r.data)
}

export function getRecentNotifications(): Promise<Notification[]> {
  return client.get('/api/v1/notifications/my/recent').then(r => r.data)
}

export function getMyNotifications(params?: {
  status?: string
  eventType?: string
  page?: number
  size?: number
}): Promise<PageResponse<Notification>> {
  return client.get('/api/v1/notifications/my', { params }).then(r => r.data)
}

export function markRead(notificationId: string): Promise<Notification> {
  return client.patch(`/api/v1/notifications/my/${notificationId}/read`).then(r => r.data)
}

export function markAllRead(): Promise<{ updated: number }> {
  return client.patch('/api/v1/notifications/my/read-all').then(r => r.data)
}
