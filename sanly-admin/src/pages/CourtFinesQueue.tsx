import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { AlertTriangle, CheckCircle2, Search } from 'lucide-react'
import { getFinesByStatus, verifyPayment, markFinePaid, waiveFine, type AdminCourtFine } from '../api/court'
import Badge from '../components/ui/Badge'
import ConfirmDialog from '../components/ui/ConfirmDialog'

type StatusFilter = 'OUTSTANDING' | 'PENDING_VERIFICATION' | 'OVERDUE' | 'PAID' | 'WAIVED' | 'APPEALING' | ''

function fineStatusVariant(s: string) {
  if (s === 'PAID' || s === 'WAIVED') return 'ACTIVE'
  if (s === 'OVERDUE') return 'FAIL'
  if (s === 'OUTSTANDING' || s === 'APPEALING' || s === 'PENDING_VERIFICATION') return 'PENDING'
  return 'SUSPENDED'
}

export default function CourtFinesQueue() {
  const qc = useQueryClient()
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('PENDING_VERIFICATION')
  const [search, setSearch] = useState('')
  const [verifyTarget, setVerifyTarget] = useState<AdminCourtFine | null>(null)
  const [markPaidTarget, setMarkPaidTarget] = useState<AdminCourtFine | null>(null)
  const [waiveTarget, setWaiveTarget] = useState<AdminCourtFine | null>(null)
  const [waiveReason, setWaiveReason] = useState('')

  const { data: fines, isLoading } = useQuery({
    queryKey: ['court-fines-admin', statusFilter],
    queryFn: () => getFinesByStatus(statusFilter),
  })

  const verifyMutation = useMutation({
    mutationFn: (code: string) => verifyPayment(code),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['court-fines-admin'] }); setVerifyTarget(null) },
  })

  const markPaidMutation = useMutation({
    mutationFn: (code: string) => markFinePaid(code),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['court-fines-admin'] }); setMarkPaidTarget(null) },
  })

  const waiveMutation = useMutation({
    mutationFn: ({ code, reason }: { code: string; reason: string }) => waiveFine(code, reason),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['court-fines-admin'] })
      setWaiveTarget(null); setWaiveReason('')
    },
  })

  const filtered = (fines ?? []).filter(f =>
    !search ||
    f.fineCode.toLowerCase().includes(search.toLowerCase()) ||
    f.citizenNationalId.includes(search) ||
    (f.caseNumber ?? '').toLowerCase().includes(search.toLowerCase())
  )

  const overdueCount = (fines ?? []).filter(f => f.status === 'OVERDUE').length
  const pendingVerifCount = (fines ?? []).filter(f => f.status === 'PENDING_VERIFICATION').length

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3">
        <AlertTriangle className="w-6 h-6 text-amber-500" />
        <h1 className="text-xl font-bold text-gray-900">Court Fines</h1>
        {pendingVerifCount > 0 && (
          <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-700">
            {pendingVerifCount} pending verification
          </span>
        )}
        {overdueCount > 0 && (
          <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-red-100 text-red-700">
            {overdueCount} overdue
          </span>
        )}
      </div>

      <div className="flex flex-wrap gap-3">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            className="pl-9 pr-4 py-2 border border-gray-200 rounded-lg text-sm w-64 focus:outline-none focus:ring-2 focus:ring-primary/30"
            placeholder="Fine code, NIN, case number..."
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
        </div>
        <select
          className="border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/30"
          value={statusFilter}
          onChange={e => setStatusFilter(e.target.value as StatusFilter)}
        >
          <option value="">All statuses</option>
          <option value="PENDING_VERIFICATION">Pending Verification</option>
          <option value="OUTSTANDING">Outstanding</option>
          <option value="OVERDUE">Overdue</option>
          <option value="APPEALING">Appealing</option>
          <option value="PAID">Paid</option>
          <option value="WAIVED">Waived</option>
        </select>
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {[1, 2, 3].map(i => <div key={i} className="h-20 bg-gray-100 rounded-xl animate-pulse" />)}
        </div>
      ) : filtered.length === 0 ? (
        <p className="text-sm text-gray-500 italic py-8 text-center">No fines found.</p>
      ) : (
        <div className="space-y-3">
          {filtered.map((f: AdminCourtFine) => (
            <div
              key={f.fineId}
              className={`bg-white border rounded-xl p-4 shadow-sm ${f.status === 'OVERDUE' ? 'border-red-300' : f.status === 'PENDING_VERIFICATION' ? 'border-amber-300' : 'border-gray-200'}`}
            >
              <div className="flex items-start justify-between gap-3">
                <div className="flex-1">
                  <div className="flex items-center gap-2 flex-wrap">
                    <p className="font-mono text-sm font-semibold text-gray-900">{f.fineCode}</p>
                    <Badge variant={fineStatusVariant(f.status)} label={f.status} />
                    {f.caseNumber && <span className="text-xs text-gray-400">Case: {f.caseNumber}</span>}
                  </div>
                  <div className="mt-1 grid grid-cols-2 gap-x-6 gap-y-0.5 text-xs text-gray-500">
                    <span>NIN: <strong className="font-mono text-gray-700">{f.citizenNationalId}</strong></span>
                    <span>Amount: <strong className={f.status === 'OVERDUE' ? 'text-red-600' : 'text-gray-700'}>{f.amount} TMT</strong></span>
                    <span>Due: <strong className={f.status === 'OVERDUE' ? 'text-red-600' : 'text-gray-700'}>{f.dueDate}</strong></span>
                    <span>Issued: <strong className="text-gray-700">{f.issuedAt?.slice(0, 10)}</strong></span>
                    <span className="col-span-2 truncate">Reason: <span className="text-gray-600">{f.reason}</span></span>
                    {f.paymentProofNote && (
                      <span className="col-span-2 bg-amber-50 border border-amber-100 rounded px-2 py-1 text-amber-700">
                        Proof: {f.paymentProofNote}
                      </span>
                    )}
                  </div>
                </div>
                <div className="flex flex-col gap-1.5 shrink-0">
                  {f.status === 'PENDING_VERIFICATION' && (
                    <button
                      onClick={() => setVerifyTarget(f)}
                      className="flex items-center gap-1 px-3 py-1.5 bg-green-600 text-white rounded-lg text-xs font-medium hover:bg-green-700"
                    >
                      <CheckCircle2 className="w-3.5 h-3.5" /> Verify Payment
                    </button>
                  )}
                  {(f.status === 'OUTSTANDING' || f.status === 'OVERDUE') && (
                    <button
                      onClick={() => setMarkPaidTarget(f)}
                      className="px-3 py-1.5 bg-primary text-white rounded-lg text-xs font-medium hover:bg-primary-dark"
                    >
                      Mark Paid
                    </button>
                  )}
                  {(f.status === 'OUTSTANDING' || f.status === 'OVERDUE' || f.status === 'APPEALING') && (
                    <button
                      onClick={() => setWaiveTarget(f)}
                      className="px-3 py-1.5 bg-gray-100 text-gray-700 rounded-lg text-xs font-medium hover:bg-gray-200"
                    >
                      Waive
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {verifyTarget && (
        <ConfirmDialog
          title="Verify Payment"
          message={`Confirm payment of ${verifyTarget.amount} TMT for fine ${verifyTarget.fineCode}? This will mark the fine as PAID.`}
          onConfirm={() => verifyMutation.mutate(verifyTarget.fineCode)}
          onCancel={() => setVerifyTarget(null)}
          loading={verifyMutation.isPending}
        />
      )}

      {markPaidTarget && (
        <ConfirmDialog
          title="Mark as Paid"
          message={`Mark fine ${markPaidTarget.fineCode} (${markPaidTarget.amount} TMT) as PAID? Use this for offline/cash payments.`}
          onConfirm={() => markPaidMutation.mutate(markPaidTarget.fineCode)}
          onCancel={() => setMarkPaidTarget(null)}
          loading={markPaidMutation.isPending}
        />
      )}

      {waiveTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="bg-white rounded-2xl shadow-xl p-6 w-full max-w-md space-y-4">
            <h2 className="text-lg font-bold text-gray-900">Waive Fine</h2>
            <p className="text-sm text-gray-600">
              Fine: <span className="font-mono font-semibold">{waiveTarget.fineCode}</span> — {waiveTarget.amount} TMT
            </p>
            <textarea
              className="w-full border border-gray-200 rounded-lg p-3 text-sm h-24 resize-none focus:outline-none focus:ring-2 focus:ring-primary/30"
              placeholder="Reason for waiving this fine..."
              value={waiveReason}
              onChange={e => setWaiveReason(e.target.value)}
            />
            <div className="flex gap-3 justify-end">
              <button
                onClick={() => { setWaiveTarget(null); setWaiveReason('') }}
                className="px-4 py-2 text-sm text-gray-600 border border-gray-200 rounded-lg hover:bg-gray-50"
              >
                Cancel
              </button>
              <button
                onClick={() => waiveMutation.mutate({ code: waiveTarget.fineCode, reason: waiveReason })}
                disabled={!waiveReason.trim() || waiveMutation.isPending}
                className="px-4 py-2 text-sm bg-amber-600 text-white rounded-lg hover:bg-amber-700 disabled:opacity-50"
              >
                {waiveMutation.isPending ? 'Waiving...' : 'Waive Fine'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
