import React from 'react'
import { TouchableOpacity, Text, View, StyleSheet } from 'react-native'
import { Colors, Radius, Spacing, Shadow } from '../constants/theme'

interface Props {
  icon: React.ReactNode
  label: string
  onPress: () => void
  color?: string
  disabled?: boolean
  wide?: boolean
}

export function ActionButton({ icon, label, onPress, color = Colors.primary, disabled, wide }: Props) {
  return (
    <TouchableOpacity
      style={[styles.btn, wide && styles.wide, disabled && styles.disabled]}
      onPress={onPress}
      activeOpacity={0.75}
      disabled={disabled}
    >
      <View style={[styles.iconWrap, { backgroundColor: color + '20' }]}>
        {icon}
      </View>
      <Text style={[styles.label, { color }]} numberOfLines={2}>{label}</Text>
    </TouchableOpacity>
  )
}

const styles = StyleSheet.create({
  btn: {
    backgroundColor: Colors.card,
    borderRadius: Radius.lg,
    padding: Spacing.md,
    alignItems: 'center',
    width: '22%',
    minWidth: 72,
    ...Shadow.sm,
  },
  wide: { width: '46%' },
  disabled: { opacity: 0.4 },
  iconWrap: {
    width: 44,
    height: 44,
    borderRadius: Radius.md,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: Spacing.sm,
  },
  label: { fontSize: 11, fontWeight: '600', textAlign: 'center' },
})
