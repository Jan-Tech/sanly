import { useQuery } from '@tanstack/react-query'
import { Package, CheckCircle, Clock, AlertCircle, XCircle, ExternalLink } from 'lucide-react'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { t } from '../utils/i18n'
import { getDeclarationsByDeclarant, type CustomsDeclaration } from '../api/customs'
import Badge from '../components/ui/Badge'
import { ServiceUnavailable } from '../components/ui/ServiceUnavailable'
import { SkeletonCard } from '../components/ui/Skeleton'

const STATUS_STEPS = ['DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'CLEARED'] as const

function statusVariant(status: string) {
  if (status === 'CLEARED') return 'ACTIVE'
  if (status === 'SUBMITTED' || status === 'UNDER_REVIEW') return 'PENDING'
  if (status === 'REJECTED') return 'FAIL'
  if (status === 'HELD') return 'SUSPENDED'
  return 'PENDING'
}

function typeVariant(type: string) {
  if (type === 'IMPORT') return 'PENDING'
  if (type === 'EXPORT') return 'ACTIVE'
  return 'SUSPENDED' // TRANSIT
}

function statusLabel(status: string, lang: string): string {
  const map: Record<string, string> = {
    CLEARED: t(lang as any, 'declarationCleared'),
    SUBMITTED: t(lang as any, 'declarationSubmitted'),
    UNDER_REVIEW: t(lang as any, 'declarationPending'),
    REJECTED: t(lang as any, 'declarationRejected'),
    HELD: t(lang as any, 'declarationHeld'),
    DRAFT: t(lang as any, 'declarationDraft'),
  }
  return map[status] ?? status
}

function StatusTracker({ status }: { status: CustomsDeclaration['status'] }) {
  if (status === 'REJECTED' || status === 'HELD') return null
  const currentIdx = STATUS_STEPS.indexOf(status as any)

  return (
    <div className="mt-3 flex items-center gap-1">
      {STATUS_STEPS.map((step, i) => {
        const done = i <= currentIdx
        const active = i === currentIdx
        return (
          <div key={step} className="flex items-center gap-1">
            <div className={`w-2 h-2 rounded-full ${done ? 'bg-primary' : 'bg-gray-200'} ${active ? 'ring-2 ring-primary/30' : ''}`} />
            {i < STATUS_STEPS.length - 1 && (
              <div className={`h-0.5 w-8 ${i < currentIdx ? 'bg-primary' : 'bg-gray-200'}`} />
            )}
          </div>
        )
      })}
    </div>
  )
}

export default function Customs() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()

  const { data: declarations, isLoading, isError } = useQuery({
    queryKey: ['customs-declarations', nationalId],
    queryFn: () => getDeclarationsByDeclarant(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  if (isError) return <ServiceUnavailable />

  const active = (declarations ?? []).filter(d =>
    ['DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'HELD'].includes(d.status))
  const history = (declarations ?? []).filter(d =>
    ['CLEARED', 'REJECTED'].includes(d.status))

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">{t(lang, 'customs')}</h1>
        <p className="mt-1 text-sm text-gray-500">{t(lang, 'customsDesc')}</p>
      </div>

      {/* Active declarations */}
      <section>
        <h2 className="text-lg font-semibold text-gray-800 mb-3 flex items-center gap-2">
          <Clock className="w-5 h-5 text-primary" />
          {t(lang, 'myDeclarations')}
          {active.length > 0 && (
            <span className="ml-1 px-2 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-700">
              {active.length} active
            </span>
          )}
        </h2>

        {isLoading ? (
          <div className="space-y-3">{[1, 2].map(i => <SkeletonCard key={i} />)}</div>
        ) : active.length === 0 ? (
          <p className="text-sm text-gray-500 italic">{t(lang, 'noDeclarations')}</p>
        ) : (
          <div className="space-y-3">
            {active.map(d => (
              <DeclarationCard key={d.declarationId} d={d} lang={lang} />
            ))}
          </div>
        )}
      </section>

      {/* History */}
      {history.length > 0 && (
        <section>
          <h2 className="text-lg font-semibold text-gray-800 mb-3 flex items-center gap-2">
            <CheckCircle className="w-5 h-5 text-gray-400" />
            Completed Declarations
          </h2>
          <div className="space-y-3">
            {history.map(d => (
              <DeclarationCard key={d.declarationId} d={d} lang={lang} />
            ))}
          </div>
        </section>
      )}
    </div>
  )
}

function DeclarationCard({ d, lang }: { d: CustomsDeclaration; lang: string }) {
  return (
    <div className="bg-white rounded-xl border border-gray-200 p-4 shadow-sm">
      <div className="flex items-start justify-between gap-3">
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 flex-wrap">
            <p className="font-semibold text-gray-900 font-mono text-sm">{d.declarationCode}</p>
            <Badge variant={typeVariant(d.declarationType)} label={d.declarationType} />
            <Badge variant={statusVariant(d.status)} label={statusLabel(d.status, lang)} />
          </div>
          <div className="mt-2 grid grid-cols-2 gap-x-6 gap-y-1 text-xs text-gray-500">
            <span>{t(lang as any, 'portCode')}: <strong className="text-gray-700">{d.portCode}</strong></span>
            {d.hsCode && <span>{t(lang as any, 'hsCodeLabel')}: <strong className="text-gray-700">{d.hsCode}</strong></span>}
            {d.dutiesOwed && <span>{t(lang as any, 'dutiesOwed')}: <strong className="text-gray-700">{d.dutiesOwed} {d.currency}</strong></span>}
            {d.dutiesPaid && d.dutiesPaid !== '0' && <span>{t(lang as any, 'dutiesPaid')}: <strong className="text-gray-700">{d.dutiesPaid} {d.currency}</strong></span>}
            <span>Date: <strong className="text-gray-700">{d.declarationDate}</strong></span>
          </div>
          {d.cargoDescription && (
            <p className="mt-1 text-xs text-gray-400 truncate">{d.cargoDescription}</p>
          )}
          {d.rejectionReason && (
            <p className="mt-1 text-xs text-red-500">Rejection: {d.rejectionReason}</p>
          )}
          <StatusTracker status={d.status} />
        </div>
        <a
          href={`/proxy/customs/api/v1/customs/declarations/verify/${d.declarationCode}`}
          target="_blank"
          rel="noopener noreferrer"
          className="shrink-0 flex items-center gap-1 text-xs text-primary hover:text-primary-dark"
          title={t(lang as any, 'verifyDeclaration')}
        >
          <ExternalLink className="w-3.5 h-3.5" />
          {t(lang as any, 'verifyDeclaration')}
        </a>
      </div>
    </div>
  )
}
