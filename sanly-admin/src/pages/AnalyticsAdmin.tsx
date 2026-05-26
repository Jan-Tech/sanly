import { useState } from 'react'
import { useQuery, useMutation } from '@tanstack/react-query'
import {
  LineChart, Line, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend
} from 'recharts'
import {
  Users, Briefcase, Activity, AlertTriangle, CalendarDays, ArrowLeftRight,
  Download, BarChart2, FileText, RefreshCw,
} from 'lucide-react'
import {
  getDashboard, getSnapshots, getTrend, getRegional, getServiceUsage,
  getPopulationKpi, getEconomyKpi, getServicesKpi, getAntiCorruptionKpi,
  generateReport, listReports, downloadReport,
  type DailySnapshot, type ReportStatus,
} from '../api/analytics'
import { formatDate } from '../utils/formatters'

const CHART_COLORS = ['#3b82f6','#10b981','#f59e0b','#ef4444','#8b5cf6','#06b6d4','#f97316','#6366f1']

const REPORT_TYPES = [
  'POPULATION_SUMMARY','BUSINESS_ACTIVITY','SERVICE_USAGE','ANTI_CORRUPTION_METRICS',
  'REGIONAL_BREAKDOWN','ECONOMIC_INDICATORS','PLATFORM_HEALTH','FULL_GOVERNMENT_REPORT','AUDIT_LOG',
]

function KpiCard({ icon, label, value, sub, color = 'text-blue-600' }: {
  icon: React.ReactNode; label: string; value: React.ReactNode; sub?: string; color?: string
}) {
  return (
    <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-4">
      <div className="flex items-center gap-2 mb-2">
        <span className={color}>{icon}</span>
        <p className="text-xs text-gray-500 font-medium">{label}</p>
      </div>
      <p className="text-2xl font-bold text-gray-900">{value}</p>
      {sub && <p className="text-xs text-gray-400 mt-0.5">{sub}</p>}
    </div>
  )
}

function ChartCard({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-4">
      <h3 className="text-sm font-semibold text-gray-700 mb-3">{title}</h3>
      {children}
    </div>
  )
}

// ── Tab: Dashboard ─────────────────────────────────────────────────────────────
function DashboardTab() {
  const { data: dash, isLoading } = useQuery({ queryKey: ['analytics-dashboard'], queryFn: getDashboard, refetchInterval: 60_000 })
  const { data: citTrend = [] } = useQuery({ queryKey: ['trend-citizens'], queryFn: () => getTrend('citizens', 30) })
  const { data: bizTrend = [] } = useQuery({ queryKey: ['trend-businesses'], queryFn: () => getTrend('businesses', 30) })
  const { data: exchTrend = [] } = useQuery({ queryKey: ['trend-exchanges'], queryFn: () => getTrend('exchanges', 30) })
  const { data: regional = [] } = useQuery({ queryKey: ['analytics-regional'], queryFn: getRegional })
  const { data: services = [] } = useQuery({ queryKey: ['analytics-services'], queryFn: () => getServiceUsage() })
  const { data: anticorr } = useQuery({ queryKey: ['analytics-anticorr'], queryFn: getAntiCorruptionKpi })

  const today = dash?.today
  if (isLoading || !today) return <p className="text-sm text-gray-400 p-4">Loading dashboard...</p>

  const topServices = services.slice(0, 8)
  const anomalyByType = (anticorr?.recentTrends ?? [])
    .reduce<Record<string, number>>((acc, t) => {
      acc[t.alertType] = (acc[t.alertType] ?? 0) + t.alertCount
      return acc
    }, {})

  return (
    <div className="space-y-5">
      {/* KPI row */}
      <div className="grid grid-cols-2 lg:grid-cols-6 gap-4">
        <KpiCard icon={<Users className="w-4 h-4" />} label="Total Citizens" value={today.totalCitizens.toLocaleString()} sub={`+${today.newRegistrationsToday} today`} color="text-blue-600" />
        <KpiCard icon={<Briefcase className="w-4 h-4" />} label="Active Businesses" value={today.activeBusinesses.toLocaleString()} sub={`+${today.newBusinessesToday} today`} color="text-green-600" />
        <KpiCard icon={<Activity className="w-4 h-4" />} label="Licenses Issued Today" value={today.licensesIssuedToday.toLocaleString()} color="text-indigo-600" />
        <KpiCard icon={<AlertTriangle className="w-4 h-4" />} label="Open Anomaly Alerts" value={today.openAnomalyAlerts.toLocaleString()} sub={`${today.totalAnomalyAlerts} total`} color={today.openAnomalyAlerts > 10 ? 'text-red-500' : 'text-yellow-500'} />
        <KpiCard icon={<CalendarDays className="w-4 h-4" />} label="Appointments Today" value={today.appointmentsToday.toLocaleString()} sub={`${today.avgAppointmentRating.toFixed(1)} avg rating`} color="text-purple-600" />
        <KpiCard icon={<ArrowLeftRight className="w-4 h-4" />} label="Bridge Exchanges Today" value={today.exchangesToday.toLocaleString()} sub={`${today.totalBridgeExchanges.toLocaleString()} total`} color="text-cyan-600" />
      </div>

      {/* Trend charts */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <ChartCard title="New Citizen Registrations (30 days)">
          <ResponsiveContainer width="100%" height={160}>
            <LineChart data={citTrend}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="date" tick={{ fontSize: 9 }} tickFormatter={d => d.slice(5)} />
              <YAxis tick={{ fontSize: 9 }} />
              <Tooltip labelFormatter={d => String(d)} />
              <Line type="monotone" dataKey="value" stroke="#3b82f6" strokeWidth={2} dot={false} name="Registrations" />
            </LineChart>
          </ResponsiveContainer>
        </ChartCard>
        <ChartCard title="New Business Registrations (30 days)">
          <ResponsiveContainer width="100%" height={160}>
            <LineChart data={bizTrend}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="date" tick={{ fontSize: 9 }} tickFormatter={d => d.slice(5)} />
              <YAxis tick={{ fontSize: 9 }} />
              <Tooltip />
              <Line type="monotone" dataKey="value" stroke="#10b981" strokeWidth={2} dot={false} name="Businesses" />
            </LineChart>
          </ResponsiveContainer>
        </ChartCard>
        <ChartCard title="Bridge Exchange Volume (30 days)">
          <ResponsiveContainer width="100%" height={160}>
            <LineChart data={exchTrend}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="date" tick={{ fontSize: 9 }} tickFormatter={d => d.slice(5)} />
              <YAxis tick={{ fontSize: 9 }} />
              <Tooltip />
              <Line type="monotone" dataKey="value" stroke="#06b6d4" strokeWidth={2} dot={false} name="Exchanges" />
            </LineChart>
          </ResponsiveContainer>
        </ChartCard>
      </div>

      {/* Regional + Service usage */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <ChartCard title="Citizens by Region (latest)">
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={regional.slice(0, 10)} layout="vertical">
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis type="number" tick={{ fontSize: 9 }} />
              <YAxis type="category" dataKey="region" tick={{ fontSize: 9 }} width={90} />
              <Tooltip />
              <Bar dataKey="citizenCount" fill="#3b82f6" name="Citizens" />
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>
        <ChartCard title="Service Usage (top 8 by request count)">
          {topServices.length > 0 ? (
            <ResponsiveContainer width="100%" height={200}>
              <PieChart>
                <Pie data={topServices} dataKey="requestCount" nameKey="serviceName" outerRadius={75} label={({ serviceName, percent }) => `${serviceName?.slice(0,8)} ${(percent * 100).toFixed(0)}%`} labelLine={false} fontSize={8}>
                  {topServices.map((_, i) => <Cell key={i} fill={CHART_COLORS[i % CHART_COLORS.length]} />)}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          ) : <p className="text-xs text-gray-400 py-8 text-center">No service usage data yet</p>}
        </ChartCard>
      </div>

      {/* Anti-corruption */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <ChartCard title="Anomaly Alerts by Type (30 days)">
          {Object.keys(anomalyByType).length > 0 ? (
            <ResponsiveContainer width="100%" height={180}>
              <BarChart data={Object.entries(anomalyByType).map(([type, count]) => ({ type, count }))}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="type" tick={{ fontSize: 9 }} />
                <YAxis tick={{ fontSize: 9 }} />
                <Tooltip />
                <Bar dataKey="count" fill="#ef4444" name="Alert Count" />
              </BarChart>
            </ResponsiveContainer>
          ) : <p className="text-xs text-gray-400 py-8 text-center">No anomaly data</p>}
        </ChartCard>
        <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-4">
          <h3 className="text-sm font-semibold text-gray-700 mb-3">Top Flagged Institutions</h3>
          {(anticorr?.topFlaggedInstitutions ?? []).length === 0 ? (
            <p className="text-xs text-gray-400">No flagged institutions</p>
          ) : (
            <ul className="space-y-2">
              {anticorr!.topFlaggedInstitutions.map((inst, i) => (
                <li key={i} className="flex items-center gap-2 text-sm">
                  <span className={`w-5 h-5 rounded-full flex items-center justify-center text-white text-xs font-bold ${i === 0 ? 'bg-red-500' : i === 1 ? 'bg-orange-400' : 'bg-yellow-400'}`}>{i + 1}</span>
                  <span className="text-gray-800">{inst}</span>
                </li>
              ))}
            </ul>
          )}
          <div className="mt-4 pt-3 border-t border-gray-100">
            <p className="text-xs text-gray-500">Resolution rate: <strong className="text-gray-800">{anticorr?.resolutionRate?.toFixed(1) ?? '0.0'}%</strong></p>
          </div>
        </div>
      </div>
    </div>
  )
}

// ── Tab: KPIs ─────────────────────────────────────────────────────────────────
function KpiTab() {
  const [activeKpi, setActiveKpi] = useState<'population' | 'economy' | 'services' | 'anti-corruption'>('population')
  const { data: pop } = useQuery({ queryKey: ['kpi-population'], queryFn: getPopulationKpi, enabled: activeKpi === 'population' })
  const { data: eco } = useQuery({ queryKey: ['kpi-economy'], queryFn: getEconomyKpi, enabled: activeKpi === 'economy' })
  const { data: svc } = useQuery({ queryKey: ['kpi-services'], queryFn: getServicesKpi, enabled: activeKpi === 'services' })
  const { data: ac } = useQuery({ queryKey: ['kpi-anticorr'], queryFn: getAntiCorruptionKpi, enabled: activeKpi === 'anti-corruption' })

  return (
    <div className="space-y-4">
      <div className="flex gap-2 flex-wrap">
        {(['population','economy','services','anti-corruption'] as const).map(tab => (
          <button key={tab} onClick={() => setActiveKpi(tab)}
            className={`px-3 py-1.5 text-sm font-medium rounded-lg capitalize transition-colors ${activeKpi === tab ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>
            {tab.replace('-',' ')}
          </button>
        ))}
      </div>

      {activeKpi === 'population' && pop && (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <KpiCard icon={<Users className="w-4 h-4" />} label="Total Citizens" value={pop.totalCitizens.toLocaleString()} />
          <KpiCard icon={<Users className="w-4 h-4" />} label="Active Citizens" value={pop.activeCitizens.toLocaleString()} />
          <KpiCard icon={<Activity className="w-4 h-4" />} label="30-Day Growth" value={`${pop.growthRate30d.toFixed(2)}%`} color="text-green-600" />
          <KpiCard icon={<Users className="w-4 h-4" />} label="Deceased" value={pop.deceasedCitizens.toLocaleString()} color="text-gray-500" />
        </div>
      )}

      {activeKpi === 'economy' && eco && (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <KpiCard icon={<Briefcase className="w-4 h-4" />} label="Active Businesses" value={eco.activeBusinesses.toLocaleString()} />
          <KpiCard icon={<Briefcase className="w-4 h-4" />} label="New Businesses (30d)" value={eco.newBusinesses30d.toLocaleString()} color="text-green-600" />
          <KpiCard icon={<Activity className="w-4 h-4" />} label="Property Transfers (30d)" value={eco.propertyTransfers30d.toLocaleString()} />
          <KpiCard icon={<Activity className="w-4 h-4" />} label="Customs Clearances (30d)" value={eco.customsClearances30d.toLocaleString()} />
        </div>
      )}

      {activeKpi === 'services' && svc && (
        <div className="space-y-4">
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            <KpiCard icon={<CalendarDays className="w-4 h-4" />} label="Avg Appointment Rating" value={`${svc.avgAppointmentRating.toFixed(1)} / 5`} color="text-yellow-500" />
            <KpiCard icon={<CalendarDays className="w-4 h-4" />} label="No-Show Rate" value={`${svc.noShowRate.toFixed(1)}%`} color={svc.noShowRate > 20 ? 'text-red-500' : 'text-green-600'} />
            <KpiCard icon={<CalendarDays className="w-4 h-4" />} label="Appointments Today" value={svc.appointmentsToday.toLocaleString()} />
            <KpiCard icon={<CalendarDays className="w-4 h-4" />} label="Total Appointments" value={svc.totalAppointments.toLocaleString()} />
          </div>
          <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-4">
            <h3 className="text-sm font-semibold text-gray-700 mb-3">Top Services by Request Count</h3>
            <table className="w-full text-sm">
              <thead><tr className="text-left text-gray-500 border-b text-xs"><th className="pb-2">Service</th><th className="pb-2">Requests</th><th className="pb-2">Avg ms</th><th className="pb-2">Error Rate</th></tr></thead>
              <tbody>
                {svc.topServices.map(s => (
                  <tr key={s.statId} className="border-b border-gray-50">
                    <td className="py-1.5 font-medium">{s.serviceName}</td>
                    <td className="py-1.5">{s.requestCount.toLocaleString()}</td>
                    <td className="py-1.5">{s.avgResponseMs.toFixed(0)}</td>
                    <td className="py-1.5"><span className={`text-xs ${s.errorRate > 5 ? 'text-red-600 font-bold' : 'text-gray-600'}`}>{s.errorRate.toFixed(1)}%</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {activeKpi === 'anti-corruption' && ac && (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <KpiCard icon={<AlertTriangle className="w-4 h-4" />} label="Total Alerts" value={ac.totalAnomalyAlerts.toLocaleString()} color="text-red-500" />
          <KpiCard icon={<AlertTriangle className="w-4 h-4" />} label="Open Alerts" value={ac.openAnomalyAlerts.toLocaleString()} color="text-red-500" />
          <KpiCard icon={<Activity className="w-4 h-4" />} label="Resolution Rate" value={`${ac.resolutionRate.toFixed(1)}%`} color="text-green-600" />
          <KpiCard icon={<Activity className="w-4 h-4" />} label="Flagged Institutions" value={ac.topFlaggedInstitutions.length} />
        </div>
      )}
    </div>
  )
}

// ── Tab: Reports ──────────────────────────────────────────────────────────────
function ReportsTab() {
  const [reportType, setReportType] = useState(REPORT_TYPES[0])
  const [dateFrom, setDateFrom] = useState(() => {
    const d = new Date(); d.setDate(d.getDate() - 30); return d.toISOString().split('T')[0]
  })
  const [dateTo, setDateTo] = useState(() => new Date().toISOString().split('T')[0])
  const [generatingCode, setGeneratingCode] = useState<string | null>(null)

  const { data: reports = [], refetch } = useQuery({ queryKey: ['analytics-reports'], queryFn: listReports, refetchInterval: 10_000 })

  const genMutation = useMutation({
    mutationFn: () => generateReport(reportType, dateFrom, dateTo),
    onSuccess: (r) => { setGeneratingCode(r.exportCode); refetch() },
  })

  const statusBadge = (status: string) => {
    if (status === 'READY') return 'bg-green-100 text-green-700'
    if (status === 'GENERATING') return 'bg-yellow-100 text-yellow-700'
    return 'bg-red-100 text-red-700'
  }

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-5">
        <h3 className="font-semibold text-gray-800 mb-4 flex items-center gap-2"><FileText className="w-4 h-4" /> Generate Report</h3>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">Report Type</label>
            <select value={reportType} onChange={e => setReportType(e.target.value)}
              className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
              {REPORT_TYPES.map(t => <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">From</label>
            <input type="date" value={dateFrom} onChange={e => setDateFrom(e.target.value)}
              className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">To</label>
            <input type="date" value={dateTo} onChange={e => setDateTo(e.target.value)}
              className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
          </div>
        </div>
        <button onClick={() => genMutation.mutate()} disabled={genMutation.isPending}
          className="mt-4 px-5 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 disabled:opacity-50 flex items-center gap-2">
          <FileText className="w-4 h-4" />
          {genMutation.isPending ? 'Generating...' : 'Generate Report'}
        </button>
        {generatingCode && <p className="text-xs text-green-700 mt-2">Report {generatingCode} is being generated…</p>}
      </div>

      <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-5">
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-semibold text-gray-800 flex items-center gap-2"><BarChart2 className="w-4 h-4" /> Generated Reports</h3>
          <button onClick={() => refetch()} className="text-xs text-gray-500 hover:text-gray-700 flex items-center gap-1"><RefreshCw className="w-3 h-3" /> Refresh</button>
        </div>
        {reports.length === 0 ? <p className="text-sm text-gray-400">No reports generated yet.</p> : (
          <table className="w-full text-sm">
            <thead><tr className="text-left text-xs text-gray-500 border-b"><th className="pb-2">Code</th><th className="pb-2">Type</th><th className="pb-2">Period</th><th className="pb-2">Generated</th><th className="pb-2">Status</th><th className="pb-2"></th></tr></thead>
            <tbody>
              {reports.map(r => (
                <tr key={r.exportCode} className="border-b border-gray-50">
                  <td className="py-2 font-mono text-xs text-blue-700">{r.exportCode}</td>
                  <td className="py-2 text-xs">{r.reportType?.replace(/_/g, ' ')}</td>
                  <td className="py-2 text-xs text-gray-500">{r.dateFrom} — {r.dateTo}</td>
                  <td className="py-2 text-xs text-gray-500">{r.generatedAt ? formatDate(r.generatedAt) : '—'}</td>
                  <td className="py-2"><span className={`text-xs px-2 py-0.5 rounded-full font-medium ${statusBadge(r.status)}`}>{r.status}</span></td>
                  <td className="py-2">
                    {r.status === 'READY' && (
                      <button onClick={() => downloadReport(r.exportCode)}
                        className="flex items-center gap-1 text-xs text-blue-600 hover:text-blue-700">
                        <Download className="w-3 h-3" /> PDF
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}

// ── Tab: Historical ───────────────────────────────────────────────────────────
function HistoricalTab() {
  const [dateFrom, setDateFrom] = useState(() => {
    const d = new Date(); d.setDate(d.getDate() - 30); return d.toISOString().split('T')[0]
  })
  const [dateTo, setDateTo] = useState(() => new Date().toISOString().split('T')[0])

  const { data: snapshots = [] } = useQuery({
    queryKey: ['analytics-snapshots', dateFrom, dateTo],
    queryFn: () => getSnapshots(dateFrom, dateTo),
  })

  return (
    <div className="space-y-4">
      <div className="flex gap-3 flex-wrap">
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">From</label>
          <input type="date" value={dateFrom} onChange={e => setDateFrom(e.target.value)}
            className="border border-gray-200 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
        </div>
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">To</label>
          <input type="date" value={dateTo} onChange={e => setDateTo(e.target.value)}
            className="border border-gray-200 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
        </div>
      </div>
      <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-x-auto">
        <table className="w-full text-xs">
          <thead className="bg-gray-50">
            <tr className="text-left text-gray-500">
              <th className="px-3 py-2">Date</th>
              <th className="px-3 py-2">Citizens</th>
              <th className="px-3 py-2">+New</th>
              <th className="px-3 py-2">Businesses</th>
              <th className="px-3 py-2">Exchanges</th>
              <th className="px-3 py-2">Appointments</th>
              <th className="px-3 py-2">Anomalies</th>
              <th className="px-3 py-2">Open Cases</th>
            </tr>
          </thead>
          <tbody>
            {snapshots.map(s => (
              <tr key={s.snapshotId} className="border-t border-gray-50 hover:bg-gray-50">
                <td className="px-3 py-2 font-medium">{s.snapshotDate}</td>
                <td className="px-3 py-2">{s.totalCitizens.toLocaleString()}</td>
                <td className="px-3 py-2 text-green-600">+{s.newRegistrationsToday}</td>
                <td className="px-3 py-2">{s.activeBusinesses.toLocaleString()}</td>
                <td className="px-3 py-2">{s.exchangesToday.toLocaleString()}</td>
                <td className="px-3 py-2">{s.appointmentsToday}</td>
                <td className="px-3 py-2 text-red-600">{s.openAnomalyAlerts}</td>
                <td className="px-3 py-2">{s.openCases.toLocaleString()}</td>
              </tr>
            ))}
            {snapshots.length === 0 && (
              <tr><td colSpan={8} className="px-3 py-6 text-center text-gray-400">No data for this period</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}

// ── Main Page ─────────────────────────────────────────────────────────────────
export default function AnalyticsAdmin() {
  const [tab, setTab] = useState<'dashboard' | 'kpi' | 'historical' | 'reports'>('dashboard')

  const tabs = [
    { id: 'dashboard' as const, label: 'Dashboard' },
    { id: 'kpi' as const, label: 'KPIs' },
    { id: 'historical' as const, label: 'Historical Data' },
    { id: 'reports' as const, label: 'Reports' },
  ]

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-3">
        <BarChart2 className="w-6 h-6 text-primary" />
        <h1 className="text-xl font-bold text-gray-900">Government Analytics</h1>
      </div>

      <div className="flex gap-1 border-b border-gray-200">
        {tabs.map(t => (
          <button key={t.id} onClick={() => setTab(t.id)}
            className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors -mb-px ${
              tab === t.id ? 'border-primary text-primary' : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}>
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'dashboard' && <DashboardTab />}
      {tab === 'kpi' && <KpiTab />}
      {tab === 'historical' && <HistoricalTab />}
      {tab === 'reports' && <ReportsTab />}
    </div>
  )
}
