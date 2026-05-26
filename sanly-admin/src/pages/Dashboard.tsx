import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { AlertTriangle, ArrowRight, Users, Activity, Building2, Bell, XCircle } from 'lucide-react'
import { checkAllServices } from '../api/health'
import { getAnomalySummary, getAnomalies, getExchangeLogs, getInstitutions } from '../api/bridge'
import { getCitizens } from '../api/registry'
import { getNotificationStats } from '../api/notifications'
import Badge from '../components/ui/Badge'
import ExchangeVolumeChart from '../components/charts/ExchangeVolumeChart'
import { formatDateTime, relativeTime, groupExchangesByHour } from '../utils/formatters'
import type { ServiceHealth } from '../types'

function ServiceCard({ s }: { s: ServiceHealth }) {
  const slow = s.healthy && s.responseTimeMs > 2000
  return (
    <div className={`card !p-3 flex items-center gap-2.5 ${!s.healthy ? 'border-red-200 bg-red-50/30' : ''}`}>
      <div className={`w-2 h-2 rounded-full shrink-0 ${s.healthy ? (slow ? 'bg-amber-400' : 'bg-green-500') : 'bg-red-500'}`} />
      <div className="flex-1 min-w-0">
        <p className="text-xs font-semibold text-dark truncate">{s.name}</p>
        <p className="text-[10px] text-gray-400">:{s.port}</p>
      </div>
      {s.healthy && s.responseTimeMs > 0 && (
        <span className={`text-[10px] font-mono ${slow ? 'text-amber-600' : 'text-gray-400'}`}>
          {s.responseTimeMs}ms
        </span>
      )}
      {!s.healthy && <span className="text-[10px] text-red-500 font-medium">Down</span>}
    </div>
  )
}

function MetricCard({ icon: Icon, label, value, to, loading }: {
  icon: React.ElementType; label: string; value: React.ReactNode; to: string; loading?: boolean
}) {
  return (
    <Link to={to} className="card hover:shadow-card-hover transition-shadow group">
      <div className="flex items-center justify-between mb-3">
        <div className="w-9 h-9 rounded-xl bg-primary/10 flex items-center justify-center text-primary">
          <Icon className="w-4.5 h-4.5" />
        </div>
        <ArrowRight className="w-4 h-4 text-gray-300 group-hover:text-primary transition-colors" />
      </div>
      {loading ? (
        <div className="space-y-1.5">
          <div className="skeleton h-7 w-16 rounded" />
          <div className="skeleton h-3 w-24 rounded" />
        </div>
      ) : (
        <>
          <p className="text-2xl font-bold text-dark">{value}</p>
          <p className="text-xs text-gray-400 mt-0.5">{label}</p>
        </>
      )}
    </Link>
  )
}

export default function Dashboard() {
  const { data: health, isLoading: healthLoading } = useQuery({
    queryKey: ['health'], queryFn: checkAllServices, refetchInterval: 30_000,
  })

  const { data: anomalySummary } = useQuery({
    queryKey: ['anomaly-summary'], queryFn: getAnomalySummary, refetchInterval: 30_000,
  })

  const { data: topAnomalies, isLoading: anomalyLoading } = useQuery({
    queryKey: ['anomalies-top'],
    queryFn: () => getAnomalies({ status: 'OPEN', size: 5 }),
    refetchInterval: 30_000,
  })

  const { data: institutions } = useQuery({
    queryKey: ['institutions'], queryFn: getInstitutions, refetchInterval: 60_000,
  })

  const { data: citizensPage } = useQuery({
    queryKey: ['citizens-count'], queryFn: () => getCitizens({ size: 1 }), staleTime: 60_000,
  })

  const now = new Date()
  const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate()).toISOString()

  const { data: exchangesPage } = useQuery({
    queryKey: ['exchanges-today'],
    queryFn: () => getExchangeLogs({ size: 500, from: todayStart }),
    refetchInterval: 30_000,
  })

  const { data: recentCitizens, isLoading: citizensLoading } = useQuery({
    queryKey: ['citizens-recent'], queryFn: () => getCitizens({ size: 10 }), staleTime: 30_000,
  })

  const { data: notifStats } = useQuery({
    queryKey: ['notification-stats'], queryFn: getNotificationStats, refetchInterval: 60_000,
  })

  const hourlyData = groupExchangesByHour(exchangesPage?.content ?? [])
  const activeInstitutions = (institutions ?? []).filter((i) => i.status === 'ACTIVE').length

  return (
    <div className="space-y-5">
      {/* Service health */}
      <div>
        <div className="flex items-center gap-2 mb-3">
          <p className="section-title !mb-0">Platform Health</p>
          <span className="text-xs text-gray-400">refreshes every 30s</span>
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-8 gap-2">
          {healthLoading
            ? Array.from({ length: 8 }).map((_, i) => <div key={i} className="skeleton h-14 rounded-xl" />)
            : (health ?? []).map((s) => <ServiceCard key={s.name} s={s} />)
          }
        </div>
      </div>

      {/* Metrics */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard icon={Users} label="Total citizens" value={citizensPage?.totalElements ?? '—'} to="/citizens" loading={!citizensPage} />
        <MetricCard icon={Activity} label="Exchanges today" value={exchangesPage?.totalElements ?? '—'} to="/audit" loading={!exchangesPage} />
        <MetricCard icon={AlertTriangle} label="Open anomaly alerts" value={anomalySummary?.totalOpen ?? '—'} to="/anomalies" loading={!anomalySummary} />
        <MetricCard icon={Building2} label="Active institutions" value={activeInstitutions || '—'} to="/institutions" loading={!institutions} />
      </div>

      {/* Notification Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard icon={Bell} label="Notifications sent (24h)" value={notifStats?.totalSentToday ?? '—'} to="/notifications" loading={!notifStats} />
        <MetricCard icon={XCircle} label="Failed deliveries (24h)" value={notifStats?.totalFailedToday ?? '—'} to="/notifications" loading={!notifStats} />
        <MetricCard icon={Bell} label="Total unread (all citizens)" value={notifStats?.totalUnread ?? '—'} to="/notifications" loading={!notifStats} />
        <div className="card">
          <p className="text-xs text-gray-400 mb-2">Top event type (24h)</p>
          {notifStats ? (
            Object.entries(notifStats.byEventType ?? {}).length > 0 ? (
              <div className="space-y-1">
                {Object.entries(notifStats.byEventType)
                  .sort((a, b) => b[1] - a[1])
                  .slice(0, 3)
                  .map(([k, v]) => (
                    <div key={k} className="flex justify-between text-xs">
                      <span className="text-gray-600 truncate max-w-[60%]">{k.replace(/_/g, ' ').toLowerCase()}</span>
                      <span className="font-semibold text-dark">{v}</span>
                    </div>
                  ))}
              </div>
            ) : <p className="text-sm text-gray-400">No data</p>
          ) : <div className="skeleton h-16 rounded-lg" />}
        </div>
      </div>

      {/* Row 3: Anomalies + Chart */}
      <div className="grid lg:grid-cols-5 gap-5">
        {/* Anomaly alerts widget */}
        <div className="lg:col-span-2 card">
          <div className="flex items-center justify-between mb-4">
            <p className="section-title !mb-0 flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 text-red-500" />
              Open Alerts
            </p>
            <Link to="/anomalies" className="text-xs text-primary hover:underline flex items-center gap-1">
              View all <ArrowRight className="w-3 h-3" />
            </Link>
          </div>
          {anomalyLoading ? (
            <div className="space-y-2">{Array.from({ length: 3 }).map((_, i) => <div key={i} className="skeleton h-12 rounded-lg" />)}</div>
          ) : !topAnomalies?.content.length ? (
            <p className="text-sm text-gray-400 text-center py-6">No open alerts</p>
          ) : (
            <div className="space-y-2">
              {topAnomalies.content.map((a) => (
                <div key={a.alertId} className="flex items-start gap-2.5 p-2.5 rounded-lg bg-gray-50 hover:bg-gray-100 transition-colors">
                  <Badge label={a.severity} status={a.severity} size="sm" />
                  <div className="flex-1 min-w-0">
                    <p className="text-xs font-medium text-dark truncate">{a.alertType.replace(/_/g, ' ')}</p>
                    <p className="text-[10px] text-gray-400 truncate">{a.institutionCode} · {relativeTime(a.detectedAt)}</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Exchange volume chart */}
        <div className="lg:col-span-3 card">
          <p className="section-title">Exchange Volume — Last 24h</p>
          <ExchangeVolumeChart data={hourlyData} loading={!exchangesPage} />
        </div>
      </div>

      {/* Recent citizens */}
      <div className="card">
        <div className="flex items-center justify-between mb-4">
          <p className="section-title !mb-0">Recent Citizen Registrations</p>
          <Link to="/citizens" className="text-xs text-primary hover:underline flex items-center gap-1">
            All citizens <ArrowRight className="w-3 h-3" />
          </Link>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-100">
                <th className="text-left px-3 py-2 text-xs text-gray-400 font-medium uppercase tracking-wide">Name</th>
                <th className="text-left px-3 py-2 text-xs text-gray-400 font-medium uppercase tracking-wide">TM-NIN</th>
                <th className="text-left px-3 py-2 text-xs text-gray-400 font-medium uppercase tracking-wide">Status</th>
                <th className="text-left px-3 py-2 text-xs text-gray-400 font-medium uppercase tracking-wide">Registered</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {citizensLoading
                ? Array.from({ length: 5 }).map((_, i) => (
                  <tr key={i}><td colSpan={4} className="px-3 py-2.5"><div className="skeleton h-3.5 rounded w-full" /></td></tr>
                ))
                : (recentCitizens?.content ?? []).map((c) => (
                  <tr key={c.nationalId} className="hover:bg-gray-50/50">
                    <td className="px-3 py-2.5 font-medium text-dark">
                      <Link to={`/citizens/${c.nationalId}`} className="hover:text-primary">
                        {c.firstName} {c.lastName}
                      </Link>
                    </td>
                    <td className="px-3 py-2.5 font-mono text-xs text-gray-500">{c.nationalId}</td>
                    <td className="px-3 py-2.5"><Badge label={c.status} status={c.status} size="sm" /></td>
                    <td className="px-3 py-2.5 text-gray-400 text-xs">{formatDateTime(c.createdAt)}</td>
                  </tr>
                ))
              }
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
