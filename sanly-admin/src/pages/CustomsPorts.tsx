import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Anchor, ToggleLeft, ToggleRight } from 'lucide-react'
import { getPorts, type AdminPort } from '../api/customs'
import axios from 'axios'
import Badge from '../components/ui/Badge'

const customsClient = axios.create({ baseURL: '/proxy/customs', timeout: 10_000 })
customsClient.interceptors.request.use((config) => {
  const token = import.meta.env.VITE_CUSTOMS_TOKEN || null
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

const PORT_TYPE_LABELS: Record<string, string> = {
  LAND_BORDER: 'Land Border', AIRPORT: 'Airport', RAILWAY: 'Railway',
  SEAPORT: 'Seaport', INLAND_DEPOT: 'Inland Depot',
}

export default function CustomsPorts() {
  const qc = useQueryClient()

  const { data: ports, isLoading } = useQuery({
    queryKey: ['customs-ports'],
    queryFn: getPorts,
  })

  const statusMutation = useMutation({
    mutationFn: ({ code, status }: { code: string; status: string }) =>
      customsClient.patch(`/api/v1/customs/ports/${code}/status`, { status }).then(r => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['customs-ports'] }),
  })

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-2">
        <Anchor className="w-5 h-5 text-primary" />
        <h1 className="text-xl font-bold text-dark">Customs Ports</h1>
        <span className="ml-1 px-2 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-500">
          {ports?.length ?? 0} ports
        </span>
      </div>

      {isLoading ? (
        <div className="space-y-3">{[1,2,3].map(i => <div key={i} className="skeleton h-14 rounded-xl" />)}</div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-100 overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-xs uppercase text-gray-400 tracking-wide">
              <tr>
                <th className="text-left px-4 py-3">Code</th>
                <th className="text-left px-4 py-3">Name</th>
                <th className="text-left px-4 py-3">Type</th>
                <th className="text-left px-4 py-3">Region</th>
                <th className="text-left px-4 py-3">Status</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {(ports ?? []).map(port => (
                <tr key={port.portId} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-mono text-xs">{port.portCode}</td>
                  <td className="px-4 py-3 font-medium">{port.name}</td>
                  <td className="px-4 py-3 text-gray-500">{PORT_TYPE_LABELS[port.portType] ?? port.portType}</td>
                  <td className="px-4 py-3 text-gray-500">{port.region || '—'}</td>
                  <td className="px-4 py-3">
                    <Badge variant={port.status === 'ACTIVE' ? 'ACTIVE' : 'FAIL'} label={port.status} />
                  </td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => statusMutation.mutate({
                        code: port.portCode,
                        status: port.status === 'ACTIVE' ? 'CLOSED' : 'ACTIVE',
                      })}
                      className="text-gray-400 hover:text-primary p-1"
                      title={port.status === 'ACTIVE' ? 'Close' : 'Reopen'}
                    >
                      {port.status === 'ACTIVE'
                        ? <ToggleRight className="w-5 h-5 text-green-500" />
                        : <ToggleLeft className="w-5 h-5 text-gray-400" />
                      }
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
