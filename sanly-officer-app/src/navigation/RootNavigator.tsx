import React, { useEffect } from 'react'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { useOfflineQueueStore } from '../store/offlineQueueStore'
import { AuthStack } from './AuthStack'
import { MainTabs } from './MainTabs'

export function RootNavigator() {
  const { token, hydrated, hydrate: hydrateAuth } = useAuthStore()
  const { hydrate: hydrateLang } = useLangStore()
  const { load: loadQueue } = useOfflineQueueStore()

  useEffect(() => {
    hydrateAuth()
    hydrateLang()
    loadQueue()
  }, [])

  if (!hydrated) return null

  return token ? <MainTabs /> : <AuthStack />
}
