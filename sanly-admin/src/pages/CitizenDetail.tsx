import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, Car, Heart, Receipt, Briefcase, FileText, Shield, MonitorSmartphone, Trash2 } from 'lucide-react'
import { getCitizen, updateCitizenStatus, adminGetSessions, adminRevokeAllSessions, type ActiveSession } from '../api/registry'
import { getExchangeLogsByNationalId } from '../api/bridge'
import { getMedicalRecords, getLicenses, getTaxpayer, getBusinesses, getBirthRecord } from '../api/services'
import Badge from '../components/ui/Badge'
import Modal from '../components/ui/Modal'
import DataTable from '../components/ui/DataTable'
import { formatDate, formatDateTime, maskNin } from '../utils/formatters'
import type { MedicalRecord, DrivingLicense, Business, ExchangeLog } from '../types'

export default function CitizenDetail() {
  const { nationalId } = useParams<{ nationalId: string }>()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [statusModal, setStatusModal] = useState(false)
  const [newStatus, setNewStatus] = useState('')

  const { data: citizen, isLoading } = useQuery({
    queryKey: ['citizen', nationalId], queryFn: () => getCitizen(nationalId!), enabled: !!nationalId,
  })
  const { data: medical } = useQuery({ queryKey: ['medical', nationalId], queryFn: () => getMedicalRecords(nationalId!), enabled: !!nationalId, retry: false })
  const { data: licenses } = useQuery({ queryKey: ['licenses', nationalId], queryFn: () => getLicenses(nationalId!), enabled: !!nationalId, retry: false })
  const { data: taxpayer } = useQuery({ queryKey: ['taxpayer', nationalId], queryFn: () => getTaxpayer(nationalId!), enabled: !!nationalId, retry: false })
  const { data: businesses } = useQuery({ queryKey: ['businesses', nationalId], queryFn: () => getBusinesses(nationalId!), enabled: !!nationalId, retry: false })
  const { data: birthRecord } = useQuery({ queryKey: ['birth', nationalId], queryFn: () => getBirthRecord(nationalId!), enabled: !!nationalId, retry: false })
  const { data: exchangeLogs } = useQuery({ queryKey: ['exchanges', nationalId], queryFn: () => getExchangeLogsByNationalId(nationalId!), enabled: !!nationalId, retry: false })
  const { data: sessions, refetch: refetchSessions } = useQuery<ActiveSession[]>({
    queryKey: ['sessions', nationalId],
    queryFn: () => adminGetSessions(nationalId!),
    enabled: !!nationalId, retry: false,
  })

  const mutation = useMutation({
    mutationFn: () => updateCitizenStatus(nationalId!, newStatus),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['citizen', nationalId] }); setStatusModal(false) },
  })

  if (isLoading) return <div className="skeleton h-64 rounded-xl" />
  if (!citizen) return <p className="text-gray-400 text-center py-12">Citizen not found.</p>

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex items-start gap-4">
        <button onClick={() => navigate('/citizens')} className="btn-ghost mt-0.5">
          <ArrowLeft className="w-4 h-4" />
        </button>
        <div className="flex-1">
          <div className="flex items-center gap-3 flex-wrap">
            <h1 className="page-title">{citizen.firstName} {citizen.lastName}</h1>
            <Badge label={citizen.status} status={citizen.status} />
          </div>
          <p className="text-sm text-gray-400 font-mono mt-0.5">{citizen.nationalId}</p>
        </div>
        <button onClick={() => { setNewStatus(citizen.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE'); setStatusModal(true) }}
          className="btn-secondary text-sm">
          {citizen.status === 'ACTIVE' ? 'Suspend' : 'Activate'}
        </button>
      </div>

      {/* Identity */}
      <div className="card">
        <p className="section-title">Identity</p>
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-x-8">
          {[
            ['Date of Birth', formatDate(citizen.dateOfBirth)],
            ['Gender', citizen.gender],
            ['Place of Birth', citizen.placeOfBirth],
            ['Address', citizen.street],
            ['Father NIN', citizen.fatherNin ?? '—'],
            ['Mother NIN', citizen.motherNin ?? '—'],
            ['Registered', formatDateTime(citizen.createdAt)],
          ].map(([k, v]) => (
            <div key={k} className="py-2 border-b border-gray-50 last:border-0">
              <p className="text-xs text-gray-400 uppercase tracking-wide">{k}</p>
              <p className="text-sm font-medium text-dark mt-0.5">{v}</p>
            </div>
          ))}
        </div>
      </div>

      {/* Civil */}
      {birthRecord && (
        <div className="card">
          <p className="section-title flex items-center gap-2"><FileText className="w-4 h-4 text-primary" />Birth Certificate</p>
          <div className="flex gap-6 text-sm">
            <div><p className="text-xs text-gray-400">Certificate No.</p><p className="font-mono font-medium">{birthRecord.certificateNumber}</p></div>
            <div><p className="text-xs text-gray-400">Place of Birth</p><p className="font-medium">{birthRecord.placeOfBirth}</p></div>
          </div>
        </div>
      )}

      {/* Medical */}
      <div className="card">
        <p className="section-title flex items-center gap-2"><Heart className="w-4 h-4 text-primary" />Medical Records ({medical?.length ?? 0})</p>
        {medical?.length ? (
          <DataTable<MedicalRecord>
            columns={[
              { key: 'testType', header: 'Test Type' },
              { key: 'result', header: 'Result', render: (r) => <Badge label={r.result} status={r.result} size="sm" /> },
              { key: 'testDate', header: 'Date', render: (r) => <span className="text-xs text-gray-500">{formatDate(r.testDate)}</span> },
              { key: 'expiryDate', header: 'Expires', render: (r) => <span className="text-xs text-gray-500">{formatDate(r.expiryDate)}</span> },
            ]}
            data={medical} keyExtractor={(r) => r.id}
          />
        ) : <p className="text-sm text-gray-400">No medical records.</p>}
      </div>

      {/* License */}
      <div className="card">
        <p className="section-title flex items-center gap-2"><Car className="w-4 h-4 text-primary" />Driving Licenses ({licenses?.length ?? 0})</p>
        {licenses?.length ? (
          <DataTable<DrivingLicense>
            columns={[
              { key: 'licenseNumber', header: 'License No.', className: 'font-mono text-xs' },
              { key: 'category', header: 'Category' },
              { key: 'status', header: 'Status', render: (l) => <Badge label={l.status} status={l.status} size="sm" /> },
              { key: 'expiryDate', header: 'Expires', render: (l) => <span className="text-xs text-gray-500">{formatDate(l.expiryDate)}</span> },
            ]}
            data={licenses} keyExtractor={(l) => l.id}
          />
        ) : <p className="text-sm text-gray-400">No driving licenses.</p>}
      </div>

      {/* Tax */}
      {taxpayer && (
        <div className="card">
          <p className="section-title flex items-center gap-2"><Receipt className="w-4 h-4 text-primary" />Tax</p>
          <div className="flex gap-6 text-sm flex-wrap">
            <div><p className="text-xs text-gray-400">Tax ID</p><p className="font-mono font-medium">{taxpayer.taxId}</p></div>
            <div><p className="text-xs text-gray-400">Type</p><p className="font-medium">{taxpayer.taxpayerType}</p></div>
            <div><p className="text-xs text-gray-400">Compliance</p><Badge label={taxpayer.complianceStatus} status={taxpayer.complianceStatus} size="sm" /></div>
          </div>
        </div>
      )}

      {/* Businesses */}
      {(businesses?.length ?? 0) > 0 && (
        <div className="card">
          <p className="section-title flex items-center gap-2"><Briefcase className="w-4 h-4 text-primary" />Businesses ({businesses!.length})</p>
          <DataTable<Business>
            columns={[
              { key: 'registrationNumber', header: 'Reg. No.', className: 'font-mono text-xs' },
              { key: 'businessName', header: 'Name' },
              { key: 'businessType', header: 'Type' },
              { key: 'status', header: 'Status', render: (b) => <Badge label={b.status} status={b.status} size="sm" /> },
            ]}
            data={businesses!} keyExtractor={(b) => b.id}
          />
        </div>
      )}

      {/* Data Access Log */}
      <div className="card">
        <p className="section-title flex items-center gap-2"><Shield className="w-4 h-4 text-primary" />Data Access Log ({exchangeLogs?.totalElements ?? 0} records)</p>
        <DataTable<ExchangeLog>
          columns={[
            { key: 'requestingCode', header: 'Institution' },
            { key: 'dataType', header: 'Data Type' },
            { key: 'purposeCode', header: 'Purpose', render: (l) => <span className="text-xs">{l.purposeCode ?? '—'}</span> },
            { key: 'result', header: 'Result', render: (l) => <Badge label={l.result} status={l.result} size="sm" /> },
            { key: 'exchangedAt', header: 'Time', render: (l) => <span className="text-xs text-gray-500">{formatDateTime(l.exchangedAt)}</span> },
          ]}
          data={exchangeLogs?.content ?? []}
          keyExtractor={(l) => String(l.id)}
        />
      </div>

      {/* Active Sessions */}
      <div className="card">
        <div className="flex items-center justify-between mb-3">
          <p className="section-title flex items-center gap-2 mb-0">
            <MonitorSmartphone className="w-4 h-4 text-primary" />
            Active Sessions ({sessions?.length ?? 0})
          </p>
          {(sessions?.length ?? 0) > 0 && (
            <button
              className="btn-danger-ghost text-xs flex items-center gap-1"
              onClick={async () => {
                await adminRevokeAllSessions(nationalId!)
                refetchSessions()
              }}
            >
              <Trash2 className="w-3 h-3" /> Revoke All
            </button>
          )}
        </div>
        {(sessions?.length ?? 0) === 0 ? (
          <p className="text-sm text-gray-400">No active sessions.</p>
        ) : (
          <div className="space-y-2">
            {sessions!.map((s) => (
              <div key={s.sessionId}
                className="flex items-start gap-3 p-3 rounded-xl bg-gray-50 border border-gray-100">
                <div className="flex-1 min-w-0">
                  <p className="text-xs font-mono text-gray-500 truncate">{s.userAgent ?? 'Unknown'}</p>
                  <p className="text-xs text-gray-400">{s.ipAddress} &bull; Last seen: {new Date(s.lastSeenAt).toLocaleString()}</p>
                  <p className="text-xs text-gray-400">Expires: {new Date(s.expiresAt).toLocaleString()}</p>
                </div>
                <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${
                  s.status === 'ACTIVE' ? 'bg-green-50 text-green-700' : 'bg-gray-100 text-gray-500'
                }`}>{s.status}</span>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Status change modal */}
      <Modal isOpen={statusModal} onClose={() => setStatusModal(false)} title="Change Citizen Status"
        footer={
          <>
            <button onClick={() => setStatusModal(false)} className="btn-secondary">Cancel</button>
            <button onClick={() => mutation.mutate()} disabled={mutation.isPending}
              className={newStatus === 'SUSPENDED' ? 'btn-danger' : 'btn-primary'}>
              {mutation.isPending ? 'Saving...' : `Set ${newStatus}`}
            </button>
          </>
        }>
        <p className="text-sm text-gray-600">
          Change status of <span className="font-semibold">{citizen.firstName} {citizen.lastName}</span> to{' '}
          <span className="font-semibold">{newStatus}</span>?
        </p>
      </Modal>
    </div>
  )
}
