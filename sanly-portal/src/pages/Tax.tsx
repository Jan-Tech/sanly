import { useQuery } from '@tanstack/react-query'
import { Receipt } from 'lucide-react'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { t } from '../utils/i18n'
import { getTaxpayer, getTaxFilings } from '../api/tax'
import type { FilingStatus } from '../types'
import Badge from '../components/ui/Badge'
import ServiceUnavailable from '../components/ui/ServiceUnavailable'
import EmptyState from '../components/ui/EmptyState'
import { SkeletonCard, SkeletonTable } from '../components/ui/Skeleton'
import { formatDate, formatDateTime } from '../utils/date'

const FILING_STATUS_LABELS: Record<FilingStatus, string> = {
  PENDING: 'Pending',
  SUBMITTED: 'Submitted',
  ACCEPTED: 'Accepted',
  REJECTED: 'Rejected',
}

export default function Tax() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()

  const { data: taxpayer, isLoading: tpLoading, isError: tpError, refetch: refetchTp } = useQuery({
    queryKey: ['taxpayer', nationalId],
    queryFn: () => getTaxpayer(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: filings, isLoading: filingsLoading } = useQuery({
    queryKey: ['tax-filings', taxpayer?.taxId],
    queryFn: () => getTaxFilings(taxpayer!.taxId),
    enabled: !!taxpayer?.taxId,
    retry: false,
  })

  return (
    <div className="space-y-6">
      <h1 className="page-title">{t(lang, 'taxTitle')}</h1>

      {/* Taxpayer info */}
      {tpLoading ? (
        <SkeletonCard />
      ) : tpError ? (
        <div className="card text-center py-10">
          <Receipt className="w-10 h-10 text-gray-300 mx-auto mb-3" />
          <p className="text-gray-500">{t(lang, 'noTaxRecord')}</p>
        </div>
      ) : taxpayer ? (
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h2 className="section-title !mb-0">{t(lang, 'taxTitle')}</h2>
            <Badge
              label={t(lang, taxpayer.complianceStatus === 'COMPLIANT' ? 'compliant' : taxpayer.complianceStatus === 'NON_COMPLIANT' ? 'nonCompliant' : 'pending')}
              status={taxpayer.complianceStatus}
            />
          </div>

          <div className="grid sm:grid-cols-2 gap-x-8">
            {[
              { label: t(lang, 'taxId'), value: <span className="font-mono">{taxpayer.taxId}</span> },
              { label: t(lang, 'taxpayerType'), value: taxpayer.taxpayerType === 'INDIVIDUAL' ? t(lang, 'individual') : taxpayer.taxpayerType },
              { label: t(lang, 'taxpayerStatus'), value: <Badge label={taxpayer.status} status={taxpayer.status} size="sm" /> },
              { label: t(lang, 'registrationDate'), value: formatDate(taxpayer.registrationDate, lang) },
            ].map(({ label, value }) => (
              <div key={label} className="py-2.5 border-b border-gray-50 last:border-0">
                <p className="text-xs text-gray-400 uppercase tracking-wide">{label}</p>
                <div className="text-sm font-medium text-dark mt-0.5">{value}</div>
              </div>
            ))}
          </div>
        </div>
      ) : null}

      {/* Filing history */}
      {taxpayer && (
        <div className="card">
          <h2 className="section-title">{t(lang, 'filingHistory')}</h2>

          {filingsLoading ? (
            <SkeletonTable rows={4} />
          ) : !filings || filings.length === 0 ? (
            <EmptyState message={t(lang, 'noData')} icon={<Receipt className="w-6 h-6 text-gray-400" />} />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-100">
                    <th className="text-left py-2 pr-4 text-xs font-medium text-gray-400 uppercase tracking-wide">{t(lang, 'taxYear')}</th>
                    <th className="text-left py-2 pr-4 text-xs font-medium text-gray-400 uppercase tracking-wide">Status</th>
                    <th className="text-left py-2 pr-4 text-xs font-medium text-gray-400 uppercase tracking-wide">Submitted</th>
                    <th className="text-left py-2 text-xs font-medium text-gray-400 uppercase tracking-wide">Processed</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {filings.map((f) => (
                    <tr key={f.id} className="hover:bg-gray-50/50">
                      <td className="py-3 pr-4 font-semibold text-dark">{f.taxYear}</td>
                      <td className="py-3 pr-4">
                        <Badge label={FILING_STATUS_LABELS[f.status] ?? f.status} status={f.status} size="sm" />
                      </td>
                      <td className="py-3 pr-4 text-gray-500">{formatDate(f.submittedAt, lang)}</td>
                      <td className="py-3 text-gray-500">{formatDate(f.processedAt, lang)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
