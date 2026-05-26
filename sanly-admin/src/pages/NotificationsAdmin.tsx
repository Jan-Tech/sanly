import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Bell, RefreshCw } from 'lucide-react'
import { getAllNotifications } from '../api/notifications'
import { formatDateTime } from '../utils/formatters'

const STATUS_OPTIONS = ['', 'UNREAD', 'READ', 'DELIVERED', 'FAILED']
const STATUS_COLORS: Record<string, string> = {
  UNREAD:    'bg-blue-100 text-blue-700',
  READ:      'bg-gray-100 text-gray-500',
  DELIVERED: 'bg-green-100 text-green-700',
  FAILED:    'bg-red-100 text-red-700',
}

export default function NotificationsAdmin() {
  const [nationalId, setNationalId] = useState('')
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(0)

  const { data, isLoading, refetch } = useQuery({
    queryKey: ['admin-notifications', nationalId, status, page],
    queryFn: () => getAllNotifications({
      nationalId: nationalId || undefined,
      status: status || undefined,
      page,
      size: 50,
    }),
    staleTime: 30_000,
  })

  const notifications = data?.content ?? []
  const totalPages = data?.totalPages ?? 0

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center">
            <Bell className="w-5 h-5 text-primary" />
          </div>
          <div>
            <h1 className="text-xl font-semibold text-dark">Notifications</h1>
            <p className="text-sm text-gray-400">All citizen notifications across the platform</p>
          </div>
        </div>
        <button
          onClick={() => refetch()}
          className="p-2 rounded-lg hover:bg-gray-100 text-gray-400"
        >
          <RefreshCw className="w-4 h-4" />
        </button>
      </div>

      {/* Filters */}
      <div className="card mb-5">
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          <div>
            <label className="block text-xs text-gray-500 mb-1">National ID</label>
            <input
              value={nationalId}
              onChange={e => { setNationalId(e.target.value); setPage(0) }}
              placeholder="Filter by NIN..."
              className="input w-full"
            />
          </div>
          <div>
            <label className="block text-xs text-gray-500 mb-1">Status</label>
            <select
              value={status}
              onChange={e => { setStatus(e.target.value); setPage(0) }}
              className="input w-full"
            >
              {STATUS_OPTIONS.map(s => (
                <option key={s} value={s}>{s || 'All statuses'}</option>
              ))}
            </select>
          </div>
          <div className="flex items-end">
            <button
              onClick={() => { setNationalId(''); setStatus(''); setPage(0) }}
              className="btn-secondary w-full"
            >
              Clear filters
            </button>
          </div>
        </div>
      </div>

      {/* Table */}
      <div className="card !p-0 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 border-b border-gray-100">
            <tr>
              <th className="text-left px-4 py-3 text-xs font-medium text-gray-500">NIN</th>
              <th className="text-left px-4 py-3 text-xs font-medium text-gray-500">Event Type</th>
              <th className="text-left px-4 py-3 text-xs font-medium text-gray-500">Title</th>
              <th className="text-left px-4 py-3 text-xs font-medium text-gray-500">Status</th>
              <th className="text-left px-4 py-3 text-xs font-medium text-gray-500">Created</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50">
            {isLoading ? (
              Array.from({ length: 8 }).map((_, i) => (
                <tr key={i}>
                  {Array.from({ length: 5 }).map((__, j) => (
                    <td key={j} className="px-4 py-3"><div className="skeleton h-4 rounded" /></td>
                  ))}
                </tr>
              ))
            ) : notifications.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-4 py-12 text-center text-gray-400 text-sm">
                  No notifications found
                </td>
              </tr>
            ) : (
              notifications.map((n) => (
                <tr key={n.notificationId} className="hover:bg-gray-50 transition-colors">
                  <td className="px-4 py-3 font-mono text-xs text-gray-500">{n.citizenNationalId}</td>
                  <td className="px-4 py-3 text-xs text-gray-600">{n.eventType.replace(/_/g, ' ')}</td>
                  <td className="px-4 py-3 text-xs text-dark max-w-xs truncate">{n.title}</td>
                  <td className="px-4 py-3">
                    <span className={`text-[10px] font-medium px-2 py-0.5 rounded-full ${STATUS_COLORS[n.status] ?? 'bg-gray-100'}`}>
                      {n.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-xs text-gray-400">{formatDateTime(n.createdAt)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-center gap-2 mt-4">
          <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}
            className="px-3 py-1.5 text-sm rounded-lg border border-gray-200 hover:bg-gray-50 disabled:opacity-40">
            Previous
          </button>
          <span className="px-3 py-1.5 text-sm text-gray-500">{page + 1} / {totalPages}</span>
          <button onClick={() => setPage(p => p + 1)} disabled={page >= totalPages - 1}
            className="px-3 py-1.5 text-sm rounded-lg border border-gray-200 hover:bg-gray-50 disabled:opacity-40">
            Next
          </button>
        </div>
      )}
    </div>
  )
}
