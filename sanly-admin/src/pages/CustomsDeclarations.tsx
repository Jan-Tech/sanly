import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Package, Search, CheckCircle, XCircle, PauseCircle } from 'lucide-react'
import { getPorts, getDeclarationsByPort, clearDeclaration, rejectDeclaration, holdDeclaration, calculateDuties, type AdminDeclaration, type AdminPort } from '../api/customs'
import Badge from '../components/ui/Badge'
import ConfirmDialog from '../components/ui/ConfirmDialog'

type StatusFilter = 'SUBMITTED' | 'UNDER_REVIEW' | 'HELD' | 'CLEARED' | 'REJECTED' | ''

function statusVariant(s: string) {
  if (s === 'CLEARED') return 'ACTIVE'
  if (s === 'SUBMITTED' || s === 'UNDER_REVIEW') return 'PENDING'
  if (s === 'REJECTED') return 'FAIL'
  if (s === 'HELD') return 'SUSPENDED'
  return 'PENDING'
}

export default function CustomsDeclarations() {
  const qc = useQueryClient()
  const [selectedPort, setSelectedPort] = useState('')
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('SUBMITTED')
  const [clearTarget, setClearTarget] = useState<AdminDeclaration | null>(null)
  const [rejectTarget, setRejectTarget] = useState<AdminDeclaration | null>(null)
  const [rejectReason, setRejectReason] = useState('')
  const [showRejectConfirm, setShowRejectConfirm] = useState(false)
  const [holdTarget, setHoldTarget] = useState<AdminDeclaration | null>(null)

  const { data: ports } = useQuery({ queryKey: ['customs-ports'], queryFn: getPorts })

  const { data: declarations, isLoading } = useQuery({
    queryKey: ['customs-declarations-admin', selectedPort, statusFilter],
    queryFn: () => selectedPort
      ? getDeclarationsByPort(selectedPort, statusFilter || undefined)
      : Promise.resolve([]),
    enabled: !!selectedPort,
  })

  const clearMutation = useMutation({
    mutationFn: (code: string) => clearDeclaration(code),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['customs-declarations-admin'] }); setClearTarget(null) },
  })

  const rejectMutation = useMutation({
    mutationFn: ({ code, reason }: { code: string; reason: string }) => rejectDeclaration(code, reason),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['customs-declarations-admin'] })
      setRejectTarget(null); setRejectReason(''); setShowRejectConfirm(false)
    },
  })

  const holdMutation = useMutation({
    mutationFn: (code: string) => holdDeclaration(code),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['customs-declarations-admin'] }); setHoldTarget(null) },
  })

  const calcMutation = useMutation({
    mutationFn: (code: string) => calculateDuties(code),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['customs-declarations-admin'] }),
  })

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-2">
        <Package className="w-5 h-5 text-primary" />
        <h1 className="text-xl font-bold text-dark">Customs Declarations</h1>
      </div>

      {/* Filters */}
      <div className="flex flex-wrap gap-3">
        <select className="border border-gray-200 rounded-lg px-3 py-2 text-sm"
          value={selectedPort} onChange={e => setSelectedPort(e.target.value)}>
          <option value="">— Select Port —</option>
          {(ports ?? []).map(p => (
            <option key={p.portCode} value={p.portCode}>{p.name} ({p.portCode})</option>
          ))}
        </select>
        <select className="border border-gray-200 rounded-lg px-3 py-2 text-sm"
          value={statusFilter} onChange={e => setStatusFilter(e.target.value as StatusFilter)}>
          <option value="">All statuses</option>
          <option value="SUBMITTED">Submitted</option>
          <option value="UNDER_REVIEW">Under Review</option>
          <option value="HELD">Held</option>
          <option value="CLEARED">Cleared</option>
          <option value="REJECTED">Rejected</option>
        </select>
      </div>

      {!selectedPort ? (
        <div className="bg-white rounded-xl border border-gray-100 p-10 text-center text-gray-400">
          <Search className="w-10 h-10 mx-auto mb-2" />
          <p>Select a port to view declarations</p>
        </div>
      ) : isLoading ? (
        <div className="space-y-3">{[1,2,3].map(i => <div key={i} className="skeleton h-20 rounded-xl" />)}</div>
      ) : !declarations || declarations.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-100 p-10 text-center text-gray-400">
          <CheckCircle className="w-10 h-10 mx-auto mb-2 text-green-400" />
          <p>No declarations match the filter</p>
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-100 overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-xs uppercase text-gray-400 tracking-wide">
              <tr>
                <th className="text-left px-4 py-3">Code</th>
                <th className="text-left px-4 py-3">Declarant</th>
                <th className="text-left px-4 py-3">Type</th>
                <th className="text-left px-4 py-3">HS Code</th>
                <th className="text-left px-4 py-3">Value</th>
                <th className="text-left px-4 py-3">Duties</th>
                <th className="text-left px-4 py-3">Date</th>
                <th className="text-left px-4 py-3">Status</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {declarations.map(d => (
                <tr key={d.declarationId} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-mono text-xs">{d.declarationCode}</td>
                  <td className="px-4 py-3 text-xs text-gray-500">
                    {d.declarantNationalId || d.declarantBusinessNumber || '—'}
                  </td>
                  <td className="px-4 py-3">
                    <Badge variant={d.declarationType === 'IMPORT' ? 'PENDING' : d.declarationType === 'EXPORT' ? 'ACTIVE' : 'SUSPENDED'}
                           label={d.declarationType} />
                  </td>
                  <td className="px-4 py-3 text-gray-500">{d.hsCode || '—'}</td>
                  <td className="px-4 py-3">{d.declaredValue || '—'} {d.currency}</td>
                  <td className="px-4 py-3 text-xs">
                    <span className="text-gray-500">Owed: </span>{d.dutiesOwed || 'TBD'}
                    {d.dutiesPaid && d.dutiesPaid !== '0' && (
                      <span className="block text-green-600">Paid: {d.dutiesPaid}</span>
                    )}
                  </td>
                  <td className="px-4 py-3 text-gray-500">{d.declarationDate}</td>
                  <td className="px-4 py-3">
                    <Badge variant={statusVariant(d.status)} label={d.status} />
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-1 justify-end">
                      {(d.status === 'SUBMITTED' || d.status === 'UNDER_REVIEW') && (
                        <>
                          <button onClick={() => calcMutation.mutate(d.declarationCode)}
                            className="text-xs px-2 py-1 bg-blue-50 text-blue-600 rounded hover:bg-blue-100" title="Calculate duties">
                            Calc
                          </button>
                          <button onClick={() => setClearTarget(d)}
                            className="text-green-600 hover:text-green-800 p-1" title="Clear">
                            <CheckCircle className="w-4 h-4" />
                          </button>
                          <button onClick={() => setHoldTarget(d)}
                            className="text-amber-500 hover:text-amber-700 p-1" title="Hold">
                            <PauseCircle className="w-4 h-4" />
                          </button>
                        </>
                      )}
                      {d.status !== 'CLEARED' && d.status !== 'REJECTED' && (
                        <button onClick={() => { setRejectTarget(d); setRejectReason('') }}
                          className="text-red-500 hover:text-red-700 p-1" title="Reject">
                          <XCircle className="w-4 h-4" />
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <ConfirmDialog
        isOpen={!!clearTarget}
        title="Clear Declaration"
        message={`Clear declaration ${clearTarget?.declarationCode}? This checks tax compliance and requires duties to be fully paid.`}
        confirmLabel="Clear"
        onConfirm={() => clearTarget && clearMutation.mutate(clearTarget.declarationCode)}
        onClose={() => setClearTarget(null)}
        loading={clearMutation.isPending}
      />

      <ConfirmDialog
        isOpen={!!holdTarget}
        title="Hold for Inspection"
        message={`Place declaration ${holdTarget?.declarationCode} on hold for inspection?`}
        confirmLabel="Hold"
        onConfirm={() => holdTarget && holdMutation.mutate(holdTarget.declarationCode)}
        onClose={() => setHoldTarget(null)}
        loading={holdMutation.isPending}
      />

      {rejectTarget && !showRejectConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="bg-white rounded-2xl shadow-xl p-6 w-full max-w-md space-y-4">
            <h2 className="text-lg font-bold text-dark">Reject Declaration</h2>
            <p className="text-sm text-gray-600">Code: <span className="font-mono">{rejectTarget.declarationCode}</span></p>
            <textarea className="w-full border border-gray-200 rounded-lg p-3 text-sm h-24 resize-none"
              placeholder="Rejection reason (required)" value={rejectReason}
              onChange={e => setRejectReason(e.target.value)} />
            <div className="flex gap-3 justify-end">
              <button className="px-4 py-2 text-sm text-gray-600 border border-gray-200 rounded-lg"
                onClick={() => setRejectTarget(null)}>Cancel</button>
              <button className="px-4 py-2 text-sm bg-red-600 text-white rounded-lg disabled:opacity-50"
                disabled={!rejectReason.trim()}
                onClick={() => setShowRejectConfirm(true)}>Reject</button>
            </div>
          </div>
        </div>
      )}

      <ConfirmDialog
        isOpen={showRejectConfirm}
        title="Confirm Rejection"
        message={`Reject ${rejectTarget?.declarationCode}? Reason: "${rejectReason}"`}
        confirmLabel="Reject"
        danger
        onConfirm={() => rejectTarget && rejectMutation.mutate({ code: rejectTarget.declarationCode, reason: rejectReason })}
        onClose={() => setShowRejectConfirm(false)}
        loading={rejectMutation.isPending}
      />
    </div>
  )
}
