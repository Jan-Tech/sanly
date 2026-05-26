import { useQuery } from '@tanstack/react-query'
import { Car, AlertCircle, CheckCircle } from 'lucide-react'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { t } from '../utils/i18n'
import { getLicenses } from '../api/dmv'
import { getMedicalRecords } from '../api/medical'
import type { DrivingLicense } from '../types'
import Badge from '../components/ui/Badge'
import ServiceUnavailable from '../components/ui/ServiceUnavailable'
import EmptyState from '../components/ui/EmptyState'
import { SkeletonCard } from '../components/ui/Skeleton'
import { formatDate, isExpired } from '../utils/date'

function LicensePhysicalCard({ license }: { license: DrivingLicense }) {
  const expired = license.status === 'EXPIRED' || isExpired(license.expiryDate)
  return (
    <div className={`relative rounded-2xl overflow-hidden p-6 text-white min-h-48 ${expired ? 'bg-gray-700' : 'bg-gradient-to-br from-dark via-dark/90 to-primary'}`}>
      {/* Pattern */}
      <div className="absolute inset-0 opacity-5">
        <div className="absolute top-4 right-4 w-32 h-32 rounded-full border-2 border-white" />
        <div className="absolute top-8 right-8 w-20 h-20 rounded-full border border-white" />
      </div>

      <div className="relative z-10">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <p className="text-white/50 text-xs uppercase tracking-widest">Türkmenistan</p>
            <p className="font-bold text-lg">Sürüjilik Şahadatnamasy</p>
            <p className="text-white/50 text-xs">Driving Licence</p>
          </div>
          <div className={`px-3 py-1 rounded-full text-xs font-bold ${expired ? 'bg-gray-500 text-gray-300' : 'bg-primary/40 text-white'}`}>
            {license.category}
          </div>
        </div>

        {/* License number */}
        <p className="font-mono text-2xl font-bold tracking-wider text-white/90 mb-4">
          {license.licenseNumber}
        </p>

        <div className="flex items-end justify-between">
          <div className="space-y-1">
            <div>
              <p className="text-white/40 text-xs uppercase">Issued</p>
              <p className="text-sm font-medium">{formatDate(license.issuedDate)}</p>
            </div>
            <div>
              <p className="text-white/40 text-xs uppercase">Expires</p>
              <p className={`text-sm font-medium ${expired ? 'text-red-400' : 'text-white'}`}>
                {formatDate(license.expiryDate)}
              </p>
            </div>
          </div>
          <Badge label={license.status} status={license.status} />
        </div>
      </div>
    </div>
  )
}

export default function License() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()

  const { data: licenses, isLoading, isError, refetch } = useQuery({
    queryKey: ['licenses', nationalId],
    queryFn: () => getLicenses(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: medicalRecords } = useQuery({
    queryKey: ['medical', nationalId],
    queryFn: () => getMedicalRecords(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const hasValidVision = (medicalRecords ?? []).some(
    (r) => r.testType === 'VISION_TEST' && r.result === 'PASS' && !isExpired(r.expiryDate)
  )

  const activeLicense = licenses?.find((l) => l.status === 'ACTIVE')
  const history = (licenses ?? []).filter((l) => l.id !== activeLicense?.id)

  return (
    <div className="space-y-6">
      <h1 className="page-title">{t(lang, 'licenseTitle')}</h1>

      {isLoading ? (
        <SkeletonCard />
      ) : isError ? (
        <ServiceUnavailable onRetry={refetch} />
      ) : !activeLicense ? (
        <div className="space-y-4">
          <EmptyState
            message={t(lang, 'noLicenseMsg')}
            icon={<Car className="w-6 h-6 text-gray-400" />}
          />
          {/* Vision test status */}
          <div className={`card !p-4 flex items-center gap-3 ${hasValidVision ? 'bg-green-50 border-green-100' : 'bg-amber-50 border-amber-100'}`}>
            {hasValidVision
              ? <CheckCircle className="w-5 h-5 text-green-600 shrink-0" />
              : <AlertCircle className="w-5 h-5 text-amber-600 shrink-0" />}
            <p className={`text-sm ${hasValidVision ? 'text-green-700' : 'text-amber-700'}`}>
              {hasValidVision
                ? 'Vision test passed — you can apply for a driving license.'
                : t(lang, 'visionRequired')}
            </p>
          </div>
        </div>
      ) : (
        <LicensePhysicalCard license={activeLicense} />
      )}

      {/* License details */}
      {activeLicense && (
        <div className="card">
          <h2 className="section-title">License Details</h2>
          <div className="grid sm:grid-cols-2 gap-x-8">
            {[
              { label: t(lang, 'licenseNumber'), value: activeLicense.licenseNumber },
              { label: t(lang, 'category'), value: `Category ${activeLicense.category}` },
              { label: t(lang, 'issuedDate'), value: formatDate(activeLicense.issuedDate, lang) },
              { label: t(lang, 'expiryDate'), value: formatDate(activeLicense.expiryDate, lang) },
            ].map(({ label, value }) => (
              <div key={label} className="py-2.5 border-b border-gray-50">
                <p className="text-xs text-gray-400 uppercase tracking-wide">{label}</p>
                <p className="text-sm font-medium text-dark mt-0.5 font-mono">{value}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* History */}
      {history.length > 0 && (
        <div className="card">
          <h2 className="section-title">License History</h2>
          <div className="divide-y divide-gray-50">
            {history.map((l) => (
              <div key={l.id} className="flex items-center justify-between py-3">
                <div>
                  <p className="text-sm font-mono font-medium text-dark">{l.licenseNumber}</p>
                  <p className="text-xs text-gray-400 mt-0.5">
                    {formatDate(l.issuedDate, lang)} — {formatDate(l.expiryDate, lang)}
                  </p>
                </div>
                <Badge label={l.status} status={l.status} size="sm" />
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
