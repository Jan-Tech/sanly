import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface AuthState {
  // Authenticated state
  token: string | null
  nationalId: string | null
  roles: string[]
  sessionId: string | null

  // OTP step state (not persisted — cleared on page reload)
  otpStep: boolean
  sessionToken: string | null
  phoneMasked: string | null

  setAuth: (token: string, nationalId: string, roles: string[], sessionId?: string | null) => void
  setOtpChallenge: (sessionToken: string, phoneMasked: string) => void
  clearOtpChallenge: () => void
  clearAuth: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      nationalId: null,
      roles: [],
      sessionId: null,
      otpStep: false,
      sessionToken: null,
      phoneMasked: null,

      setAuth: (token, nationalId, roles, sessionId = null) =>
        set({ token, nationalId, roles, sessionId, otpStep: false, sessionToken: null, phoneMasked: null }),

      setOtpChallenge: (sessionToken, phoneMasked) =>
        set({ otpStep: true, sessionToken, phoneMasked }),

      clearOtpChallenge: () =>
        set({ otpStep: false, sessionToken: null, phoneMasked: null }),

      clearAuth: () =>
        set({ token: null, nationalId: null, roles: [], sessionId: null,
              otpStep: false, sessionToken: null, phoneMasked: null }),
    }),
    {
      name: 'sanly-auth',
      // Don't persist OTP step — it resets on reload intentionally
      partialize: (state) => ({
        token: state.token,
        nationalId: state.nationalId,
        roles: state.roles,
        sessionId: state.sessionId,
      }),
    }
  )
)
