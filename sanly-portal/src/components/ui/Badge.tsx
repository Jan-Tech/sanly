import { badgeClasses, statusVariant, type BadgeVariant } from '../../utils/badges'

interface BadgeProps {
  label: string
  status?: string
  variant?: BadgeVariant
  size?: 'sm' | 'md'
}

export default function Badge({ label, status, variant, size = 'md' }: BadgeProps) {
  const v = variant ?? (status ? statusVariant(status) : 'blue')
  const cls = badgeClasses[v]
  const sizeCls = size === 'sm' ? 'px-2 py-0.5 text-xs' : 'px-2.5 py-1 text-xs'
  return (
    <span className={`inline-flex items-center rounded-full font-medium ${sizeCls} ${cls}`}>
      {label}
    </span>
  )
}
