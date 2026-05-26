import { useQuery } from '@tanstack/react-query'
import { Shield, AlertTriangle } from 'lucide-react'
import { getInspectionsDue, type AdminInspection } from '../api/vehicle'

export default function VehicleInsuranceAlerts() {
  const { data: inspections, isLoading } = useQuery({
    queryKey: ['inspections-due'],
    queryFn: getInspectionsDue,
  })

  const today = new Date()

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3">
        <Shield className="w-6 h-6 text-amber-500" />
        <h1 className="text-xl font-bold text-gray-900">Inspections Due</h1>
        {(inspections?.length ?? 0) > 0 && (
          <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-700">
            {inspections!.length} due within 30 days
          </span>
        )}
      </div>

      <p className="text-sm text-gray-500">
        Vehicles whose technical inspection is due within the next 30 days.
        Owners are automatically notified by the daily scheduler.
      </p>

      {isLoading ? (
        <div className="space-y-3">{[1,2,3].map(i => <div key={i} className="h-16 bg-gray-100 rounded-xl animate-pulse" />)}</div>
      ) : (inspections ?? []).length === 0 ? (
        <p className="text-sm text-gray-500 italic py-8 text-center">No vehicles with inspection due soon.</p>
      ) : (
        <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-xs text-gray-400 uppercase">
              <tr>
                <th className="px-4 py-3 text-left">Plate Number</th>
                <th className="px-4 py-3 text-left">Last Inspection</th>
                <th className="px-4 py-3 text-left">Next Due</th>
                <th className="px-4 py-3 text-left">Last Result</th>
                <th className="px-4 py-3 text-left">Days Remaining</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {inspections!.map((insp: AdminInspection) => {
                const dueDate = new Date(insp.nextInspectionDue)
                const daysLeft = Math.ceil((dueDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
                const isOverdue = daysLeft < 0
                return (
                  <tr key={insp.inspectionId} className={`hover:bg-gray-50 ${isOverdue ? 'bg-red-50' : ''}`}>
                    <td className="px-4 py-3 font-mono font-semibold">{insp.plateNumber}</td>
                    <td className="px-4 py-3 text-gray-500">{insp.inspectionDate}</td>
                    <td className={`px-4 py-3 font-semibold ${isOverdue ? 'text-red-600' : 'text-amber-700'}`}>
                      {insp.nextInspectionDue}
                    </td>
                    <td className="px-4 py-3">
                      <span className={`text-xs font-medium ${insp.result === 'PASSED' ? 'text-green-600' : insp.result === 'FAILED' ? 'text-red-600' : 'text-amber-600'}`}>
                        {insp.result}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      {isOverdue ? (
                        <span className="flex items-center gap-1 text-xs text-red-600 font-semibold">
                          <AlertTriangle className="w-3.5 h-3.5" />
                          {Math.abs(daysLeft)} days overdue
                        </span>
                      ) : (
                        <span className="text-xs text-amber-600">{daysLeft} days</span>
                      )}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
