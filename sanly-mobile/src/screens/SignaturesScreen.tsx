import React from 'react'
import { ScrollView, StyleSheet } from 'react-native'
import { useLangStore } from '../store/langStore'
import { t } from '../i18n'
import { EmptyState } from '../components/EmptyState'
import { FileSignature } from 'lucide-react-native'
import { Colors, Spacing } from '../constants/theme'

export function SignaturesScreen() {
  const { lang } = useLangStore()
  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      <EmptyState
        message={t(lang, 'noSignatures')}
        icon={<FileSignature size={24} color={Colors.textMuted} />}
      />
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { flex: 1, padding: Spacing.lg },
})
