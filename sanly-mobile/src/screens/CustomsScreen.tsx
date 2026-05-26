import React from 'react'
import { ScrollView, StyleSheet } from 'react-native'
import { useQuery } from '@tanstack/react-query'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { getDeclarationsByDeclarant } from '../api/customs'
import { t } from '../i18n'
import { LoadingView } from '../components/LoadingView'
import { SectionCard } from '../components/SectionCard'
import { InfoRow } from '../components/InfoRow'
import { StatusBadge } from '../components/StatusBadge'
import { EmptyState } from '../components/EmptyState'
import { Colors, Spacing } from '../constants/theme'
import { formatDate, formatMoney } from '../utils/formatters'

export function CustomsScreen() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()
  const { data, isLoading } = useQuery({
    queryKey: ['customs', nationalId],
    queryFn: () => getDeclarationsByDeclarant(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  if (isLoading) return <LoadingView message={t(lang, 'loading')} />
  const declarations = data ?? []

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      {declarations.length === 0 && <EmptyState message={t(lang, 'noDeclarations')} />}
      {declarations.map(d => (
        <SectionCard key={d.declarationId} title={d.declarationCode}>
          <InfoRow label="Type" value={d.declarationType.replace(/_/g, ' ')} />
          <InfoRow label={t(lang, 'status')} value={<StatusBadge label={d.status} status={d.status} />} />
          <InfoRow label="Port" value={d.portCode} />
          {d.dutiesOwed && <InfoRow label="Duties Owed" value={formatMoney(d.dutiesOwed)} />}
          {d.dutiesPaid && <InfoRow label="Duties Paid" value={formatMoney(d.dutiesPaid)} />}
          <InfoRow label={t(lang, 'timestamp')} value={formatDate(d.submittedAt, lang)} last />
        </SectionCard>
      ))}
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 32 },
})
