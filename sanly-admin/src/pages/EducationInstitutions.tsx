import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { GraduationCap } from 'lucide-react'
import { getInstitutions, updateInstitutionStatus, type EducationInstitution } from '../api/education'
import Badge from '../components/ui/Badge'
import ConfirmDialog from '../components/ui/ConfirmDialog'

const STATUS_ACTIONS = [
  { label: 'Activate',  value: 'ACTIVE'    as const, icon: CheckCircle,  color: 'text-green-600' },
  { label: 'Suspend',   value: 'SUSPENDED' as const, icon: PauseCircle,  color: 'text-yellow-600' },
  { label: 'Close',     value: 'CLOSED'    as const, icon: XCircle,      color: 'text-red-500' },
]

const TYPE_LABELS: Record<string, string> = {
  PRIMARY_SCHOOL: 'Primary School',
  SECONDARY_SCHOOL: 'Secondary School',
  VOCATIONAL: 'Vocational',
  UNIVERSITY: 'University',
  POSTGRADUATE: 'Postgraduate',
  INTERNATIONAL: 'International',
}

export default function EducationInstitutions() {
  const qc = useQueryClient()
  const [confirm, setConfirm] = useState<{
    institution: EducationInstitution
    status: 'ACTIVE' | 'SUSPENDED' | 'CLOSED'
  } | null>(null)

  const { data: institutions, isLoading } = useQuery({
    queryKey: ['edu-institutions'],
    queryFn: getInstitutions,
  })

  const mutation = useMutation({
    mutationFn: ({ code, status }: { code: string; status: 'ACTIVE' | 'SUSPENDED' | 'CLOSED' }) =>
      updateInstitutionStatus(code, status),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['edu-institutions'] })
      setConfirm(null)
    },
  })

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-2">
        <GraduationCap className="w-5 h-5 text-primary" />
        <h1 className="text-xl font-bold text-dark">Education Institutions</h1>
        <span className="ml-auto text-sm text-gray-400">{institutions?.length ?? 0} registered</span>
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {[1,2,3].map(i => <div key={i} className="skeleton h-20 rounded-xl" />)}
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-100 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-xs uppercase text-gray-400 tracking-wide">
              <tr>
                <th className="text-left px-4 py-3">Code</th>
                <th className="text-left px-4 py-3">Name</th>
                <th className="text-left px-4 py-3">Type</th>
                <th className="text-left px-4 py-3">Region</th>
                <th className="text-left px-4 py-3">Accredited Until</th>
                <th className="text-left px-4 py-3">Status</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {(institutions ?? []).map(inst => (
                <tr key={inst.institutionId} className="hover:bg-gray-50/50 transition-colors">
                  <td className="px-4 py-3 font-mono text-xs text-gray-500">{inst.institutionCode}</td>
                  <td className="px-4 py-3 font-medium text-dark">{inst.name}</td>
                  <td className="px-4 py-3 text-gray-500">{TYPE_LABELS[inst.type] || inst.type}</td>
                  <td className="px-4 py-3 text-gray-500">{inst.region}</td>
                  <td className="px-4 py-3 text-gray-500">{inst.accreditedUntil ?? '—'}</td>
                  <td className="px-4 py-3">
                    <Badge
                      label={inst.status}
                      status={inst.status === 'ACTIVE' ? 'ACTIVE' : inst.status === 'SUSPENDED' ? 'PENDING' : 'FAIL'}
                      size="sm"
                    />
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-1">
                      {STATUS_ACTIONS.filter(a => a.value !== inst.status).map(action => (
                        <button
                          key={action.value}
                          onClick={() => setConfirm({ institution: inst, status: action.value })}
                          className={`text-xs font-medium ${action.color} hover:opacity-70 transition-opacity`}
                        >
                          {action.label}
                        </button>
                      ))}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <ConfirmDialog
        isOpen={confirm !== null}
        onClose={() => setConfirm(null)}
        title={confirm ? `${confirm.status === 'ACTIVE' ? 'Activate' : confirm.status === 'SUSPENDED' ? 'Suspend' : 'Close'} Institution` : ''}
        message={confirm ? `Change "${confirm.institution.name}" status to ${confirm.status}?` : ''}
        confirmLabel={confirm?.status === 'CLOSED' ? 'Close Institution' : confirm?.status ?? 'Confirm'}
        onConfirm={() => confirm && mutation.mutate({ code: confirm.institution.institutionCode, status: confirm.status })}
        loading={mutation.isPending}
        danger={confirm?.status === 'CLOSED'}
      />
    </div>
  )
}
