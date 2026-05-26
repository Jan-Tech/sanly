import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Grid3X3, List, Trash2, CheckCircle } from 'lucide-react'
import { getPermissions, grantPermission, revokePermission, getInstitutions } from '../api/bridge'
import type { InstitutionPermission } from '../types'
import DataTable from '../components/ui/DataTable'
import Modal from '../components/ui/Modal'
import ConfirmDialog from '../components/ui/ConfirmDialog'
import { formatDate } from '../utils/formatters'

const DATA_TYPES = [
  'VISION_TEST', 'MEDICAL_CLEARANCE', 'CRIMINAL_RECORD', 'TAX_STATUS',
  'DRIVING_LICENSE', 'BUSINESS_REGISTRATION', 'BIRTH_RECORD', 'MARRIAGE_RECORD', 'DEATH_RECORD',
]

export default function Permissions() {
  const qc = useQueryClient()
  const [view, setView] = useState<'table' | 'matrix'>('table')
  const [grantModal, setGrantModal] = useState(false)
  const [revokeTarget, setRevokeTarget] = useState<InstitutionPermission | null>(null)
  const [form, setForm] = useState({ requestingCode: '', targetCode: '', dataType: '' })

  const { data: permissions, isLoading } = useQuery({
    queryKey: ['permissions'], queryFn: getPermissions,
  })

  const { data: institutions } = useQuery({
    queryKey: ['institutions'], queryFn: getInstitutions,
  })

  const grantMutation = useMutation({
    mutationFn: () => grantPermission(form),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['permissions'] }); setGrantModal(false); setForm({ requestingCode: '', targetCode: '', dataType: '' }) },
  })

  const revokeMutation = useMutation({
    mutationFn: () => revokePermission(revokeTarget!.id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['permissions'] }); setRevokeTarget(null) },
  })

  const active = (permissions ?? []).filter((p) => p.active)
  const institutionCodes = (institutions ?? []).map((i) => i.institutionCode)

  // Build permission matrix: requesting → dataType → Set<targetCodes>
  const matrix: Record<string, Record<string, boolean>> = {}
  active.forEach((p) => {
    if (!matrix[p.requestingCode]) matrix[p.requestingCode] = {}
    matrix[p.requestingCode][p.dataType] = true
  })

  const columns = [
    { key: 'requestingCode', header: 'Requesting', className: 'font-mono text-xs font-semibold' },
    { key: 'targetCode', header: 'Target', className: 'font-mono text-xs' },
    { key: 'dataType', header: 'Data Type', render: (p: InstitutionPermission) =>
      <span className="text-xs bg-blue-50 text-blue-700 px-2 py-0.5 rounded font-mono">{p.dataType}</span> },
    { key: 'grantedAt', header: 'Granted', render: (p: InstitutionPermission) =>
      <span className="text-xs text-gray-500">{formatDate(p.grantedAt)}</span> },
    { key: 'actions', header: '', render: (p: InstitutionPermission) => (
      <button onClick={() => setRevokeTarget(p)} className="btn-ghost text-red-500 hover:text-red-700 text-xs py-1">
        <Trash2 className="w-3.5 h-3.5" /> Revoke
      </button>
    )},
  ]

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div className="flex gap-1 bg-gray-100 rounded-lg p-0.5">
          <button onClick={() => setView('table')} className={`px-3 py-1.5 rounded-md text-sm font-medium transition-colors flex items-center gap-1.5 ${view === 'table' ? 'bg-white shadow-sm text-dark' : 'text-gray-500'}`}>
            <List className="w-3.5 h-3.5" /> Table
          </button>
          <button onClick={() => setView('matrix')} className={`px-3 py-1.5 rounded-md text-sm font-medium transition-colors flex items-center gap-1.5 ${view === 'matrix' ? 'bg-white shadow-sm text-dark' : 'text-gray-500'}`}>
            <Grid3X3 className="w-3.5 h-3.5" /> Matrix
          </button>
        </div>
        <button onClick={() => setGrantModal(true)} className="btn-primary text-sm">
          <Plus className="w-4 h-4" /> Grant Permission
        </button>
      </div>

      {view === 'table' ? (
        <div className="card !p-0 overflow-hidden">
          <DataTable<InstitutionPermission>
            columns={columns} data={active}
            keyExtractor={(p) => String(p.id)}
            loading={isLoading} emptyMessage="No permissions configured."
          />
        </div>
      ) : (
        <div className="card overflow-x-auto">
          <p className="section-title">Permission Matrix — Requesting Institution × Data Type</p>
          <table className="text-xs">
            <thead>
              <tr className="border-b border-gray-100">
                <th className="text-left px-3 py-2 text-gray-400 font-medium min-w-36">Institution</th>
                {DATA_TYPES.map((dt) => (
                  <th key={dt} className="px-2 py-2 text-gray-400 font-medium text-center min-w-20 whitespace-nowrap">
                    {dt.replace(/_/g, ' ')}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {institutionCodes.map((code) => (
                <tr key={code} className="hover:bg-gray-50/50">
                  <td className="px-3 py-2 font-mono font-semibold text-dark">{code}</td>
                  {DATA_TYPES.map((dt) => (
                    <td key={dt} className="px-2 py-2 text-center">
                      {matrix[code]?.[dt]
                        ? <CheckCircle className="w-4 h-4 text-green-500 mx-auto" />
                        : <span className="text-gray-200">—</span>}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Grant modal */}
      <Modal isOpen={grantModal} onClose={() => setGrantModal(false)} title="Grant Permission"
        footer={
          <>
            <button onClick={() => setGrantModal(false)} className="btn-secondary">Cancel</button>
            <button onClick={() => grantMutation.mutate()} disabled={grantMutation.isPending || !form.requestingCode || !form.targetCode || !form.dataType} className="btn-primary">
              {grantMutation.isPending ? 'Granting...' : 'Grant'}
            </button>
          </>
        }>
        <div className="space-y-4">
          <div>
            <label className="label">Requesting Institution</label>
            <select className="select" value={form.requestingCode} onChange={(e) => setForm((f) => ({ ...f, requestingCode: e.target.value }))}>
              <option value="">Select…</option>
              {institutionCodes.map((c) => <option key={c} value={c}>{c}</option>)}
            </select>
          </div>
          <div>
            <label className="label">Target Institution (data provider)</label>
            <select className="select" value={form.targetCode} onChange={(e) => setForm((f) => ({ ...f, targetCode: e.target.value }))}>
              <option value="">Select…</option>
              {institutionCodes.map((c) => <option key={c} value={c}>{c}</option>)}
            </select>
          </div>
          <div>
            <label className="label">Data Type</label>
            <select className="select" value={form.dataType} onChange={(e) => setForm((f) => ({ ...f, dataType: e.target.value }))}>
              <option value="">Select…</option>
              {DATA_TYPES.map((dt) => <option key={dt} value={dt}>{dt}</option>)}
            </select>
          </div>
          {form.requestingCode && form.targetCode && form.dataType && (
            <div className="p-3 bg-primary/5 rounded-lg text-sm text-primary">
              <strong>{form.requestingCode}</strong> → query <strong>{form.dataType}</strong> from <strong>{form.targetCode}</strong>
            </div>
          )}
        </div>
      </Modal>

      <ConfirmDialog
        isOpen={!!revokeTarget} onClose={() => setRevokeTarget(null)}
        onConfirm={() => revokeMutation.mutate()}
        title="Revoke Permission"
        message={`Revoke ${revokeTarget?.requestingCode}'s access to ${revokeTarget?.dataType} from ${revokeTarget?.targetCode}?`}
        confirmLabel="Revoke" danger loading={revokeMutation.isPending}
      />
    </div>
  )
}
