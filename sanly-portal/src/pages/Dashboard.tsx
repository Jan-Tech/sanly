import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Car, Receipt, Briefcase, Stethoscope, ArrowRight, Building2 as BuildingIcon, Shield, GraduationCap, Home, HeartHandshake, Package, Scale, Truck, PiggyBank, FileSignature, CalendarDays, FileText, Activity } from 'lucide-react'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { t } from '../utils/i18n'
import { getCitizen } from '../api/registry'
import { getLicenses } from '../api/dmv'
import { getTaxpayer } from '../api/tax'
import { getBusinesses } from '../api/business'
import { getMedicalRecords } from '../api/medical'
import { getAccessLogs } from '../api/bridge'
import { getDiplomas } from '../api/education'
import { getPropertiesByOwner } from '../api/land'
import { getActiveClaims } from '../api/social'
import { getDeclarationsByDeclarant } from '../api/customs'
import { getFinesByCitizen } from '../api/court'
import { getVehiclesByOwner } from '../api/vehicle'
import { getPensionAccount } from '../api/pension'
import { getMySignatures } from '../api/signature'
import { getMyAppointments } from '../api/appointments'
import { getMyCertificates, getMyTrackedItems, type CertificateRecord, type TrackedItem } from '../api/documents'
import { getPendingConsents } from '../api/banking'
import Badge from '../components/ui/Badge'
import { SkeletonCard } from '../components/ui/Skeleton'
import { formatDateTime } from '../utils/date'

interface SummaryCardProps {
  icon: React.ReactNode
  label: string
  value: React.ReactNode
  to: string
  loading?: boolean
}

function SummaryCard({ icon, label, value, to, loading }: SummaryCardProps) {
  return (
    <Link to={to} className="card hover:shadow-card-hover transition-shadow group">
      <div className="flex items-start justify-between">
        <div className="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center text-primary">
          {icon}
        </div>
        <ArrowRight className="w-4 h-4 text-gray-300 group-hover:text-primary transition-colors" />
      </div>
      <div className="mt-4">
        {loading ? (
          <div className="space-y-2">
            <div className="skeleton h-6 w-24 rounded" />
            <div className="skeleton h-3 w-16 rounded" />
          </div>
        ) : (
          <>
            <div className="text-lg font-semibold text-dark">{value}</div>
            <p className="text-xs text-gray-400 mt-0.5">{label}</p>
          </>
        )}
      </div>
    </Link>
  )
}

export default function Dashboard() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()

  const { data: citizen, isLoading: citizenLoading } = useQuery({
    queryKey: ['citizen', nationalId],
    queryFn: () => getCitizen(nationalId!),
    enabled: !!nationalId,
    staleTime: 5 * 60 * 1000,
  })

  const { data: licenses, isLoading: licensesLoading } = useQuery({
    queryKey: ['licenses', nationalId],
    queryFn: () => getLicenses(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: taxpayer, isLoading: taxLoading } = useQuery({
    queryKey: ['taxpayer', nationalId],
    queryFn: () => getTaxpayer(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: businesses, isLoading: businessLoading } = useQuery({
    queryKey: ['businesses', nationalId],
    queryFn: () => getBusinesses(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: medicalRecords, isLoading: medicalLoading } = useQuery({
    queryKey: ['medical', nationalId],
    queryFn: () => getMedicalRecords(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: logs, isLoading: logsLoading } = useQuery({
    queryKey: ['access-logs', nationalId],
    queryFn: () => getAccessLogs(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: properties, isLoading: propertyLoading } = useQuery({
    queryKey: ['properties', nationalId],
    queryFn: () => getPropertiesByOwner(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: diplomas, isLoading: diplomasLoading } = useQuery({
    queryKey: ['diplomas', nationalId],
    queryFn: () => getDiplomas(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: activeClaims, isLoading: benefitsLoading } = useQuery({
    queryKey: ['active-claims', nationalId],
    queryFn: () => getActiveClaims(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: customsDeclarations, isLoading: customsLoading } = useQuery({
    queryKey: ['customs-declarations', nationalId],
    queryFn: () => getDeclarationsByDeclarant(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: ownedVehicles, isLoading: vehiclesLoading } = useQuery({
    queryKey: ['vehicles', nationalId],
    queryFn: () => getVehiclesByOwner(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: pensionAccount, isLoading: pensionLoading } = useQuery({
    queryKey: ['pension-account', nationalId],
    queryFn: () => getPensionAccount(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: aptPage, isLoading: aptLoading } = useQuery({
    queryKey: ['apt-count'],
    queryFn: () => getMyAppointments(0, 1),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: sigPage, isLoading: sigLoading } = useQuery({
    queryKey: ['my-signatures-count'],
    queryFn: () => getMySignatures(0, 1),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: certList, isLoading: certLoading } = useQuery({
    queryKey: ['my-certificates-count'],
    queryFn: getMyCertificates,
    enabled: !!nationalId,
    retry: false,
  })

  const { data: trackerList, isLoading: trackerLoading } = useQuery({
    queryKey: ['my-tracked-count'],
    queryFn: () => getMyTrackedItems(),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: pendingConsents, isLoading: consentLoading } = useQuery({
    queryKey: ['pending-consents-count'],
    queryFn: getPendingConsents,
    enabled: !!nationalId,
    retry: false,
    refetchInterval: 30_000,
  })

  const { data: courtFines, isLoading: courtLoading } = useQuery({
    queryKey: ['court-fines', nationalId],
    queryFn: () => getFinesByCitizen(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const pendingCourtFines = (courtFines ?? []).filter(f => ['OUTSTANDING', 'OVERDUE', 'PENDING_VERIFICATION', 'APPEALING'].includes(f.status))
  const overdueCourtFines = pendingCourtFines.filter(f => f.status === 'OVERDUE')

  const activeLicense = licenses?.find((l) => l.status === 'ACTIVE')
  const activeBusinesses = businesses?.filter((b) => b.status === 'ACTIVE') ?? []
  const recentLogs = (logs ?? []).slice(0, 5)

  const licenseValue = licensesLoading ? null : activeLicense
    ? <Badge label={t(lang, 'active')} status="ACTIVE" />
    : <span className="text-gray-400 text-sm">{t(lang, 'noLicense')}</span>

  const taxValue = taxLoading ? null : taxpayer
    ? <Badge label={t(lang, taxpayer.complianceStatus === 'COMPLIANT' ? 'compliant' : taxpayer.complianceStatus === 'NON_COMPLIANT' ? 'nonCompliant' : 'pending')} status={taxpayer.complianceStatus} />
    : <span className="text-gray-400 text-sm">{t(lang, 'notRegistered')}</span>

  return (
    <div className="space-y-6">
      {/* Citizen header */}
      <div className="card !p-5">
        {citizenLoading ? (
          <div className="flex items-center gap-4">
            <div className="skeleton w-14 h-14 rounded-full" />
            <div className="space-y-2">
              <div className="skeleton h-5 w-40 rounded" />
              <div className="skeleton h-3 w-24 rounded" />
            </div>
          </div>
        ) : citizen ? (
          <div className="flex items-center gap-4">
            <div className="w-14 h-14 rounded-full bg-primary/10 flex items-center justify-center text-primary text-xl font-bold shrink-0">
              {citizen.firstName[0]}
            </div>
            <div>
              <div className="flex items-center gap-2 flex-wrap">
                <h1 className="text-xl font-bold text-dark">
                  {t(lang, 'welcome')}, {citizen.firstName}
                </h1>
                <Badge label={t(lang, citizen.status === 'ACTIVE' ? 'active' : citizen.status === 'DECEASED' ? 'deceased' : 'inactive')} status={citizen.status} />
              </div>
              <p className="text-sm text-gray-400 mt-0.5 font-mono">NIN: {nationalId}</p>
            </div>
          </div>
        ) : null}
      </div>

      {/* Summary cards */}
      <div>
        <h2 className="section-title">{t(lang, 'summaryTitle')}</h2>
        <div className="grid grid-cols-2 lg:grid-cols-4 xl:grid-cols-16 gap-4">
          <SummaryCard
            icon={<Car className="w-5 h-5" />}
            label={t(lang, 'licenseStatus')}
            value={licenseValue}
            to="/license"
            loading={licensesLoading}
          />
          <SummaryCard
            icon={<Receipt className="w-5 h-5" />}
            label={t(lang, 'taxStatus')}
            value={taxValue}
            to="/tax"
            loading={taxLoading}
          />
          <SummaryCard
            icon={<Briefcase className="w-5 h-5" />}
            label={t(lang, 'businessCount')}
            value={businessLoading ? null : <span className="text-2xl font-bold">{activeBusinesses.length}</span>}
            to="/business"
            loading={businessLoading}
          />
          <SummaryCard
            icon={<Stethoscope className="w-5 h-5" />}
            label={t(lang, 'medicalCount')}
            value={medicalLoading ? null : <span className="text-2xl font-bold">{medicalRecords?.length ?? 0}</span>}
            to="/medical"
            loading={medicalLoading}
          />
          <SummaryCard
            icon={<GraduationCap className="w-5 h-5" />}
            label={t(lang, 'educationCount')}
            value={diplomasLoading ? null : <span className="text-2xl font-bold">{diplomas?.filter(d => d.status === 'VALID').length ?? 0}</span>}
            to="/education"
            loading={diplomasLoading}
          />
          <SummaryCard
            icon={<Home className="w-5 h-5" />}
            label={t(lang, 'propertyCount')}
            value={propertyLoading ? null : <span className="text-2xl font-bold">{properties?.length ?? 0}</span>}
            to="/property"
            loading={propertyLoading}
          />
          <SummaryCard
            icon={<HeartHandshake className="w-5 h-5" />}
            label={t(lang, 'benefitCount')}
            value={benefitsLoading ? null : <span className="text-2xl font-bold">{activeClaims?.length ?? 0}</span>}
            to="/benefits"
            loading={benefitsLoading}
          />
          <SummaryCard
            icon={<Package className="w-5 h-5" />}
            label={t(lang, 'customsCount')}
            value={customsLoading ? null : <span className="text-2xl font-bold">{customsDeclarations?.filter(d => ['SUBMITTED','UNDER_REVIEW','HELD'].includes(d.status)).length ?? 0}</span>}
            to="/customs"
            loading={customsLoading}
          />
          <SummaryCard
            icon={<Truck className="w-5 h-5" />}
            label={t(lang, 'vehicleCount')}
            value={vehiclesLoading ? null : <span className="text-2xl font-bold">{ownedVehicles?.filter(v => v.status === 'REGISTERED' || v.status === 'UNDER_TRANSFER').length ?? 0}</span>}
            to="/vehicles"
            loading={vehiclesLoading}
          />
          <SummaryCard
            icon={<Scale className="w-5 h-5" />}
            label={t(lang, 'courtCount')}
            value={courtLoading ? null : (
              <div className="flex items-center gap-1.5">
                <span className="text-2xl font-bold">{pendingCourtFines.length}</span>
                {overdueCourtFines.length > 0 && (
                  <span className="px-1.5 py-0.5 rounded-full text-[10px] font-bold bg-red-100 text-red-700">
                    {overdueCourtFines.length} overdue
                  </span>
                )}
              </div>
            )}
            to="/court"
            loading={courtLoading}
          />
          <SummaryCard
            icon={<PiggyBank className="w-5 h-5" />}
            label={t(lang, 'pensionCount')}
            value={pensionLoading ? null : (
              pensionAccount
                ? <Badge label={pensionAccount.status} status={pensionAccount.status === 'ELIGIBLE' || pensionAccount.status === 'PAYING' ? 'ACTIVE' : 'PENDING'} />
                : <span className="text-gray-400 text-sm">{t(lang, 'noPensionAccount').slice(0,10)}…</span>
            )}
            to="/pension"
            loading={pensionLoading}
          />
          <SummaryCard
            icon={<CalendarDays className="w-5 h-5" />}
            label={t(lang, 'appointmentCount')}
            value={aptLoading ? null : <span className="text-2xl font-bold">{
              (aptPage?.content ?? []).filter(a => ['BOOKED','CONFIRMED'].includes(a.status)).length
            }</span>}
            to="/appointments"
            loading={aptLoading}
          />
          <SummaryCard
            icon={<FileSignature className="w-5 h-5" />}
            label={t(lang, 'signatureCount')}
            value={sigLoading ? null : <span className="text-2xl font-bold">{sigPage?.totalElements ?? 0}</span>}
            to="/signatures"
            loading={sigLoading}
          />
          <SummaryCard
            icon={<FileText className="w-5 h-5" />}
            label={t(lang, 'documentCount')}
            value={certLoading ? null : <span className="text-2xl font-bold">{(certList ?? []).filter((c: CertificateRecord) => c.status === 'ACTIVE').length}</span>}
            to="/documents"
            loading={certLoading}
          />
          <SummaryCard
            icon={<Activity className="w-5 h-5" />}
            label={t(lang, 'trackerCount')}
            value={trackerLoading ? null : <span className="text-2xl font-bold">{(trackerList ?? []).filter(i => !i.isCompleted).length}</span>}
            to="/tracker"
            loading={trackerLoading}
          />
          <SummaryCard
            icon={<BuildingIcon className="w-5 h-5" />}
            label={t(lang, 'bankingCount')}
            value={consentLoading ? null : (
              <div className="flex items-center gap-1.5">
                <span className="text-2xl font-bold">{(pendingConsents ?? []).length}</span>
                {(pendingConsents ?? []).length > 0 && (
                  <span className="px-1.5 py-0.5 rounded-full text-[10px] font-bold bg-red-100 text-red-700">!</span>
                )}
              </div>
            )}
            to="/banking"
            loading={consentLoading}
          />
        </div>
      </div>

      {/* Recent access log */}
      <div className="card">
        <div className="flex items-center justify-between mb-4">
          <h2 className="section-title !mb-0 flex items-center gap-2">
            <Shield className="w-5 h-5 text-primary" />
            {t(lang, 'recentActivity')}
          </h2>
          <Link to="/access-log" className="text-sm text-primary hover:text-primary-dark font-medium flex items-center gap-1">
            {t(lang, 'viewAll')} <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        {logsLoading ? (
          <div className="space-y-3">
            {[1,2,3].map(i => <div key={i} className="skeleton h-12 rounded-lg" />)}
          </div>
        ) : recentLogs.length === 0 ? (
          <p className="text-sm text-gray-400 py-4 text-center">{t(lang, 'noData')}</p>
        ) : (
          <div className="divide-y divide-gray-50">
            {recentLogs.map((log) => (
              <div key={log.id} className="flex items-center gap-3 py-3">
                <div className="w-8 h-8 rounded-lg bg-gray-100 flex items-center justify-center shrink-0">
                  <Building2 className="w-4 h-4 text-gray-400" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-dark truncate">{log.requestingInstitutionCode}</p>
                  <p className="text-xs text-gray-400 truncate">{log.dataType}</p>
                </div>
                <div className="text-right shrink-0">
                  <Badge label={log.success ? t(lang, 'success') : t(lang, 'denied')} status={log.success ? 'ACTIVE' : 'FAIL'} size="sm" />
                  <p className="text-xs text-gray-400 mt-1">{formatDateTime(log.timestamp, lang)}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
