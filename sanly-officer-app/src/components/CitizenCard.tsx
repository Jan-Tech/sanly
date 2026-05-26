import React from 'react'
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native'
import { ChevronRight, User } from 'lucide-react-native'
import { StatusBadge } from './StatusBadge'
import { Colors, Radius, Spacing, Shadow } from '../constants/theme'
import { maskNin, formatDate } from '../utils/formatters'
import type { Citizen } from '../types'

interface Props {
  citizen: Citizen
  onPress?: () => void
  showFull?: boolean
}

export function CitizenCard({ citizen, onPress, showFull }: Props) {
  const initials = `${citizen.firstName?.[0] ?? ''}${citizen.lastName?.[0] ?? ''}`.toUpperCase()

  return (
    <TouchableOpacity
      style={styles.card}
      onPress={onPress}
      activeOpacity={onPress ? 0.7 : 1}
      disabled={!onPress}
    >
      <View style={styles.avatar}>
        <Text style={styles.avatarText}>{initials || <User size={20} color={Colors.textMuted} />}</Text>
      </View>
      <View style={styles.info}>
        <View style={styles.nameRow}>
          <Text style={styles.name}>{citizen.firstName} {citizen.lastName}</Text>
          <StatusBadge label={citizen.status} status={citizen.status} size="sm" />
        </View>
        <Text style={styles.nin}>{showFull ? citizen.nationalId : maskNin(citizen.nationalId)}</Text>
        {showFull && (
          <>
            <Text style={styles.detail}>DOB: {formatDate(citizen.dateOfBirth)}</Text>
            <Text style={styles.detail}>Gender: {citizen.gender}</Text>
          </>
        )}
      </View>
      {onPress && <ChevronRight size={16} color={Colors.textMuted} />}
    </TouchableOpacity>
  )
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: Colors.card,
    borderRadius: Radius.lg,
    padding: Spacing.lg,
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.md,
    ...Shadow.sm,
  },
  avatar: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: Colors.primaryBg,
    justifyContent: 'center',
    alignItems: 'center',
  },
  avatarText: { fontSize: 16, fontWeight: '700', color: Colors.primary },
  info: { flex: 1 },
  nameRow: { flexDirection: 'row', alignItems: 'center', gap: Spacing.sm, flexWrap: 'wrap' },
  name: { fontSize: 15, fontWeight: '600', color: Colors.dark },
  nin: { fontSize: 12, color: Colors.textMuted, marginTop: 2, fontVariant: ['tabular-nums'] },
  detail: { fontSize: 12, color: Colors.textSecondary, marginTop: 2 },
})
