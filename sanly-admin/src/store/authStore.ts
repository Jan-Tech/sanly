import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface AuthState {
  token: string | null
  username: string | null
  roles: string[]
  setAuth: (token: string, username: string, roles: string[]) => void
  clearAuth: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      username: null,
      roles: [],
      setAuth: (token, username, roles) => set({ token, username, roles }),
      clearAuth: () => set({ token: null, username: null, roles: [] }),
    }),
    { name: 'sanly-admin-auth' }
  )
)
