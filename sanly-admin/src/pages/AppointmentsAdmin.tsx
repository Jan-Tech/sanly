import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { CalendarDays, CheckCircle2, XCircle, Clock, Star, TrendingUp } from 'lucide-react'
import { getAdminAllAppointments, getAdminStats, type Appointment } from '../api/appointments'
import Badge from '../components/ui/Badge'
import DataTable from '../components/ui/DataTable'
import { formatDate, formatDateTime } from '../utils/formatters'

const STATUS_OPTS = ['', 'BOOKED', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'NO_SHOW']

function statusBadge(s: string) {
  if (s === 'BOOKED' || s === 'CONFIRMED') return 'ACTIVE'
  if (s === 'COMPLETED') return 'COMPLIANT'
  if (s === 'CANCELLED') return 'SUSPENDED'
  return 'PENDING'
}

function pct(n: number) { return `${(n * 100).toFixed(1)}%` }

export default function AppointmentsAdmin() {
  const [page, setPage] = useState(0)
  const [filterStatus, setFilterStatus] = useState('')

  const { data: stats } = useQuery({
    queryKey: ['apt-admin-stats'],
    queryFn: getAdminStats,
    refetchInterval: 30_000,
  })

  const { data: aptPage, isLoading } = useQuery({
    queryKey: ['apt-admin-all', page, filterStatus],
    queryFn: () => getAdminAllAppointments({ page, size: 20, status: filterStatus || undefined }),
  })

  const apts = aptPage?.content ?? []

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-3">
        <CalendarDays className="w-6 h-6 text-primary" />
        <h1 className="page-title">Appointments</h1>
      </div>

      {/* Stats */}
      {stats && (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {[
            { label: 'Total Booked', value: stats.totalBooked, icon: <CalendarDays className="w-5 h-5 text-primary" /> },
            { label: 'Completed', value: `${stats.totalCompleted} (${pct(stats.completionRate)})`, icon: <CheckCircle2 className="w-5 h-5 text-green-500" /> },
            { label: 'No-Show Rate', value: pct(stats.noShowRate), icon: <XCircle className="w-5 h-5 text-red-500" /> },
            { label: 'Avg Rating', value: stats.averageRating > 0 ? `${stats.averageRating.toFixed(1)} ★` : '—', icon: <Star className="w-5 h-5 text-yellow-400" /> },
          ].map(({ label, value, icon }) => (
            <div key={label} className="card !p-4">
              <div className="flex items-center gap-2 mb-1">{icon}<p className="text-xs text-gray-400 uppercase tracking-wide">{label}</p></div>
              <p className="text-xl font-bold text-dark">{value}</p>
            </div>
          ))}
        </div>
      )}

      {/* Filter */}
      <div className="card !p-3 flex gap-3">
        <select className="input !py-1.5 !text-sm w-auto" value={filterStatus} onChange={e => { setFilterStatus(e.target.value); setPage(0) }}>
          {STATUS_OPTS.map(s => <option key={s} value={s}>{s || 'All Statuses'}</option>)}
        </select>
      </div>

      {/* Table */}
      <div className="card">
        <p className="section-title">All Appointments</p>
        {isLoading ? (
          <div className="space-y-3">{[1,2,3].map(i => <div key={i} className="skeleton h-10 rounded" />)}</div>
        ) : (
          <>
            <DataTable<Appointment>
              columns={[
                { key: 'appointmentCode', header: 'Code', className: 'font-mono text-xs' },
                { key: 'citizenNationalId', header: 'NIN', className: 'font-mono text-xs' },
                { key: 'officeName', header: 'Office', render: (a) => <span className="text-xs truncate max-w-[140px] block">{a.officeName}</span> },
                { key: 'serviceName', header: 'Service', render: (a) => <span className="text-xs truncate max-w-[120px] block">{a.serviceName}</span> },
                { key: 'appointmentDate', header: 'Date', render: (a) => <span className="text-xs text-gray-500">{formatDate(a.appointmentDate)} {a.slotTime?.slice(0,5)}</span> },
                { key: 'status', header: 'Status', render: (a) => <Badge label={a.status} status={statusBadge(a.status)} size="sm" /> },
                { key: 'citizenRating', header: 'Rating', render: (a) => a.citizenRating ? <span className="text-xs">{a.citizenRating} ★</span> : <span className="text-xs text-gray-300">—</span> },
              ]}
              data={apts}
              keyExtractor={(a) => a.appointmentId}
            />
            {aptPage && aptPage.totalPages > 1 && (
              <div className="flex justify-center gap-2 mt-4">
                <button className="btn-secondary text-sm" disabled={page === 0} onClick={() => setPage(p => p - 1)}>Previous</button>
                <span className="text-sm text-gray-500 self-center">Page {page + 1} of {aptPage.totalPages}</span>
                <button className="btn-secondary text-sm" disabled={aptPage.last} onClick={() => setPage(p => p + 1)}>Next</button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  )
}
