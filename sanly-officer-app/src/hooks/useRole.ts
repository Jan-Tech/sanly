import { useAuthStore } from '../store/authStore'
import type { OfficerRole } from '../types'
import { roleColors } from '../constants/theme'

const roleLabels: Record<OfficerRole, string> = {
  ROLE_POLICE:    'Police Officer',
  ROLE_DMV:       'DMV Officer',
  ROLE_MEDICAL:   'Medical Officer',
  ROLE_CUSTOMS:   'Customs Officer',
  ROLE_CIVIL:     'Civil Registry Officer',
  ROLE_COURT:     'Court Officer',
  ROLE_EDUCATION: 'Education Officer',
  ROLE_LAND:      'Land Registry Officer',
  ROLE_TAX:       'Tax Officer',
  ROLE_SOCIAL:    'Social Services Officer',
  ROLE_ADMIN:     'Administrator',
}

export function useRole() {
  const officer = useAuthStore(s => s.officer)
  const role = officer?.role ?? 'ROLE_ADMIN'

  return {
    role,
    hasRole: (r: OfficerRole) => role === r,
    isPolice:    role === 'ROLE_POLICE',
    isDmv:       role === 'ROLE_DMV',
    isMedical:   role === 'ROLE_MEDICAL',
    isCustoms:   role === 'ROLE_CUSTOMS',
    isCivil:     role === 'ROLE_CIVIL',
    isCourt:     role === 'ROLE_COURT',
    isEducation: role === 'ROLE_EDUCATION',
    isLand:      role === 'ROLE_LAND',
    isTax:       role === 'ROLE_TAX',
    isSocial:    role === 'ROLE_SOCIAL',
    isAdmin:     role === 'ROLE_ADMIN',
    label:       roleLabels[role],
    colors:      roleColors[role] ?? roleColors['ROLE_ADMIN'],
  }
}
