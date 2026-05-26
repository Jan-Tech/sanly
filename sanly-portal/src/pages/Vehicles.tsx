import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Car, CheckCircle2, AlertTriangle, Clock, Shield, ChevronDown, ChevronUp } from 'lucide-react'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { t } from '../utils/i18n'
import {
  getVehiclesByOwner, getInsuranceByPlate, getInspectionsByPlate, getOwnershipHistory,
  type VehicleRecord,
} from '../api/vehicle'
import Badge from '../components/ui/Badge'
import { ServiceUnavailable } from '../components/ui/ServiceUnavailable'
import { SkeletonCard } from '../components/ui/Skeleton'

function fuelTypeBadge(fuel: string) {
  if (fuel === 'ELECTRIC') return 'ACTIVE'
  if (fuel === 'HYBRID') return 'PENDING'
  return 'SUSPENDED'
}

function vehicleStatusVariant(status: string) {
  if (status === 'REGISTERED') return 'ACTIVE'
  if (status === 'STOLEN') return 'FAIL'
  if (status === 'UNDER_TRANSFER') return 'PENDING'
  return 'SUSPENDED'
}

function insuranceVariant(status: string) {
  if (status === 'ACTIVE') return 'ACTIVE'
  if (status === 'EXPIRED') return 'FAIL'
  return 'SUSPENDED'
}

function inspectionVariant(result: string) {
  if (result === 'PASSED') return 'ACTIVE'
  if (result === 'FAILED') return 'FAIL'
  return 'PENDING'
}

function VehicleCard({ vehicle }: { vehicle: VehicleRecord }) {
  const [expanded, setExpanded] = useState(false)
  const { lang } = useLangStore()

  const { data: insurance } = useQuery({
    queryKey: ['vehicle-insurance', vehicle.plateNumber],
    queryFn: () => getInsuranceByPlate(vehicle.plateNumber),
    enabled: expanded,
    retry: false,
  })

  const { data: inspections } = useQuery({
    queryKey: ['vehicle-inspections', vehicle.plateNumber],
    queryFn: () => getInspectionsByPlate(vehicle.plateNumber),
    enabled: expanded,
    retry: false,
  })

  const { data: history } = useQuery({
    queryKey: ['vehicle-ownership', vehicle.plateNumber],
    queryFn: () => getOwnershipHistory(vehicle.plateNumber),
    enabled: expanded,
    retry: false,
  })

  const activeInsurance = (insurance ?? []).find(i => i.status === 'ACTIVE')
  const lastInspection = (inspections ?? [])[0]
  const isInspectionOverdue = lastInspection && new Date(lastInspection.nextInspectionDue) < new Date()

  return (
    <div className={`bg-white rounded-xl border shadow-sm ${vehicle.status === 'STOLEN' ? 'border-red-300' : 'border-gray-200'}`}>
      <div className="p-4">
        <div className="flex items-start justify-between gap-3">
          <div className="flex-1">
            <div className="flex items-center gap-2 flex-wrap">
              <p className="font-mono text-lg font-bold text-gray-900 tracking-wider">{vehicle.plateNumber}</p>
              <Badge variant={vehicleStatusVariant(vehicle.status)} label={vehicle.status} />
            </div>
            <p className="text-sm font-medium text-gray-700 mt-1">
              {vehicle.make} {vehicle.model} · {vehicle.year}
            </p>
            <div className="flex flex-wrap gap-2 mt-2">
              {vehicle.color && (
                <span className="text-xs text-gray-500">{vehicle.color}</span>
              )}
              <Badge variant={fuelTypeBadge(vehicle.fuelType)} label={vehicle.fuelType} />
              <span className="text-xs text-gray-400">{vehicle.vehicleType.replace(/_/g, ' ')}</span>
            </div>
          </div>
          <div className="text-right shrink-0 space-y-1">
            {activeInsurance ? (
              <div className="flex items-center gap-1 text-xs text-green-600">
                <Shield className="w-3.5 h-3.5" />
                <span>Insured until {activeInsurance.validUntil}</span>
              </div>
            ) : (
              <div className="flex items-center gap-1 text-xs text-red-500">
                <AlertTriangle className="w-3.5 h-3.5" />
                <span>No active insurance</span>
              </div>
            )}
            {lastInspection && (
              <div className={`flex items-center gap-1 text-xs ${isInspectionOverdue ? 'text-red-500' : 'text-gray-500'}`}>
                <Clock className="w-3.5 h-3.5" />
                <span>
                  {isInspectionOverdue ? 'Inspection OVERDUE' : `Inspection due ${lastInspection.nextInspectionDue}`}
                </span>
              </div>
            )}
          </div>
        </div>

        <button
          onClick={() => setExpanded(!expanded)}
          className="mt-3 w-full flex items-center justify-center gap-1 text-xs text-gray-400 hover:text-primary transition-colors"
        >
          {expanded ? <ChevronUp className="w-3.5 h-3.5" /> : <ChevronDown className="w-3.5 h-3.5" />}
          {expanded ? 'Hide details' : 'Show details'}
        </button>
      </div>

      {expanded && (
        <div className="border-t border-gray-100 p-4 space-y-4">
          <div>
            <p className="text-xs font-semibold text-gray-500 uppercase mb-2">{t(lang, 'inspectionStatus')}</p>
            {(inspections ?? []).length === 0 ? (
              <p className="text-xs text-gray-400 italic">No inspection records.</p>
            ) : (
              <div className="space-y-1">
                {inspections!.slice(0, 3).map(insp => (
                  <div key={insp.inspectionId} className="flex items-center justify-between text-xs text-gray-600">
                    <span>{insp.inspectionDate}</span>
                    <Badge variant={inspectionVariant(insp.result)} label={insp.result} />
                    <span className="text-gray-400">Next due: {insp.nextInspectionDue}</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div>
            <p className="text-xs font-semibold text-gray-500 uppercase mb-2">{t(lang, 'insuranceStatus')}</p>
            {(insurance ?? []).length === 0 ? (
              <p className="text-xs text-gray-400 italic">No insurance records.</p>
            ) : (
              <div className="space-y-1">
                {insurance!.slice(0, 3).map(ins => (
                  <div key={ins.insuranceId} className="flex items-center justify-between text-xs text-gray-600">
                    <span>{ins.insuranceCompany}</span>
                    <Badge variant={insuranceVariant(ins.status)} label={ins.status} />
                    <span className="text-gray-400">{ins.validFrom} → {ins.validUntil}</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div>
            <p className="text-xs font-semibold text-gray-500 uppercase mb-2">{t(lang, 'ownershipHistory')}</p>
            {(history ?? []).length === 0 ? (
              <p className="text-xs text-gray-400 italic">No ownership history.</p>
            ) : (
              <div className="space-y-1">
                {history!.map(h => (
                  <div key={h.ownershipId} className="flex items-center justify-between text-xs text-gray-600">
                    <span>{h.acquiredVia.replace(/_/g, ' ')}</span>
                    <span className="font-mono text-gray-400">{h.ownerNationalId ?? h.ownerBusinessNumber}</span>
                    <span className="text-gray-400">{h.ownershipStartDate}{h.ownershipEndDate ? ` → ${h.ownershipEndDate}` : ' → now'}</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="text-xs text-gray-400 pt-1 border-t border-gray-50">
            VIN: <span className="font-mono">{vehicle.vin}</span>
            {vehicle.engineVolume && <> · {vehicle.engineVolume}L</>}
          </div>
        </div>
      )}
    </div>
  )
}

export default function Vehicles() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()

  const { data: vehicles, isLoading, isError } = useQuery({
    queryKey: ['vehicles', nationalId],
    queryFn: () => getVehiclesByOwner(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  if (isError) return <ServiceUnavailable />

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
          <Car className="w-6 h-6 text-primary" />
          {t(lang, 'vehicles')}
        </h1>
        <p className="mt-1 text-sm text-gray-500">{t(lang, 'vehiclesDesc')}</p>
      </div>

      {isLoading ? (
        <div className="space-y-4">{[1, 2].map(i => <SkeletonCard key={i} />)}</div>
      ) : (vehicles ?? []).length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-200 p-8 text-center">
          <Car className="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <p className="text-gray-500">{t(lang, 'noVehicles')}</p>
        </div>
      ) : (
        <div className="space-y-4">
          {vehicles!.map(v => <VehicleCard key={v.vehicleId} vehicle={v} />)}
        </div>
      )}
    </div>
  )
}
