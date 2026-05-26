import { useQuery } from '@tanstack/react-query'
import { Home, ArrowLeftRight, ExternalLink, MapPin, Ruler } from 'lucide-react'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { t } from '../utils/i18n'
import { getPropertiesByOwner, getTransfersByCitizen } from '../api/land'
import Badge from '../components/ui/Badge'
import { ServiceUnavailable } from '../components/ui/ServiceUnavailable'
import { SkeletonCard } from '../components/ui/Skeleton'

const TYPE_LABELS: Record<string, string> = {
  RESIDENTIAL_APARTMENT: 'Apartment',
  RESIDENTIAL_HOUSE: 'House',
  COMMERCIAL: 'Commercial',
  AGRICULTURAL: 'Agricultural',
  INDUSTRIAL: 'Industrial',
  LAND_PLOT: 'Land Plot',
  GARAGE: 'Garage',
  OTHER: 'Other',
}

const VIA_LABELS: Record<string, string> = {
  PURCHASE: 'Purchase', INHERITANCE: 'Inheritance', GIFT: 'Gift',
  STATE_GRANT: 'State Grant', COURT_ORDER: 'Court Order',
}

function propertyStatusToVariant(status: string) {
  if (status === 'REGISTERED') return 'ACTIVE'
  if (status === 'UNDER_TRANSFER') return 'PENDING'
  if (status === 'DISPUTED') return 'FAIL'
  return 'PENDING'
}

export default function Property() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()

  const { data: properties, isLoading: propsLoading, isError: propsError } = useQuery({
    queryKey: ['properties', nationalId],
    queryFn: () => getPropertiesByOwner(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: transfers, isLoading: transLoading } = useQuery({
    queryKey: ['land-transfers', nationalId],
    queryFn: () => getTransfersByCitizen(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  if (propsError) return <ServiceUnavailable service="Land Registry" />

  const publicVerifyUrl = (code: string) =>
    `/proxy/land/api/v1/land/properties/verify/${code}`

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-dark flex items-center gap-2">
          <Home className="w-6 h-6 text-primary" />
          {t(lang, 'property')}
        </h1>
        <p className="text-sm text-gray-400 mt-1">{t(lang, 'propertyDesc')}</p>
      </div>

      {/* Properties */}
      <div>
        <h2 className="section-title">{t(lang, 'myProperties')}</h2>
        {propsLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {[1,2].map(i => <SkeletonCard key={i} />)}
          </div>
        ) : !properties || properties.length === 0 ? (
          <div className="card text-center py-10 text-gray-400">
            <Home className="w-10 h-10 mx-auto mb-2 opacity-30" />
            <p>{t(lang, 'noProperties')}</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {properties.map(pd => {
              const p = pd.property
              const myOwnership = pd.owners.find(o => o.ownerNationalId === nationalId)
              return (
                <div key={p.propertyId}
                     className={`card border-l-4 ${p.status === 'REGISTERED' ? 'border-l-green-500' : p.status === 'UNDER_TRANSFER' ? 'border-l-amber-400' : 'border-l-red-400'}`}>
                  <div className="flex items-start justify-between gap-2">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 flex-wrap mb-1">
                        <span className="font-mono text-xs text-gray-500">{p.cadastralNumber}</span>
                        <Badge label={TYPE_LABELS[p.propertyType] || p.propertyType}
                               status="ACTIVE" size="sm" />
                        <Badge label={p.status.replace('_', ' ')}
                               status={propertyStatusToVariant(p.status)} size="sm" />
                      </div>
                      <div className="flex items-start gap-1 text-sm text-gray-600">
                        <MapPin className="w-3.5 h-3.5 mt-0.5 shrink-0 text-gray-400" />
                        <span className="truncate">{p.address}</span>
                      </div>
                      <p className="text-xs text-gray-400 mt-1">{p.region}</p>
                      {p.area && (
                        <div className="flex items-center gap-1 text-xs text-gray-400 mt-1">
                          <Ruler className="w-3 h-3" />
                          <span>{p.area} m²</span>
                        </div>
                      )}
                      {myOwnership && (
                        <p className="text-xs text-gray-500 mt-1.5">
                          {myOwnership.ownershipShare}% share ·
                          {VIA_LABELS[myOwnership.acquiredVia] || myOwnership.acquiredVia} ·
                          {myOwnership.acquiredAt}
                        </p>
                      )}
                    </div>
                  </div>
                  {p.status === 'REGISTERED' && (
                    <a href={publicVerifyUrl(p.cadastralNumber)}
                       target="_blank" rel="noopener noreferrer"
                       className="mt-3 inline-flex items-center gap-1.5 text-xs text-primary hover:text-primary-dark font-medium">
                      <ExternalLink className="w-3.5 h-3.5" />
                      Verify / Share with Bank
                    </a>
                  )}
                </div>
              )
            })}
          </div>
        )}
      </div>

      {/* Transfer history */}
      <div>
        <h2 className="section-title">{t(lang, 'transferHistory')}</h2>
        {transLoading ? (
          <div className="space-y-2">
            {[1,2,3].map(i => <div key={i} className="skeleton h-14 rounded-lg" />)}
          </div>
        ) : !transfers || transfers.length === 0 ? (
          <div className="card text-center py-8 text-gray-400">
            <ArrowLeftRight className="w-8 h-8 mx-auto mb-2 opacity-30" />
            <p>{t(lang, 'noTransfers')}</p>
          </div>
        ) : (
          <div className="card divide-y divide-gray-50">
            {transfers.map(tr => (
              <div key={tr.applicationId} className="py-3 first:pt-0 last:pb-0 flex items-center gap-3">
                <div className="w-8 h-8 rounded-lg bg-gray-100 flex items-center justify-center shrink-0">
                  <ArrowLeftRight className="w-4 h-4 text-gray-400" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-dark font-mono">{tr.cadastralNumber}</p>
                  <p className="text-xs text-gray-400">
                    {tr.transferType} · {tr.fromNationalId === nationalId ? '→ ' + tr.toNationalId : '← ' + tr.fromNationalId}
                  </p>
                  <p className="text-xs text-gray-400">{tr.applicationDate}</p>
                </div>
                <Badge label={tr.status}
                       status={tr.status === 'APPROVED' ? 'ACTIVE' : tr.status === 'REJECTED' ? 'FAIL' : 'PENDING'}
                       size="sm" />
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
