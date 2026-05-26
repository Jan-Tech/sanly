import React from 'react'
import { View, Text, StyleSheet } from 'react-native'
import { Colors, Radius } from '../constants/theme'

type Variant = 'success' | 'warning' | 'error' | 'info' | 'neutral' | 'primary'

const variantStyles: Record<Variant, { bg: string; text: string }> = {
  success: { bg: Colors.successBg, text: Colors.success },
  warning: { bg: Colors.warningBg, text: Colors.warning },
  error:   { bg: Colors.errorBg,   text: Colors.error },
  info:    { bg: Colors.infoBg,    text: Colors.info },
  neutral: { bg: Colors.borderLight, text: Colors.textSecondary },
  primary: { bg: Colors.primaryBg, text: Colors.primary },
}

function variantForStatus(status: string): Variant {
  const s = status.toUpperCase()
  if (['VALID', 'ACTIVE', 'CLEARED', 'PAID', 'APPROVED', 'GRADUATED', 'PLACED', 'ACCEPTED', 'COMPLETED', 'ON_DUTY'].includes(s)) return 'success'
  if (['PENDING', 'SUBMITTED', 'SCHEDULED', 'UNDER_TRANSFER'].includes(s)) return 'warning'
  if (['EXPIRED', 'REVOKED', 'SUSPENDED', 'REJECTED', 'OVERDUE', 'OUTSTANDING', 'DECEASED', 'NO_SHOW', 'HELD'].includes(s)) return 'error'
  if (['REGISTERED', 'CONFIRMED', 'INSPECTED'].includes(s)) return 'info'
  if (['OFF_DUTY', 'INACTIVE', 'DEREGISTERED', 'DISSOLVED'].includes(s)) return 'neutral'
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
  const colors = variantStyles[v]
  return (
    <View style={[styles.badge, { backgroundColor: colors.bg }, size === 'sm' && styles.sm]}>
      <Text style={[styles.text, { color: colors.text }, size === 'sm' && styles.textSm]}>
        {label}
      </Text>
    </View>
  )
}

const styles = StyleSheet.create({
  badge: { borderRadius: Radius.full, paddingHorizontal: 10, paddingVertical: 4, alignSelf: 'flex-start' },
  text: { fontSize: 12, fontWeight: '700', textTransform: 'uppercase', letterSpacing: 0.4 },
  sm: { paddingHorizontal: 6, paddingVertical: 2 },
  textSm: { fontSize: 10 },
})
