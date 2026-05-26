import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Scale, Search, Calendar } from 'lucide-react'
import { getCasesByStatus, type AdminCourtCase } from '../api/court'
import Badge from '../components/ui/Badge'

type StatusFilter = 'FILED' | 'UNDER_REVIEW' | 'HEARING_SCHEDULED' | 'IN_PROGRESS' | 'DECIDED' | 'APPEALED' | 'CLOSED' | 'DISMISSED' | ''

const CASE_STEPS = ['FILED', 'UNDER_REVIEW', 'HEARING_SCHEDULED', 'IN_PROGRESS', 'DECIDED']

function statusVariant(s: string) {
  if (s === 'DECIDED' || s === 'CLOSED') return 'ACTIVE'
  if (s === 'DISMISSED') return 'FAIL'
  if (s === 'APPEALED') return 'SUSPENDED'
  return 'PENDING'
}

function CaseProgressBar({ status }: { status: string }) {
  const idx = CASE_STEPS.indexOf(status)
  if (idx < 0) return null
  return (
    <div className="flex items-center gap-1 mt-2">
      {CASE_STEPS.map((step, i) => (
        <div key={step} className="flex items-center gap-1">
          <div className={`w-2 h-2 rounded-full ${i <= idx ? 'bg-primary' : 'bg-gray-200'} ${i === idx ? 'ring-2 ring-primary/30' : ''}`} />
          {i < CASE_STEPS.length - 1 && <div className={`h-0.5 w-5 ${i < idx ? 'bg-primary' : 'bg-gray-200'}`} />}
        </div>
      ))}
    </div>
  )
}

export default function CourtCases() {
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('FILED')
  const [search, setSearch] = useState('')

  const { data: cases, isLoading } = useQuery({
    queryKey: ['court-cases-admin', statusFilter],
    queryFn: () => getCasesByStatus(statusFilter),
    enabled: true,
  })

  const filtered = (cases ?? []).filter(c =>
    !search ||
    c.caseNumber.toLowerCase().includes(search.toLowerCase()) ||
    c.plaintiffNationalId.includes(search) ||
    c.defendantNationalId.includes(search) ||
    c.courtCode.toLowerCase().includes(search.toLowerCase())
  )

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3">
        <Scale className="w-6 h-6 text-primary" />
        <h1 className="text-xl font-bold text-gray-900">Court Cases</h1>
      </div>

      <div className="flex flex-wrap gap-3">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            className="pl-9 pr-4 py-2 border border-gray-200 rounded-lg text-sm w-64 focus:outline-none focus:ring-2 focus:ring-primary/30"
            placeholder="Case number, NIN, court..."
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
        </div>
        <select
          className="border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/30"
          value={statusFilter}
          onChange={e => setStatusFilter(e.target.value as StatusFilter)}
        >
          <option value="">All statuses</option>
          <option value="FILED">Filed</option>
          <option value="UNDER_REVIEW">Under Review</option>
          <option value="HEARING_SCHEDULED">Hearing Scheduled</option>
          <option value="IN_PROGRESS">In Progress</option>
          <option value="DECIDED">Decided</option>
          <option value="APPEALED">Appealed</option>
          <option value="CLOSED">Closed</option>
          <option value="DISMISSED">Dismissed</option>
        </select>
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {[1, 2, 3].map(i => <div key={i} className="h-24 bg-gray-100 rounded-xl animate-pulse" />)}
        </div>
      ) : filtered.length === 0 ? (
        <p className="text-sm text-gray-500 italic py-8 text-center">No cases found.</p>
      ) : (
        <div className="space-y-3">
          {filtered.map((c: AdminCourtCase) => (
            <div key={c.caseId} className="bg-white border border-gray-200 rounded-xl p-4 shadow-sm">
              <div className="flex items-start justify-between gap-3">
                <div className="flex-1">
                  <div className="flex items-center gap-2 flex-wrap">
                    <p className="font-mono text-sm font-semibold text-gray-900">{c.caseNumber}</p>
                    <Badge variant={statusVariant(c.status)} label={c.status} />
                    <span className="text-xs text-gray-400">{c.caseType.replace(/_/g, ' ')}</span>
                  </div>
                  <div className="mt-1 grid grid-cols-2 gap-x-6 gap-y-0.5 text-xs text-gray-500">
                    <span>Court: <strong className="text-gray-700">{c.courtCode}</strong></span>
                    <span>Filed: <strong className="text-gray-700">{c.filedAt?.slice(0, 10)}</strong></span>
                    <span>Plaintiff NIN: <strong className="font-mono text-gray-700">{c.plaintiffNationalId}</strong></span>
                    <span>Defendant NIN: <strong className="font-mono text-gray-700">{c.defendantNationalId}</strong></span>
                    {c.hearingDate && (
                      <span className="col-span-2 flex items-center gap-1 text-amber-600">
                        <Calendar className="w-3 h-3" />
                        Hearing: <strong>{c.hearingDate}</strong>
                      </span>
                    )}
                    {c.assignedJudgeOfficerId && (
                      <span className="col-span-2">Judge: <strong className="text-gray-700">{c.assignedJudgeOfficerId}</strong></span>
                    )}
                  </div>
                  <CaseProgressBar status={c.status} />
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
