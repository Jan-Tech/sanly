import { useQuery } from '@tanstack/react-query'
import { Briefcase, Building2 } from 'lucide-react'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { t } from '../utils/i18n'
import { getBusinesses } from '../api/business'
import type { Business, BusinessType } from '../types'
import Badge from '../components/ui/Badge'
import ServiceUnavailable from '../components/ui/ServiceUnavailable'
import EmptyState from '../components/ui/EmptyState'
import { SkeletonCard } from '../components/ui/Skeleton'
import { formatDate } from '../utils/date'

const BUSINESS_TYPE_LABELS: Record<BusinessType, string> = {
  SOLE_PROPRIETORSHIP: 'Sole Proprietorship',
  LLC: 'LLC',
  JSC: 'Joint Stock Company',
  PARTNERSHIP: 'Partnership',
}

function BusinessCard({ business, lang }: { business: Business; lang: string }) {
  return (
    <div className="card hover:shadow-card-hover transition-shadow">
      <div className="flex items-start justify-between gap-3">
        <div className="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center text-primary shrink-0">
          <Building2 className="w-5 h-5" />
        </div>
        <Badge label={business.status} status={business.status} />
      </div>

      <div className="mt-3">
        <h3 className="font-semibold text-dark text-base">{business.businessName}</h3>
        <p className="text-sm text-gray-400 mt-0.5 font-mono">{business.registrationNumber}</p>
      </div>

      <div className="mt-4 grid grid-cols-2 gap-3 text-sm">
        <div>
          <p className="text-xs text-gray-400 uppercase tracking-wide">{t(lang as any, 'businessType')}</p>
          <p className="font-medium text-dark mt-0.5">{BUSINESS_TYPE_LABELS[business.businessType] ?? business.businessType}</p>
        </div>
        <div>
          <p className="text-xs text-gray-400 uppercase tracking-wide">{t(lang as any, 'registrationDate')}</p>
          <p className="font-medium text-dark mt-0.5">{formatDate(business.registrationDate, lang as any)}</p>
        </div>
        <div>
          <p className="text-xs text-gray-400 uppercase tracking-wide">{t(lang as any, 'expiryDate')}</p>
          <p className="font-medium text-dark mt-0.5">{formatDate(business.expiryDate, lang as any)}</p>
        </div>
      </div>
    </div>
  )
}

export default function Business() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()

  const { data: businesses, isLoading, isError, refetch } = useQuery({
    queryKey: ['businesses', nationalId],
    queryFn: () => getBusinesses(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const active = (businesses ?? []).filter((b) => b.status === 'ACTIVE')
  const other = (businesses ?? []).filter((b) => b.status !== 'ACTIVE')

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="page-title">{t(lang, 'businessTitle')}</h1>
        {businesses && (
          <span className="text-sm text-gray-400">
            {businesses.length} total · {active.length} active
          </span>
        )}
      </div>

      {isLoading ? (
        <div className="grid sm:grid-cols-2 gap-4">
          <SkeletonCard />
          <SkeletonCard />
        </div>
      ) : isError ? (
        <ServiceUnavailable onRetry={refetch} />
      ) : businesses && businesses.length === 0 ? (
        <EmptyState message={t(lang, 'noBusinesses')} icon={<Briefcase className="w-6 h-6 text-gray-400" />} />
      ) : (
        <>
          {active.length > 0 && (
            <div>
              <h2 className="section-title">Active Businesses</h2>
              <div className="grid sm:grid-cols-2 gap-4">
                {active.map((b) => <BusinessCard key={b.id} business={b} lang={lang} />)}
              </div>
            </div>
          )}
          {other.length > 0 && (
            <div>
              <h2 className="section-title">Other</h2>
              <div className="grid sm:grid-cols-2 gap-4">
                {other.map((b) => <BusinessCard key={b.id} business={b} lang={lang} />)}
              </div>
            </div>
          )}
        </>
      )}
    </div>
  )
}
