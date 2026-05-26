import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Building2, CheckCircle2, XCircle, Clock, RefreshCw, Key } from 'lucide-react'
import {
  listBanks, approveBank, updateBankStatus, rotateApiKey,
  getAdminConsents, getAdminAccessLog, getBankingStats,
  type RegisteredBank,
} from '../api/banking'
import Badge from '../components/ui/Badge'
import DataTable from '../components/ui/DataTable'
import { formatDate, formatDateTime } from '../utils/formatters'

const STATUS_COLORS: Record<string, 'ACTIVE' | 'PENDING' | 'SUSPENDED' | 'COMPLIANT'> = {
  ACTIVE: 'ACTIVE',
  PENDING_APPROVAL: 'PENDING',
  SUSPENDED: 'SUSPENDED',
  REVOKED: 'SUSPENDED',
}

const CONSENT_STATUS_COLORS: Record<string, 'ACTIVE' | 'COMPLIANT' | 'SUSPENDED' | 'PENDING'> = {
  APPROVED: 'COMPLIANT',
  PENDING: 'ACTIVE',
  REJECTED: 'SUSPENDED',
  EXPIRED: 'PENDING',
}

function StatCard({ label, value, icon, color = 'text-blue-600' }: { label: string; value: number; icon: React.ReactNode; color?: string }) {
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

// ── Banks Tab ─────────────────────────────────────────────────────────────────
function BanksTab() {
  const qc = useQueryClient()
  const [rotatedKey, setRotatedKey] = useState<{ bankCode: string; apiKey: string } | null>(null)

  const { data: banks = [] } = useQuery({ queryKey: ['admin-banks'], queryFn: listBanks, refetchInterval: 30_000 })

  const approveMutation = useMutation({
    mutationFn: (bankCode: string) => approveBank(bankCode),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-banks'] }),
  })
  const statusMutation = useMutation({
    mutationFn: ({ bankCode, status }: { bankCode: string; status: string }) => updateBankStatus(bankCode, status),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-banks'] }),
  })
  const rotateMutation = useMutation({
    mutationFn: (bankCode: string) => rotateApiKey(bankCode),
    onSuccess: (data) => setRotatedKey(data),
  })

  const pending = banks.filter(b => b.status === 'PENDING_APPROVAL')
  const active = banks.filter(b => b.status !== 'PENDING_APPROVAL')

  return (
    <div className="space-y-6">
      {pending.length > 0 && (
        <div>
          <h3 className="font-semibold text-gray-700 mb-3 flex items-center gap-2">
            <Clock className="w-4 h-4 text-yellow-500" /> Pending Approval ({pending.length})
          </h3>
          <div className="space-y-3">
            {pending.map(bank => (
              <div key={bank.bankId} className="bg-yellow-50 border border-yellow-200 rounded-xl p-4 flex items-center justify-between gap-4">
                <div>
                  <p className="font-semibold text-gray-900">{bank.bankName}</p>
                  <p className="text-xs text-gray-500 font-mono">{bank.bankCode} · {bank.licenseNumber}</p>
                  <p className="text-xs text-gray-400">{bank.contactEmail}</p>
                </div>
                <div className="flex gap-2 shrink-0">
                  <button
                    onClick={() => approveMutation.mutate(bank.bankCode)}
                    disabled={approveMutation.isPending}
                    className="px-3 py-1.5 bg-green-600 text-white text-xs font-medium rounded-lg hover:bg-green-700 disabled:opacity-50"
                  >
                    Approve
                  </button>
                  <button
                    onClick={() => statusMutation.mutate({ bankCode: bank.bankCode, status: 'REVOKED' })}
                    className="px-3 py-1.5 bg-red-100 text-red-700 text-xs font-medium rounded-lg hover:bg-red-200"
                  >
                    Reject
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      <div>
        <h3 className="font-semibold text-gray-700 mb-3">Registered Banks</h3>
        <div className="space-y-2">
          {active.map(bank => (
            <div key={bank.bankId} className="bg-white border border-gray-100 rounded-xl p-4 flex items-center justify-between gap-4">
              <div>
                <div className="flex items-center gap-2">
                  <p className="font-medium text-gray-900">{bank.bankName}</p>
                  <Badge status={STATUS_COLORS[bank.status] ?? 'PENDING'} label={bank.status.replace('_', ' ')} />
                </div>
                <p className="text-xs text-gray-500 font-mono mt-0.5">{bank.bankCode} · {bank.licenseNumber}</p>
                <p className="text-xs text-gray-400">{bank.contactEmail} · Registered {formatDate(bank.registeredAt)}</p>
              </div>
              <div className="flex gap-2 shrink-0">
                <button
                  onClick={() => rotateMutation.mutate(bank.bankCode)}
                  disabled={rotateMutation.isPending}
                  className="flex items-center gap-1 px-3 py-1.5 border border-gray-200 text-gray-600 text-xs font-medium rounded-lg hover:bg-gray-50"
                >
                  <Key className="w-3 h-3" /> Rotate Key
                </button>
                {bank.status === 'ACTIVE' ? (
                  <button
                    onClick={() => statusMutation.mutate({ bankCode: bank.bankCode, status: 'SUSPENDED' })}
                    className="px-3 py-1.5 bg-yellow-100 text-yellow-700 text-xs font-medium rounded-lg hover:bg-yellow-200"
                  >
                    Suspend
                  </button>
                ) : bank.status === 'SUSPENDED' ? (
                  <button
                    onClick={() => statusMutation.mutate({ bankCode: bank.bankCode, status: 'ACTIVE' })}
                    className="px-3 py-1.5 bg-green-100 text-green-700 text-xs font-medium rounded-lg hover:bg-green-200"
                  >
                    Activate
                  </button>
                ) : null}
              </div>
            </div>
          ))}
        </div>
      </div>

      {rotatedKey && (
        <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-md p-6 space-y-4">
            <h3 className="font-bold text-gray-900 flex items-center gap-2"><Key className="w-4 h-4" /> New API Key</h3>
            <p className="text-sm text-gray-600">This is the only time this key will be shown. Share it securely with <strong>{rotatedKey.bankCode}</strong>.</p>
            <div className="bg-gray-50 rounded-lg p-3 font-mono text-xs break-all text-gray-800">{rotatedKey.apiKey}</div>
            <button onClick={() => setRotatedKey(null)} className="w-full py-2 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700">
              I've saved the key
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

// ── Consent Log Tab ───────────────────────────────────────────────────────────
function ConsentLogTab() {
  const [page, setPage] = useState(0)
  const [filterStatus, setFilterStatus] = useState('')
  const [filterBank, setFilterBank] = useState('')

  const { data: consentsPage } = useQuery({
    queryKey: ['admin-consents', page, filterStatus, filterBank],
    queryFn: () => getAdminConsents({ page, size: 20, status: filterStatus || undefined, bankCode: filterBank || undefined }),
  })

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap gap-3">
        <input value={filterBank} onChange={e => { setFilterBank(e.target.value); setPage(0) }}
          placeholder="Filter by bank code..."
          className="border border-gray-200 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/30 font-mono w-44" />
        <select value={filterStatus} onChange={e => { setFilterStatus(e.target.value); setPage(0) }}
          className="border border-gray-200 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/30">
          {['', 'PENDING', 'APPROVED', 'REJECTED', 'EXPIRED'].map(s => <option key={s} value={s}>{s || 'All Statuses'}</option>)}
        </select>
      </div>
      <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-xs text-gray-500">
            <tr>
              <th className="px-3 py-2 text-left">Code</th>
              <th className="px-3 py-2 text-left">Bank</th>
              <th className="px-3 py-2 text-left">Citizen NIN</th>
              <th className="px-3 py-2 text-left">Requested</th>
              <th className="px-3 py-2 text-left">Status</th>
            </tr>
          </thead>
          <tbody>
            {(consentsPage?.content ?? []).map(c => (
              <tr key={c.consentId} className="border-t border-gray-50 hover:bg-gray-50">
                <td className="px-3 py-2 font-mono text-xs text-blue-700">{c.consentCode}</td>
                <td className="px-3 py-2">{c.bankName ?? c.bankCode}</td>
                <td className="px-3 py-2 font-mono text-xs">{c.citizenNationalId}</td>
                <td className="px-3 py-2 text-gray-500 text-xs">{formatDateTime(c.requestedAt)}</td>
                <td className="px-3 py-2">
                  <Badge status={CONSENT_STATUS_COLORS[c.status] ?? 'PENDING'} label={c.status} />
                </td>
              </tr>
            ))}
            {(consentsPage?.content ?? []).length === 0 && (
              <tr><td colSpan={5} className="px-3 py-6 text-center text-gray-400 text-sm">No consent records found</td></tr>
            )}
          </tbody>
        </table>
      </div>
      {(consentsPage?.totalPages ?? 0) > 1 && (
        <div className="flex justify-center gap-2">
          <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0} className="px-3 py-1 text-sm border rounded-lg disabled:opacity-40">Prev</button>
          <span className="px-3 py-1 text-sm text-gray-500">{page + 1} / {consentsPage?.totalPages}</span>
          <button onClick={() => setPage(p => p + 1)} disabled={page >= (consentsPage?.totalPages ?? 1) - 1} className="px-3 py-1 text-sm border rounded-lg disabled:opacity-40">Next</button>
        </div>
      )}
    </div>
  )
}

// ── Access Log Tab ────────────────────────────────────────────────────────────
function AccessLogTab() {
  const [page, setPage] = useState(0)
  const [filterBank, setFilterBank] = useState('')

  const { data: accessPage } = useQuery({
    queryKey: ['admin-access-log', page, filterBank],
    queryFn: () => getAdminAccessLog({ page, size: 20, bankCode: filterBank || undefined }),
  })

  return (
    <div className="space-y-4">
      <div>
        <input value={filterBank} onChange={e => { setFilterBank(e.target.value); setPage(0) }}
          placeholder="Filter by bank code..."
          className="border border-gray-200 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/30 font-mono w-44" />
      </div>
      <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-xs text-gray-500">
            <tr>
              <th className="px-3 py-2 text-left">Bank</th>
              <th className="px-3 py-2 text-left">Citizen NIN</th>
              <th className="px-3 py-2 text-left">Consent Code</th>
              <th className="px-3 py-2 text-left">Accessed At</th>
              <th className="px-3 py-2 text-left">Scopes</th>
              <th className="px-3 py-2 text-left">Result</th>
            </tr>
          </thead>
          <tbody>
            {(accessPage?.content ?? []).map(a => {
              const scopes = Array.isArray(a.scopesAccessed) ? a.scopesAccessed : JSON.parse(a.scopesAccessed ?? '[]')
              return (
                <tr key={a.accessId} className="border-t border-gray-50 hover:bg-gray-50">
                  <td className="px-3 py-2">{a.bankName ?? a.bankCode}</td>
                  <td className="px-3 py-2 font-mono text-xs">{a.citizenNationalId}</td>
                  <td className="px-3 py-2 font-mono text-xs text-blue-700">{a.consentCode}</td>
                  <td className="px-3 py-2 text-gray-500 text-xs">{formatDateTime(a.accessedAt)}</td>
                  <td className="px-3 py-2 text-xs text-gray-600">{scopes.length} scopes</td>
                  <td className="px-3 py-2">
                    <Badge status={a.responseStatus === 'SUCCESS' ? 'ACTIVE' : a.responseStatus === 'PARTIAL' ? 'PENDING' : 'SUSPENDED'} label={a.responseStatus} />
                  </td>
                </tr>
              )
            })}
            {(accessPage?.content ?? []).length === 0 && (
              <tr><td colSpan={6} className="px-3 py-6 text-center text-gray-400 text-sm">No access records found</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}

// ── Main Page ─────────────────────────────────────────────────────────────────
export default function BankingAdmin() {
  const [tab, setTab] = useState<'banks' | 'consents' | 'access-log'>('banks')
  const { data: stats } = useQuery({ queryKey: ['banking-stats'], queryFn: getBankingStats, refetchInterval: 30_000 })

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-3">
        <Building2 className="w-6 h-6 text-primary" />
        <h1 className="text-xl font-bold text-gray-900">Banking API</h1>
      </div>

      {stats && (
        <div className="grid grid-cols-2 lg:grid-cols-5 gap-4">
          <StatCard label="Consent Requests Today" value={stats.consentRequestsToday} icon={<Clock className="w-5 h-5" />} color="text-blue-500" />
          <StatCard label="Approved Today" value={stats.approvedToday} icon={<CheckCircle2 className="w-5 h-5" />} color="text-green-600" />
          <StatCard label="Rejected Today" value={stats.rejectedToday} icon={<XCircle className="w-5 h-5" />} color="text-red-500" />
          <StatCard label="Expired Today" value={stats.expiredToday} icon={<Clock className="w-5 h-5" />} color="text-gray-400" />
          <StatCard label="Active Banks" value={stats.totalActiveBanks} icon={<Building2 className="w-5 h-5" />} color="text-primary" />
        </div>
      )}

      <div className="flex gap-1 border-b border-gray-200">
        {(['banks', 'consents', 'access-log'] as const).map(t => (
          <button key={t} onClick={() => setTab(t)}
            className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors capitalize -mb-px ${
              tab === t ? 'border-primary text-primary' : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}>
            {t === 'banks' ? 'Registered Banks' : t === 'consents' ? 'Consent Log' : 'Access Log'}
          </button>
        ))}
      </div>

      {tab === 'banks' && <BanksTab />}
      {tab === 'consents' && <ConsentLogTab />}
      {tab === 'access-log' && <AccessLogTab />}
    </div>
  )
}
