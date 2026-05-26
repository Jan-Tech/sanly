import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Coins } from 'lucide-react'
import { getScheduledPayments, markPaymentPaid, type AdminPayment } from '../api/pension'
import Badge from '../components/ui/Badge'
import ConfirmDialog from '../components/ui/ConfirmDialog'
import { useState } from 'react'

export default function PensionPayments() {
  const qc = useQueryClient()
  const [markTarget, setMarkTarget] = useState<AdminPayment | null>(null)

  const { data: payments, isLoading } = useQuery({
    queryKey: ['scheduled-payments'],
    queryFn: getScheduledPayments,
  })

  const markPaidMutation = useMutation({
    mutationFn: (id: string) => markPaymentPaid(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['scheduled-payments'] }); setMarkTarget(null) },
  })

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3">
        <Coins className="w-6 h-6 text-primary" />
        <h1 className="text-xl font-bold text-gray-900">Pension Payments — This Month</h1>
        {(payments?.length ?? 0) > 0 && (
          <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-700">
            {payments!.length} scheduled
          </span>
        )}
      </div>

      {isLoading ? (
        <div className="space-y-2">{[1,2,3].map(i => <div key={i} className="h-16 bg-gray-100 rounded-xl animate-pulse" />)}</div>
      ) : (payments ?? []).length === 0 ? (
        <p className="text-sm text-gray-500 italic py-8 text-center">No scheduled payments this month.</p>
      ) : (
        <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-xs text-gray-400 uppercase">
              <tr>
                <th className="px-4 py-3 text-left">Account</th>
                <th className="px-4 py-3 text-left">NIN</th>
                <th className="px-4 py-3 text-left">Month</th>
                <th className="px-4 py-3 text-right">Amount</th>
                <th className="px-4 py-3 text-left">Scheduled</th>
                <th className="px-4 py-3 text-left">Status</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {payments!.map((p: AdminPayment) => (
                <tr key={p.paymentId} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-mono text-xs">{p.accountCode}</td>
                  <td className="px-4 py-3 font-mono text-xs">{p.citizenNationalId}</td>
                  <td className="px-4 py-3 text-xs">{p.paymentMonth}</td>
                  <td className="px-4 py-3 text-right font-semibold">{p.amount} TMT</td>
                  <td className="px-4 py-3 text-xs text-gray-500">{p.scheduledDate}</td>
                  <td className="px-4 py-3">
                    <Badge variant={p.status === 'PAID' ? 'ACTIVE' : p.status === 'FAILED' ? 'FAIL' : 'PENDING'} label={p.status} />
                  </td>
                  <td className="px-4 py-3 text-right">
                    {p.status === 'SCHEDULED' && (
                      <button onClick={() => setMarkTarget(p)}
                        className="px-3 py-1 bg-green-600 text-white text-xs rounded-lg hover:bg-green-700">
                        Mark Paid
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {markTarget && (
        <ConfirmDialog
          title="Mark Payment as Paid"
          message={`Confirm payment of ${markTarget.amount} TMT for account ${markTarget.accountCode} (${markTarget.paymentMonth})?`}
          onConfirm={() => markPaidMutation.mutate(markTarget.paymentId)}
          onCancel={() => setMarkTarget(null)}
          loading={markPaidMutation.isPending}
        />
      )}
    </div>
  )
}
