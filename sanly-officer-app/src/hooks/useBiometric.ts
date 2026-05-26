import { useState, useEffect } from 'react'
import * as LocalAuthentication from 'expo-local-authentication'
import * as SecureStore from 'expo-secure-store'

const BIOMETRICS_KEY = 'officer_biometrics_enabled'

export function useBiometrics() {
  const [available, setAvailable] = useState(false)
  const [enrolled, setEnrolled] = useState(false)
  const [enabled, setEnabled] = useState(false)

  useEffect(() => {
    async function check() {
      const [hw, en, stored] = await Promise.all([
        LocalAuthentication.hasHardwareAsync(),
        LocalAuthentication.isEnrolledAsync(),
        SecureStore.getItemAsync(BIOMETRICS_KEY),
      ])
      setAvailable(hw)
      setEnrolled(en)
      setEnabled(stored === 'true')
    }
    check()
  }, [])

  async function authenticate(prompt = 'Authenticate to continue'): Promise<boolean> {
    const result = await LocalAuthentication.authenticateAsync({
      promptMessage: prompt,
      fallbackLabel: 'Use Passcode',
      cancelLabel: 'Cancel',
      disableDeviceFallback: false,
    })
    return result.success
  }

  async function toggleBiometrics(value: boolean) {
    await SecureStore.setItemAsync(BIOMETRICS_KEY, String(value))
    setEnabled(value)
  }

  return {
    available,
    enrolled,
    enabled,
    canUseBiometrics: available && enrolled && enabled,
    authenticate,
    toggleBiometrics,
  }
}
