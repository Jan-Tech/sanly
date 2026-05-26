import { create } from 'zustand'
import * as SecureStore from 'expo-secure-store'
import type { Lang } from '../i18n'

interface LangState {
  lang: Lang
  hydrated: boolean
  hydrate: () => Promise<void>
  setLang: (lang: Lang) => Promise<void>
}

export const useLangStore = create<LangState>((set) => ({
  lang: 'en',
  hydrated: false,

  hydrate: async () => {
    try {
      const stored = await SecureStore.getItemAsync('officer_lang')
      set({ lang: (stored as Lang) ?? 'en', hydrated: true })
    } catch {
      set({ hydrated: true })
    }
  },

  setLang: async (lang) => {
    await SecureStore.setItemAsync('officer_lang', lang)
    set({ lang })
  },
}))
