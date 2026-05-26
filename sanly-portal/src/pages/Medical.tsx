import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Stethoscope, Eye } from 'lucide-react'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { t } from '../utils/i18n'
import { getMedicalRecords } from '../api/medical'
import type { MedicalRecord, TestType } from '../types'
import Badge from '../components/ui/Badge'
import ServiceUnavailable from '../components/ui/ServiceUnavailable'
import EmptyState from '../components/ui/EmptyState'
import { SkeletonTable } from '../components/ui/Skeleton'
import { formatDate } from '../utils/date'
import { isExpired } from '../utils/date'

const TEST_TYPE_LABELS: Record<TestType, string> = {
  VISION_TEST: 'Vision Test',
  GENERAL_CLEARANCE: 'General Clearance',
  MENTAL_HEALTH: 'Mental Health',
  SUBSTANCE_ABUSE: 'Substance Abuse',
  CARDIOVASCULAR: 'Cardiovascular',
}

export default function Medical() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()
  const [filter, setFilter] = useState<TestType | 'ALL'>('ALL')

  const { data: records, isLoading, isError, refetch } = useQuery({
    queryKey: ['medical', nationalId],
    queryFn: () => getMedicalRecords(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const visionTest = records?.find((r) => r.testType === 'VISION_TEST' && r.result === 'PASS' && !isExpired(r.expiryDate))

  const filtered = (records ?? []).filter(
    (r) => filter === 'ALL' || r.testType === filter
  ).sort((a, b) => new Date(b.testDate).getTime() - new Date(a.testDate).getTime())

  const testTypes = Array.from(new Set((records ?? []).map((r) => r.testType)))

  return (
    <div className="space-y-6">
      <h1 className="page-title">{t(lang, 'medicalTitle')}</h1>

      {/* Vision test highlight */}
      <div className={`card !p-4 flex items-center gap-4 border-l-4 ${visionTest ? 'border-green-400 bg-green-50' : 'border-amber-400 bg-amber-50'}`}>
        <Eye className={`w-6 h-6 shrink-0 ${visionTest ? 'text-green-600' : 'text-amber-600'}`} />
        <div>
          <p className={`text-sm font-semibold ${visionTest ? 'text-green-700' : 'text-amber-700'}`}>
            {visionTest ? 'Vision Test: Valid' : 'Vision Test: Required'}
          </p>
          <p className={`text-xs mt-0.5 ${visionTest ? 'text-green-600' : 'text-amber-600'}`}>
            {visionTest
              ? `Expires ${formatDate(visionTest.expiryDate, lang)}`
              : t(lang, 'visionTestNote')}
          </p>
        </div>
      </div>

      {/* Filter */}
      {!isLoading && !isError && records && records.length > 0 && (
        <div className="flex items-center gap-2 flex-wrap">
          <span className="text-xs text-gray-400 font-medium">{t(lang, 'filterByType')}:</span>
          <button
            onClick={() => setFilter('ALL')}
            className={`px-3 py-1 rounded-full text-xs font-medium transition-colors ${filter === 'ALL' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
          >
            {t(lang, 'allTypes')}
          </button>
          {testTypes.map((type) => (
            <button
              key={type}
              onClick={() => setFilter(type)}
              className={`px-3 py-1 rounded-full text-xs font-medium transition-colors ${filter === type ? 'bg-primary text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
            >
              {TEST_TYPE_LABELS[type]}
            </button>
          ))}
        </div>
      )}

      {/* Records */}
      {isLoading ? (
        <div className="card"><SkeletonTable rows={5} /></div>
      ) : isError ? (
        <ServiceUnavailable onRetry={refetch} />
      ) : filtered.length === 0 ? (
        <EmptyState message={t(lang, 'noData')} icon={<Stethoscope className="w-6 h-6 text-gray-400" />} />
      ) : (
        <div className="space-y-3">
          {filtered.map((record) => (
            <MedicalCard key={record.id} record={record} lang={lang} />
          ))}
        </div>
      )}
    </div>
  )
}

function MedicalCard({ record, lang }: { record: MedicalRecord; lang: string }) {
  const expired = isExpired(record.expiryDate)
  return (
    <div className="card hover:shadow-card-hover transition-shadow">
      <div className="flex items-start justify-between gap-4">
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 flex-wrap">
            <p className="font-semibold text-dark">
              {TEST_TYPE_LABELS[record.testType] ?? record.testType}
            </p>
            <Badge label={record.result} status={record.result} />
            {expired && record.expiryDate && (
              <Badge label="Expired" status="EXPIRED" size="sm" />
            )}
          </div>
          <p className="text-sm text-gray-400 mt-1">
            {t(lang as any, 'testDate')}: {formatDate(record.testDate, lang as any)}
            {record.expiryDate && ` · ${t(lang as any, 'expiryDate')}: ${formatDate(record.expiryDate, lang as any)}`}
          </p>
        </div>
      </div>
    </div>
  )
}
