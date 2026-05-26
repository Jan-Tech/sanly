import Modal from './Modal'
import { AlertTriangle } from 'lucide-react'

interface ConfirmDialogProps {
  isOpen: boolean
  onClose: () => void
  onConfirm: () => void
  title: string
  message: string
  confirmLabel?: string
  danger?: boolean
  loading?: boolean
}

export default function ConfirmDialog({
  isOpen, onClose, onConfirm, title, message,
  confirmLabel = 'Confirm', danger = false, loading = false,
}: ConfirmDialogProps) {
  return (
    <Modal isOpen={isOpen} onClose={onClose} title="" size="sm"
      footer={
        <>
          <button onClick={onClose} className="btn-secondary">Cancel</button>
          <button onClick={onConfirm} disabled={loading}
            className={danger ? 'btn-danger' : 'btn-primary'}>
            {loading ? 'Processing...' : confirmLabel}
          </button>
        </>
      }>
      <div className="flex flex-col items-center text-center gap-4 py-2">
        <div className={`w-12 h-12 rounded-full flex items-center justify-center ${danger ? 'bg-red-100' : 'bg-primary/10'}`}>
          <AlertTriangle className={`w-6 h-6 ${danger ? 'text-red-600' : 'text-primary'}`} />
        </div>
        <div>
          <p className="font-semibold text-dark text-base">{title}</p>
          <p className="text-sm text-gray-500 mt-1">{message}</p>
        </div>
      </div>
    </Modal>
  )
}
