import { create } from 'zustand'
import * as SecureStore from 'expo-secure-store'
import type { Lang } from '../i18n'

const LANG_KEY = 'sanly_lang'

interface LangState {
  lang: Lang
  setLang: (lang: Lang) => Promise<void>
  hydrate: () => Promise<void>
}

export const useLangStore = create<LangState>()((set) => ({
  lang: 'en',

  hydrate: async () => {
    try {
      const stored = await SecureStore.getItemAsync(LANG_KEY)
      if (stored === 'en' || stored === 'tk' || stored === 'ru') {
        set({ lang: stored })
      }
    } catch { /* use default */ }
  },

  setLang: async (lang) => {
    await SecureStore.setItemAsync(LANG_KEY, lang)
    set({ lang })
  },
}))
