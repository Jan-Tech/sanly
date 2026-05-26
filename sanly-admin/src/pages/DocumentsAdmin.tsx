import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { FileText, Activity, CheckCircle2, Trash2 } from 'lucide-react'
import {
  getAdminDocumentStats, getAdminCertificates, adminRevokeCertificate,
  getAdminTrackedItems, type AdminCertificate, type AdminTrackedItem,
} from '../api/documents'
import Badge from '../components/ui/Badge'
import DataTable from '../components/ui/DataTable'
import { formatDate, formatDateTime } from '../utils/formatters'

const CERT_STATUS_COLORS: Record<string, 'ACTIVE' | 'SUSPENDED' | 'PENDING'> = {
  ACTIVE: 'ACTIVE',
  EXPIRED: 'PENDING',
  REVOKED: 'SUSPENDED',
}

const TRACK_STATUS_COLORS: Record<string, 'ACTIVE' | 'COMPLIANT' | 'SUSPENDED' | 'PENDING'> = {
  COMPLETED: 'COMPLIANT',
  APPROVED: 'COMPLIANT',
  REJECTED: 'SUSPENDED',
  CANCELLED: 'SUSPENDED',
  SUBMITTED: 'ACTIVE',
  IN_REVIEW: 'ACTIVE',
  PENDING_DOCS: 'PENDING',
  ON_HOLD: 'PENDING',
}

interface StatCardProps { label: string; value: number; icon: React.ReactNode; color?: string }
function StatCard({ label, value, icon, color = 'text-primary' }: StatCardProps) {
  return (
    <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-4 flex items-center gap-3">
      <div className={`w-10 h-10 rounded-xl bg-gray-50 flex items-center justify-center ${color}`}>{icon}</div>
      <div>
        <p className="text-2xl font-bold text-gray-900">{value.toLocaleString()}</p>
        <p className="text-xs text-gray-400">{label}</p>
      </div>
    </div>
  )
}

export default function DocumentsAdmin() {
  const qc = useQueryClient()
  const [activeTab, setActiveTab] = useState<'certificates' | 'tracker'>('certificates')
  const [certPage, setCertPage] = useState(0)
  const [trackPage, setTrackPage] = useState(0)
  const [revokeCode, setRevokeCode] = useState<string | null>(null)
  const [revokeReason, setRevokeReason] = useState('')

  const { data: stats } = useQuery({
    queryKey: ['docs-admin-stats'],
    queryFn: getAdminDocumentStats,
    refetchInterval: 30_000,
  })

  const { data: certsData, isLoading: certsLoading } = useQuery({
    queryKey: ['admin-certs', certPage],
    queryFn: () => getAdminCertificates({ page: certPage, size: 20 }),
  })

  const { data: trackData, isLoading: trackLoading } = useQuery({
    queryKey: ['admin-tracking', trackPage],
    queryFn: () => getAdminTrackedItems({ page: trackPage, size: 20 }),
  })

  const revokeMutation = useMutation({
    mutationFn: () => adminRevokeCertificate(revokeCode!, revokeReason),
    onSuccess: () => {
      setRevokeCode(null)
      setRevokeReason('')
      qc.invalidateQueries({ queryKey: ['admin-certs'] })
      qc.invalidateQueries({ queryKey: ['docs-admin-stats'] })
    },
  })

  const certColumns = [
    { header: 'Code', cell: (c: AdminCertificate) => <span className="font-mono text-xs text-blue-700">{c.certificateCode}</span> },
    { header: 'NIN', cell: (c: AdminCertificate) => <span className="font-mono text-xs">{c.citizenNationalId}</span> },
    { header: 'Type', cell: (c: AdminCertificate) => c.documentType.replace(/_/g, ' ') },
    { header: 'Issued', cell: (c: AdminCertificate) => formatDate(c.issuedAt) },
    { header: 'Expires', cell: (c: AdminCertificate) => formatDate(c.expiresAt) },
    { header: 'Status', cell: (c: AdminCertificate) => <Badge status={CERT_STATUS_COLORS[c.status] ?? 'PENDING'} label={c.status} /> },
    {
      header: '',
      cell: (c: AdminCertificate) => c.status === 'ACTIVE' ? (
        <button
          onClick={() => setRevokeCode(c.certificateCode)}
          className="flex items-center gap-1 text-xs text-red-600 hover:text-red-700"
        >
          <Trash2 className="w-3 h-3" /> Revoke
        </button>
      ) : null,
    },
  ]

  const trackColumns = [
    { header: 'Code', cell: (item: AdminTrackedItem) => <span className="font-mono text-xs text-blue-700">{item.trackingCode}</span> },
    { header: 'NIN', cell: (item: AdminTrackedItem) => <span className="font-mono text-xs">{item.citizenNationalId}</span> },
    { header: 'Title', cell: (item: AdminTrackedItem) => <span className="truncate max-w-xs block">{item.title}</span> },
    { header: 'Service', cell: (item: AdminTrackedItem) => item.sourceService },
    { header: 'Type', cell: (item: AdminTrackedItem) => item.itemType.replace(/_/g, ' ') },
    { header: 'Updated', cell: (item: AdminTrackedItem) => formatDateTime(item.lastUpdatedAt) },
    { header: 'Status', cell: (item: AdminTrackedItem) => <Badge status={TRACK_STATUS_COLORS[item.currentStatus] ?? 'PENDING'} label={item.currentStatus} /> },
  ]

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-3">
        <FileText className="w-6 h-6 text-primary" />
        <h1 className="text-xl font-bold text-gray-900">Documents & Tracking</h1>
      </div>

      {/* Stats */}
      {stats && (
        <div className="grid grid-cols-2 lg:grid-cols-3 gap-4">
          <StatCard label="Active Certificates" value={stats.activeCerts} icon={<CheckCircle2 className="w-5 h-5" />} color="text-green-600" />
          <StatCard label="Generated Today" value={stats.certsGeneratedToday} icon={<FileText className="w-5 h-5" />} color="text-blue-500" />
          <StatCard label="Verifications Today" value={stats.verificationsToday} icon={<Activity className="w-5 h-5" />} color="text-purple-500" />
        </div>
      )}

      {/* Tabs */}
      <div className="flex gap-2 border-b border-gray-200">
        {(['certificates', 'tracker'] as const).map(tab => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors capitalize -mb-px ${
              activeTab === tab ? 'border-primary text-primary' : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            {tab === 'certificates' ? 'Certificates' : 'Status Tracker'}
          </button>
        ))}
      </div>

      {activeTab === 'certificates' && (
        <DataTable
          columns={certColumns}
          data={certsData?.content ?? []}
          isLoading={certsLoading}
          page={certPage}
          totalPages={certsData?.totalPages ?? 0}
          onPageChange={setCertPage}
          rowKey={(c: AdminCertificate) => c.certificateId}
        />
      )}

      {activeTab === 'tracker' && (
        <DataTable
          columns={trackColumns}
          data={trackData?.content ?? []}
          isLoading={trackLoading}
          page={trackPage}
          totalPages={trackData?.totalPages ?? 0}
          onPageChange={setTrackPage}
          rowKey={(item: AdminTrackedItem) => item.trackingId}
        />
      )}

      {/* Revoke Modal */}
      {revokeCode && (
        <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-md p-6 space-y-4">
            <h3 className="font-bold text-gray-900">Revoke Certificate</h3>
            <p className="text-sm text-gray-600">
              Revoking <strong className="font-mono">{revokeCode}</strong>. This cannot be undone.
            </p>
            <textarea
              value={revokeReason}
              onChange={e => setRevokeReason(e.target.value)}
              placeholder="Reason for revocation..."
              rows={3}
              className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-400"
            />
            <div className="flex gap-3 justify-end">
              <button onClick={() => { setRevokeCode(null); setRevokeReason('') }} className="px-4 py-2 text-sm text-gray-600 hover:text-gray-800">Cancel</button>
              <button
                onClick={() => revokeMutation.mutate()}
                disabled={!revokeReason.trim() || revokeMutation.isPending}
                className="px-4 py-2 bg-red-600 text-white text-sm font-medium rounded-lg hover:bg-red-700 disabled:opacity-50"
              >
                {revokeMutation.isPending ? 'Revoking...' : 'Revoke'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
