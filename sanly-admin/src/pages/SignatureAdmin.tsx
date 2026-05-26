import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { FileSignature, CheckCircle2, XCircle, Trash2 } from 'lucide-react'
import {
  adminGetAllSignatures, adminGetSignatureStats, adminRevokeSignature,
  type SignatureRecord,
} from '../api/signature'
import Badge from '../components/ui/Badge'
import DataTable from '../components/ui/DataTable'
import Modal from '../components/ui/Modal'
import { formatDateTime } from '../utils/formatters'

export default function SignatureAdmin() {
  const qc = useQueryClient()
  const [page, setPage] = useState(0)
  const [revokeTarget, setRevokeTarget] = useState<SignatureRecord | null>(null)
  const [revokeReason, setRevokeReason] = useState('')

  const { data: stats } = useQuery({
    queryKey: ['sig-stats'],
    queryFn: adminGetSignatureStats,
    refetchInterval: 30_000,
  })

  const { data: sigPage, isLoading } = useQuery({
    queryKey: ['admin-signatures', page],
    queryFn: () => adminGetAllSignatures(page, 20),
  })

  const revokeMutation = useMutation({
    mutationFn: () => adminRevokeSignature(revokeTarget!.signatureCode, revokeReason),
    onSuccess: () => {
      setRevokeTarget(null)
      setRevokeReason('')
      qc.invalidateQueries({ queryKey: ['admin-signatures'] })
      qc.invalidateQueries({ queryKey: ['sig-stats'] })
    },
  })

  const sigs = sigPage?.content ?? []

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-3">
        <FileSignature className="w-6 h-6 text-primary" />
        <h1 className="page-title">Digital Signatures</h1>
      </div>

      {/* Stats */}
      {stats && (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {[
            { label: 'Signed Today', value: stats.signedToday, icon: <CheckCircle2 className="w-5 h-5 text-green-500" /> },
            { label: 'Verified Today', value: stats.verifiedToday, icon: <FileSignature className="w-5 h-5 text-primary" /> },
            { label: 'Total Revoked', value: stats.revokedTotal, icon: <XCircle className="w-5 h-5 text-red-500" /> },
            { label: 'Total Signatures', value: stats.totalSignatures, icon: <FileSignature className="w-5 h-5 text-gray-400" /> },
          ].map(({ label, value, icon }) => (
            <div key={label} className="card !p-4">
              <div className="flex items-center gap-2 mb-1">{icon}<p className="text-xs text-gray-400 uppercase tracking-wide">{label}</p></div>
              <p className="text-2xl font-bold text-dark">{value}</p>
            </div>
          ))}
        </div>
      )}

      {/* Table */}
      <div className="card">
        <p className="section-title">All Signatures</p>
        {isLoading ? (
          <div className="space-y-3">{[1,2,3].map(i => <div key={i} className="skeleton h-10 rounded" />)}</div>
        ) : (
          <>
            <DataTable<SignatureRecord>
              columns={[
                {
                  key: 'signatureCode',
                  header: 'Code',
                  className: 'font-mono text-xs',
                },
                { key: 'signerNationalId', header: 'NIN', className: 'font-mono text-xs' },
                {
                  key: 'documentName',
                  header: 'Document',
                  render: (s) => <span className="text-xs truncate max-w-[120px] block">{s.documentName}</span>,
                },
                {
                  key: 'purpose',
                  header: 'Purpose',
                  render: (s) => <span className="text-xs truncate max-w-[150px] block">{s.purpose}</span>,
                },
                {
                  key: 'status',
                  header: 'Status',
                  render: (s) => <Badge label={s.status} status={s.status === 'VALID' ? 'ACTIVE' : 'SUSPENDED'} size="sm" />,
                },
                {
                  key: 'signedAt',
                  header: 'Signed At',
                  render: (s) => <span className="text-xs text-gray-500">{formatDateTime(s.signedAt)}</span>,
                },
                {
                  key: 'signatureId',
                  header: 'Actions',
                  render: (s) => s.status === 'VALID' ? (
                    <button
                      className="text-red-400 hover:text-red-600 p-1 rounded"
                      onClick={() => setRevokeTarget(s)}
                      title="Revoke"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  ) : null,
                },
              ]}
              data={sigs}
              keyExtractor={(s) => s.signatureId}
            />

            {/* Pagination */}
            {sigPage && sigPage.totalPages > 1 && (
              <div className="flex justify-center gap-2 mt-4">
                <button className="btn-secondary text-sm" disabled={page === 0} onClick={() => setPage(p => p - 1)}>
                  Previous
                </button>
                <span className="text-sm text-gray-500 self-center">
                  Page {page + 1} of {sigPage.totalPages}
                </span>
                <button className="btn-secondary text-sm" disabled={sigPage.last} onClick={() => setPage(p => p + 1)}>
                  Next
                </button>
              </div>
            )}
          </>
        )}
      </div>

      {/* Revoke Modal */}
      <Modal
        isOpen={!!revokeTarget}
        onClose={() => setRevokeTarget(null)}
        title={`Revoke ${revokeTarget?.signatureCode}`}
        footer={
          <>
            <button className="btn-secondary" onClick={() => setRevokeTarget(null)}>Cancel</button>
            <button
              className="bg-red-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-red-700 disabled:opacity-50"
              disabled={!revokeReason.trim() || revokeMutation.isPending}
              onClick={() => revokeMutation.mutate()}
            >
              {revokeMutation.isPending ? 'Revoking...' : 'Confirm Revoke'}
            </button>
          </>
        }
      >
        <p className="text-sm text-amber-600 mb-3">
          This action is irreversible. The signature record will be permanently marked as REVOKED.
        </p>
        <label className="label">Reason</label>
        <textarea
          className="input min-h-[80px]"
          value={revokeReason}
          onChange={(e) => setRevokeReason(e.target.value)}
          placeholder="Enter admin reason for revocation..."
        />
      </Modal>
    </div>
  )
}
