import React from 'react'
import { View, Text, StyleSheet } from 'react-native'
import { Colors, Radius, Spacing, Shadow } from '../constants/theme'

interface Props {
  title?: string
  children: React.ReactNode
}

export function SectionCard({ title, children }: Props) {
  return (
    <View style={styles.wrapper}>
      {title && <Text style={styles.title}>{title}</Text>}
      <View style={styles.card}>{children}</View>
    </View>
  )
}

const styles = StyleSheet.create({
  wrapper: { marginBottom: Spacing.lg },
  title: {
    fontSize: 11,
    fontWeight: '700',
    color: Colors.textSecondary,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
    marginBottom: Spacing.sm,
    paddingLeft: 4,
  },
  card: {
    backgroundColor: Colors.card,
    borderRadius: Radius.lg,
    overflow: 'hidden',
    ...Shadow.sm,
  },
})
