import { Platform, StyleSheet } from 'react-native'

export const Colors = {
  // Brand
  primary: '#0D7377',
  primaryLight: '#14A7AD',
  primaryDark: '#0A5C60',
  primaryBg: '#E6F5F6',

  // Navy (header, sidebar)
  navy: '#1A1A2E',
  navyLight: '#252542',
  navyDark: '#0F0F1F',
  navyMid: '#2D2D50',

  // Neutral
  dark: '#0F172A',
  background: '#F1F5F9',
  card: '#FFFFFF',
  border: '#E2E8F0',
  borderLight: '#F1F5F9',
  textPrimary: '#0F172A',
  textSecondary: '#475569',
  textMuted: '#94A3B8',

  // Status
  success: '#059669',
  successBg: '#D1FAE5',
  warning: '#D97706',
  warningBg: '#FEF3C7',
  error: '#DC2626',
  errorBg: '#FEE2E2',
  info: '#2563EB',
  infoBg: '#DBEAFE',

  // Role accent colors
  police: '#1E40AF',
  policeBg: '#DBEAFE',
  medical: '#059669',
  medicalBg: '#D1FAE5',
  customs: '#7C3AED',
  customsBg: '#EDE9FE',
  civil: '#0E7490',
  civilBg: '#CFFAFE',
  court: '#9F1239',
  courtBg: '#FFE4E6',
  education: '#B45309',
  educationBg: '#FEF3C7',
  land: '#065F46',
  landBg: '#D1FAE5',
  dmv: '#1D4ED8',
  dmvBg: '#DBEAFE',
  tax: '#92400E',
  taxBg: '#FEF3C7',
  social: '#4338CA',
  socialBg: '#E0E7FF',
}

export const Spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
  xxl: 24,
  xxxl: 32,
}

export const Radius = {
  sm: 6,
  md: 10,
  lg: 14,
  xl: 20,
  full: 9999,
}

export const Shadow = {
  sm: Platform.select({
    ios: { shadowColor: '#000', shadowOffset: { width: 0, height: 1 }, shadowOpacity: 0.06, shadowRadius: 3 },
    android: { elevation: 2 },
    default: {},
  }) as object,
  md: Platform.select({
    ios: { shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.08, shadowRadius: 6 },
    android: { elevation: 4 },
    default: {},
  }) as object,
  lg: Platform.select({
    ios: { shadowColor: '#000', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.12, shadowRadius: 12 },
    android: { elevation: 8 },
    default: {},
  }) as object,
}

export const roleColors: Record<string, { bg: string; text: string; badge: string }> = {
  ROLE_POLICE:    { bg: Colors.policeBg,    text: Colors.police,    badge: Colors.police },
  ROLE_DMV:       { bg: Colors.dmvBg,       text: Colors.dmv,       badge: Colors.dmv },
  ROLE_MEDICAL:   { bg: Colors.medicalBg,   text: Colors.medical,   badge: Colors.medical },
  ROLE_CUSTOMS:   { bg: Colors.customsBg,   text: Colors.customs,   badge: Colors.customs },
  ROLE_CIVIL:     { bg: Colors.civilBg,     text: Colors.civil,     badge: Colors.civil },
  ROLE_COURT:     { bg: Colors.courtBg,     text: Colors.court,     badge: Colors.court },
  ROLE_EDUCATION: { bg: Colors.educationBg, text: Colors.education, badge: Colors.education },
  ROLE_LAND:      { bg: Colors.landBg,      text: Colors.land,      badge: Colors.land },
  ROLE_TAX:       { bg: Colors.taxBg,       text: Colors.tax,       badge: Colors.tax },
  ROLE_SOCIAL:    { bg: Colors.socialBg,    text: Colors.social,    badge: Colors.social },
  ROLE_ADMIN:     { bg: Colors.primaryBg,   text: Colors.primary,   badge: Colors.primary },
}
