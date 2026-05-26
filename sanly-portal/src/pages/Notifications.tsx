import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Bell, CheckCheck, RefreshCw } from 'lucide-react'
import { getMyNotifications, markRead, markAllRead, type Notification } from '../api/notifications'

const STATUS_TABS = [
  { value: '', label: 'All' },
  { value: 'UNREAD', label: 'Unread' },
  { value: 'READ', label: 'Read' },
]

const EVENT_LABELS: Record<string, string> = {
  DATA_ACCESSED_BY_POLICE:           'Police access',
  DATA_ACCESSED_BY_TAX:              'Tax authority access',
  DATA_ACCESSED_BY_DMV:              'DMV access',
  DATA_ACCESSED_BY_MEDICAL:          'Medical access',
  DATA_ACCESSED_BY_BUSINESS:         'Business registry access',
  DATA_ACCESSED_UNKNOWN_INSTITUTION: 'Data accessed',
  DRIVING_LICENSE_ISSUED:            'License issued',
  DRIVING_LICENSE_SUSPENDED:         'License suspended',
  MEDICAL_RECORD_ADDED:              'Medical record',
  TAX_STATUS_CHANGED:                'Tax status change',
  BUSINESS_REGISTRATION_APPROVED:    'Business approved',
  BUSINESS_REGISTRATION_REJECTED:    'Business rejected',
  CRIMINAL_RECORD_ADDED:             'Criminal record',
  CIVIL_BIRTH_REGISTERED:            'Birth registered',
  CIVIL_MARRIAGE_REGISTERED:         'Marriage registered',
  CIVIL_DEATH_REGISTERED:            'Death registered',
  ANOMALY_DETECTED_ON_YOUR_DATA:     'Unusual access',
  SUSPICIOUS_ACCESS_DETECTED:        'Suspicious access',
  CITIZEN_STATUS_CHANGED:            'Status changed',
  PASSWORD_CHANGED:                  'Password changed',
}

const STATUS_COLORS: Record<string, string> = {
  UNREAD:    'bg-blue-100 text-blue-700',
  READ:      'bg-gray-100 text-gray-500',
  DELIVERED: 'bg-green-100 text-green-700',
  FAILED:    'bg-red-100 text-red-700',
}

function timeAgo(iso: string): string {
  const diff = Date.now() - new Date(iso).getTime()
  const m = Math.floor(diff / 60000)
  if (m < 1) return 'just now'
  if (m < 60) return `${m}m ago`
  const h = Math.floor(m / 60)
  if (h < 24) return `${h}h ago`
  return new Date(iso).toLocaleDateString()
}

export default function Notifications() {
  const queryClient = useQueryClient()
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(0)

  const { data, isLoading, refetch } = useQuery({
    queryKey: ['my-notifications', status, page],
    queryFn: () => getMyNotifications({ status: status || undefined, page, size: 20 }),
    staleTime: 30_000,
  })

  const { mutate: doMarkRead } = useMutation({
    mutationFn: markRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-notifications'] })
      queryClient.invalidateQueries({ queryKey: ['notifications-count'] })
    },
  })

  const { mutate: doMarkAll, isPending: markingAll } = useMutation({
    mutationFn: markAllRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-notifications'] })
      queryClient.invalidateQueries({ queryKey: ['notifications-count'] })
    },
  })

  const notifications = data?.content ?? []
  const totalPages = data?.totalPages ?? 0

  return (
    <div className="max-w-2xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center">
            <Bell className="w-5 h-5 text-primary" />
          </div>
          <div>
            <h1 className="text-xl font-semibold text-dark">Notifications</h1>
            <p className="text-sm text-gray-500">Alerts about changes to your government data</p>
          </div>
        </div>

        <div className="flex gap-2">
          <button
            onClick={() => refetch()}
            className="p-2 rounded-lg hover:bg-gray-100 text-gray-400 transition-colors"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
          <button
            onClick={() => doMarkAll()}
            disabled={markingAll}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm text-gray-600 hover:bg-gray-100 transition-colors disabled:opacity-50"
          >
            <CheckCheck className="w-4 h-4" />
            Mark all read
          </button>
        </div>
      </div>

      {/* Status tabs */}
      <div className="flex gap-1 bg-gray-100 rounded-xl p-1 mb-5">
        {STATUS_TABS.map(tab => (
          <button
            key={tab.value}
            onClick={() => { setStatus(tab.value); setPage(0) }}
            className={`flex-1 py-1.5 text-sm font-medium rounded-lg transition-colors ${
              status === tab.value ? 'bg-white text-dark shadow-sm' : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* List */}
      <div className="space-y-2">
        {isLoading && (
          <div className="flex justify-center py-12">
            <div className="w-6 h-6 border-2 border-primary border-t-transparent rounded-full animate-spin" />
          </div>
        )}

        {!isLoading && notifications.length === 0 && (
          <div className="text-center py-16 text-gray-400">
            <Bell className="w-10 h-10 mx-auto mb-3 opacity-30" />
            <p className="text-sm">No notifications</p>
          </div>
        )}

        {notifications.map((n: Notification) => (
          <div
            key={n.notificationId}
            className={`bg-white rounded-xl border p-4 cursor-pointer transition-all hover:shadow-sm ${
              n.status === 'UNREAD' ? 'border-primary/30 bg-primary/5' : 'border-gray-100'
            }`}
            onClick={() => { if (n.status === 'UNREAD') doMarkRead(n.notificationId) }}
          >
            <div className="flex items-start gap-3">
              {n.status === 'UNREAD' && (
                <div className="mt-1.5 w-2 h-2 rounded-full bg-primary shrink-0" />
              )}
              <div className={`flex-1 ${n.status !== 'UNREAD' ? 'ml-5' : ''}`}>
                <div className="flex items-center justify-between gap-2 flex-wrap">
                  <p className="text-sm font-semibold text-dark">{n.title}</p>
                  <div className="flex items-center gap-2 shrink-0">
                    <span className={`text-[10px] font-medium px-2 py-0.5 rounded-full ${STATUS_COLORS[n.status] ?? 'bg-gray-100'}`}>
                      {n.status}
                    </span>
                    <span className="text-xs text-gray-400">{timeAgo(n.createdAt)}</span>
                  </div>
                </div>
                <p className="text-xs text-gray-500 mt-1 leading-relaxed">{n.body}</p>
                <p className="text-[10px] text-gray-400 mt-2 uppercase tracking-wide">
                  {EVENT_LABELS[n.eventType] ?? n.eventType}
                </p>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-center gap-2 mt-6">
          <button
            onClick={() => setPage(p => Math.max(0, p - 1))}
            disabled={page === 0}
            className="px-3 py-1.5 text-sm rounded-lg border border-gray-200 hover:bg-gray-50 disabled:opacity-40"
          >
            Previous
          </button>
          <span className="px-3 py-1.5 text-sm text-gray-500">
            {page + 1} / {totalPages}
          </span>
          <button
            onClick={() => setPage(p => p + 1)}
            disabled={page >= totalPages - 1}
            className="px-3 py-1.5 text-sm rounded-lg border border-gray-200 hover:bg-gray-50 disabled:opacity-40"
          >
            Next
          </button>
        </div>
      )}
    </div>
  )
}
