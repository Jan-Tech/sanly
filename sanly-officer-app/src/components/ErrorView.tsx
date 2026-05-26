import React from 'react'
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native'
import { AlertCircle } from 'lucide-react-native'
import { Colors, Spacing, Radius } from '../constants/theme'

interface Props {
  message?: string
  onRetry?: () => void
}

export function ErrorView({ message = 'An error occurred', onRetry }: Props) {
  return (
    <View style={styles.container}>
      <AlertCircle size={32} color={Colors.error} />
      <Text style={styles.message}>{message}</Text>
      {onRetry && (
        <TouchableOpacity style={styles.btn} onPress={onRetry}>
          <Text style={styles.btnText}>Retry</Text>
        </TouchableOpacity>
      )}
    </View>
  )
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: Spacing.xl, backgroundColor: Colors.background },
  message: { marginTop: Spacing.md, fontSize: 14, color: Colors.textSecondary, textAlign: 'center' },
  btn: { marginTop: Spacing.lg, backgroundColor: Colors.primary, borderRadius: Radius.md, paddingHorizontal: Spacing.xl, paddingVertical: Spacing.sm },
  btnText: { color: '#fff', fontWeight: '600', fontSize: 14 },
})
