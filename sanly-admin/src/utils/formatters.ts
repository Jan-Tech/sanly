export function formatDate(value: string | null | undefined): string {
  if (!value) return '—'
  try {
    return new Intl.DateTimeFormat('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }).format(new Date(value))
  } catch { return value }
}

export function formatDateTime(value: string | null | undefined): string {
  if (!value) return '—'
  try {
    return new Intl.DateTimeFormat('en-GB', {
      day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
    }).format(new Date(value))
  } catch { return value }
}

export function maskNin(nin: string | null | undefined): string {
  if (!nin) return '—'
  if (nin.length !== 11) return nin
  return nin.slice(0, 3) + 'xxxxxx' + nin.slice(-2)
}

export function relativeTime(value: string): string {
  const diff = Date.now() - new Date(value).getTime()
  const m = Math.floor(diff / 60000)
  if (m < 1) return 'just now'
  if (m < 60) return `${m}m ago`
  const h = Math.floor(m / 60)
  if (h < 24) return `${h}h ago`
  return `${Math.floor(h / 24)}d ago`
}

export function groupExchangesByHour(logs: { exchangedAt: string; result: string }[]): Array<{
  hour: string; total: number; success: number; denied: number
}> {
  const buckets: Record<string, { total: number; success: number; denied: number }> = {}
  const now = new Date()

  for (let i = 23; i >= 0; i--) {
    const t = new Date(now.getTime() - i * 3_600_000)
    const key = t.getHours().toString().padStart(2, '0') + ':00'
    buckets[key] = { total: 0, success: 0, denied: 0 }
  }

  logs.forEach((l) => {
    const h = new Date(l.exchangedAt).getHours().toString().padStart(2, '0') + ':00'
    if (buckets[h]) {
      buckets[h].total++
      if (l.result === 'SUCCESS') buckets[h].success++
      if (l.result === 'DENIED') buckets[h].denied++
    }
  })

  return Object.entries(buckets).map(([hour, v]) => ({ hour, ...v }))
}
