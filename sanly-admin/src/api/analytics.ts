import axios from 'axios'
import type { ApiResponse } from '../types'

const client = axios.create({
  baseURL: import.meta.env.VITE_GATEWAY_URL ?? 'http://localhost:8080',
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

// ── Types ─────────────────────────────────────────────────────────────────────

export interface TrendPoint { date: string; value: number }

export interface DailySnapshot {
  snapshotId: string
  snapshotDate: string
  totalCitizens: number
  activeCitizens: number
  deceasedCitizens: number
  newRegistrationsToday: number
  totalBusinesses: number
  activeBusinesses: number
  newBusinessesToday: number
  totalLicenses: number
  licensesIssuedToday: number
  totalDiplomas: number
  diplomasIssuedToday: number
  totalProperties: number
  transfersToday: number
  totalBenefitClaims: number
  activeClaimants: number
  totalCourtCases: number
  openCases: number
  totalCustomsDeclarations: number
  clearancesToday: number
  totalAppointments: number
  appointmentsToday: number
  noShowCount: number
  avgAppointmentRating: number
  totalBridgeExchanges: number
  exchangesToday: number
  totalAnomalyAlerts: number
  openAnomalyAlerts: number
}

export interface RegionalStat {
  statId: string
  statDate: string
  region: string
  citizenCount: number
  businessCount: number
  propertyCount: number
  appointmentCount: number
  benefitClaimCount: number
}

export interface ServiceUsageStat {
  statId: string
  statDate: string
  serviceName: string
  endpoint?: string
  requestCount: number
  avgResponseMs: number
  errorCount: number
  errorRate: number
}

export interface AnomalyTrend {
  trendId: string
  statDate: string
  institutionCode: string
  alertType: string
  alertCount: number
  resolvedCount: number
  avgResolutionHours: number
}

export interface ServiceHealthSummary { allHealthy: boolean; degradedServices: string[] }
export interface DashboardResponse {
  today: DailySnapshot
  trends: Record<string, TrendPoint[]>
  topRegions: RegionalStat[]
  serviceHealth: ServiceHealthSummary
}

export interface PopulationKpi {
  totalCitizens: number; activeCitizens: number; deceasedCitizens: number
  growthRate30d: number; malePercent: number; femalePercent: number
  topRegions: RegionalStat[]
}
export interface EconomyKpi {
  businessFormationRate30d: number; activeBusinesses: number; newBusinesses30d: number
  propertyTransfers30d: number; customsClearances30d: number
  activeBenefitClaimants: number; totalBenefitClaims: number
}
export interface ServicesKpi {
  topServices: ServiceUsageStat[]; avgAppointmentRating: number
  noShowRate: number; appointmentsToday: number; totalAppointments: number
}
export interface AntiCorruptionKpi {
  totalAnomalyAlerts: number; openAnomalyAlerts: number; resolutionRate: number
  recentTrends: AnomalyTrend[]; topFlaggedInstitutions: string[]
}

export interface ReportStatus {
  exportCode: string; status: string; reportType: string
  dateFrom: string; dateTo: string
  generatedAt: string; completedAt?: string; expiresAt?: string; failureReason?: string
}

// ── API calls ─────────────────────────────────────────────────────────────────

export const getDashboard = () =>
  client.get<ApiResponse<DashboardResponse>>('/api/v1/analytics/dashboard').then(r => r.data.data!)

export const getSnapshots = (from?: string, to?: string) =>
  client.get<ApiResponse<DailySnapshot[]>>('/api/v1/analytics/snapshots', { params: { from, to } }).then(r => r.data.data ?? [])

export const getTrend = (metric: string, days = 30) =>
  client.get<ApiResponse<TrendPoint[]>>(`/api/v1/analytics/trends/${metric}`, { params: { days } }).then(r => r.data.data ?? [])

export const getRegional = () =>
  client.get<ApiResponse<RegionalStat[]>>('/api/v1/analytics/regional').then(r => r.data.data ?? [])

export const getServiceUsage = (service?: string, from?: string, to?: string) =>
  client.get<ApiResponse<ServiceUsageStat[]>>('/api/v1/analytics/service-usage', { params: { service, from, to } }).then(r => r.data.data ?? [])

export const getAnomalyTrends = (from?: string, to?: string) =>
  client.get<ApiResponse<AnomalyTrend[]>>('/api/v1/analytics/anomaly-trends', { params: { from, to } }).then(r => r.data.data ?? [])

export const getPopulationKpi = () =>
  client.get<ApiResponse<PopulationKpi>>('/api/v1/analytics/kpi/population').then(r => r.data.data!)

export const getEconomyKpi = () =>
  client.get<ApiResponse<EconomyKpi>>('/api/v1/analytics/kpi/economy').then(r => r.data.data!)

export const getServicesKpi = () =>
  client.get<ApiResponse<ServicesKpi>>('/api/v1/analytics/kpi/services').then(r => r.data.data!)

export const getAntiCorruptionKpi = () =>
  client.get<ApiResponse<AntiCorruptionKpi>>('/api/v1/analytics/kpi/anti-corruption').then(r => r.data.data!)

export const generateReport = (reportType: string, dateFrom: string, dateTo: string) =>
  client.post<ApiResponse<ReportStatus>>('/api/v1/analytics/reports/generate', { reportType, dateFrom, dateTo }).then(r => r.data.data!)

export const listReports = () =>
  client.get<ApiResponse<ReportStatus[]>>('/api/v1/analytics/reports').then(r => r.data.data ?? [])

export const getReportStatus = (exportCode: string) =>
  client.get<ApiResponse<ReportStatus>>(`/api/v1/analytics/reports/${exportCode}/status`).then(r => r.data.data!)

export const downloadReport = async (exportCode: string): Promise<void> => {
  const res = await client.get(`/api/v1/analytics/reports/${exportCode}/download`, { responseType: 'blob' })
  const url = URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }))
  const a = document.createElement('a'); a.href = url; a.download = `${exportCode}.pdf`; a.click()
  URL.revokeObjectURL(url)
}

export const analyticsLogin = (username: string, password: string) =>
  client.post<ApiResponse<{ token: string; username: string }>>('/api/v1/analytics/auth/login', { username, password }).then(r => r.data.data!)
