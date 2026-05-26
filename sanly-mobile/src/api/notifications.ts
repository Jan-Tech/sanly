import { notificationsClient } from './client'
import type { Notification, PageResponse } from '../types'

export async function getUnreadCount(): Promise<number> {
  const res = await notificationsClient.get<{ count: number }>('/api/v1/notifications/my/unread-count')
  return res.data.count ?? 0
}

export async function getMyNotifications(params?: { page?: number; size?: number }): Promise<PageResponse<Notification>> {
  const res = await notificationsClient.get<PageResponse<Notification>>('/api/v1/notifications/my', { params })
  return res.data
}

export async function markRead(notificationId: string): Promise<void> {
  await notificationsClient.patch(`/api/v1/notifications/my/${notificationId}/read`)
}

export async function markAllRead(): Promise<void> {
  await notificationsClient.patch('/api/v1/notifications/my/read-all')
}
