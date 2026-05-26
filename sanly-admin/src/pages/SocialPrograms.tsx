import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { List, PlusCircle, ToggleLeft, ToggleRight } from 'lucide-react'
import { getPrograms, createProgram, updateProgramStatus, type AdminBenefitProgram } from '../api/social'
import Badge from '../components/ui/Badge'

const BENEFIT_TYPES = [
  'CHILD_BENEFIT', 'MATERNITY', 'PATERNITY', 'DISABILITY',
  'UNEMPLOYMENT', 'PENSION', 'SURVIVOR', 'HOUSING', 'EDUCATION_GRANT', 'LOW_INCOME',
]

function programStatusVariant(status: string) {
  if (status === 'ACTIVE') return 'ACTIVE'
  if (status === 'SUSPENDED') return 'SUSPENDED'
  return 'FAIL'
}

export default function SocialPrograms() {
  const qc = useQueryClient()
  const [showCreate, setShowCreate] = useState(false)
  const [form, setForm] = useState({
    name: '', description: '', benefitType: 'CHILD_BENEFIT',
    monthlyAmount: '', eligibilityCriteria: '', maxDurationMonths: '',
  })

  const { data: programs, isLoading } = useQuery({
    queryKey: ['social-programs'],
    queryFn: getPrograms,
  })

  const createMutation = useMutation({
    mutationFn: () => createProgram({
      name: form.name,
      description: form.description,
      benefitType: form.benefitType,
      monthlyAmount: form.monthlyAmount,
      eligibilityCriteria: form.eligibilityCriteria,
      maxDurationMonths: form.maxDurationMonths ? Number(form.maxDurationMonths) : null,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['social-programs'] })
      setShowCreate(false)
      setForm({ name: '', description: '', benefitType: 'CHILD_BENEFIT', monthlyAmount: '', eligibilityCriteria: '', maxDurationMonths: '' })
    },
  })

  const statusMutation = useMutation({
    mutationFn: ({ code, status }: { code: string; status: string }) => updateProgramStatus(code, status),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['social-programs'] }),
  })

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <List className="w-5 h-5 text-primary" />
          <h1 className="text-xl font-bold text-dark">Benefit Programs</h1>
          <span className="ml-1 px-2 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-500">
            {programs?.length ?? 0} programs
          </span>
        </div>
        <button
          onClick={() => setShowCreate(true)}
          className="flex items-center gap-2 px-4 py-2 bg-primary text-white rounded-lg text-sm font-medium hover:bg-primary-dark"
        >
          <PlusCircle className="w-4 h-4" />
          New Program
        </button>
      </div>

      {isLoading ? (
        <div className="space-y-3">{[1,2,3].map(i => <div key={i} className="skeleton h-16 rounded-xl" />)}</div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-100 overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-xs uppercase text-gray-400 tracking-wide">
              <tr>
                <th className="text-left px-4 py-3">Code</th>
                <th className="text-left px-4 py-3">Name</th>
                <th className="text-left px-4 py-3">Type</th>
                <th className="text-left px-4 py-3">Monthly Amount</th>
                <th className="text-left px-4 py-3">Max Duration</th>
                <th className="text-left px-4 py-3">Status</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {(programs ?? []).map(prog => (
                <tr key={prog.programId} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-mono text-xs">{prog.programCode}</td>
                  <td className="px-4 py-3 font-medium">{prog.name}</td>
                  <td className="px-4 py-3 text-gray-500 text-xs">{prog.benefitType}</td>
                  <td className="px-4 py-3">{prog.monthlyAmount}</td>
                  <td className="px-4 py-3 text-gray-500">{prog.maxDurationMonths ? `${prog.maxDurationMonths}mo` : 'Indefinite'}</td>
                  <td className="px-4 py-3"><Badge variant={programStatusVariant(prog.status)} label={prog.status} /></td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => statusMutation.mutate({
                        code: prog.programCode,
                        status: prog.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE',
                      })}
                      className="text-gray-400 hover:text-primary p-1"
                      title={prog.status === 'ACTIVE' ? 'Suspend' : 'Activate'}
                    >
                      {prog.status === 'ACTIVE'
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

      {showCreate && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="bg-white rounded-2xl shadow-xl p-6 w-full max-w-lg space-y-4">
            <h2 className="text-lg font-bold text-dark">Create Benefit Program</h2>
            <div className="grid grid-cols-2 gap-4">
              <div className="col-span-2">
                <label className="text-xs text-gray-500 mb-1 block">Program Name</label>
                <input className="w-full border border-gray-200 rounded-lg p-2 text-sm" value={form.name}
                  onChange={e => setForm(f => ({ ...f, name: e.target.value }))} />
              </div>
              <div>
                <label className="text-xs text-gray-500 mb-1 block">Benefit Type</label>
                <select className="w-full border border-gray-200 rounded-lg p-2 text-sm" value={form.benefitType}
                  onChange={e => setForm(f => ({ ...f, benefitType: e.target.value }))}>
                  {BENEFIT_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                </select>
              </div>
              <div>
                <label className="text-xs text-gray-500 mb-1 block">Monthly Amount</label>
                <input className="w-full border border-gray-200 rounded-lg p-2 text-sm" value={form.monthlyAmount}
                  onChange={e => setForm(f => ({ ...f, monthlyAmount: e.target.value }))} placeholder="e.g. 500.00" />
              </div>
              <div>
                <label className="text-xs text-gray-500 mb-1 block">Max Duration (months, blank = indefinite)</label>
                <input type="number" className="w-full border border-gray-200 rounded-lg p-2 text-sm" value={form.maxDurationMonths}
                  onChange={e => setForm(f => ({ ...f, maxDurationMonths: e.target.value }))} />
              </div>
              <div>
                <label className="text-xs text-gray-500 mb-1 block">Description</label>
                <input className="w-full border border-gray-200 rounded-lg p-2 text-sm" value={form.description}
                  onChange={e => setForm(f => ({ ...f, description: e.target.value }))} />
              </div>
              <div className="col-span-2">
                <label className="text-xs text-gray-500 mb-1 block">Eligibility Criteria</label>
                <textarea className="w-full border border-gray-200 rounded-lg p-2 text-sm h-16 resize-none"
                  value={form.eligibilityCriteria}
                  onChange={e => setForm(f => ({ ...f, eligibilityCriteria: e.target.value }))} />
              </div>
            </div>
            <div className="flex gap-3 justify-end">
              <button className="px-4 py-2 text-sm text-gray-600 border border-gray-200 rounded-lg hover:bg-gray-50"
                onClick={() => setShowCreate(false)}>Cancel</button>
              <button
                className="px-4 py-2 text-sm bg-primary text-white rounded-lg hover:bg-primary-dark disabled:opacity-50"
                disabled={!form.name || !form.monthlyAmount || createMutation.isPending}
                onClick={() => createMutation.mutate()}
              >
                {createMutation.isPending ? 'Creating...' : 'Create'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
