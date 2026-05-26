import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { AlertTriangle, CheckCircle, XCircle } from 'lucide-react'
import { getAnomalies, getAnomalySummary, reviewAnomaly } from '../api/bridge'
import type { AnomalyAlert } from '../types'
import Badge from '../components/ui/Badge'
import Modal from '../components/ui/Modal'
import { formatDateTime, relativeTime } from '../utils/formatters'

const ANOMALY_LABELS: Record<string, string> = {
  HIGH_VOLUME_QUERIES: 'High Volume Queries',
  REPEATED_CITIZEN_QUERY: 'Repeated Citizen Query',
  OFF_HOURS_SENSITIVE_ACCESS: 'Off-Hours Sensitive Access',
  SELF_QUERY_SUSPICION: 'Unaccompanied Routine Check',
  BULK_CITIZEN_SCAN: 'Bulk Citizen Scan',
  DENIED_REPEATED_ATTEMPT: 'Repeated Denied Attempts',
}

function AlertCard({ alert, onReview }: { alert: AnomalyAlert; onReview: (a: AnomalyAlert, action: 'REVIEWED' | 'DISMISSED') => void }) {
  return (
    <div className={`card hover:shadow-card-hover transition-shadow ${
      alert.severity === 'CRITICAL' ? 'border-red-200' :
      alert.severity === 'HIGH'     ? 'border-orange-200' :
      alert.severity === 'MEDIUM'   ? 'border-amber-200' : ''
    }`}>
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-center gap-2 flex-wrap">
          <Badge label={alert.severity} status={alert.severity} />
          <span className="text-sm font-semibold text-dark">{ANOMALY_LABELS[alert.alertType] ?? alert.alertType.replace(/_/g, ' ')}</span>
        </div>
        {alert.status === 'OPEN' && (
          <div className="flex gap-1.5 shrink-0">
            <button onClick={() => onReview(alert, 'REVIEWED')} className="btn-secondary text-xs py-1 gap-1">
              <CheckCircle className="w-3.5 h-3.5 text-green-600" /> Review
            </button>
            <button onClick={() => onReview(alert, 'DISMISSED')} className="btn-ghost text-xs py-1 gap-1 text-gray-400">
              <XCircle className="w-3.5 h-3.5" /> Dismiss
            </button>
          </div>
        )}
      </div>
      <p className="text-sm text-gray-600 mt-2">{alert.description}</p>
      <div className="flex items-center gap-4 mt-3 text-xs text-gray-400 flex-wrap">
        <span>Institution: <span className="font-mono font-semibold text-dark">{alert.institutionCode}</span></span>
        {alert.nationalIdInvolved && <span>NID: <span className="font-mono">{alert.nationalIdInvolved}</span></span>}
        <span>Detected {relativeTime(alert.detectedAt)}</span>
        {alert.reviewedBy && <span>Reviewed by <span className="font-medium">{alert.reviewedBy}</span> on {formatDateTime(alert.reviewedAt)}</span>}
      </div>
    </div>
  )
}

export default function Anomalies() {
  const qc = useQueryClient()
  const [tab, setTab] = useState<'OPEN' | 'REVIEWED' | 'DISMISSED'>('OPEN')
  const [reviewTarget, setReviewTarget] = useState<{ alert: AnomalyAlert; action: 'REVIEWED' | 'DISMISSED' } | null>(null)
  const [reviewer, setReviewer] = useState('')

  const { data: summary } = useQuery({
    queryKey: ['anomaly-summary'], queryFn: getAnomalySummary, refetchInterval: 30_000,
  })

  const { data, isLoading } = useQuery({
    queryKey: ['anomalies', tab],
    queryFn: () => getAnomalies({ status: tab, size: 100 }),
  })

  const mutation = useMutation({
    mutationFn: () => reviewAnomaly(reviewTarget!.alert.alertId, { status: reviewTarget!.action, reviewedBy: reviewer }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['anomalies'] })
      qc.invalidateQueries({ queryKey: ['anomaly-summary'] })
      setReviewTarget(null)
      setReviewer('')
    },
  })

  const TABS: { key: 'OPEN' | 'REVIEWED' | 'DISMISSED'; label: string }[] = [
    { key: 'OPEN', label: `Open (${summary?.totalOpen ?? '…'})` },
    { key: 'REVIEWED', label: 'Reviewed' },
    { key: 'DISMISSED', label: 'Dismissed' },
  ]

  return (
    <div className="space-y-5">
      {/* Summary bar */}
      {summary && (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          {[
            { sev: 'CRITICAL', color: 'text-red-600 bg-red-50 border-red-200' },
            { sev: 'HIGH',     color: 'text-orange-600 bg-orange-50 border-orange-200' },
            { sev: 'MEDIUM',   color: 'text-amber-600 bg-amber-50 border-amber-200' },
            { sev: 'LOW',      color: 'text-blue-600 bg-blue-50 border-blue-200' },
          ].map(({ sev, color }) => (
            <div key={sev} className={`card !p-4 border ${color}`}>
              <p className="text-2xl font-bold">{summary.openBySeverity[sev] ?? 0}</p>
              <p className="text-xs font-semibold mt-0.5 uppercase tracking-wide">{sev} Open</p>
            </div>
          ))}
        </div>
      )}

      {/* Tabs */}
      <div className="flex gap-1 border-b border-gray-200">
        {TABS.map((t) => (
          <button key={t.key} onClick={() => setTab(t.key)}
            className={`px-4 py-2 text-sm font-medium transition-colors border-b-2 -mb-px ${
              tab === t.key ? 'border-primary text-primary' : 'border-transparent text-gray-500 hover:text-dark'
            }`}>
            {t.label}
          </button>
        ))}
      </div>

      {/* Alerts */}
      {isLoading ? (
        <div className="space-y-3">{Array.from({ length: 4 }).map((_, i) => <div key={i} className="skeleton h-28 rounded-xl" />)}</div>
      ) : !data?.content.length ? (
        <div className="text-center py-16 text-gray-400">
          <AlertTriangle className="w-10 h-10 mx-auto mb-3 opacity-30" />
          <p>No {tab.toLowerCase()} alerts</p>
        </div>
      ) : (
        <div className="space-y-3">
          {data.content.map((a) => (
            <AlertCard key={a.alertId} alert={a}
              onReview={(alert, action) => { setReviewTarget({ alert, action }); setReviewer('') }} />
          ))}
        </div>
      )}

      {/* Review modal */}
      <Modal isOpen={!!reviewTarget} onClose={() => setReviewTarget(null)} title={`Mark as ${reviewTarget?.action}`}
        footer={
          <>
            <button onClick={() => setReviewTarget(null)} className="btn-secondary">Cancel</button>
            <button onClick={() => mutation.mutate()} disabled={!reviewer || mutation.isPending}
              className="btn-primary">
              {mutation.isPending ? 'Saving...' : 'Confirm'}
            </button>
          </>
        }>
        <div className="space-y-4">
          <p className="text-sm text-gray-600">
            Mark this alert as <strong>{reviewTarget?.action}</strong>. This action is recorded.
          </p>
          <div>
            <label className="label">Your name / officer ID</label>
            <input className="input" placeholder="e.g. admin or OFFICER-001"
              value={reviewer} onChange={(e) => setReviewer(e.target.value)} />
          </div>
        </div>
      </Modal>
    </div>
  )
}
