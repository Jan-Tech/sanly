import { useQuery } from '@tanstack/react-query'
import { PiggyBank, TrendingUp, Calendar, CheckCircle2, AlertTriangle } from 'lucide-react'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { t } from '../utils/i18n'
import {
  getPensionAccount, getPensionEligibility, getPensionContributions, getPensionPayments,
} from '../api/pension'
import Badge from '../components/ui/Badge'
import { ServiceUnavailable } from '../components/ui/ServiceUnavailable'
import { SkeletonCard } from '../components/ui/Skeleton'

function accountStatusVariant(status: string) {
  if (status === 'ELIGIBLE' || status === 'PAYING') return 'ACTIVE'
  if (status === 'CLOSED' || status === 'SUSPENDED') return 'FAIL'
  return 'PENDING'
}

function contributionStatusVariant(status: string) {
  if (status === 'VERIFIED') return 'ACTIVE'
  if (status === 'REJECTED') return 'FAIL'
  return 'PENDING'
}

function paymentStatusVariant(status: string) {
  if (status === 'PAID') return 'ACTIVE'
  if (status === 'FAILED') return 'FAIL'
  return 'PENDING'
}

export default function Pension() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()

  const { data: account, isLoading: accountLoading, isError } = useQuery({
    queryKey: ['pension-account', nationalId],
    queryFn: () => getPensionAccount(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: eligibility, isLoading: eligibilityLoading } = useQuery({
    queryKey: ['pension-eligibility', nationalId],
    queryFn: () => getPensionEligibility(nationalId!),
    enabled: !!nationalId && !!account,
    retry: false,
  })

  const { data: contributions, isLoading: contributionsLoading } = useQuery({
    queryKey: ['pension-contributions', account?.accountCode],
    queryFn: () => getPensionContributions(account!.accountCode),
    enabled: !!account,
    retry: false,
  })

  const { data: payments } = useQuery({
    queryKey: ['pension-payments', account?.accountCode],
    queryFn: () => getPensionPayments(account!.accountCode),
    enabled: !!account && account.status === 'PAYING',
    retry: false,
  })

  if (isError && !accountLoading) return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold text-gray-900">{t(lang, 'pension')}</h1>
      <div className="bg-white rounded-xl border border-gray-200 p-8 text-center">
        <PiggyBank className="w-12 h-12 text-gray-300 mx-auto mb-3" />
        <p className="text-gray-500">{t(lang, 'noPensionAccount')}</p>
      </div>
    </div>
  )

  const totalMonths = contributions?.filter(c => c.status === 'VERIFIED').length ?? 0
  const progressPct = Math.min(100, Math.round((totalMonths / 300) * 100))

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
          <PiggyBank className="w-6 h-6 text-primary" />
          {t(lang, 'pension')}
        </h1>
        <p className="mt-1 text-sm text-gray-500">{t(lang, 'pensionDesc')}</p>
      </div>

      {accountLoading ? (
        <SkeletonCard />
      ) : account ? (
        <>
          {/* Account summary */}
          <div className="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
            <div className="flex items-start justify-between gap-3">
              <div>
                <p className="font-mono text-sm font-semibold text-gray-500">{account.accountCode}</p>
                <div className="flex items-center gap-2 mt-1">
                  <Badge variant={accountStatusVariant(account.status)} label={account.status} />
                  {account.status === 'ELIGIBLE' && (
                    <span className="text-xs text-green-600 font-medium">Ready to apply!</span>
                  )}
                </div>
              </div>
              <div className="text-right text-xs text-gray-500">
                <p>Opened: {account.openedAt?.slice(0,10)}</p>
                <p>Employment start: {account.employmentStartDate}</p>
                <p>Retirement at: age {account.retirementAgeTarget}</p>
              </div>
            </div>
            <div className="mt-4 grid grid-cols-3 gap-4 text-center">
              <div className="bg-gray-50 rounded-lg p-3">
                <p className="text-xs text-gray-400">{t(lang, 'totalContributions')}</p>
                <p className="text-lg font-bold text-gray-900 mt-1">{parseFloat(account.totalContributions || '0').toFixed(2)} TMT</p>
              </div>
              <div className="bg-gray-50 rounded-lg p-3">
                <p className="text-xs text-gray-400">{t(lang, 'employerContributions')}</p>
                <p className="text-lg font-bold text-gray-900 mt-1">{parseFloat(account.totalEmployerContributions || '0').toFixed(2)} TMT</p>
              </div>
              <div className="bg-gray-50 rounded-lg p-3">
                <p className="text-xs text-gray-400">{t(lang, 'citizenContributions')}</p>
                <p className="text-lg font-bold text-gray-900 mt-1">{parseFloat(account.totalCitizenContributions || '0').toFixed(2)} TMT</p>
              </div>
            </div>
          </div>

          {/* Eligibility card */}
          {!eligibilityLoading && eligibility && (
            <div className={`rounded-xl border p-5 shadow-sm ${eligibility.eligible ? 'bg-green-50 border-green-200' : 'bg-white border-gray-200'}`}>
              <div className="flex items-start justify-between gap-3 mb-4">
                <div>
                  <h2 className="text-base font-semibold text-gray-800 flex items-center gap-2">
                    {eligibility.eligible
                      ? <><CheckCircle2 className="w-5 h-5 text-green-500" /> {t(lang, 'pensionEligible')}</>
                      : <><Calendar className="w-5 h-5 text-primary" /> {t(lang, 'pensionEligibility')}</>
                    }
                  </h2>
                  <p className="text-sm text-gray-600 mt-0.5">
                    {eligibility.eligible
                      ? `Eligible since ${eligibility.eligibleAt}`
                      : `Eligible from ${eligibility.eligibleAt} (${eligibility.yearsRemaining} years remaining)`
                    }
                  </p>
                </div>
                <div className="text-right">
                  <p className="text-xs text-gray-400">{t(lang, 'projectedMonthly')}</p>
                  <p className="text-xl font-bold text-gray-900">{parseFloat(eligibility.projectedMonthlyAmount || '0').toFixed(2)} TMT</p>
                </div>
              </div>

              <div>
                <div className="flex justify-between text-xs text-gray-500 mb-1">
                  <span>{totalMonths} months contributed</span>
                  <span>300 months (25 years)</span>
                </div>
                <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-primary rounded-full transition-all"
                    style={{ width: `${progressPct}%` }}
                  />
                </div>
                <p className="text-xs text-gray-400 mt-1 text-right">{progressPct}% of full pension</p>
              </div>

              {eligibility.eligible && account.status === 'ELIGIBLE' && (
                <div className="mt-4 p-3 bg-green-100 rounded-lg flex items-center gap-2 text-sm text-green-800">
                  <TrendingUp className="w-4 h-4 shrink-0" />
                  You are eligible to apply for retirement. Contact a pension office or submit via portal.
                </div>
              )}
            </div>
          )}

          {/* Payment history (PAYING accounts) */}
          {account.status === 'PAYING' && (payments ?? []).length > 0 && (
            <section>
              <h2 className="text-lg font-semibold text-gray-800 mb-3 flex items-center gap-2">
                <CheckCircle2 className="w-5 h-5 text-green-500" />
                {t(lang, 'pensionPayments')}
              </h2>
              <div className="bg-white rounded-xl border border-gray-100 overflow-hidden">
                <table className="w-full text-sm">
                  <thead className="bg-gray-50 text-xs text-gray-400 uppercase">
                    <tr>
                      <th className="px-4 py-3 text-left">Month</th>
                      <th className="px-4 py-3 text-left">Amount</th>
                      <th className="px-4 py-3 text-left">Status</th>
                      <th className="px-4 py-3 text-left">Paid Date</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-50">
                    {payments!.slice(0, 12).map(p => (
                      <tr key={p.paymentId} className="hover:bg-gray-50">
                        <td className="px-4 py-3 font-mono text-xs">{p.paymentMonth}</td>
                        <td className="px-4 py-3 font-semibold">{p.amount} TMT</td>
                        <td className="px-4 py-3"><Badge variant={paymentStatusVariant(p.status)} label={p.status} /></td>
                        <td className="px-4 py-3 text-gray-500 text-xs">{p.paidAt?.slice(0,10) ?? p.scheduledDate}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </section>
          )}

          {/* Contribution history */}
          <section>
            <h2 className="text-lg font-semibold text-gray-800 mb-3 flex items-center gap-2">
              <TrendingUp className="w-5 h-5 text-primary" />
              {t(lang, 'contributionHistory')}
            </h2>
            {contributionsLoading ? (
              <div className="space-y-2">{[1,2,3].map(i => <div key={i} className="h-10 bg-gray-100 rounded-lg animate-pulse" />)}</div>
            ) : (contributions ?? []).length === 0 ? (
              <p className="text-sm text-gray-500 italic">{t(lang, 'noData')}</p>
            ) : (
              <div className="bg-white rounded-xl border border-gray-100 overflow-hidden">
                <table className="w-full text-sm">
                  <thead className="bg-gray-50 text-xs text-gray-400 uppercase">
                    <tr>
                      <th className="px-4 py-3 text-left">Month</th>
                      <th className="px-4 py-3 text-left">Employer</th>
                      <th className="px-4 py-3 text-right">Employer Amt</th>
                      <th className="px-4 py-3 text-right">Citizen Amt</th>
                      <th className="px-4 py-3 text-right">Total</th>
                      <th className="px-4 py-3 text-left">Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-50">
                    {contributions!.slice(0, 24).map(c => (
                      <tr key={c.contributionId} className="hover:bg-gray-50">
                        <td className="px-4 py-2.5 font-mono text-xs">{c.contributionMonth}</td>
                        <td className="px-4 py-2.5 text-xs text-gray-500">{c.employerCode ?? 'Self'}</td>
                        <td className="px-4 py-2.5 text-right text-xs">{c.employerAmount ?? '—'}</td>
                        <td className="px-4 py-2.5 text-right text-xs">{c.citizenAmount ?? '—'}</td>
                        <td className="px-4 py-2.5 text-right font-semibold text-xs">{c.totalAmount} TMT</td>
                        <td className="px-4 py-2.5"><Badge variant={contributionStatusVariant(c.status)} label={c.status} /></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        </>
      ) : null}
    </div>
  )
}
