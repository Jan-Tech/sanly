import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Award, CheckCircle, XCircle } from 'lucide-react'
import { getAllDiplomas, revokeDiploma, type Diploma } from '../api/education'
import ConfirmDialog from '../components/ui/ConfirmDialog'
import Modal from '../components/ui/Modal'

const LEVEL_LABELS: Record<string, string> = {
  PRIMARY: 'Primary',
  SECONDARY: 'Secondary',
  VOCATIONAL_CERTIFICATE: 'Vocational Cert.',
  BACHELORS: "Bachelor's",
  MASTERS: "Master's",
  PHD: 'PhD',
  POSTDOCTORAL: 'Postdoctoral',
}

const HONORS_LABELS: Record<string, string> = {
  NONE: '',
  CUM_LAUDE: 'Cum Laude',
  MAGNA_CUM_LAUDE: 'Magna Cum Laude',
  SUMMA_CUM_LAUDE: 'Summa Cum Laude',
}

export default function EducationDiplomas() {
  const qc = useQueryClient()
  const [search, setSearch] = useState('')
  const [filterStatus, setFilterStatus] = useState<'ALL' | 'VALID' | 'REVOKED'>('ALL')
  const [filterLevel, setFilterLevel] = useState('ALL')
  const [revokeTarget, setRevokeTarget] = useState<Diploma | null>(null)
  const [revokeReason, setRevokeReason] = useState('')
  const [showConfirm, setShowConfirm] = useState(false)

  const { data: diplomas, isLoading } = useQuery({
    queryKey: ['edu-diplomas'],
    queryFn: getAllDiplomas,
  })

  const mutation = useMutation({
    mutationFn: ({ code, reason }: { code: string; reason: string }) =>
      revokeDiploma(code, reason),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['edu-diplomas'] })
      setRevokeTarget(null)
      setRevokeReason('')
      setShowConfirm(false)
    },
  })

  const levels = Array.from(new Set((diplomas ?? []).map(d => d.programLevel)))

  const filtered = (diplomas ?? []).filter(d => {
    const matchSearch = search === '' ||
      d.diplomaCode.toLowerCase().includes(search.toLowerCase()) ||
      d.citizenNationalId.toLowerCase().includes(search.toLowerCase()) ||
      d.programName.toLowerCase().includes(search.toLowerCase()) ||
      d.institutionCode.toLowerCase().includes(search.toLowerCase())
    const matchStatus = filterStatus === 'ALL' || d.status === filterStatus
    const matchLevel = filterLevel === 'ALL' || d.programLevel === filterLevel
    return matchSearch && matchStatus && matchLevel
  })

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-2">
        <Award className="w-5 h-5 text-primary" />
        <h1 className="text-xl font-bold text-dark">Diplomas</h1>
        <span className="ml-auto text-sm text-gray-400">{filtered.length} / {diplomas?.length ?? 0}</span>
      </div>

      {/* Filters */}
      <div className="flex flex-wrap gap-3">
        <input
          type="text"
          placeholder="Search diploma code, NIN, program…"
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="input flex-1 min-w-48"
        />
        <select value={filterStatus} onChange={e => setFilterStatus(e.target.value as any)} className="input w-36">
          <option value="ALL">All statuses</option>
          <option value="VALID">Valid</option>
          <option value="REVOKED">Revoked</option>
        </select>
        <select value={filterLevel} onChange={e => setFilterLevel(e.target.value)} className="input w-44">
          <option value="ALL">All levels</option>
          {levels.map(l => <option key={l} value={l}>{LEVEL_LABELS[l] || l}</option>)}
        </select>
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {[1,2,3].map(i => <div key={i} className="skeleton h-16 rounded-xl" />)}
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-100 overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-xs uppercase text-gray-400 tracking-wide">
              <tr>
                <th className="text-left px-4 py-3">Diploma Code</th>
                <th className="text-left px-4 py-3">Citizen NIN</th>
                <th className="text-left px-4 py-3">Institution</th>
                <th className="text-left px-4 py-3">Program</th>
                <th className="text-left px-4 py-3">Level</th>
                <th className="text-left px-4 py-3">Graduated</th>
                <th className="text-left px-4 py-3">Honors</th>
                <th className="text-left px-4 py-3">Status</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {filtered.map(d => (
                <tr key={d.diplomaId} className="hover:bg-gray-50/50 transition-colors">
                  <td className="px-4 py-3 font-mono text-xs text-gray-600">{d.diplomaCode}</td>
                  <td className="px-4 py-3 font-mono text-xs text-gray-500">{d.citizenNationalId}</td>
                  <td className="px-4 py-3 text-gray-500 text-xs">{d.institutionCode}</td>
                  <td className="px-4 py-3 font-medium text-dark max-w-48 truncate">{d.programName}</td>
                  <td className="px-4 py-3 text-gray-500 text-xs">{LEVEL_LABELS[d.programLevel] || d.programLevel}</td>
                  <td className="px-4 py-3 text-gray-500 text-xs">{d.graduationDate}</td>
                  <td className="px-4 py-3 text-xs text-gray-500">{HONORS_LABELS[d.honors] || '—'}</td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-1.5">
                      {d.status === 'VALID'
                        ? <><CheckCircle className="w-3.5 h-3.5 text-green-500" /><span className="text-xs text-green-600 font-medium">Valid</span></>
                        : <><XCircle className="w-3.5 h-3.5 text-red-400" /><span className="text-xs text-red-500 font-medium">Revoked</span></>
                      }
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    {d.status === 'VALID' && (
                      <button
                        onClick={() => { setRevokeTarget(d); setRevokeReason('') }}
                        className="text-xs font-medium text-red-500 hover:text-red-700 transition-colors"
                      >
                        Revoke
                      </button>
                    )}
                    {d.status === 'REVOKED' && d.revokedReason && (
                      <span className="text-xs text-gray-400 italic truncate max-w-24 block" title={d.revokedReason}>
                        {d.revokedReason}
                      </span>
                    )}
                  </td>
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr><td colSpan={9} className="px-4 py-10 text-center text-gray-400">No diplomas found.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Revoke reason modal */}
      <Modal
        isOpen={revokeTarget !== null && !showConfirm}
        onClose={() => setRevokeTarget(null)}
        title={revokeTarget ? `Revoke Diploma ${revokeTarget.diplomaCode}` : ''}
        footer={
          <>
            <button onClick={() => setRevokeTarget(null)} className="btn-secondary text-sm">Cancel</button>
            <button
              onClick={() => setShowConfirm(true)}
              disabled={!revokeReason.trim()}
              className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white text-sm font-medium rounded-lg disabled:opacity-40 transition-colors"
            >
              Continue
            </button>
          </>
        }
      >
        <div className="space-y-4">
          <p className="text-sm text-gray-600">
            You are about to revoke the diploma for citizen <strong>{revokeTarget?.citizenNationalId}</strong>.
            This action will be recorded on SANLY Bridge and the citizen will be notified.
          </p>
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Reason for revocation *</label>
            <textarea
              className="input w-full h-24 resize-none"
              placeholder="Enter reason…"
              value={revokeReason}
              onChange={e => setRevokeReason(e.target.value)}
            />
          </div>
        </div>
      </Modal>

      <ConfirmDialog
        isOpen={showConfirm && revokeTarget !== null}
        onClose={() => setShowConfirm(false)}
        title="Confirm Revocation"
        message={revokeTarget ? `Revoke diploma ${revokeTarget.diplomaCode}? This cannot be undone.` : ''}
        confirmLabel="Revoke Diploma"
        onConfirm={() => revokeTarget && mutation.mutate({ code: revokeTarget.diplomaCode, reason: revokeReason })}
        loading={mutation.isPending}
        danger
      />
    </div>
  )
}
