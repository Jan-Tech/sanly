import type { Lang } from '../i18n'

export function formatDate(iso: string | null | undefined, lang: Lang = 'en'): string {
  if (!iso) return '—'
  try {
    return new Date(iso).toLocaleDateString(
      lang === 'ru' ? 'ru-RU' : lang === 'tk' ? 'tk-TM' : 'en-GB',
      { day: '2-digit', month: 'short', year: 'numeric' }
    )
  } catch {
    return iso
  }
}

export function formatDateTime(iso: string | null | undefined, lang: Lang = 'en'): string {
  if (!iso) return '—'
  try {
    return new Date(iso).toLocaleString(
      lang === 'ru' ? 'ru-RU' : lang === 'tk' ? 'tk-TM' : 'en-GB',
      { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' }
    )
  } catch {
    return iso
  }
}

export function formatMoney(value: number | null | undefined, currency = 'TMT'): string {
  if (value == null) return '—'
  return `${value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} ${currency}`
}

export function maskNin(nin: string | null | undefined): string {
  if (!nin) return '—'
  if (nin.length < 6) return nin
  return nin.slice(0, 3) + '****' + nin.slice(-3)
}

export function formatTestType(type: string): string {
  return type.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase())
}

export function timeAgo(iso: string): string {
  const diff = Date.now() - new Date(iso).getTime()
  const minutes = Math.floor(diff / 60_000)
  if (minutes < 1) return 'just now'
  if (minutes < 60) return `${minutes}m ago`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}h ago`
  return `${Math.floor(hours / 24)}d ago`
}

export function decodeJwt(token: string): Record<string, unknown> {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64 + '=='.slice(0, (4 - (base64.length % 4)) % 4)
    const decoded = decodeURIComponent(
      atob(padded)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(decoded)
  } catch {
    return {}
  }
}
