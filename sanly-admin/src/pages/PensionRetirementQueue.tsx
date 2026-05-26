import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { PiggyBank, CheckCircle } from 'lucide-react'
import {
  getRetirementApplications, approveRetirement, rejectRetirement,
  type AdminRetirementApplication,
} from '../api/pension'
import Badge from '../components/ui/Badge'
import ConfirmDialog from '../components/ui/ConfirmDialog'

function statusVariant(s: string) {
  if (s === 'APPROVED') return 'ACTIVE'
  if (s === 'REJECTED') return 'FAIL'
  return 'PENDING'
}

export default function PensionRetirementQueue() {
  const qc = useQueryClient()
  const [statusFilter, setStatusFilter] = useState('PENDING')
  const [approveTarget, setApproveTarget] = useState<AdminRetirementApplication | null>(null)
  const [rejectTarget, setRejectTarget] = useState<AdminRetirementApplication | null>(null)
  const [rejectReason, setRejectReason] = useState('')

  const { data: applications, isLoading } = useQuery({
    queryKey: ['retirement-applications', statusFilter],
    queryFn: () => getRetirementApplications(statusFilter),
  })

  const approveMutation = useMutation({
    mutationFn: (id: string) => approveRetirement(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['retirement-applications'] }); setApproveTarget(null) },
  })

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => rejectRetirement(id, reason),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['retirement-applications'] }); setRejectTarget(null); setRejectReason('') },
  })

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3">
        <PiggyBank className="w-6 h-6 text-primary" />
        <h1 className="text-xl font-bold text-gray-900">Retirement Applications</h1>
        {statusFilter === 'PENDING' && (applications?.length ?? 0) > 0 && (
          <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-700">
            {applications!.length} pending
          </span>
        )}
      </div>

      <select className="border border-gray-200 rounded-lg px-3 py-2 text-sm"
        value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
        <option value="PENDING">Pending</option>
        <option value="APPROVED">Approved</option>
        <option value="REJECTED">Rejected</option>
      </select>

      {isLoading ? (
        <div className="space-y-3">{[1,2,3].map(i => <div key={i} className="h-24 bg-gray-100 rounded-xl animate-pulse" />)}</div>
      ) : (applications ?? []).length === 0 ? (
        <p className="text-sm text-gray-500 italic py-8 text-center">No applications found.</p>
      ) : (
        <div className="space-y-3">
          {applications!.map((app: AdminRetirementApplication) => (
            <div key={app.applicationId} className="bg-white border border-gray-200 rounded-xl p-4 shadow-sm">
              <div className="flex items-start justify-between gap-3">
                <div className="flex-1">
                  <div className="flex items-center gap-2 flex-wrap">
                    <p className="font-mono text-sm font-semibold">{app.accountCode}</p>
                    <Badge variant={statusVariant(app.status)} label={app.status} />
                  </div>
                  <div className="mt-1 text-xs text-gray-500 space-x-3">
                    <span>NIN: <strong className="font-mono text-gray-700">{app.citizenNationalId}</strong></span>
                    <span>Applied: {app.appliedAt?.slice(0,10)}</span>
                    <span>Start requested: {app.requestedStartDate}</span>
                    {app.monthlyPensionAmount && <span>Monthly: <strong className="text-green-700">{app.monthlyPensionAmount} TMT</strong></span>}
                  </div>
                  {app.rejectionReason && <p className="text-xs text-red-600 mt-1">{app.rejectionReason}</p>}
                </div>
                {app.status === 'PENDING' && (
                  <div className="flex gap-2 shrink-0">
                    <button onClick={() => setApproveTarget(app)}
                      className="flex items-center gap-1 px-3 py-1.5 bg-green-600 text-white text-xs rounded-lg hover:bg-green-700">
                      <CheckCircle className="w-3.5 h-3.5" /> Approve
                    </button>
                    <button onClick={() => setRejectTarget(app)}
                      className="px-3 py-1.5 bg-red-100 text-red-700 text-xs rounded-lg hover:bg-red-200">
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
          title="Approve Retirement"
          message={`Approve retirement for account ${approveTarget.accountCode}? The system will calculate the monthly pension amount and start scheduling payments.`}
          onConfirm={() => approveMutation.mutate(approveTarget.applicationId)}
          onCancel={() => setApproveTarget(null)}
          loading={approveMutation.isPending}
        />
      )}

      {rejectTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="bg-white rounded-2xl shadow-xl p-6 w-full max-w-md space-y-4">
            <h2 className="text-lg font-bold text-gray-900">Reject Application</h2>
            <p className="text-sm text-gray-600">Account: <strong>{rejectTarget.accountCode}</strong></p>
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
                className="px-4 py-2 text-sm bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-50">
                {rejectMutation.isPending ? 'Rejecting...' : 'Reject'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
