import type { Lang } from './i18n'

const localeMap: Record<Lang, string> = {
  en: 'en-GB',
  tk: 'tk-TM',
  ru: 'ru-RU',
}

export function formatDate(value: string | null | undefined, lang: Lang = 'en'): string {
  if (!value) return '—'
  try {
    return new Intl.DateTimeFormat(localeMap[lang], {
      day: '2-digit',
      month: 'long',
      year: 'numeric',
    }).format(new Date(value))
  } catch {
    return value
  }
}

export function formatDateTime(value: string | null | undefined, lang: Lang = 'en'): string {
  if (!value) return '—'
  try {
    return new Intl.DateTimeFormat(localeMap[lang], {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(new Date(value))
  } catch {
    return value
  }
}

export function isExpired(value: string | null | undefined): boolean {
  if (!value) return false
  return new Date(value) < new Date()
}
