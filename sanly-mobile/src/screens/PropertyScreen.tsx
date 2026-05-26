import React from 'react'
import { ScrollView, StyleSheet } from 'react-native'
import { useQuery } from '@tanstack/react-query'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { getPropertiesByOwner } from '../api/land'
import { t } from '../i18n'
import { LoadingView } from '../components/LoadingView'
import { SectionCard } from '../components/SectionCard'
import { InfoRow } from '../components/InfoRow'
import { StatusBadge } from '../components/StatusBadge'
import { EmptyState } from '../components/EmptyState'
import { Colors, Spacing } from '../constants/theme'
import { formatDate } from '../utils/formatters'

export function PropertyScreen() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()
  const { data, isLoading } = useQuery({
    queryKey: ['properties', nationalId],
    queryFn: () => getPropertiesByOwner(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  if (isLoading) return <LoadingView message={t(lang, 'loading')} />
  const properties = data ?? []

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      {properties.length === 0 && <EmptyState message={t(lang, 'noProperties')} />}
      {properties.map(p => (
        <SectionCard key={p.propertyId} title={p.cadastralCode}>
          <InfoRow label="Type" value={p.propertyType.replace(/_/g, ' ')} />
          <InfoRow label="Area" value={`${p.area} m²`} />
          <InfoRow label={t(lang, 'address')} value={p.address} />
          <InfoRow label={t(lang, 'status')} value={<StatusBadge label={p.status} status={p.status} />} />
          <InfoRow label={t(lang, 'registrationDate')} value={formatDate(p.registeredAt, lang)} last />
        </SectionCard>
      ))}
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 32 },
})
