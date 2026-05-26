import React from 'react'
import { View, Text, StyleSheet } from 'react-native'
import { Colors, Radius } from '../constants/theme'

type Variant = 'success' | 'warning' | 'error' | 'info' | 'neutral' | 'purple'

const variantColors: Record<Variant, { bg: string; text: string }> = {
  success: { bg: Colors.successBg, text: Colors.success },
  warning: { bg: Colors.warningBg, text: '#B45309' },
  error: { bg: Colors.errorBg, text: Colors.error },
  info: { bg: Colors.infoBg, text: Colors.info },
  neutral: { bg: Colors.borderLight, text: Colors.textSecondary },
  purple: { bg: Colors.purpleBg, text: Colors.purple },
}

function variantForStatus(status: string): Variant {
  const s = status?.toUpperCase()
  if (['ACTIVE', 'VALID', 'PASS', 'COMPLIANT', 'REGISTERED', 'APPROVED', 'PAID', 'SUCCESS', 'GRADUATED', 'EMPLOYED', 'ELIGIBLE', 'PAYING', 'CLEARED'].includes(s)) return 'success'
  if (['SUSPENDED', 'NON_COMPLIANT', 'FAIL', 'HELD', 'UNDER_REVIEW', 'PENDING_VERIFICATION'].includes(s)) return 'warning'
  if (['REVOKED', 'REJECTED', 'FAILED', 'DECEASED', 'DENIED', 'OVERDUE', 'OUTSTANDING', 'EXPIRED', 'CANCELLED', 'SCRAPPED'].includes(s)) return 'error'
  if (['PENDING', 'SUBMITTED', 'UNDER_TRANSFER', 'UNDER_INSPECTION', 'DRAFT'].includes(s)) return 'info'
  if (['ACCUMULATING', 'ENROLLED', 'ACTIVE_POLICY'].includes(s)) return 'purple'
  return 'neutral'
}

interface Props {
  label: string
  status?: string
  variant?: Variant
  size?: 'sm' | 'md'
}

export function StatusBadge({ label, status, variant, size = 'md' }: Props) {
  const v = variant ?? (status ? variantForStatus(status) : 'neutral')
  const colors = variantColors[v]
  return (
    <View style={[styles.badge, { backgroundColor: colors.bg }, size === 'sm' && styles.sm]}>
      <Text style={[styles.text, { color: colors.text }, size === 'sm' && styles.smText]}>{label}</Text>
    </View>
  )
}

const styles = StyleSheet.create({
  badge: { paddingHorizontal: 8, paddingVertical: 3, borderRadius: Radius.full, alignSelf: 'flex-start' },
  sm: { paddingHorizontal: 6, paddingVertical: 2 },
  text: { fontSize: 12, fontWeight: '600' },
  smText: { fontSize: 10 },
})
