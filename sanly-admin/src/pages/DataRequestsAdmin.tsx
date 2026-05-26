import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { FileText, CheckCircle2, XCircle, Clock, Eye } from 'lucide-react'
import {
  getAllDataRequests, startReview, approveRequest, rejectRequest, partialApproveRequest,
  type DataRequest,
} from '../api/dataRequests'
import Badge from '../components/ui/Badge'
import Modal from '../components/ui/Modal'
import { formatDateTime } from '../utils/formatters'

const STATUS_OPTS = ['', 'PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'PARTIALLY_APPROVED']
const TYPE_OPTS = ['', 'DELETE_MEDICAL_RECORDS', 'DELETE_CRIMINAL_RECORD', 'CORRECT_PERSONAL_INFO',
  'DELETE_AUDIT_LOG_ENTRIES', 'REVIEW_DATA_ACCESS', 'EXPORT_MY_DATA', 'DELETE_ACCOUNT', 'OTHER']

function statusBadgeStatus(s: string) {
  if (s === 'APPROVED') return 'ACTIVE'
  if (s === 'REJECTED') return 'SUSPENDED'
  return 'PENDING'
}

export default function DataRequestsAdmin() {
  const qc = useQueryClient()
  const [page, setPage] = useState(0)
  const [filterStatus, setFilterStatus] = useState('')
  const [filterType, setFilterType] = useState('')
  const [selected, setSelected] = useState<DataRequest | null>(null)
  const [actionNotes, setActionNotes] = useState('')
  const [actionType, setActionType] = useState<'approve' | 'reject' | 'partial' | null>(null)

  const { data: requestPage, isLoading } = useQuery({
    queryKey: ['data-requests', page, filterStatus, filterType],
    queryFn: () => getAllDataRequests({ page, size: 20, status: filterStatus || undefined, requestType: filterType || undefined }),
  })

  const reviewMutation = useMutation({
    mutationFn: (code: string) => startReview(code),
    onSuccess: (updated) => { setSelected(updated); qc.invalidateQueries({ queryKey: ['data-requests'] }) },
  })

  const actionMutation = useMutation({
    mutationFn: () => {
      if (!selected || !actionType) throw new Error('No target')
      if (actionType === 'approve') return approveRequest(selected.requestCode, actionNotes)
      if (actionType === 'reject') return rejectRequest(selected.requestCode, actionNotes)
      return partialApproveRequest(selected.requestCode, actionNotes)
    },
    onSuccess: (updated) => {
      setSelected(updated); setActionType(null); setActionNotes('')
      qc.invalidateQueries({ queryKey: ['data-requests'] })
    },
  })

  const requests = requestPage?.content ?? []

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-3">
        <FileText className="w-6 h-6 text-primary" />
        <h1 className="page-title">Data Requests</h1>
      </div>

      {/* Filters */}
      <div className="card !p-3 flex flex-wrap gap-3">
        <select className="input !py-1.5 !text-sm w-auto" value={filterStatus} onChange={e => { setFilterStatus(e.target.value); setPage(0) }}>
          {STATUS_OPTS.map(s => <option key={s} value={s}>{s || 'All Statuses'}</option>)}
        </select>
        <select className="input !py-1.5 !text-sm w-auto" value={filterType} onChange={e => { setFilterType(e.target.value); setPage(0) }}>
          {TYPE_OPTS.map(t => <option key={t} value={t}>{t ? t.replace(/_/g, ' ') : 'All Types'}</option>)}
        </select>
      </div>

      {/* Requests table */}
      <div className="card">
        {isLoading ? (
          <div className="space-y-3">{[1,2,3].map(i => <div key={i} className="skeleton h-12 rounded" />)}</div>
        ) : requests.length === 0 ? (
          <p className="text-sm text-gray-400 py-8 text-center">No data requests found.</p>
        ) : (
          <div className="space-y-2">
            {requests.map((r) => (
              <div key={r.requestId} className="flex items-start gap-3 p-3 rounded-xl border border-gray-100 bg-gray-50 hover:bg-gray-100 transition-colors">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 flex-wrap">
                    <span className="font-mono text-xs font-semibold">{r.requestCode}</span>
                    <Badge label={r.status.replace('_', ' ')} status={statusBadgeStatus(r.status)} size="sm" />
                  </div>
                  <p className="text-xs text-gray-500 mt-0.5">
                    NIN: {r.citizenNationalId} · {r.requestType.replace(/_/g, ' ')} · {r.affectedService}
                  </p>
                  <p className="text-xs text-gray-400">{formatDateTime(r.submittedAt)}</p>
                </div>
                <button
                  className="btn-secondary text-xs flex items-center gap-1 shrink-0"
                  onClick={() => setSelected(r)}
                >
                  <Eye className="w-3 h-3" />Review
                </button>
              </div>
            ))}

            {requestPage && requestPage.totalPages > 1 && (
              <div className="flex justify-center gap-2 pt-2">
                <button className="btn-secondary text-sm" disabled={page === 0} onClick={() => setPage(p => p - 1)}>Previous</button>
                <span className="text-sm text-gray-500 self-center">Page {page + 1} of {requestPage.totalPages}</span>
                <button className="btn-secondary text-sm" disabled={requestPage.last} onClick={() => setPage(p => p + 1)}>Next</button>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Detail Modal */}
      {selected && (
        <Modal
          isOpen={!!selected}
          onClose={() => { setSelected(null); setActionType(null); setActionNotes('') }}
          title={selected.requestCode}
          footer={
            selected.status === 'PENDING' || selected.status === 'UNDER_REVIEW' ? (
              <div className="flex gap-2 flex-wrap">
                {selected.status === 'PENDING' && (
                  <button className="btn-secondary text-sm flex items-center gap-1"
                    disabled={reviewMutation.isPending}
                    onClick={() => reviewMutation.mutate(selected.requestCode)}>
                    <Clock className="w-3 h-3" /> Start Review
                  </button>
                )}
                <button className="btn-primary text-sm flex items-center gap-1" onClick={() => setActionType('approve')}>
                  <CheckCircle2 className="w-3 h-3" /> Approve
                </button>
                <button className="btn-secondary text-sm flex items-center gap-1" onClick={() => setActionType('partial')}>
                  Partial
                </button>
                <button className="text-sm px-3 py-1.5 rounded-lg border border-red-200 text-red-600 hover:bg-red-50 flex items-center gap-1"
                  onClick={() => setActionType('reject')}>
                  <XCircle className="w-3 h-3" /> Reject
                </button>
              </div>
            ) : (
              <button className="btn-secondary text-sm" onClick={() => setSelected(null)}>Close</button>
            )
          }
        >
          <div className="space-y-3">
            <div className="grid grid-cols-2 gap-2 text-sm">
              {[
                ['NIN', selected.citizenNationalId],
                ['Type', selected.requestType.replace(/_/g, ' ')],
                ['Service', selected.affectedService],
                ['Status', selected.status],
                ['Submitted', formatDateTime(selected.submittedAt)],
                ...(selected.reviewedAt ? [['Reviewed', formatDateTime(selected.reviewedAt)]] : []),
                ...(selected.reviewedByOfficerId ? [['Officer', selected.reviewedByOfficerId]] : []),
              ].map(([k, v]) => (
                <div key={k}>
                  <p className="text-xs text-gray-400 uppercase tracking-wide">{k}</p>
                  <p className="text-sm font-medium text-dark">{v}</p>
                </div>
              ))}
            </div>

            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide mb-1">Description</p>
              <p className="text-sm text-gray-700 bg-gray-50 rounded-lg p-3">{selected.description}</p>
            </div>

            {selected.reviewerNotes && (
              <div>
                <p className="text-xs text-gray-400 uppercase tracking-wide mb-1">Reviewer Notes</p>
                <p className="text-sm text-gray-700">{selected.reviewerNotes}</p>
              </div>
            )}

            {selected.resolutionDescription && (
              <div>
                <p className="text-xs text-gray-400 uppercase tracking-wide mb-1">Resolution</p>
                <p className="text-sm text-gray-700">{selected.resolutionDescription}</p>
              </div>
            )}

            {actionType && (
              <div className="border-t pt-3">
                <label className="label">
                  {actionType === 'approve' ? 'Resolution Description' : actionType === 'reject' ? 'Rejection Reason' : 'Partial Fulfillment Notes'}
                </label>
                <textarea
                  className="input min-h-[80px]"
                  value={actionNotes}
                  onChange={(e) => setActionNotes(e.target.value)}
                  placeholder={actionType === 'reject' ? 'Explain what cannot be fulfilled and why (legal retention requirements)...' : 'Describe what was done...'}
                />
                {actionType === 'approve' && selected.requestType === 'EXPORT_MY_DATA' && (
                  <p className="text-xs text-blue-600 mt-1">A data export will be automatically compiled and made available to the citizen.</p>
                )}
                <button
                  className="btn-primary mt-2 text-sm"
                  disabled={!actionNotes.trim() || actionMutation.isPending}
                  onClick={() => actionMutation.mutate()}
                >
                  {actionMutation.isPending ? 'Saving...' : `Confirm ${actionType === 'partial' ? 'Partial Approval' : actionType.charAt(0).toUpperCase() + actionType.slice(1)}`}
                </button>
              </div>
            )}
          </div>
        </Modal>
      )}
    </div>
  )
}
