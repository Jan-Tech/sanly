import React from 'react'
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native'
import { AlertCircle } from 'lucide-react-native'
import { Colors, Radius, Spacing } from '../constants/theme'

interface Props {
  message?: string
  onRetry?: () => void
  retryLabel?: string
}

export function ErrorView({ message = 'Something went wrong', onRetry, retryLabel = 'Retry' }: Props) {
  return (
    <View style={styles.container}>
      <View style={styles.iconWrap}>
        <AlertCircle size={28} color={Colors.error} />
      </View>
      <Text style={styles.message}>{message}</Text>
      {onRetry && (
        <TouchableOpacity style={styles.btn} onPress={onRetry} activeOpacity={0.7}>
          <Text style={styles.btnText}>{retryLabel}</Text>
        </TouchableOpacity>
      )}
    </View>
  )
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: Spacing.xl, backgroundColor: Colors.background },
  iconWrap: { width: 56, height: 56, borderRadius: 28, backgroundColor: Colors.errorBg, justifyContent: 'center', alignItems: 'center', marginBottom: Spacing.md },
  message: { fontSize: 14, color: Colors.textSecondary, textAlign: 'center', lineHeight: 20, marginBottom: Spacing.lg },
  btn: { backgroundColor: Colors.primary, paddingHorizontal: Spacing.xl, paddingVertical: Spacing.md, borderRadius: Radius.md },
  btnText: { color: '#fff', fontWeight: '600', fontSize: 14 },
})
