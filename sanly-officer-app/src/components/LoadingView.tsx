import React from 'react'
import { View, ActivityIndicator, Text, StyleSheet } from 'react-native'
import { Colors, Spacing } from '../constants/theme'

export function LoadingView({ message }: { message?: string }) {
  return (
    <View style={styles.container}>
      <ActivityIndicator size="large" color={Colors.primary} />
      {message && <Text style={styles.message}>{message}</Text>}
    </View>
  )
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: Colors.background },
  message: { marginTop: Spacing.md, fontSize: 14, color: Colors.textMuted },
})
