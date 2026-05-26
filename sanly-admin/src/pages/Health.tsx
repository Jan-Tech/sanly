import { useQuery } from '@tanstack/react-query'
import { CheckCircle, XCircle, Clock, RefreshCw, ExternalLink } from 'lucide-react'
import { checkAllServices } from '../api/health'
import { formatDateTime } from '../utils/formatters'
import type { ServiceHealth } from '../types'

const SWAGGER_LINKS: Record<string, string> = {
  'Citizen Registry': 'http://localhost:8080/swagger-ui.html',
  'SANLY Bridge':     'http://localhost:8081/swagger-ui.html',
  'Medical':          'http://localhost:8082/swagger-ui.html',
  'DMV':              'http://localhost:8083/swagger-ui.html',
  'Police':           'http://localhost:8084/swagger-ui.html',
  'Tax':              'http://localhost:8085/swagger-ui.html',
  'Business':         'http://localhost:8086/swagger-ui.html',
  'Civil Registry':   'http://localhost:8087/swagger-ui.html',
}

function ServiceCard({ s }: { s: ServiceHealth }) {
  const slow = s.healthy && s.responseTimeMs > 2000
  const color = !s.healthy ? 'border-red-200 bg-red-50/30' : slow ? 'border-amber-200 bg-amber-50/30' : 'border-green-200 bg-green-50/30'

  return (
    <div className={`card border ${color}`}>
      <div className="flex items-start justify-between">
        <div>
          <div className="flex items-center gap-2">
            {s.healthy
              ? <CheckCircle className={`w-5 h-5 ${slow ? 'text-amber-500' : 'text-green-500'}`} />
              : <XCircle className="w-5 h-5 text-red-500" />}
            <span className="font-semibold text-dark">{s.name}</span>
          </div>
          <p className="text-xs text-gray-400 mt-1">Port {s.port} · {s.url}</p>
        </div>
        <a href={SWAGGER_LINKS[s.name]} target="_blank" rel="noreferrer"
          className="text-xs text-primary hover:underline flex items-center gap-1 mt-0.5">
          API Docs <ExternalLink className="w-3 h-3" />
        </a>
      </div>

      <div className="mt-4 flex items-center gap-6 text-sm">
        <div>
          <p className="text-xs text-gray-400">Status</p>
          <p className={`font-semibold mt-0.5 ${s.healthy ? (slow ? 'text-amber-600' : 'text-green-600') : 'text-red-600'}`}>
            {s.healthy ? (slow ? 'Slow' : 'Healthy') : 'Unreachable'}
          </p>
        </div>
        <div>
          <p className="text-xs text-gray-400">Response Time</p>
          <p className={`font-semibold mt-0.5 font-mono ${slow ? 'text-amber-600' : 'text-gray-700'}`}>
            {s.responseTimeMs >= 0 ? `${s.responseTimeMs}ms` : '—'}
          </p>
        </div>
        <div>
          <p className="text-xs text-gray-400">Last Checked</p>
          <p className="text-gray-500 text-xs mt-0.5">{formatDateTime(s.checkedAt)}</p>
        </div>
      </div>
    </div>
  )
}

export default function Health() {
  const { data, isLoading, dataUpdatedAt, refetch, isFetching } = useQuery({
    queryKey: ['health'], queryFn: checkAllServices, refetchInterval: 15_000,
  })

  const healthy = (data ?? []).filter((s) => s.healthy).length
  const total = (data ?? []).length

  return (
    <div className="space-y-5">
      {/* Status bar */}
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div className="flex items-center gap-4">
          <div className={`flex items-center gap-2 text-sm font-semibold ${healthy === total ? 'text-green-600' : healthy === 0 ? 'text-red-600' : 'text-amber-600'}`}>
            <div className={`w-2.5 h-2.5 rounded-full animate-pulse ${healthy === total ? 'bg-green-500' : healthy === 0 ? 'bg-red-500' : 'bg-amber-500'}`} />
            {isLoading ? 'Checking…' : `${healthy}/${total} services healthy`}
          </div>
          <div className="flex items-center gap-1 text-xs text-gray-400">
            <Clock className="w-3.5 h-3.5" />
            Auto-refresh every 15s
          </div>
        </div>
        <button onClick={() => refetch()} disabled={isFetching} className="btn-secondary text-sm">
          <RefreshCw className={`w-4 h-4 ${isFetching ? 'animate-spin' : ''}`} />
          Refresh now
        </button>
      </div>

      {isLoading ? (
        <div className="grid sm:grid-cols-2 lg:grid-cols-2 gap-4">
          {Array.from({ length: 8 }).map((_, i) => <div key={i} className="skeleton h-32 rounded-xl" />)}
        </div>
      ) : (
        <div className="grid sm:grid-cols-2 gap-4">
          {(data ?? []).map((s) => <ServiceCard key={s.name} s={s} />)}
        </div>
      )}

      {dataUpdatedAt > 0 && (
        <p className="text-xs text-gray-400 text-center">
          Last updated: {formatDateTime(new Date(dataUpdatedAt).toISOString())}
        </p>
      )}
    </div>
  )
}
