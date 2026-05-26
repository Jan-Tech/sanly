import React from 'react'
import { View, Text, StyleSheet } from 'react-native'
import { InboxIcon } from 'lucide-react-native'
import { Colors, Spacing } from '../constants/theme'

interface Props { message: string; icon?: React.ReactNode }

export function EmptyState({ message, icon }: Props) {
  return (
    <View style={styles.container}>
      <View style={styles.iconWrap}>
        {icon ?? <InboxIcon size={24} color={Colors.textMuted} />}
      </View>
      <Text style={styles.text}>{message}</Text>
    </View>
  )
}

const styles = StyleSheet.create({
  container: { alignItems: 'center', paddingVertical: Spacing.xxxl },
  iconWrap: { marginBottom: Spacing.md },
  text: { fontSize: 14, color: Colors.textMuted, textAlign: 'center' },
})
