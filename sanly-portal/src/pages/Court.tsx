import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Scale, AlertTriangle, CheckCircle2, Clock, FileText } from 'lucide-react'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { t } from '../utils/i18n'
import { getCasesByCitizen, getFinesByCitizen, submitPaymentProof, type CourtCase, type CourtFine } from '../api/court'
import Badge from '../components/ui/Badge'
import { ServiceUnavailable } from '../components/ui/ServiceUnavailable'
import { SkeletonCard } from '../components/ui/Skeleton'

const CASE_STEPS = ['FILED', 'UNDER_REVIEW', 'HEARING_SCHEDULED', 'IN_PROGRESS', 'DECIDED']

function caseStatusVariant(status: string) {
  if (status === 'DECIDED') return 'ACTIVE'
  if (status === 'DISMISSED') return 'SUSPENDED'
  if (status === 'CLOSED') return 'FAIL'
  return 'PENDING'
}

function fineStatusVariant(status: string) {
  if (status === 'PAID' || status === 'WAIVED') return 'ACTIVE'
  if (status === 'OVERDUE') return 'FAIL'
  if (status === 'OUTSTANDING' || status === 'APPEALING') return 'PENDING'
  return 'SUSPENDED'
}

function CaseProgressBar({ status }: { status: string }) {
  const idx = CASE_STEPS.indexOf(status)
  if (idx < 0) return null
  return (
    <div className="flex items-center gap-1 mt-2">
      {CASE_STEPS.map((step, i) => (
        <div key={step} className="flex items-center gap-1">
          <div className={`w-2 h-2 rounded-full ${i <= idx ? 'bg-primary' : 'bg-gray-200'} ${i === idx ? 'ring-2 ring-primary/30' : ''}`} />
          {i < CASE_STEPS.length - 1 && <div className={`h-0.5 w-6 ${i < idx ? 'bg-primary' : 'bg-gray-200'}`} />}
        </div>
      ))}
    </div>
  )
}

function PaymentModal({ fine, nationalId, onClose }: {
  fine: CourtFine; nationalId: string; onClose: () => void
}) {
  const [note, setNote] = useState('')
  const qc = useQueryClient()

  const mutation = useMutation({
    mutationFn: () => submitPaymentProof(fine.fineCode, nationalId, note),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['court-fines', nationalId] })
      onClose()
    },
  })

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="bg-white rounded-2xl shadow-xl p-6 w-full max-w-md space-y-4">
        <h2 className="text-lg font-bold text-gray-900">Submit Payment Proof</h2>
        <div className="text-sm text-gray-600 space-y-1">
          <p>Fine: <span className="font-mono font-semibold">{fine.fineCode}</span></p>
          <p>Amount: <span className="font-semibold text-red-600">{fine.amount} TMT</span></p>
        </div>
        <textarea
          className="w-full border border-gray-200 rounded-lg p-3 text-sm h-24 resize-none focus:outline-none focus:ring-2 focus:ring-primary/30"
          placeholder="Enter payment reference, bank transfer ID, or description of payment..."
          value={note}
          onChange={e => setNote(e.target.value)}
        />
        <div className="flex gap-3 justify-end">
          <button onClick={onClose} className="px-4 py-2 text-sm text-gray-600 border border-gray-200 rounded-lg hover:bg-gray-50">
            Cancel
          </button>
          <button
            onClick={() => mutation.mutate()}
            disabled={!note.trim() || mutation.isPending}
            className="px-4 py-2 text-sm bg-primary text-white rounded-lg hover:bg-primary-dark disabled:opacity-50"
          >
            {mutation.isPending ? 'Submitting...' : 'Submit Proof'}
          </button>
        </div>
      </div>
    </div>
  )
}

export default function Court() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()
  const [paymentTarget, setPaymentTarget] = useState<CourtFine | null>(null)

  const { data: cases, isLoading: casesLoading, isError: casesError } = useQuery({
    queryKey: ['court-cases', nationalId],
    queryFn: () => getCasesByCitizen(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: fines, isLoading: finesLoading } = useQuery({
    queryKey: ['court-fines', nationalId],
    queryFn: () => getFinesByCitizen(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  if (casesError) return <ServiceUnavailable />

  const activeCases = (cases ?? []).filter(c => !['CLOSED', 'DISMISSED'].includes(c.status))
  const closedCases = (cases ?? []).filter(c => ['CLOSED', 'DISMISSED', 'DECIDED'].includes(c.status))
  const pendingFines = (fines ?? []).filter(f => ['OUTSTANDING', 'OVERDUE', 'PENDING_VERIFICATION', 'APPEALING'].includes(f.status))
  const paidFines = (fines ?? []).filter(f => ['PAID', 'WAIVED'].includes(f.status))
  const overdueCount = pendingFines.filter(f => f.status === 'OVERDUE').length
  const totalOutstanding = pendingFines
    .filter(f => ['OUTSTANDING', 'OVERDUE'].includes(f.status))
    .reduce((sum, f) => sum + parseFloat(f.amount || '0'), 0)

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">{t(lang, 'court')}</h1>
        <p className="mt-1 text-sm text-gray-500">{t(lang, 'courtDesc')}</p>
      </div>

      {/* Outstanding fines banner */}
      {overdueCount > 0 && (
        <div className="bg-red-50 border border-red-200 rounded-xl p-4 flex items-center gap-3">
          <AlertTriangle className="w-5 h-5 text-red-600 shrink-0" />
          <div>
            <p className="font-semibold text-red-800">
              {overdueCount} overdue fine{overdueCount > 1 ? 's' : ''} — immediate action required
            </p>
            <p className="text-sm text-red-600">Total outstanding: {totalOutstanding.toFixed(2)} TMT</p>
          </div>
        </div>
      )}

      {/* Pending fines */}
      {pendingFines.length > 0 && (
        <section>
          <h2 className="text-lg font-semibold text-gray-800 mb-3 flex items-center gap-2">
            <AlertTriangle className="w-5 h-5 text-amber-500" />
            {t(lang, 'outstandingFines')}
          </h2>
          <div className="space-y-3">
            {pendingFines.map(fine => (
              <div key={fine.fineId}
                   className={`bg-white rounded-xl border p-4 shadow-sm ${fine.status === 'OVERDUE' ? 'border-red-300' : 'border-gray-200'}`}>
                <div className="flex items-start justify-between gap-3">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 flex-wrap">
                      <p className="font-mono text-sm font-semibold text-gray-900">{fine.fineCode}</p>
                      <Badge variant={fineStatusVariant(fine.status)} label={fine.status} />
                    </div>
                    <p className="text-xs text-gray-500 mt-1">{fine.reason}</p>
                    <div className="mt-2 flex items-center gap-4 text-xs text-gray-500">
                      <span>{t(lang, 'fineAmount')}: <strong className="text-gray-800">{fine.amount} TMT</strong></span>
                      <span>{t(lang, 'dueDate')}: <strong className={fine.status === 'OVERDUE' ? 'text-red-600' : 'text-gray-800'}>{fine.dueDate}</strong></span>
                    </div>
                  </div>
                  {(fine.status === 'OUTSTANDING' || fine.status === 'OVERDUE') && (
                    <button
                      onClick={() => setPaymentTarget(fine)}
                      className="shrink-0 px-3 py-1.5 bg-primary text-white rounded-lg text-xs font-medium hover:bg-primary-dark"
                    >
                      {t(lang, 'submitPayment')}
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </section>
      )}

      {/* Active Cases */}
      <section>
        <h2 className="text-lg font-semibold text-gray-800 mb-3 flex items-center gap-2">
          <Scale className="w-5 h-5 text-primary" />
          {t(lang, 'myCases')}
          {activeCases.length > 0 && (
            <span className="ml-1 px-2 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-700">
              {activeCases.length} active
            </span>
          )}
        </h2>
        {casesLoading ? (
          <div className="space-y-3">{[1, 2].map(i => <SkeletonCard key={i} />)}</div>
        ) : activeCases.length === 0 ? (
          <p className="text-sm text-gray-500 italic">{t(lang, 'noCases')}</p>
        ) : (
          <div className="space-y-3">
            {activeCases.map(c => (
              <div key={c.caseId} className="bg-white rounded-xl border border-gray-200 p-4 shadow-sm">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p className="font-mono text-sm font-semibold text-gray-900">{c.caseNumber}</p>
                    <p className="text-xs text-gray-500 mt-0.5">{c.caseType.replace(/_/g, ' ')} · {c.courtCode}</p>
                    {c.hearingDate && (
                      <p className="text-xs text-amber-600 mt-1 flex items-center gap-1">
                        <Clock className="w-3 h-3" /> Hearing: {c.hearingDate}
                      </p>
                    )}
                  </div>
                  <Badge variant={caseStatusVariant(c.status)} label={c.status} />
                </div>
                <CaseProgressBar status={c.status} />
              </div>
            ))}
          </div>
        )}
      </section>

      {/* Paid fines history */}
      {paidFines.length > 0 && (
        <section>
          <h2 className="text-lg font-semibold text-gray-800 mb-3 flex items-center gap-2">
            <CheckCircle2 className="w-5 h-5 text-green-500" />
            {t(lang, 'fineHistory')}
          </h2>
          <div className="bg-white rounded-xl border border-gray-100 overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-xs text-gray-400 uppercase">
                <tr>
                  <th className="px-4 py-3 text-left">{t(lang, 'fineCode')}</th>
                  <th className="px-4 py-3 text-left">{t(lang, 'fineAmount')}</th>
                  <th className="px-4 py-3 text-left">Reason</th>
                  <th className="px-4 py-3 text-left">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {paidFines.map(f => (
                  <tr key={f.fineId} className="hover:bg-gray-50">
                    <td className="px-4 py-3 font-mono text-xs">{f.fineCode}</td>
                    <td className="px-4 py-3">{f.amount} TMT</td>
                    <td className="px-4 py-3 text-gray-500 text-xs truncate max-w-[200px]">{f.reason}</td>
                    <td className="px-4 py-3"><Badge variant={fineStatusVariant(f.status)} label={f.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}

      {paymentTarget && nationalId && (
        <PaymentModal fine={paymentTarget} nationalId={nationalId} onClose={() => setPaymentTarget(null)} />
      )}
    </div>
  )
}
