import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { ArrowLeftRight } from 'lucide-react'
import { getTransfersByStatus, approveTransfer, rejectTransfer, type AdminTransfer } from '../api/vehicle'
import Badge from '../components/ui/Badge'
import ConfirmDialog from '../components/ui/ConfirmDialog'

function transferStatusVariant(s: string) {
  if (s === 'APPROVED') return 'ACTIVE'
  if (s === 'REJECTED') return 'FAIL'
  if (s === 'PENDING') return 'PENDING'
  return 'SUSPENDED'
}

export default function VehicleTransfers() {
  const qc = useQueryClient()
  const [statusFilter, setStatusFilter] = useState('PENDING')
  const [approveTarget, setApproveTarget] = useState<AdminTransfer | null>(null)
  const [rejectTarget, setRejectTarget] = useState<AdminTransfer | null>(null)
  const [rejectReason, setRejectReason] = useState('')

  const { data: transfers, isLoading } = useQuery({
    queryKey: ['admin-transfers', statusFilter],
    queryFn: () => getTransfersByStatus(statusFilter),
  })

  const approveMutation = useMutation({
    mutationFn: (id: string) => approveTransfer(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['admin-transfers'] }); setApproveTarget(null) },
  })

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => rejectTransfer(id, reason),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['admin-transfers'] })
      setRejectTarget(null); setRejectReason('')
    },
  })

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3">
        <ArrowLeftRight className="w-6 h-6 text-primary" />
        <h1 className="text-xl font-bold text-gray-900">Vehicle Transfers</h1>
        {statusFilter === 'PENDING' && (transfers?.length ?? 0) > 0 && (
          <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-700">
            {transfers!.length} pending
          </span>
        )}
      </div>

      <select
        className="border border-gray-200 rounded-lg px-3 py-2 text-sm"
        value={statusFilter}
        onChange={e => setStatusFilter(e.target.value)}
      >
        <option value="PENDING">Pending</option>
        <option value="APPROVED">Approved</option>
        <option value="REJECTED">Rejected</option>
        <option value="CANCELLED">Cancelled</option>
      </select>

      {isLoading ? (
        <div className="space-y-3">{[1,2,3].map(i => <div key={i} className="h-24 bg-gray-100 rounded-xl animate-pulse" />)}</div>
      ) : (transfers ?? []).length === 0 ? (
        <p className="text-sm text-gray-500 italic py-8 text-center">No transfers found.</p>
      ) : (
        <div className="space-y-3">
          {transfers!.map((t: AdminTransfer) => (
            <div key={t.applicationId} className="bg-white border border-gray-200 rounded-xl p-4 shadow-sm">
              <div className="flex items-start justify-between gap-3">
                <div className="flex-1">
                  <div className="flex items-center gap-2 flex-wrap">
                    <p className="font-mono text-sm font-bold">{t.plateNumber}</p>
                    <Badge variant={transferStatusVariant(t.status)} label={t.status} />
                    <span className="text-xs text-gray-400">{t.transferType}</span>
                  </div>
                  <div className="mt-1 text-xs text-gray-500 space-x-3">
                    <span>From: <strong className="font-mono text-gray-700">{t.fromNationalId}</strong></span>
                    <span>To: <strong className="font-mono text-gray-700">{t.toNationalId ?? t.toBusinessNumber}</strong></span>
                    <span>Applied: {t.applicationDate}</span>
                    {t.agreedPrice && <span>Price: {t.agreedPrice} TMT</span>}
                  </div>
                  {t.rejectionReason && (
                    <p className="text-xs text-red-600 mt-1">Reason: {t.rejectionReason}</p>
                  )}
                </div>
                {t.status === 'PENDING' && (
                  <div className="flex gap-2 shrink-0">
                    <button
                      onClick={() => setApproveTarget(t)}
                      className="px-3 py-1.5 bg-green-600 text-white text-xs rounded-lg hover:bg-green-700"
                    >
                      Approve
                    </button>
                    <button
                      onClick={() => setRejectTarget(t)}
                      className="px-3 py-1.5 bg-red-100 text-red-700 text-xs rounded-lg hover:bg-red-200"
                    >
                      Reject
                    </button>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {approveTarget && (
        <ConfirmDialog
          title="Approve Transfer"
          message={`Approve ownership transfer of vehicle ${approveTarget.plateNumber} from ${approveTarget.fromNationalId} to ${approveTarget.toNationalId ?? approveTarget.toBusinessNumber}? Tax compliance will be verified.`}
          onConfirm={() => approveMutation.mutate(approveTarget.applicationId)}
          onCancel={() => setApproveTarget(null)}
          loading={approveMutation.isPending}
        />
      )}

      {rejectTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="bg-white rounded-2xl shadow-xl p-6 w-full max-w-md space-y-4">
            <h2 className="text-lg font-bold text-gray-900">Reject Transfer</h2>
            <p className="text-sm text-gray-600">Vehicle: <strong>{rejectTarget.plateNumber}</strong></p>
            <textarea
              className="w-full border border-gray-200 rounded-lg p-3 text-sm h-24 resize-none focus:outline-none focus:ring-2 focus:ring-primary/30"
              placeholder="Reason for rejection..."
              value={rejectReason}
              onChange={e => setRejectReason(e.target.value)}
            />
            <div className="flex gap-3 justify-end">
              <button onClick={() => { setRejectTarget(null); setRejectReason('') }}
                className="px-4 py-2 text-sm border border-gray-200 rounded-lg hover:bg-gray-50">Cancel</button>
              <button
                onClick={() => rejectMutation.mutate({ id: rejectTarget.applicationId, reason: rejectReason })}
                disabled={!rejectReason.trim() || rejectMutation.isPending}
                className="px-4 py-2 text-sm bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-50"
              >
                {rejectMutation.isPending ? 'Rejecting...' : 'Reject'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
