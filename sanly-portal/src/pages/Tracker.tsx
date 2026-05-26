import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Activity, ChevronDown, ChevronRight } from 'lucide-react'
import { useLangStore } from '../store/langStore'
import { t } from '../utils/i18n'
import { getMyTrackedItems, getTrackedItem, type TrackedItem } from '../api/documents'
import Badge from '../components/ui/Badge'

const STATUS_COLORS: Record<string, 'green' | 'yellow' | 'red' | 'gray' | 'blue'> = {
  SUBMITTED: 'blue',
  IN_REVIEW: 'yellow',
  APPROVED: 'green',
  REJECTED: 'red',
  COMPLETED: 'green',
  PENDING_DOCS: 'yellow',
  CANCELLED: 'gray',
  ON_HOLD: 'gray',
}

function statusLabel(status: string, lang: string): string {
  const map: Record<string, any> = {
    SUBMITTED: 'trackStatusSubmitted',
    IN_REVIEW: 'trackStatusInReview',
    APPROVED: 'trackStatusApproved',
    REJECTED: 'trackStatusRejected',
    COMPLETED: 'trackStatusCompleted',
    PENDING_DOCS: 'trackStatusPendingDocs',
    CANCELLED: 'trackStatusCancelled',
    ON_HOLD: 'trackStatusOnHold',
  }
  const key = map[status]
  return key ? t(lang as any, key) : status
}

function Timeline({ item }: { item: TrackedItem }) {
  const { lang } = useLangStore()
  const sorted = [...(item.updates ?? [])].sort(
    (a, b) => new Date(a.updatedAt).getTime() - new Date(b.updatedAt).getTime()
  )
  return (
    <ol className="relative border-l border-gray-200 ml-3 mt-3 space-y-5">
      {sorted.map((u) => (
        <li key={u.updateId} className="ml-4">
          <div className="absolute -left-1.5 w-3 h-3 rounded-full border-2 border-white bg-blue-500" />
          <time className="text-xs text-gray-400">{new Date(u.updatedAt).toLocaleString()}</time>
          <div className="flex items-center gap-2 mt-0.5">
            <Badge color={STATUS_COLORS[u.status] ?? 'gray'}>{statusLabel(u.status, lang)}</Badge>
            <span className="text-xs text-gray-500">{u.updatedByService}</span>
          </div>
          {u.description && <p className="text-sm text-gray-700 mt-0.5">{u.description}</p>}
        </li>
      ))}
    </ol>
  )
}

function TrackedItemRow({ item }: { item: TrackedItem }) {
  const { lang } = useLangStore()
  const [expanded, setExpanded] = useState(false)
  const [detail, setDetail] = useState<TrackedItem | null>(null)
  const [loading, setLoading] = useState(false)

  async function handleExpand() {
    if (!expanded && !detail) {
      setLoading(true)
      try {
        const d = await getTrackedItem(item.trackingCode)
        setDetail(d)
      } catch { /* fall back to item.updates */ }
      finally { setLoading(false) }
    }
    setExpanded(prev => !prev)
  }

  const displayItem = detail ?? item

  return (
    <div className="border border-gray-100 rounded-xl overflow-hidden">
      <button
        onClick={handleExpand}
        className="w-full flex items-center justify-between px-4 py-3 hover:bg-gray-50 text-left transition-colors"
      >
        <div className="flex items-center gap-3 min-w-0">
          {expanded ? <ChevronDown className="w-4 h-4 text-gray-400 shrink-0" /> : <ChevronRight className="w-4 h-4 text-gray-400 shrink-0" />}
          <div className="min-w-0">
            <p className="font-medium text-sm text-gray-900 truncate">{item.title}</p>
            <p className="text-xs text-gray-400 font-mono">{item.trackingCode}</p>
          </div>
        </div>
        <div className="flex items-center gap-3 shrink-0 ml-2">
          <span className="text-xs text-gray-400 hidden sm:block">{item.sourceService}</span>
          <Badge color={STATUS_COLORS[item.currentStatus] ?? 'gray'}>
            {statusLabel(item.currentStatus, lang)}
          </Badge>
        </div>
      </button>

      {expanded && (
        <div className="px-4 pb-4 border-t border-gray-50 bg-gray-50/50">
          <dl className="grid grid-cols-2 sm:grid-cols-3 gap-x-4 gap-y-2 text-sm mt-3">
            <div>
              <dt className="text-xs text-gray-500 font-medium">{t(lang, 'trackingItemType')}</dt>
              <dd className="text-gray-800">{item.itemType.replace(/_/g, ' ')}</dd>
            </div>
            <div>
              <dt className="text-xs text-gray-500 font-medium">{t(lang, 'trackingService')}</dt>
              <dd className="text-gray-800">{item.sourceService}</dd>
            </div>
            <div>
              <dt className="text-xs text-gray-500 font-medium">Created</dt>
              <dd className="text-gray-800">{new Date(item.createdAt).toLocaleDateString()}</dd>
            </div>
            {item.completedAt && (
              <div>
                <dt className="text-xs text-gray-500 font-medium">Completed</dt>
                <dd className="text-gray-800">{new Date(item.completedAt).toLocaleDateString()}</dd>
              </div>
            )}
          </dl>

          <h4 className="text-sm font-semibold text-gray-700 mt-4 mb-1">{t(lang, 'trackingTimeline')}</h4>
          {loading ? (
            <p className="text-xs text-gray-400">{t(lang, 'loading')}</p>
          ) : (displayItem.updates?.length ?? 0) === 0 ? (
            <p className="text-xs text-gray-400">{t(lang, 'noData')}</p>
          ) : (
            <Timeline item={displayItem} />
          )}
        </div>
      )}
    </div>
  )
}

export default function Tracker() {
  const { lang } = useLangStore()
  const [search, setSearch] = useState('')

  const { data: items = [], isLoading } = useQuery({
    queryKey: ['my-tracked-items'],
    queryFn: () => getMyTrackedItems(),
  })

  const filtered = items.filter(item =>
    !search ||
    item.trackingCode.toLowerCase().includes(search.toLowerCase()) ||
    item.title.toLowerCase().includes(search.toLowerCase()) ||
    item.sourceService.toLowerCase().includes(search.toLowerCase())
  )

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
          <Activity className="w-6 h-6 text-blue-600" />
          {t(lang, 'tracker')}
        </h1>
        <p className="text-sm text-gray-500 mt-1">{t(lang, 'trackerDesc')}</p>
      </div>

      <div>
        <input
          value={search}
          onChange={e => setSearch(e.target.value)}
          placeholder={`${t(lang, 'trackingCode')} / ${t(lang, 'trackingService')}...`}
          className="w-full max-w-md border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      {isLoading ? (
        <p className="text-sm text-gray-400">{t(lang, 'loading')}</p>
      ) : filtered.length === 0 ? (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-8 text-center">
          <Activity className="w-10 h-10 text-gray-300 mx-auto mb-3" />
          <p className="text-sm text-gray-400">{t(lang, 'noTrackedItems')}</p>
        </div>
      ) : (
        <div className="space-y-2">
          {filtered.map(item => (
            <TrackedItemRow key={item.trackingId} item={item} />
          ))}
        </div>
      )}
    </div>
  )
}
