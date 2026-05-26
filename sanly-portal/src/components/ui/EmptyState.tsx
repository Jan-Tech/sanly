import { FileX } from 'lucide-react'

interface EmptyStateProps {
  message: string
  icon?: React.ReactNode
}

export default function EmptyState({ message, icon }: EmptyStateProps) {
  return (
    <div className="card flex flex-col items-center justify-center py-12 text-center gap-3">
      <div className="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center">
        {icon ?? <FileX className="w-6 h-6 text-gray-400" />}
      </div>
      <p className="text-sm text-gray-500 max-w-sm">{message}</p>
    </div>
  )
}
