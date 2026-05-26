import React from 'react'
import { View, ActivityIndicator, Text, StyleSheet } from 'react-native'
import { Colors } from '../constants/theme'

interface Props { message?: string }

export function LoadingView({ message }: Props) {
  return (
    <View style={styles.container}>
      <ActivityIndicator size="large" color={Colors.primary} />
      {message && <Text style={styles.text}>{message}</Text>}
    </View>
  )
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: Colors.background, gap: 12 },
  text: { fontSize: 14, color: Colors.textSecondary, marginTop: 8 },
})
