export type BadgeVariant = 'green' | 'red' | 'yellow' | 'orange' | 'gray' | 'blue'

export function statusVariant(status: string): BadgeVariant {
  switch (status?.toUpperCase()) {
    case 'ACTIVE':
    case 'PASS':
    case 'COMPLIANT':
    case 'ACCEPTED':
    case 'APPROVED':
      return 'green'
    case 'FAIL':
    case 'NON_COMPLIANT':
    case 'REVOKED':
    case 'REJECTED':
      return 'red'
    case 'PENDING':
    case 'SUBMITTED':
      return 'yellow'
    case 'SUSPENDED':
      return 'orange'
    case 'EXPIRED':
    case 'DISSOLVED':
    case 'DEREGISTERED':
    case 'DECEASED':
    case 'INACTIVE':
      return 'gray'
    default:
      return 'blue'
  }
}

export const badgeClasses: Record<BadgeVariant, string> = {
  green: 'bg-green-50 text-green-700 ring-1 ring-green-200',
  red: 'bg-red-50 text-red-700 ring-1 ring-red-200',
  yellow: 'bg-amber-50 text-amber-700 ring-1 ring-amber-200',
  orange: 'bg-orange-50 text-orange-700 ring-1 ring-orange-200',
  gray: 'bg-gray-100 text-gray-600 ring-1 ring-gray-200',
  blue: 'bg-blue-50 text-blue-700 ring-1 ring-blue-200',
}
