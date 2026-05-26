import React from 'react'
import { TouchableOpacity, View, Text, StyleSheet } from 'react-native'
import { Colors, Radius, Shadow, Spacing } from '../constants/theme'

interface Props {
  icon: React.ReactNode
  label: string
  badge?: number | string
  onPress: () => void
  color?: string
}

export function ServiceGridItem({ icon, label, badge, onPress, color = Colors.primaryBg }: Props) {
  return (
    <TouchableOpacity style={styles.item} onPress={onPress} activeOpacity={0.75}>
      <View style={[styles.iconWrap, { backgroundColor: color }]}>{icon}</View>
      {badge !== undefined && badge !== 0 && (
        <View style={styles.badge}>
          <Text style={styles.badgeText}>{badge}</Text>
        </View>
      )}
      <Text style={styles.label} numberOfLines={2}>{label}</Text>
    </TouchableOpacity>
  )
}

const styles = StyleSheet.create({
  item: {
    width: '22%',
    marginHorizontal: '1.5%',
    marginBottom: Spacing.lg,
    alignItems: 'center',
  },
  iconWrap: {
    width: 56,
    height: 56,
    borderRadius: Radius.lg,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: Spacing.xs + 2,
    ...Shadow.sm,
  },
  badge: {
    position: 'absolute',
    top: -4,
    right: 6,
    minWidth: 18,
    height: 18,
    borderRadius: 9,
    backgroundColor: Colors.error,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 4,
  },
  badgeText: { color: '#fff', fontSize: 10, fontWeight: '700' },
  label: { fontSize: 11, color: Colors.text, textAlign: 'center', lineHeight: 14 },
})
