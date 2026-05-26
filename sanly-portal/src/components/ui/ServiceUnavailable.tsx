import { WifiOff, RefreshCw } from 'lucide-react'
import { t } from '../../utils/i18n'
import { useLangStore } from '../../store/langStore'

interface ServiceUnavailableProps {
  onRetry?: () => void
}

export default function ServiceUnavailable({ onRetry }: ServiceUnavailableProps) {
  const { lang } = useLangStore()
  return (
    <div className="card flex flex-col items-center justify-center py-12 text-center gap-4">
      <div className="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center">
        <WifiOff className="w-6 h-6 text-gray-400" />
      </div>
      <div>
        <p className="font-semibold text-dark">{t(lang, 'serviceUnavailable')}</p>
        <p className="text-sm text-gray-500 mt-1">{t(lang, 'serviceUnavailableMsg')}</p>
      </div>
      {onRetry && (
        <button onClick={onRetry} className="btn-secondary gap-2">
          <RefreshCw className="w-4 h-4" />
          {t(lang, 'retry')}
        </button>
      )}
    </div>
  )
}
