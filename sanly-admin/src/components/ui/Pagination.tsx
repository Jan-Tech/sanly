import { ChevronLeft, ChevronRight } from 'lucide-react'

interface PaginationProps {
  page: number
  totalPages: number
  totalElements: number
  size: number
  onChange: (page: number) => void
}

export default function Pagination({ page, totalPages, totalElements, size, onChange }: PaginationProps) {
  if (totalPages <= 1) return null
  const from = page * size + 1
  const to = Math.min((page + 1) * size, totalElements)

  return (
    <div className="flex items-center justify-between px-4 py-3 border-t border-gray-100 text-sm">
      <span className="text-gray-500">
        {from}–{to} of {totalElements}
      </span>
      <div className="flex items-center gap-1">
        <button
          disabled={page === 0}
          onClick={() => onChange(page - 1)}
          className="p-1.5 rounded-lg hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed">
          <ChevronLeft className="w-4 h-4" />
        </button>
        <span className="px-3 text-gray-600">Page {page + 1} / {totalPages}</span>
        <button
          disabled={page >= totalPages - 1}
          onClick={() => onChange(page + 1)}
          className="p-1.5 rounded-lg hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed">
          <ChevronRight className="w-4 h-4" />
        </button>
      </div>
    </div>
  )
}
