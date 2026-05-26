import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { ArrowLeftRight, CheckCircle, XCircle } from 'lucide-react'
import { getPendingTransfers, approveTransfer, rejectTransfer, type AdminTransfer } from '../api/land'
import Badge from '../components/ui/Badge'
import ConfirmDialog from '../components/ui/ConfirmDialog'
import Modal from '../components/ui/Modal'

export default function LandTransfers() {
  const qc = useQueryClient()
  const [approveTarget, setApproveTarget] = useState<AdminTransfer | null>(null)
  const [rejectTarget, setRejectTarget] = useState<AdminTransfer | null>(null)
  const [rejectReason, setRejectReason] = useState('')
  const [showRejectConfirm, setShowRejectConfirm] = useState(false)

  const { data: pending, isLoading } = useQuery({
    queryKey: ['land-pending-transfers'],
    queryFn: getPendingTransfers,
    refetchInterval: 30_000,
  })

  const approveMutation = useMutation({
    mutationFn: approveTransfer,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['land-pending-transfers'] })
      setApproveTarget(null)
    },
  })

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => rejectTransfer(id, reason),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['land-pending-transfers'] })
      setRejectTarget(null)
      setRejectReason('')
      setShowRejectConfirm(false)
    },
  })

  const TRANSFER_TYPE_LABELS: Record<string, string> = {
    SALE: 'Sale', GIFT: 'Gift', INHERITANCE: 'Inheritance', COURT_ORDER: 'Court Order',
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-2">
        <ArrowLeftRight className="w-5 h-5 text-primary" />
        <h1 className="text-xl font-bold text-dark">Pending Transfer Applications</h1>
        <span className={`ml-1 px-2 py-0.5 rounded-full text-xs font-medium ${(pending?.length ?? 0) > 0 ? 'bg-amber-100 text-amber-700' : 'bg-gray-100 text-gray-500'}`}>
          {pending?.length ?? 0} pending
        </span>
      </div>

      <p className="text-sm text-gray-500">
        Before approving, the system automatically queries SANLY Bridge to verify the seller's
        tax compliance. If the seller is <strong>NON_COMPLIANT</strong>, approval will be blocked.
      </p>

      {isLoading ? (
        <div className="space-y-3">
          {[1,2,3].map(i => <div key={i} className="skeleton h-20 rounded-xl" />)}
        </div>
      ) : !pending || pending.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-100 p-10 text-center text-gray-400">
          <CheckCircle className="w-10 h-10 mx-auto mb-2 text-green-400" />
          <p className="font-medium">No pending transfers</p>
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-100 overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-xs uppercase text-gray-400 tracking-wide">
              <tr>
                <th className="text-left px-4 py-3">Property</th>
                <th className="text-left px-4 py-3">Type</th>
                <th className="text-left px-4 py-3">From</th>
                <th className="text-left px-4 py-3">To</th>
                <th className="text-left px-4 py-3">Date</th>
                <th className="text-left px-4 py-3">Status</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {pending.map(t => (
                <tr key={t.applicationId} className="hover:bg-gray-50/50 transition-colors">
                  <td className="px-4 py-3 font-mono text-xs text-primary">{t.cadastralNumber}</td>
                  <td className="px-4 py-3 text-gray-600 text-xs">{TRANSFER_TYPE_LABELS[t.transferType] || t.transferType}</td>
                  <td className="px-4 py-3 font-mono text-xs text-gray-500">{t.fromNationalId}</td>
                  <td className="px-4 py-3 font-mono text-xs text-gray-500">{t.toNationalId}</td>
                  <td className="px-4 py-3 text-gray-400 text-xs">{t.applicationDate}</td>
                  <td className="px-4 py-3">
                    <Badge label={t.status} status={t.status === 'PENDING' ? 'PENDING' : t.status === 'APPROVED' ? 'ACTIVE' : 'FAIL'} size="sm" />
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => setApproveTarget(t)}
                        className="text-xs font-medium text-green-600 hover:text-green-800 transition-colors flex items-center gap-1"
                      >
                        <CheckCircle className="w-3.5 h-3.5" /> Approve
                      </button>
                      <button
                        onClick={() => { setRejectTarget(t); setRejectReason('') }}
                        className="text-xs font-medium text-red-500 hover:text-red-700 transition-colors flex items-center gap-1"
                      >
                        <XCircle className="w-3.5 h-3.5" /> Reject
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Approve confirm */}
      <ConfirmDialog
        isOpen={approveTarget !== null}
        onClose={() => setApproveTarget(null)}
        title="Approve Transfer"
        message={approveTarget
          ? `Approve transfer of ${approveTarget.cadastralNumber} from ${approveTarget.fromNationalId} to ${approveTarget.toNationalId}? Tax compliance will be checked automatically.`
          : ''}
        confirmLabel="Approve Transfer"
        onConfirm={() => approveTarget && approveMutation.mutate(approveTarget.applicationId)}
        loading={approveMutation.isPending}
      />

      {/* Reject reason modal */}
      <Modal
        isOpen={rejectTarget !== null && !showRejectConfirm}
        onClose={() => setRejectTarget(null)}
        title={rejectTarget ? `Reject Transfer ${rejectTarget.cadastralNumber}` : ''}
        footer={
          <>
            <button onClick={() => setRejectTarget(null)} className="btn-secondary text-sm">Cancel</button>
            <button
              onClick={() => setShowRejectConfirm(true)}
              disabled={!rejectReason.trim()}
              className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white text-sm font-medium rounded-lg disabled:opacity-40 transition-colors"
            >
              Continue
            </button>
          </>
        }
      >
        <div className="space-y-3">
          <p className="text-sm text-gray-600">
            Provide a reason for rejecting this transfer application. The seller will be notified.
          </p>
          <textarea
            className="input w-full h-20 resize-none"
            placeholder="Rejection reason…"
            value={rejectReason}
            onChange={e => setRejectReason(e.target.value)}
          />
        </div>
      </Modal>

      <ConfirmDialog
        isOpen={showRejectConfirm && rejectTarget !== null}
        onClose={() => setShowRejectConfirm(false)}
        title="Confirm Rejection"
        message={rejectTarget ? `Reject transfer of ${rejectTarget.cadastralNumber}?` : ''}
        confirmLabel="Reject"
        onConfirm={() => rejectTarget && rejectMutation.mutate({ id: rejectTarget.applicationId, reason: rejectReason })}
        loading={rejectMutation.isPending}
        danger
      />
    </div>
  )
}
