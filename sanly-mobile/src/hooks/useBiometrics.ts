import { useEffect, useState, useCallback } from 'react'
import * as LocalAuthentication from 'expo-local-authentication'
import * as SecureStore from 'expo-secure-store'
import { useAuthStore } from '../store/authStore'

const BIOMETRICS_ENABLED_KEY = 'sanly_biometrics_enabled'

export function useBiometrics() {
  const [available, setAvailable] = useState(false)
  const [enrolled, setEnrolled] = useState(false)
  const [enabled, setEnabled] = useState(false)
  const { token, setAuth, nationalId, roles } = useAuthStore()

  useEffect(() => {
    ;(async () => {
      const hasHardware = await LocalAuthentication.hasHardwareAsync()
      const isEnrolled = await LocalAuthentication.isEnrolledAsync()
      const storedEnabled = await SecureStore.getItemAsync(BIOMETRICS_ENABLED_KEY)
      setAvailable(hasHardware)
      setEnrolled(isEnrolled)
      setEnabled(storedEnabled === 'true')
    })()
  }, [])

  const authenticate = useCallback(async (): Promise<boolean> => {
    if (!available || !enrolled) return false
    const result = await LocalAuthentication.authenticateAsync({
      promptMessage: 'Authenticate to access Sanly',
      fallbackLabel: 'Use Password',
      cancelLabel: 'Cancel',
    })
    return result.success
  }, [available, enrolled])

  const toggleBiometrics = useCallback(async (value: boolean) => {
    await SecureStore.setItemAsync(BIOMETRICS_ENABLED_KEY, value ? 'true' : 'false')
    setEnabled(value)
  }, [])

  const canUseBiometrics = available && enrolled && enabled && !!token

  return { available, enrolled, enabled, canUseBiometrics, authenticate, toggleBiometrics }
}
