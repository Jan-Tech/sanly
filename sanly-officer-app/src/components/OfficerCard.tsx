import React from 'react'
import { View, Text, StyleSheet } from 'react-native'
import { Shield } from 'lucide-react-native'
import { Colors, Radius, Spacing, Shadow, roleColors } from '../constants/theme'
import type { Officer } from '../types'

interface Props {
  officer: Officer
  compact?: boolean
}

export function OfficerCard({ officer, compact }: Props) {
  const rc = roleColors[officer.role] ?? roleColors['ROLE_ADMIN']
  const initials = officer.officerName
    .split(' ')
    .slice(0, 2)
    .map(w => w[0])
    .join('')
    .toUpperCase()

  if (compact) {
    return (
      <View style={styles.compact}>
        <View style={[styles.avatarSm, { backgroundColor: rc.bg }]}>
          <Text style={[styles.avatarTextSm, { color: rc.text }]}>{initials}</Text>
        </View>
        <View>
          <Text style={styles.nameSm}>{officer.officerName}</Text>
          <Text style={styles.roleSm}>{officer.institution}</Text>
        </View>
      </View>
    )
  }

  return (
    <View style={styles.card}>
      <View style={[styles.avatar, { backgroundColor: rc.bg }]}>
        <Text style={[styles.avatarText, { color: rc.text }]}>{initials}</Text>
      </View>
      <View style={styles.info}>
        <Text style={styles.name}>{officer.officerName}</Text>
        {officer.badgeNumber && (
          <View style={styles.badgeRow}>
            <Shield size={12} color={Colors.textMuted} />
            <Text style={styles.badge}>#{officer.badgeNumber}</Text>
          </View>
        )}
        <View style={[styles.roleChip, { backgroundColor: rc.bg }]}>
          <Text style={[styles.roleText, { color: rc.text }]}>
            {officer.role.replace('ROLE_', '')}
          </Text>
        </View>
      </View>
    </View>
  )
}

const styles = StyleSheet.create({
  card: { flexDirection: 'row', alignItems: 'center', gap: Spacing.md },
  avatar: { width: 52, height: 52, borderRadius: 26, justifyContent: 'center', alignItems: 'center' },
  avatarText: { fontSize: 20, fontWeight: '700' },
  info: { flex: 1 },
  name: { fontSize: 17, fontWeight: '700', color: '#fff' },
  badgeRow: { flexDirection: 'row', alignItems: 'center', gap: 4, marginTop: 2 },
  badge: { fontSize: 12, color: 'rgba(255,255,255,0.7)' },
  roleChip: { alignSelf: 'flex-start', borderRadius: Radius.full, paddingHorizontal: 8, paddingVertical: 2, marginTop: 6 },
  roleText: { fontSize: 11, fontWeight: '700', letterSpacing: 0.4 },
  // Compact variant
  compact: { flexDirection: 'row', alignItems: 'center', gap: Spacing.sm },
  avatarSm: { width: 36, height: 36, borderRadius: 18, justifyContent: 'center', alignItems: 'center' },
  avatarTextSm: { fontSize: 14, fontWeight: '700' },
  nameSm: { fontSize: 14, fontWeight: '600', color: Colors.dark },
  roleSm: { fontSize: 11, color: Colors.textMuted, marginTop: 1 },
})
