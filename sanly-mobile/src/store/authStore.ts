import { create } from 'zustand'
import * as SecureStore from 'expo-secure-store'

const SECURE_KEY = 'sanly_auth_token'
const NATIONAL_ID_KEY = 'sanly_national_id'
const SESSION_ID_KEY = 'sanly_session_id'

interface AuthState {
  token: string | null
  nationalId: string | null
  roles: string[]
  sessionId: string | null
  hydrated: boolean
  // OTP step
  otpStep: boolean
  sessionToken: string | null
  phoneMasked: string | null

  hydrate: () => Promise<void>
  setAuth: (token: string, nationalId: string, roles: string[], sessionId?: string | null) => Promise<void>
  setOtpChallenge: (sessionToken: string, phoneMasked: string) => void
  clearOtpChallenge: () => void
  clearAuth: () => Promise<void>
}

export const useAuthStore = create<AuthState>()((set) => ({
  token: null,
  nationalId: null,
  roles: [],
  sessionId: null,
  hydrated: false,
  otpStep: false,
  sessionToken: null,
  phoneMasked: null,

  hydrate: async () => {
    try {
      const [token, nationalId, sessionId] = await Promise.all([
        SecureStore.getItemAsync(SECURE_KEY),
        SecureStore.getItemAsync(NATIONAL_ID_KEY),
        SecureStore.getItemAsync(SESSION_ID_KEY),
      ])
      set({ token, nationalId, sessionId, hydrated: true })
    } catch {
      set({ hydrated: true })
    }
  },

  setAuth: async (token, nationalId, roles, sessionId = null) => {
    await Promise.all([
      SecureStore.setItemAsync(SECURE_KEY, token),
      SecureStore.setItemAsync(NATIONAL_ID_KEY, nationalId),
      sessionId ? SecureStore.setItemAsync(SESSION_ID_KEY, sessionId) : SecureStore.deleteItemAsync(SESSION_ID_KEY),
    ])
    set({ token, nationalId, roles, sessionId, otpStep: false, sessionToken: null, phoneMasked: null })
  },

  setOtpChallenge: (sessionToken, phoneMasked) =>
    set({ otpStep: true, sessionToken, phoneMasked }),

  clearOtpChallenge: () =>
    set({ otpStep: false, sessionToken: null, phoneMasked: null }),

  clearAuth: async () => {
    await Promise.all([
      SecureStore.deleteItemAsync(SECURE_KEY),
      SecureStore.deleteItemAsync(NATIONAL_ID_KEY),
      SecureStore.deleteItemAsync(SESSION_ID_KEY),
    ])
    set({ token: null, nationalId: null, roles: [], sessionId: null, otpStep: false, sessionToken: null, phoneMasked: null })
  },
}))
