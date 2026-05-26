import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Truck, Search } from 'lucide-react'
import { getVehiclesByStatus, updateVehicleStatus, type AdminVehicle } from '../api/vehicle'
import Badge from '../components/ui/Badge'
import ConfirmDialog from '../components/ui/ConfirmDialog'

type StatusFilter = 'REGISTERED' | 'STOLEN' | 'UNDER_TRANSFER' | 'DEREGISTERED' | ''

function vehicleStatusVariant(s: string) {
  if (s === 'REGISTERED') return 'ACTIVE'
  if (s === 'STOLEN') return 'FAIL'
  if (s === 'UNDER_TRANSFER') return 'PENDING'
  return 'SUSPENDED'
}

export default function VehicleRegistry() {
  const qc = useQueryClient()
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('REGISTERED')
  const [search, setSearch] = useState('')
  const [stolenTarget, setStolenTarget] = useState<AdminVehicle | null>(null)

  const { data: vehicles, isLoading } = useQuery({
    queryKey: ['admin-vehicles', statusFilter],
    queryFn: () => getVehiclesByStatus(statusFilter),
  })

  const stolenMutation = useMutation({
    mutationFn: (plate: string) => updateVehicleStatus(plate, 'STOLEN'),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['admin-vehicles'] }); setStolenTarget(null) },
  })

  const filtered = (vehicles ?? []).filter(v =>
    !search ||
    v.plateNumber.toLowerCase().includes(search.toLowerCase()) ||
    v.vin.toLowerCase().includes(search.toLowerCase()) ||
    v.make.toLowerCase().includes(search.toLowerCase()) ||
    v.model.toLowerCase().includes(search.toLowerCase())
  )

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3">
        <Truck className="w-6 h-6 text-primary" />
        <h1 className="text-xl font-bold text-gray-900">Vehicle Registry</h1>
      </div>

      <div className="flex flex-wrap gap-3">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            className="pl-9 pr-4 py-2 border border-gray-200 rounded-lg text-sm w-64 focus:outline-none focus:ring-2 focus:ring-primary/30"
            placeholder="Plate, VIN, make, model..."
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
        </div>
        <select
          className="border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none"
          value={statusFilter}
          onChange={e => setStatusFilter(e.target.value as StatusFilter)}
        >
          <option value="">All statuses</option>
          <option value="REGISTERED">Registered</option>
          <option value="UNDER_TRANSFER">Under Transfer</option>
          <option value="STOLEN">Stolen</option>
          <option value="DEREGISTERED">Deregistered</option>
        </select>
      </div>

      {isLoading ? (
        <div className="space-y-3">{[1,2,3].map(i => <div key={i} className="h-20 bg-gray-100 rounded-xl animate-pulse" />)}</div>
      ) : filtered.length === 0 ? (
        <p className="text-sm text-gray-500 italic py-8 text-center">No vehicles found.</p>
      ) : (
        <div className="space-y-3">
          {filtered.map((v: AdminVehicle) => (
            <div key={v.vehicleId} className={`bg-white border rounded-xl p-4 shadow-sm ${v.status === 'STOLEN' ? 'border-red-300' : 'border-gray-200'}`}>
              <div className="flex items-start justify-between gap-3">
                <div className="flex-1">
                  <div className="flex items-center gap-2 flex-wrap">
                    <p className="font-mono text-base font-bold text-gray-900">{v.plateNumber}</p>
                    <Badge variant={vehicleStatusVariant(v.status)} label={v.status} />
                  </div>
                  <p className="text-sm text-gray-700 mt-0.5">{v.make} {v.model} · {v.year} · {v.color}</p>
                  <div className="text-xs text-gray-400 mt-1 space-x-3">
                    <span>VIN: <span className="font-mono">{v.vin}</span></span>
                    <span>{v.vehicleType.replace(/_/g, ' ')}</span>
                    <span>{v.fuelType}</span>
                    <span>Registered: {v.registeredAt?.slice(0,10)}</span>
                  </div>
                </div>
                {v.status === 'REGISTERED' && (
                  <button
                    onClick={() => setStolenTarget(v)}
                    className="shrink-0 px-3 py-1.5 bg-red-600 text-white text-xs rounded-lg hover:bg-red-700"
                  >
                    Mark Stolen
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {stolenTarget && (
        <ConfirmDialog
          title="Mark as Stolen"
          message={`Mark vehicle ${stolenTarget.plateNumber} (${stolenTarget.make} ${stolenTarget.model}) as STOLEN? This will publish an alert to the bridge and notify the owner.`}
          danger
          onConfirm={() => stolenMutation.mutate(stolenTarget.plateNumber)}
          onCancel={() => setStolenTarget(null)}
          loading={stolenMutation.isPending}
        />
      )}
    </div>
  )
}
