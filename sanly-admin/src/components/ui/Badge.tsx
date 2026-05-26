type Variant = 'green' | 'red' | 'yellow' | 'orange' | 'gray' | 'blue' | 'purple'

const variants: Record<Variant, string> = {
  green:  'bg-green-50 text-green-700 ring-1 ring-green-200',
  red:    'bg-red-50 text-red-700 ring-1 ring-red-200',
  yellow: 'bg-amber-50 text-amber-700 ring-1 ring-amber-200',
  orange: 'bg-orange-50 text-orange-700 ring-1 ring-orange-200',
  gray:   'bg-gray-100 text-gray-600 ring-1 ring-gray-200',
  blue:   'bg-blue-50 text-blue-700 ring-1 ring-blue-200',
  purple: 'bg-purple-50 text-purple-700 ring-1 ring-purple-200',
}

function variantFromStatus(status: string): Variant {
  switch (status?.toUpperCase()) {
    case 'ACTIVE': case 'SUCCESS': case 'PASS': case 'COMPLIANT': case 'ACCEPTED': case 'APPROVED': return 'green'
    case 'FAIL': case 'NON_COMPLIANT': case 'REVOKED': case 'DENIED': case 'REJECTED': case 'CRITICAL': return 'red'
    case 'PENDING': case 'SUBMITTED': case 'HIGH': return 'orange'
    case 'MEDIUM': return 'yellow'
    case 'LOW': return 'blue'
    case 'SUSPENDED': return 'orange'
    case 'EXPIRED': case 'DISSOLVED': case 'DECEASED': case 'DISMISSED': return 'gray'
    case 'REVIEWED': return 'purple'
    case 'PUBLISH': return 'blue'
    case 'NOT_FOUND': return 'gray'
    default: return 'blue'
  }
}

interface BadgeProps {
  label: string
  status?: string
  variant?: Variant
  size?: 'sm' | 'md'
}

export default function Badge({ label, status, variant, size = 'md' }: BadgeProps) {
  const v = variant ?? (status ? variantFromStatus(status) : 'blue')
  const sz = size === 'sm' ? 'px-2 py-0.5 text-[10px]' : 'px-2.5 py-1 text-xs'
  return <span className={`inline-flex items-center rounded-full font-medium ${sz} ${variants[v]}`}>{label}</span>
}
