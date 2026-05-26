import { create } from 'zustand'
import * as SecureStore from 'expo-secure-store'
import { decodeJwt } from '../utils/formatters'
import type { OfficerRole, Institution, Officer } from '../types'

const KEYS = {
  token: 'officer_token',
  officerId: 'officer_id',
  role: 'officer_role',
  name: 'officer_name',
  institution: 'officer_institution',
  badge: 'officer_badge',
}

interface AuthState {
  token: string | null
  officer: Officer | null
  hydrated: boolean
  hydrate: () => Promise<void>
  setAuth: (token: string) => Promise<void>
  clearAuth: () => Promise<void>
}

function extractOfficerFromJwt(token: string): Partial<Officer> {
  const payload = decodeJwt(token)
  const roles = (payload.roles as string[]) ?? []
  const officerRole = roles.find(r =>
    ['ROLE_POLICE', 'ROLE_DMV', 'ROLE_MEDICAL', 'ROLE_CUSTOMS', 'ROLE_CIVIL',
      'ROLE_COURT', 'ROLE_EDUCATION', 'ROLE_LAND', 'ROLE_TAX', 'ROLE_SOCIAL', 'ROLE_ADMIN'].includes(r)
  ) as OfficerRole | undefined

  const roleToInstitution: Record<OfficerRole, Institution> = {
    ROLE_POLICE: 'POLICE',
    ROLE_DMV: 'DMV',
    ROLE_MEDICAL: 'MEDICAL',
    ROLE_CUSTOMS: 'CUSTOMS',
    ROLE_CIVIL: 'CIVIL',
    ROLE_COURT: 'COURT',
    ROLE_EDUCATION: 'EDUCATION',
    ROLE_LAND: 'LAND',
    ROLE_TAX: 'TAX',
    ROLE_SOCIAL: 'SOCIAL',
    ROLE_ADMIN: 'ADMIN',
  }

  return {
    officerId: payload.sub as string ?? '',
    officerName: (payload.name as string) ?? (payload.sub as string) ?? 'Officer',
    role: officerRole ?? 'ROLE_ADMIN',
    institution: officerRole ? roleToInstitution[officerRole] : 'ADMIN',
    badgeNumber: payload.badgeNumber as string | undefined,
    region: payload.region as string | undefined,
  }
}

export const useAuthStore = create<AuthState>((set) => ({
  token: null,
  officer: null,
  hydrated: false,

  hydrate: async () => {
    try {
      const token = await SecureStore.getItemAsync(KEYS.token)
      if (token) {
        const partial = extractOfficerFromJwt(token)
        set({
          token,
          officer: partial as Officer,
          hydrated: true,
        })
      } else {
        set({ hydrated: true })
      }
    } catch {
      set({ hydrated: true })
    }
  },

  setAuth: async (token: string) => {
    const partial = extractOfficerFromJwt(token)
    await SecureStore.setItemAsync(KEYS.token, token)
    set({
      token,
      officer: partial as Officer,
    })
  },

  clearAuth: async () => {
    await Promise.all(Object.values(KEYS).map(k => SecureStore.deleteItemAsync(k)))
    set({ token: null, officer: null })
  },
}))
