import { useQuery } from '@tanstack/react-query'
import { HeartHandshake, Coins, Briefcase, TrendingUp } from 'lucide-react'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { t } from '../utils/i18n'
import { getActiveClaims, getAllClaims, getPayments, getUnemploymentStatus, getPensionAccount } from '../api/social'
import Badge from '../components/ui/Badge'
import { ServiceUnavailable } from '../components/ui/ServiceUnavailable'
import { SkeletonCard } from '../components/ui/Skeleton'

const BENEFIT_LABELS: Record<string, string> = {
  CHILD_BENEFIT: 'Child Benefit',
  MATERNITY: 'Maternity',
  PATERNITY: 'Paternity',
  DISABILITY: 'Disability',
  UNEMPLOYMENT: 'Unemployment',
  PENSION: 'Pension',
  SURVIVOR: 'Survivor',
  HOUSING: 'Housing',
  EDUCATION_GRANT: 'Education Grant',
  LOW_INCOME: 'Low Income Support',
}

function claimStatusVariant(status: string) {
  if (status === 'ACTIVE') return 'ACTIVE'
  if (status === 'PENDING') return 'PENDING'
  if (status === 'SUSPENDED') return 'SUSPENDED'
  if (status === 'REJECTED' || status === 'CANCELLED' || status === 'EXPIRED') return 'FAIL'
  return 'PENDING'
}

function paymentStatusVariant(status: string) {
  if (status === 'PAID') return 'ACTIVE'
  if (status === 'SCHEDULED') return 'PENDING'
  return 'FAIL'
}

export default function Benefits() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()

  const { data: activeClaims, isLoading: claimsLoading, isError: claimsError } = useQuery({
    queryKey: ['active-claims', nationalId],
    queryFn: () => getActiveClaims(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: payments, isLoading: paymentsLoading } = useQuery({
    queryKey: ['benefit-payments', nationalId],
    queryFn: () => getPayments(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: unemployment, isLoading: unemploymentLoading } = useQuery({
    queryKey: ['unemployment', nationalId],
    queryFn: () => getUnemploymentStatus(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: pension, isLoading: pensionLoading } = useQuery({
    queryKey: ['pension', nationalId],
    queryFn: () => getPensionAccount(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  if (claimsError) return <ServiceUnavailable />

  const recentPayments = (payments ?? []).slice(0, 10)

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">{t(lang, 'benefits')}</h1>
        <p className="mt-1 text-sm text-gray-500">{t(lang, 'benefitsDesc')}</p>
      </div>

      {/* Active Benefits */}
      <section>
        <h2 className="text-lg font-semibold text-gray-800 mb-3 flex items-center gap-2">
          <HeartHandshake className="w-5 h-5 text-primary" />
          {t(lang, 'activeBenefits')}
        </h2>
        {claimsLoading ? (
          <div className="space-y-3">{[1, 2].map(i => <SkeletonCard key={i} />)}</div>
        ) : !activeClaims || activeClaims.length === 0 ? (
          <p className="text-sm text-gray-500 italic">{t(lang, 'noActiveBenefits')}</p>
        ) : (
          <div className="space-y-3">
            {activeClaims.map(claim => (
              <div key={claim.claimId} className="bg-white rounded-xl border border-gray-200 p-4 shadow-sm">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p className="font-semibold text-gray-900">
                      {BENEFIT_LABELS[claim.programCode?.replace(/TM-BEN-\d+/, '')] ?? claim.programCode}
                    </p>
                    <p className="text-xs text-gray-400 mt-0.5">{t(lang, 'claimCode')}: {claim.claimCode}</p>
                    {claim.expiresAt && (
                      <p className="text-xs text-gray-400">
                        {t(lang, 'expiryDate')}: {claim.expiresAt}
                      </p>
                    )}
                  </div>
                  <Badge variant={claimStatusVariant(claim.status)} label={claim.status} />
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      {/* Pension Account */}
      <section>
        <h2 className="text-lg font-semibold text-gray-800 mb-3 flex items-center gap-2">
          <TrendingUp className="w-5 h-5 text-primary" />
          {t(lang, 'pensionAccount')}
        </h2>
        {pensionLoading ? (
          <SkeletonCard />
        ) : !pension ? (
          <p className="text-sm text-gray-500 italic">{t(lang, 'noPensionAccount')}</p>
        ) : (
          <div className="bg-white rounded-xl border border-gray-200 p-4 shadow-sm">
            <div className="flex items-start justify-between gap-3 mb-4">
              <p className="font-semibold text-gray-900">{t(lang, 'pensionAccount')}</p>
              <Badge
                variant={pension.status === 'ELIGIBLE' || pension.status === 'PAYING' ? 'ACTIVE' : 'PENDING'}
                label={pension.status}
              />
            </div>
            <dl className="grid grid-cols-2 gap-x-6 gap-y-2 text-sm">
              <div>
                <dt className="text-gray-500">{t(lang, 'contributionStart')}</dt>
                <dd className="font-medium text-gray-900">{pension.contributionStartDate}</dd>
              </div>
              <div>
                <dt className="text-gray-500">{t(lang, 'eligibleAt')}</dt>
                <dd className="font-medium text-gray-900">{pension.eligibleAt}</dd>
              </div>
              {pension.totalContributions && (
                <div>
                  <dt className="text-gray-500">{t(lang, 'totalContributions')}</dt>
                  <dd className="font-medium text-gray-900">{pension.totalContributions}</dd>
                </div>
              )}
            </dl>
          </div>
        )}
      </section>

      {/* Unemployment */}
      <section>
        <h2 className="text-lg font-semibold text-gray-800 mb-3 flex items-center gap-2">
          <Briefcase className="w-5 h-5 text-primary" />
          {t(lang, 'unemploymentStatus')}
        </h2>
        {unemploymentLoading ? (
          <SkeletonCard />
        ) : !unemployment ? (
          <p className="text-sm text-gray-500 italic">{t(lang, 'noUnemploymentRecord')}</p>
        ) : (
          <div className="bg-white rounded-xl border border-gray-200 p-4 shadow-sm">
            <div className="flex items-start justify-between gap-3">
              <div>
                <p className="font-semibold text-gray-900">{t(lang, 'unemploymentStatus')}</p>
                <p className="text-xs text-gray-400 mt-1">
                  {t(lang, 'registrationDate')}: {unemployment.registeredAt?.slice(0, 10)}
                </p>
                {unemployment.lastEmploymentDate && (
                  <p className="text-xs text-gray-400">
                    Last Employment: {unemployment.lastEmploymentDate}
                  </p>
                )}
              </div>
              <Badge
                variant={unemployment.status === 'REGISTERED' ? 'PENDING' : unemployment.status === 'EMPLOYED' ? 'ACTIVE' : 'FAIL'}
                label={unemployment.status}
              />
            </div>
          </div>
        )}
      </section>

      {/* Payment History */}
      <section>
        <h2 className="text-lg font-semibold text-gray-800 mb-3 flex items-center gap-2">
          <Coins className="w-5 h-5 text-primary" />
          {t(lang, 'paymentHistory')}
        </h2>
        {paymentsLoading ? (
          <div className="space-y-2">{[1, 2, 3].map(i => <SkeletonCard key={i} />)}</div>
        ) : recentPayments.length === 0 ? (
          <p className="text-sm text-gray-500 italic">{t(lang, 'noPayments')}</p>
        ) : (
          <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
            <table className="min-w-full text-sm">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-4 py-3 text-left font-medium text-gray-600">{t(lang, 'paymentPeriod')}</th>
                  <th className="px-4 py-3 text-left font-medium text-gray-600">{t(lang, 'amount')}</th>
                  <th className="px-4 py-3 text-left font-medium text-gray-600">{t(lang, 'claimCode')}</th>
                  <th className="px-4 py-3 text-left font-medium text-gray-600">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {recentPayments.map(p => (
                  <tr key={p.paymentId} className="hover:bg-gray-50">
                    <td className="px-4 py-3 text-gray-900">{p.paymentPeriod}</td>
                    <td className="px-4 py-3 font-medium text-gray-900">{p.amount}</td>
                    <td className="px-4 py-3 text-gray-500 text-xs">{p.claimCode}</td>
                    <td className="px-4 py-3">
                      <Badge variant={paymentStatusVariant(p.status)} label={p.status} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  )
}
