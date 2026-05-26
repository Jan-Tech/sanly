import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Download, Eye } from 'lucide-react'
import { getExchangeLogs } from '../api/bridge'
import type { ExchangeLog } from '../types'
import Badge from '../components/ui/Badge'
import DataTable from '../components/ui/DataTable'
import Modal from '../components/ui/Modal'
import Pagination from '../components/ui/Pagination'
import { formatDateTime, maskNin } from '../utils/formatters'
import { downloadCSV } from '../utils/csv'

const RESULTS = ['', 'SUCCESS', 'DENIED', 'NOT_FOUND', 'ERROR']
const DATA_TYPES = ['', 'VISION_TEST', 'MEDICAL_CLEARANCE', 'CRIMINAL_RECORD', 'TAX_STATUS', 'DRIVING_LICENSE', 'BUSINESS_REGISTRATION', 'BIRTH_RECORD', 'MARRIAGE_RECORD', 'DEATH_RECORD']
const PURPOSES = ['', 'TRAFFIC_STOP', 'CRIMINAL_INVESTIGATION', 'COURT_ORDER', 'ROUTINE_CHECK', 'BORDER_CONTROL', 'EMERGENCY', 'LICENSE_ISSUANCE', 'TAX_AUDIT', 'BUSINESS_VERIFICATION', 'CIVIL_REGISTRATION', 'MEDICAL_CLEARANCE', 'OTHER']

export default function AuditLog() {
  const [page, setPage] = useState(0)
  const [filters, setFilters] = useState({
    institutionCode: '', nationalId: '', dataType: '', result: '', purposeCode: '', from: '', to: '',
  })
  const [detail, setDetail] = useState<ExchangeLog | null>(null)

  const { data, isLoading } = useQuery({
    queryKey: ['audit', page, filters],
    queryFn: () => getExchangeLogs({ page, size: 50, ...Object.fromEntries(Object.entries(filters).filter(([, v]) => v)) }),
    placeholderData: (prev) => prev,
  })

  const handleExport = async () => {
    const all = await getExchangeLogs({ size: 5000, ...Object.fromEntries(Object.entries(filters).filter(([, v]) => v)) })
    downloadCSV('audit_log.csv', ['ID', 'Time', 'Requesting', 'Target', 'Data Type', 'NID (masked)', 'Purpose', 'Case Ref', 'Result'],
      (all.content ?? []).map((l) => [l.id, l.exchangedAt, l.requestingCode, l.targetCode ?? '', l.dataType, maskNin(l.nationalId), l.purposeCode ?? '', l.caseReference ?? '', l.result])
    )
  }

  const setFilter = (k: string, v: string) => { setFilters((f) => ({ ...f, [k]: v })); setPage(0) }

  const columns = [
    { key: 'exchangedAt', header: 'Time', render: (l: ExchangeLog) => <span className="text-xs text-gray-500 whitespace-nowrap">{formatDateTime(l.exchangedAt)}</span> },
    { key: 'requestingCode', header: 'Requesting', className: 'font-mono text-xs font-semibold' },
    { key: 'targetCode', header: 'Target', className: 'font-mono text-xs', render: (l: ExchangeLog) => l.targetCode ?? '—' },
    { key: 'dataType', header: 'Data Type', render: (l: ExchangeLog) => <span className="text-[11px] bg-blue-50 text-blue-700 px-1.5 py-0.5 rounded font-mono">{l.dataType}</span> },
    { key: 'nationalId', header: 'NID (masked)', render: (l: ExchangeLog) => <span className="font-mono text-xs">{maskNin(l.nationalId)}</span> },
    { key: 'purposeCode', header: 'Purpose', render: (l: ExchangeLog) => <span className="text-xs text-gray-500">{l.purposeCode ?? '—'}</span> },
    { key: 'result', header: 'Result', render: (l: ExchangeLog) => <Badge label={l.result} status={l.result} size="sm" /> },
    { key: 'actions', header: '', render: (l: ExchangeLog) => (
      <button onClick={(e) => { e.stopPropagation(); setDetail(l) }} className="btn-ghost p-1.5" title="View detail">
        <Eye className="w-3.5 h-3.5" />
      </button>
    )},
  ]

  return (
    <div className="space-y-4">
      {/* Filters */}
      <div className="card !p-4">
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
          <div>
            <label className="label">Institution</label>
            <input className="input text-sm" placeholder="INST_*" value={filters.institutionCode}
              onChange={(e) => setFilter('institutionCode', e.target.value)} />
          </div>
          <div>
            <label className="label">National ID</label>
            <input className="input text-sm font-mono" placeholder="11 digits" value={filters.nationalId}
              onChange={(e) => setFilter('nationalId', e.target.value)} />
          </div>
          <div>
            <label className="label">Data Type</label>
            <select className="select text-sm" value={filters.dataType} onChange={(e) => setFilter('dataType', e.target.value)}>
              {DATA_TYPES.map((t) => <option key={t} value={t}>{t || 'All types'}</option>)}
            </select>
          </div>
          <div>
            <label className="label">Result</label>
            <select className="select text-sm" value={filters.result} onChange={(e) => setFilter('result', e.target.value)}>
              {RESULTS.map((r) => <option key={r} value={r}>{r || 'All results'}</option>)}
            </select>
          </div>
          <div>
            <label className="label">Purpose Code</label>
            <select className="select text-sm" value={filters.purposeCode} onChange={(e) => setFilter('purposeCode', e.target.value)}>
              {PURPOSES.map((p) => <option key={p} value={p}>{p || 'All purposes'}</option>)}
            </select>
          </div>
          <div>
            <label className="label">From</label>
            <input type="datetime-local" className="input text-sm" value={filters.from} onChange={(e) => setFilter('from', e.target.value ? new Date(e.target.value).toISOString() : '')} />
          </div>
          <div>
            <label className="label">To</label>
            <input type="datetime-local" className="input text-sm" value={filters.to} onChange={(e) => setFilter('to', e.target.value ? new Date(e.target.value).toISOString() : '')} />
          </div>
          <div className="flex items-end">
            <button onClick={handleExport} className="btn-secondary w-full text-sm">
              <Download className="w-4 h-4" /> Export CSV
            </button>
          </div>
        </div>
      </div>

      <div className="card !p-0 overflow-hidden">
        <div className="px-4 py-3 border-b border-gray-100 text-sm text-gray-500">
          {data ? `${data.totalElements} records` : 'Loading…'}
        </div>
        <DataTable<ExchangeLog>
          columns={columns} data={data?.content ?? []}
          keyExtractor={(l) => String(l.id)}
          loading={isLoading} emptyMessage="No exchange logs found."
        />
        {data && <Pagination page={page} totalPages={data.totalPages} totalElements={data.totalElements} size={50} onChange={setPage} />}
      </div>

      {/* Detail modal */}
      <Modal isOpen={!!detail} onClose={() => setDetail(null)} title="Exchange Log Detail" size="md">
        {detail && (
          <div className="space-y-3 text-sm">
            {[
              ['ID', detail.id],
              ['Timestamp', formatDateTime(detail.exchangedAt)],
              ['Requesting Institution', detail.requestingCode],
              ['Target Institution', detail.targetCode ?? '—'],
              ['Data Type', detail.dataType],
              ['National ID (full)', detail.nationalId ?? '—'],
              ['Purpose Code', detail.purposeCode ?? '—'],
              ['Case Reference', detail.caseReference ?? '—'],
              ['Justification', detail.details ?? '—'],
              ['Result', detail.result],
              ['Response Time', detail.responseTimeMs != null ? `${detail.responseTimeMs}ms` : '—'],
            ].map(([k, v]) => (
              <div key={k} className="flex gap-3">
                <span className="text-gray-400 min-w-36">{k}</span>
                <span className={`font-medium text-dark ${k === 'National ID (full)' ? 'font-mono' : ''}`}>{String(v)}</span>
              </div>
            ))}
          </div>
        )}
      </Modal>
    </div>
  )
}
