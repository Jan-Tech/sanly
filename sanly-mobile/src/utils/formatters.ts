import type { Lang } from '../i18n'

const dateLocales: Record<Lang, string> = {
  en: 'en-GB',
  tk: 'tk-TM',
  ru: 'ru-RU',
}

export function formatDate(iso: string | null | undefined, lang: Lang = 'en'): string {
  if (!iso) return '—'
  try {
    return new Date(iso).toLocaleDateString(dateLocales[lang], {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    })
  } catch {
    return iso
  }
}

export function formatDateTime(iso: string | null | undefined, lang: Lang = 'en'): string {
  if (!iso) return '—'
  try {
    return new Date(iso).toLocaleString(dateLocales[lang], {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  } catch {
    return iso
  }
}

export function formatMoney(value: string | null | undefined): string {
  if (!value) return '—'
  const n = parseFloat(value)
  if (isNaN(n)) return value
  return n.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' TMT'
}

export function maskNin(nin: string): string {
  if (nin.length < 5) return nin
  return nin.substring(0, 3) + '•'.repeat(nin.length - 5) + nin.substring(nin.length - 2)
}

export function formatTestType(type: string): string {
  return type.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase())
}
