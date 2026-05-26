import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Search } from 'lucide-react'
import { getCitizens } from '../api/registry'
import type { Citizen } from '../types'
import Badge from '../components/ui/Badge'
import DataTable from '../components/ui/DataTable'
import Pagination from '../components/ui/Pagination'
import { formatDate } from '../utils/formatters'

const STATUS_OPTIONS = [
  { value: '', label: 'All statuses' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'SUSPENDED', label: 'Suspended' },
  { value: 'DECEASED', label: 'Deceased' },
  { value: 'INACTIVE', label: 'Inactive' },
]

export default function Citizens() {
  const navigate = useNavigate()
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [debouncedSearch, setDebouncedSearch] = useState('')
  const [status, setStatus] = useState('')

  const handleSearchChange = (v: string) => {
    setSearch(v)
    const t = setTimeout(() => { setDebouncedSearch(v); setPage(0) }, 400)
    return () => clearTimeout(t)
  }

  const { data, isLoading } = useQuery({
    queryKey: ['citizens', page, debouncedSearch, status],
    queryFn: () => getCitizens({ page, size: 20, search: debouncedSearch || undefined, status: status || undefined }),
    placeholderData: (prev) => prev,
  })

  const columns = [
    { key: 'nationalId', header: 'TM-NIN', className: 'font-mono text-xs' },
    { key: 'name', header: 'Full Name',
      render: (c: Citizen) => `${c.firstName} ${c.lastName}` },
    { key: 'dateOfBirth', header: 'Date of Birth',
      render: (c: Citizen) => formatDate(c.dateOfBirth) },
    { key: 'status', header: 'Status',
      render: (c: Citizen) => <Badge label={c.status} status={c.status} size="sm" /> },
    { key: 'createdAt', header: 'Registered',
      render: (c: Citizen) => <span className="text-gray-500 text-xs">{formatDate(c.createdAt)}</span> },
  ]

  return (
    <div className="space-y-4">
      {/* Filters */}
      <div className="flex gap-3 flex-wrap">
        <div className="relative flex-1 min-w-60">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input type="text" className="input pl-9" placeholder="Search by name or TM-NIN…"
            value={search} onChange={(e) => handleSearchChange(e.target.value)} />
        </div>
        <select className="select w-44"
          value={status} onChange={(e) => { setStatus(e.target.value); setPage(0) }}>
          {STATUS_OPTIONS.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>
      </div>

      {/* Table */}
      <div className="card !p-0 overflow-hidden">
        <DataTable<Citizen>
          columns={columns}
          data={data?.content ?? []}
          keyExtractor={(c) => c.nationalId}
          loading={isLoading}
          emptyMessage="No citizens found."
          onRowClick={(c) => navigate(`/citizens/${c.nationalId}`)}
        />
        {data && (
          <Pagination
            page={page} totalPages={data.totalPages}
            totalElements={data.totalElements} size={20}
            onChange={setPage}
          />
        )}
      </div>
    </div>
  )
}
