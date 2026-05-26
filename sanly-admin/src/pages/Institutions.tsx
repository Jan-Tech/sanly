import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Key, RotateCcw } from 'lucide-react'
import { getInstitutions, registerInstitution, updateInstitutionStatus, rotateInstitutionKey } from '../api/bridge'
import type { Institution } from '../types'
import Badge from '../components/ui/Badge'
import DataTable from '../components/ui/DataTable'
import Modal from '../components/ui/Modal'
import ConfirmDialog from '../components/ui/ConfirmDialog'
import { formatDate } from '../utils/formatters'

const DATA_TYPES = [
  'VISION_TEST', 'MEDICAL_CLEARANCE', 'CRIMINAL_RECORD', 'TAX_STATUS',
  'DRIVING_LICENSE', 'BUSINESS_REGISTRATION', 'BIRTH_RECORD', 'MARRIAGE_RECORD', 'DEATH_RECORD',
]

export default function Institutions() {
  const qc = useQueryClient()
  const [registerModal, setRegisterModal] = useState(false)
  const [newKey, setNewKey] = useState<{ code: string; key: string } | null>(null)
  const [rotateTarget, setRotateTarget] = useState<Institution | null>(null)
  const [suspendTarget, setSuspendTarget] = useState<Institution | null>(null)

  const [form, setForm] = useState({
    institutionCode: '', name: '', description: '', publishableTypes: [] as string[]
  })

  const { data: institutions, isLoading } = useQuery({
    queryKey: ['institutions'], queryFn: getInstitutions,
  })

  const registerMutation = useMutation({
    mutationFn: () => registerInstitution(form),
    onSuccess: (data) => {
      qc.invalidateQueries({ queryKey: ['institutions'] })
      setRegisterModal(false)
      setNewKey({ code: form.institutionCode, key: data.rawApiKey })
      setForm({ institutionCode: '', name: '', description: '', publishableTypes: [] })
    },
  })

  const rotateMutation = useMutation({
    mutationFn: () => rotateInstitutionKey(rotateTarget!.institutionCode),
    onSuccess: (data) => {
      setRotateTarget(null)
      setNewKey({ code: rotateTarget!.institutionCode, key: data.rawApiKey })
    },
  })

  const statusMutation = useMutation({
    mutationFn: () => updateInstitutionStatus(
      suspendTarget!.institutionCode,
      suspendTarget!.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE'
    ),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['institutions'] }); setSuspendTarget(null) },
  })

  const columns = [
    { key: 'institutionCode', header: 'Code', className: 'font-mono text-xs font-semibold' },
    { key: 'name', header: 'Name' },
    { key: 'status', header: 'Status',
      render: (i: Institution) => <Badge label={i.status} status={i.status} size="sm" /> },
    { key: 'publishableTypes', header: 'Publishable Types',
      render: (i: Institution) => (
        <div className="flex flex-wrap gap-1">
          {(i.publishableTypes ?? []).slice(0, 3).map((t) => (
            <span key={t} className="text-[10px] bg-blue-50 text-blue-600 px-1.5 py-0.5 rounded">{t}</span>
          ))}
          {(i.publishableTypes ?? []).length > 3 && (
            <span className="text-[10px] text-gray-400">+{i.publishableTypes.length - 3}</span>
          )}
        </div>
      )},
    { key: 'actions', header: '',
      render: (i: Institution) => (
        <div className="flex items-center gap-1.5">
          <button onClick={(e) => { e.stopPropagation(); setSuspendTarget(i) }}
            className={`btn-ghost text-xs py-1 ${i.status === 'ACTIVE' ? 'text-orange-600' : 'text-green-600'}`}>
            {i.status === 'ACTIVE' ? 'Suspend' : 'Activate'}
          </button>
          <button onClick={(e) => { e.stopPropagation(); setRotateTarget(i) }}
            className="btn-ghost text-xs py-1">
            <RotateCcw className="w-3.5 h-3.5" />
          </button>
        </div>
      )},
  ]

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <p className="text-sm text-gray-500">{institutions?.length ?? 0} institutions registered</p>
        <button onClick={() => setRegisterModal(true)} className="btn-primary text-sm">
          <Plus className="w-4 h-4" /> Register Institution
        </button>
      </div>

      <div className="card !p-0 overflow-hidden">
        <DataTable<Institution>
          columns={columns} data={institutions ?? []}
          keyExtractor={(i) => i.institutionCode}
          loading={isLoading} emptyMessage="No institutions registered."
        />
      </div>

      {/* Register modal */}
      <Modal isOpen={registerModal} onClose={() => setRegisterModal(false)} title="Register New Institution"
        footer={
          <>
            <button onClick={() => setRegisterModal(false)} className="btn-secondary">Cancel</button>
            <button onClick={() => registerMutation.mutate()} disabled={registerMutation.isPending} className="btn-primary">
              {registerMutation.isPending ? 'Registering...' : 'Register'}
            </button>
          </>
        }>
        <div className="space-y-4">
          <div>
            <label className="label">Institution Code</label>
            <input className="input font-mono" placeholder="INST_EXAMPLE"
              value={form.institutionCode} onChange={(e) => setForm((f) => ({ ...f, institutionCode: e.target.value.toUpperCase() }))} />
          </div>
          <div>
            <label className="label">Name</label>
            <input className="input" placeholder="Ministry of Something"
              value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} />
          </div>
          <div>
            <label className="label">Description (optional)</label>
            <textarea className="input h-20 resize-none" placeholder="Brief description…"
              value={form.description} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} />
          </div>
          <div>
            <label className="label">Publishable Data Types</label>
            <div className="flex flex-wrap gap-2">
              {DATA_TYPES.map((dt) => (
                <button key={dt} onClick={() => setForm((f) => ({
                  ...f,
                  publishableTypes: f.publishableTypes.includes(dt)
                    ? f.publishableTypes.filter((t) => t !== dt)
                    : [...f.publishableTypes, dt]
                }))}
                  className={`px-2.5 py-1 rounded-full text-xs font-medium transition-colors ${
                    form.publishableTypes.includes(dt)
                      ? 'bg-primary text-white'
                      : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  }`}>
                  {dt}
                </button>
              ))}
            </div>
          </div>
        </div>
      </Modal>

      {/* New API Key display */}
      <Modal isOpen={!!newKey} onClose={() => setNewKey(null)} title="API Key Generated">
        <div className="space-y-3">
          <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg text-sm text-amber-700">
            <strong>Store this key now.</strong> It will not be shown again.
          </div>
          <div>
            <label className="label">Institution Code</label>
            <p className="font-mono text-sm text-dark">{newKey?.code}</p>
          </div>
          <div>
            <label className="label">API Key</label>
            <div className="p-3 bg-gray-900 rounded-lg">
              <p className="font-mono text-xs text-green-400 break-all">{newKey?.key}</p>
            </div>
          </div>
          <button onClick={() => newKey && navigator.clipboard.writeText(newKey.key)}
            className="btn-secondary w-full"><Key className="w-4 h-4" /> Copy to Clipboard</button>
        </div>
      </Modal>

      <ConfirmDialog
        isOpen={!!rotateTarget} onClose={() => setRotateTarget(null)}
        onConfirm={() => rotateMutation.mutate()}
        title="Rotate API Key"
        message={`Rotating the key for ${rotateTarget?.institutionCode} will immediately invalidate the current key. The institution will need to update their configuration.`}
        confirmLabel="Rotate Key" danger
        loading={rotateMutation.isPending}
      />

      <ConfirmDialog
        isOpen={!!suspendTarget} onClose={() => setSuspendTarget(null)}
        onConfirm={() => statusMutation.mutate()}
        title={`${suspendTarget?.status === 'ACTIVE' ? 'Suspend' : 'Activate'} Institution`}
        message={`This will ${suspendTarget?.status === 'ACTIVE' ? 'prevent' : 'allow'} ${suspendTarget?.institutionCode} from making data exchange requests.`}
        confirmLabel={suspendTarget?.status === 'ACTIVE' ? 'Suspend' : 'Activate'}
        danger={suspendTarget?.status === 'ACTIVE'}
        loading={statusMutation.isPending}
      />
    </div>
  )
}
