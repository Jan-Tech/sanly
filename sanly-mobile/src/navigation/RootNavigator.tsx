import React, { useEffect } from 'react'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { AuthStack } from './AuthStack'
import { MainTabs } from './MainTabs'

export function RootNavigator() {
  const { token, hydrated, hydrate: hydrateAuth } = useAuthStore()
  const { hydrate: hydrateLang } = useLangStore()

  useEffect(() => {
    hydrateAuth()
    hydrateLang()
  }, [])

  if (!hydrated) return null

  return token ? <MainTabs /> : <AuthStack />
}
