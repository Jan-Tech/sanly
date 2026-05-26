import React from 'react'
import { ScrollView, View, Text, StyleSheet } from 'react-native'
import { useQuery } from '@tanstack/react-query'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { getMedicalRecords } from '../api/medical'
import { t } from '../i18n'
import { LoadingView } from '../components/LoadingView'
import { SectionCard } from '../components/SectionCard'
import { InfoRow } from '../components/InfoRow'
import { StatusBadge } from '../components/StatusBadge'
import { EmptyState } from '../components/EmptyState'
import { Colors, Radius, Spacing } from '../constants/theme'
import { formatDate, formatTestType } from '../utils/formatters'

export function MedicalScreen() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()
  const { data, isLoading, isError } = useQuery({
    queryKey: ['medical', nationalId],
    queryFn: () => getMedicalRecords(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  if (isLoading) return <LoadingView message={t(lang, 'loading')} />

  const records = data ?? []

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      {isError && (
        <View style={styles.unavailable}>
          <Text style={styles.unavailableText}>{t(lang, 'serviceUnavailableMsg')}</Text>
        </View>
      )}
      {!isError && records.length === 0 && <EmptyState message={t(lang, 'noMedicalRecords')} />}
      {records.map((r, i) => (
        <SectionCard key={r.id} title={formatTestType(r.testType)}>
          <InfoRow label={t(lang, 'result')} value={<StatusBadge label={t(lang, r.result === 'PASS' ? 'pass' : r.result === 'FAIL' ? 'fail' : 'pending')} status={r.result} />} />
          <InfoRow label={t(lang, 'testDate')} value={formatDate(r.testDate, lang)} />
          <InfoRow label={t(lang, 'expiryDate')} value={formatDate(r.expiryDate, lang)} last />
        </SectionCard>
      ))}
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 32 },
  unavailable: { backgroundColor: Colors.warningBg, borderRadius: Radius.md, padding: Spacing.md, marginBottom: Spacing.md },
  unavailableText: { color: '#B45309', fontSize: 13 },
})
