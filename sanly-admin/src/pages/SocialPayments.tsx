import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Coins, CheckCircle2 } from 'lucide-react'
import { getScheduledPayments, markPaymentPaid, type AdminPayment } from '../api/social'
import Badge from '../components/ui/Badge'

export default function SocialPayments() {
  const qc = useQueryClient()

  const { data: scheduled, isLoading } = useQuery({
    queryKey: ['social-scheduled-payments'],
    queryFn: getScheduledPayments,
    refetchInterval: 60_000,
  })

  const paidMutation = useMutation({
    mutationFn: (paymentId: string) => markPaymentPaid(paymentId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['social-scheduled-payments'] }),
  })

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-2">
        <Coins className="w-5 h-5 text-primary" />
        <h1 className="text-xl font-bold text-dark">Scheduled Payments</h1>
        <span className={`ml-1 px-2 py-0.5 rounded-full text-xs font-medium ${(scheduled?.length ?? 0) > 0 ? 'bg-amber-100 text-amber-700' : 'bg-gray-100 text-gray-500'}`}>
          {scheduled?.length ?? 0} scheduled
        </span>
      </div>
      <p className="text-sm text-gray-500">
        Payments scheduled for the current period. Mark each as PAID after disbursement.
        New payments are auto-created on the 1st of each month by the scheduled job.
      </p>

      {isLoading ? (
        <div className="space-y-3">{[1,2,3].map(i => <div key={i} className="skeleton h-16 rounded-xl" />)}</div>
      ) : !scheduled || scheduled.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-100 p-10 text-center text-gray-400">
          <CheckCircle2 className="w-10 h-10 mx-auto mb-2 text-green-400" />
          <p className="font-medium">All payments processed for this period</p>
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-100 overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-xs uppercase text-gray-400 tracking-wide">
              <tr>
                <th className="text-left px-4 py-3">Citizen NIN</th>
                <th className="text-left px-4 py-3">Claim Code</th>
                <th className="text-left px-4 py-3">Period</th>
                <th className="text-left px-4 py-3">Amount</th>
                <th className="text-left px-4 py-3">Scheduled Date</th>
                <th className="text-left px-4 py-3">Status</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {scheduled.map(p => (
                <tr key={p.paymentId} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-mono text-xs">{p.citizenNationalId}</td>
                  <td className="px-4 py-3 font-mono text-xs">{p.claimCode}</td>
                  <td className="px-4 py-3">{p.paymentPeriod}</td>
                  <td className="px-4 py-3 font-semibold">{p.amount}</td>
                  <td className="px-4 py-3 text-gray-500">{p.scheduledDate}</td>
                  <td className="px-4 py-3"><Badge variant="PENDING" label="SCHEDULED" /></td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => paidMutation.mutate(p.paymentId)}
                      disabled={paidMutation.isPending}
                      className="px-3 py-1.5 bg-green-600 text-white rounded-lg text-xs font-medium hover:bg-green-700 disabled:opacity-50"
                    >
                      Mark Paid
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
