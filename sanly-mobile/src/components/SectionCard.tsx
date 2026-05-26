import React from 'react'
import { View, Text, StyleSheet, type ViewStyle } from 'react-native'
import { Colors, Radius, Shadow, Spacing } from '../constants/theme'

interface Props {
  title?: string
  children: React.ReactNode
  style?: ViewStyle
  noPad?: boolean
}

export function SectionCard({ title, children, style, noPad }: Props) {
  return (
    <View style={[styles.card, style]}>
      {title && <Text style={styles.title}>{title}</Text>}
      <View style={noPad ? undefined : styles.content}>{children}</View>
    </View>
  )
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: Colors.card,
    borderRadius: Radius.lg,
    marginBottom: Spacing.md,
    ...Shadow.md,
  },
  title: {
    fontSize: 13,
    fontWeight: '700',
    color: Colors.textSecondary,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
    paddingHorizontal: Spacing.lg,
    paddingTop: Spacing.lg,
    paddingBottom: Spacing.sm,
  },
  content: {
    paddingHorizontal: Spacing.lg,
    paddingBottom: Spacing.lg,
  },
})
