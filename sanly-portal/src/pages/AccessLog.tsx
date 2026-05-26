import { useState, useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Shield, Eye, Filter } from 'lucide-react'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { t } from '../utils/i18n'
import { getAccessLogs } from '../api/bridge'
import Badge from '../components/ui/Badge'
import ServiceUnavailable from '../components/ui/ServiceUnavailable'
import EmptyState from '../components/ui/EmptyState'
import { SkeletonTable } from '../components/ui/Skeleton'
import { formatDateTime } from '../utils/date'

const DATA_TYPE_LABELS: Record<string, string> = {
  VISION_TEST: 'Vision Test',
  MEDICAL_CLEARANCE: 'Medical Clearance',
  CRIMINAL_RECORD: 'Criminal Record',
  TAX_STATUS: 'Tax Status',
  DRIVING_LICENSE: 'Driving License',
  BUSINESS_REGISTRATION: 'Business Registration',
  BIRTH_RECORD: 'Birth Record',
  MARRIAGE_RECORD: 'Marriage Record',
  DEATH_RECORD: 'Death Record',
}

const INST_LABELS: Record<string, string> = {
  INST_MEDICAL: 'Medical Service',
  INST_DMV: 'DMV',
  INST_POLICE: 'Police',
  INST_TAX: 'Tax Service',
  INST_BUSINESS: 'Business Registry',
  INST_CIVIL: 'Civil Registry',
  INST_PORTAL: 'Citizen Portal',
}

export default function AccessLog() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()
  const [instFilter, setInstFilter] = useState<string>('ALL')

  const { data: logs, isLoading, isError, refetch } = useQuery({
    queryKey: ['access-logs', nationalId],
    queryFn: () => getAccessLogs(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const institutions = useMemo(() => {
    const codes = Array.from(new Set((logs ?? []).map((l) => l.requestingInstitutionCode)))
    return codes.sort()
  }, [logs])

  const filtered = useMemo(() => {
    return (logs ?? []).filter(
      (l) => instFilter === 'ALL' || l.requestingInstitutionCode === instFilter
    )
  }, [logs, instFilter])

  return (
    <div className="space-y-6">
      <div>
        <h1 className="page-title flex items-center gap-2">
          <Shield className="w-7 h-7 text-primary" />
          {t(lang, 'accessLogTitle')}
        </h1>
        <p className="text-sm text-gray-500 mt-1 max-w-2xl">{t(lang, 'accessLogSubtitle')}</p>
      </div>

      {/* Privacy notice */}
      <div className="card !p-4 bg-primary/5 border-primary/10 flex items-start gap-3">
        <Eye className="w-5 h-5 text-primary shrink-0 mt-0.5" />
        <div>
          <p className="text-sm font-semibold text-primary">Your data, your right to know</p>
          <p className="text-sm text-primary/70 mt-0.5">
            Every time a government institution requests your data through SANLY Bridge, it is recorded here.
            This cannot be deleted or modified. Inspired by Estonia's data access transparency model.
          </p>
        </div>
      </div>

      {/* Filters */}
      {!isLoading && !isError && logs && logs.length > 0 && (
        <div className="flex items-center gap-2 flex-wrap">
          <Filter className="w-4 h-4 text-gray-400" />
          <button
            onClick={() => setInstFilter('ALL')}
            className={`px-3 py-1 rounded-full text-xs font-medium transition-colors ${instFilter === 'ALL' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
          >
            {t(lang, 'filterByInstitution')}
          </button>
          {institutions.map((code) => (
            <button
              key={code}
              onClick={() => setInstFilter(code)}
              className={`px-3 py-1 rounded-full text-xs font-medium transition-colors ${instFilter === code ? 'bg-primary text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
            >
              {INST_LABELS[code] ?? code}
            </button>
          ))}
        </div>
      )}

      {/* Stats summary */}
      {!isLoading && !isError && logs && logs.length > 0 && (
        <div className="grid grid-cols-3 gap-4">
          {[
            { label: 'Total Requests', value: logs.length },
            { label: 'Granted', value: logs.filter((l) => l.success).length },
            { label: 'Denied', value: logs.filter((l) => !l.success).length },
          ].map(({ label, value }) => (
            <div key={label} className="card !p-4 text-center">
              <p className="text-2xl font-bold text-dark">{value}</p>
              <p className="text-xs text-gray-400 mt-0.5">{label}</p>
            </div>
          ))}
        </div>
      )}

      {/* Table */}
      <div className="card">
        {isLoading ? (
          <SkeletonTable rows={8} />
        ) : isError ? (
          <ServiceUnavailable onRetry={refetch} />
        ) : filtered.length === 0 ? (
          <EmptyState message={t(lang, 'noLogs')} icon={<Shield className="w-6 h-6 text-gray-400" />} />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-100">
                  <th className="text-left py-2 pr-4 text-xs font-medium text-gray-400 uppercase tracking-wide">{t(lang, 'institution')}</th>
                  <th className="text-left py-2 pr-4 text-xs font-medium text-gray-400 uppercase tracking-wide">{t(lang, 'dataType')}</th>
                  <th className="text-left py-2 pr-4 text-xs font-medium text-gray-400 uppercase tracking-wide">{t(lang, 'timestamp')}</th>
                  <th className="text-left py-2 text-xs font-medium text-gray-400 uppercase tracking-wide">{t(lang, 'accessResult')}</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {filtered.map((log) => (
                  <tr key={log.id} className="hover:bg-gray-50/50">
                    <td className="py-3 pr-4">
                      <p className="font-medium text-dark">{INST_LABELS[log.requestingInstitutionCode] ?? log.requestingInstitutionCode}</p>
                    </td>
                    <td className="py-3 pr-4 text-gray-600">
                      {DATA_TYPE_LABELS[log.dataType] ?? log.dataType}
                    </td>
                    <td className="py-3 pr-4 text-gray-500 whitespace-nowrap">
                      {formatDateTime(log.timestamp, lang)}
                    </td>
                    <td className="py-3">
                      <Badge
                        label={log.success ? t(lang, 'success') : t(lang, 'denied')}
                        status={log.success ? 'ACTIVE' : 'FAIL'}
                        size="sm"
                      />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
