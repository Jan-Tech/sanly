import React from 'react'
import { View, Text, StyleSheet } from 'react-native'
import { Colors, Spacing } from '../constants/theme'

interface Props {
  label: string
  value: React.ReactNode
  last?: boolean
}

export function InfoRow({ label, value, last }: Props) {
  return (
    <View style={[styles.row, !last && styles.border]}>
      <Text style={styles.label}>{label}</Text>
      {typeof value === 'string' || typeof value === 'number'
        ? <Text style={styles.value}>{value}</Text>
        : value}
    </View>
  )
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: Spacing.sm + 2,
    paddingHorizontal: Spacing.lg,
  },
  border: { borderBottomWidth: StyleSheet.hairlineWidth, borderBottomColor: Colors.border },
  label: { fontSize: 13, color: Colors.textSecondary, flex: 1 },
  value: { fontSize: 13, fontWeight: '500', color: Colors.dark, flex: 2, textAlign: 'right' },
})
