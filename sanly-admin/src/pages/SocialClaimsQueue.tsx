import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { HeartHandshake, CheckCircle, XCircle } from 'lucide-react'
import { getPendingClaims, approveClaim, rejectClaim, type AdminBenefitClaim } from '../api/social'
import Badge from '../components/ui/Badge'
import ConfirmDialog from '../components/ui/ConfirmDialog'

export default function SocialClaimsQueue() {
  const qc = useQueryClient()
  const [approveTarget, setApproveTarget] = useState<AdminBenefitClaim | null>(null)
  const [rejectTarget, setRejectTarget] = useState<AdminBenefitClaim | null>(null)
  const [rejectReason, setRejectReason] = useState('')
  const [showRejectConfirm, setShowRejectConfirm] = useState(false)

  const { data: pending, isLoading } = useQuery({
    queryKey: ['social-pending-claims'],
    queryFn: getPendingClaims,
    refetchInterval: 30_000,
  })

  const approveMutation = useMutation({
    mutationFn: (claimCode: string) => approveClaim(claimCode),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['social-pending-claims'] })
      setApproveTarget(null)
    },
  })

  const rejectMutation = useMutation({
    mutationFn: ({ code, reason }: { code: string; reason: string }) => rejectClaim(code, reason),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['social-pending-claims'] })
      setRejectTarget(null)
      setRejectReason('')
      setShowRejectConfirm(false)
    },
  })

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-2">
        <HeartHandshake className="w-5 h-5 text-primary" />
        <h1 className="text-xl font-bold text-dark">Pending Benefit Claims</h1>
        <span className={`ml-1 px-2 py-0.5 rounded-full text-xs font-medium ${(pending?.length ?? 0) > 0 ? 'bg-amber-100 text-amber-700' : 'bg-gray-100 text-gray-500'}`}>
          {pending?.length ?? 0} pending
        </span>
      </div>

      <p className="text-sm text-gray-500">
        Approving a claim checks the citizen's tax compliance via SANLY Bridge. If <strong>NON_COMPLIANT</strong>, approval is blocked.
        Auto-triggered claims are already approved and will not appear here.
      </p>

      {isLoading ? (
        <div className="space-y-3">
          {[1,2,3].map(i => <div key={i} className="skeleton h-20 rounded-xl" />)}
        </div>
      ) : !pending || pending.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-100 p-10 text-center text-gray-400">
          <CheckCircle className="w-10 h-10 mx-auto mb-2 text-green-400" />
          <p className="font-medium">No pending claims</p>
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-100 overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-xs uppercase text-gray-400 tracking-wide">
              <tr>
                <th className="text-left px-4 py-3">Claim Code</th>
                <th className="text-left px-4 py-3">Citizen NIN</th>
                <th className="text-left px-4 py-3">Program</th>
                <th className="text-left px-4 py-3">Type</th>
                <th className="text-left px-4 py-3">Applied</th>
                <th className="text-left px-4 py-3">Status</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {pending.map(claim => (
                <tr key={claim.claimId} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-mono text-xs">{claim.claimCode}</td>
                  <td className="px-4 py-3 font-mono text-xs">{claim.citizenNationalId}</td>
                  <td className="px-4 py-3">{claim.programCode}</td>
                  <td className="px-4 py-3 capitalize text-gray-500">{claim.claimType.replace(/_/g, ' ')}</td>
                  <td className="px-4 py-3 text-gray-500">{claim.appliedAt?.slice(0, 10)}</td>
                  <td className="px-4 py-3"><Badge variant="PENDING" label="PENDING" /></td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2 justify-end">
                      <button
                        className="text-green-600 hover:text-green-800 p-1"
                        onClick={() => setApproveTarget(claim)}
                        title="Approve"
                      >
                        <CheckCircle className="w-4 h-4" />
                      </button>
                      <button
                        className="text-red-500 hover:text-red-700 p-1"
                        onClick={() => { setRejectTarget(claim); setRejectReason('') }}
                        title="Reject"
                      >
                        <XCircle className="w-4 h-4" />
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
        isOpen={!!approveTarget}
        title="Approve Claim"
        message={`Approve claim ${approveTarget?.claimCode} for citizen ${approveTarget?.citizenNationalId}? Tax compliance will be verified.`}
        confirmLabel="Approve"
        onConfirm={() => approveTarget && approveMutation.mutate(approveTarget.claimCode)}
        onClose={() => setApproveTarget(null)}
        loading={approveMutation.isPending}
      />

      {/* Reject modal */}
      {rejectTarget && !showRejectConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="bg-white rounded-2xl shadow-xl p-6 w-full max-w-md space-y-4">
            <h2 className="text-lg font-bold text-dark">Reject Claim</h2>
            <p className="text-sm text-gray-600">Claim: <span className="font-mono">{rejectTarget.claimCode}</span></p>
            <textarea
              className="w-full border border-gray-200 rounded-lg p-3 text-sm h-24 resize-none focus:outline-none focus:ring-2 focus:ring-primary/30"
              placeholder="Rejection reason (required)"
              value={rejectReason}
              onChange={e => setRejectReason(e.target.value)}
            />
            <div className="flex gap-3 justify-end">
              <button className="px-4 py-2 text-sm text-gray-600 border border-gray-200 rounded-lg hover:bg-gray-50"
                onClick={() => setRejectTarget(null)}>Cancel</button>
              <button
                className="px-4 py-2 text-sm bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-50"
                disabled={!rejectReason.trim()}
                onClick={() => setShowRejectConfirm(true)}
              >
                Reject
              </button>
            </div>
          </div>
        </div>
      )}

      <ConfirmDialog
        isOpen={showRejectConfirm}
        title="Confirm Rejection"
        message={`Reject claim ${rejectTarget?.claimCode}? Reason: "${rejectReason}"`}
        confirmLabel="Reject"
        danger
        onConfirm={() => rejectTarget && rejectMutation.mutate({ code: rejectTarget.claimCode, reason: rejectReason })}
        onClose={() => setShowRejectConfirm(false)}
        loading={rejectMutation.isPending}
      />
    </div>
  )
}
