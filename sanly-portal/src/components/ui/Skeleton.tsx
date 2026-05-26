interface SkeletonProps {
  className?: string
  count?: number
}

function SkeletonLine({ className = '' }: { className?: string }) {
  return <div className={`skeleton rounded ${className}`} />
}

export function SkeletonCard() {
  return (
    <div className="card space-y-3">
      <SkeletonLine className="h-4 w-1/3" />
      <SkeletonLine className="h-8 w-1/2" />
      <SkeletonLine className="h-3 w-2/3" />
    </div>
  )
}

export function SkeletonRow() {
  return (
    <div className="flex items-center gap-4 py-3">
      <SkeletonLine className="h-8 w-8 rounded-full shrink-0" />
      <div className="flex-1 space-y-2">
        <SkeletonLine className="h-3 w-1/3" />
        <SkeletonLine className="h-3 w-1/2" />
      </div>
      <SkeletonLine className="h-6 w-16 rounded-full" />
    </div>
  )
}

export function SkeletonTable({ rows = 5 }: { rows?: number }) {
  return (
    <div className="space-y-1">
      {Array.from({ length: rows }).map((_, i) => (
        <SkeletonRow key={i} />
      ))}
    </div>
  )
}

export default function Skeleton({ className = '', count = 1 }: SkeletonProps) {
  return (
    <>
      {Array.from({ length: count }).map((_, i) => (
        <SkeletonLine key={i} className={className} />
      ))}
    </>
  )
}
