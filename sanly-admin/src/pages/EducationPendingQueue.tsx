import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { BookOpen, CheckCircle } from 'lucide-react'
import { getPendingEnrollments, type PendingEnrollment } from '../api/education'
import { educationClient } from '../api/client'
import Badge from '../components/ui/Badge'

async function activateEnrollment(enrollmentId: string): Promise<PendingEnrollment> {
  const res = await educationClient.patch<PendingEnrollment>(
    `/api/v1/education/enrollments/${enrollmentId}/status`,
    { status: 'ACTIVE' }
  )
  return res.data
}

export default function EducationPendingQueue() {
  const qc = useQueryClient()

  const { data: pending, isLoading } = useQuery({
    queryKey: ['edu-pending'],
    queryFn: getPendingEnrollments,
    refetchInterval: 60_000,
  })

  const mutation = useMutation({
    mutationFn: activateEnrollment,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['edu-pending'] }),
  })

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-2">
        <BookOpen className="w-5 h-5 text-primary" />
        <h1 className="text-xl font-bold text-dark">School Enrollment Queue</h1>
        <span className="ml-1 px-2 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-700">
          {pending?.length ?? 0} pending
        </span>
      </div>

      <p className="text-sm text-gray-500">
        Children approaching school age (turning 6) are automatically queued here by the Civil Registry
        life-event system. Assign them to a school by activating their enrollment.
      </p>

      {isLoading ? (
        <div className="space-y-3">
          {[1,2,3,4].map(i => <div key={i} className="skeleton h-14 rounded-xl" />)}
        </div>
      ) : !pending || pending.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-100 p-10 text-center text-gray-400">
          <CheckCircle className="w-10 h-10 mx-auto mb-2 text-green-400" />
          <p className="font-medium">All caught up!</p>
          <p className="text-sm mt-1">No children are currently waiting for school enrollment.</p>
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-100 overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-xs uppercase text-gray-400 tracking-wide">
              <tr>
                <th className="text-left px-4 py-3">Child NIN</th>
                <th className="text-left px-4 py-3">Expected School Year</th>
                <th className="text-left px-4 py-3">Program</th>
                <th className="text-left px-4 py-3">Queued At</th>
                <th className="text-left px-4 py-3">Status</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {pending.map(e => (
                <tr key={e.enrollmentId} className="hover:bg-gray-50/50 transition-colors">
                  <td className="px-4 py-3 font-mono text-xs text-gray-600">{e.citizenNationalId}</td>
                  <td className="px-4 py-3 text-gray-700 font-medium">
                    {e.expectedGraduationYear
                      ? `${e.enrollmentDate.slice(0, 4)} → ${e.expectedGraduationYear}`
                      : e.enrollmentDate.slice(0, 4)}
                  </td>
                  <td className="px-4 py-3 text-gray-500">{e.programName ?? '—'}</td>
                  <td className="px-4 py-3 text-gray-400 text-xs">
                    {new Date(e.createdAt).toLocaleDateString()}
                  </td>
                  <td className="px-4 py-3">
                    <Badge label="Pending" status="PENDING" size="sm" />
                  </td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => mutation.mutate(e.enrollmentId)}
                      disabled={mutation.isPending}
                      className="text-xs font-medium text-primary hover:text-primary-dark transition-colors disabled:opacity-40"
                    >
                      Activate
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
