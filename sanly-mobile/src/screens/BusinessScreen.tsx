import React from 'react'
import { ScrollView, StyleSheet } from 'react-native'
import { useQuery } from '@tanstack/react-query'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { getBusinesses } from '../api/business'
import { t } from '../i18n'
import { LoadingView } from '../components/LoadingView'
import { SectionCard } from '../components/SectionCard'
import { InfoRow } from '../components/InfoRow'
import { StatusBadge } from '../components/StatusBadge'
import { EmptyState } from '../components/EmptyState'
import { Colors, Spacing } from '../constants/theme'
import { formatDate } from '../utils/formatters'

export function BusinessScreen() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()
  const { data, isLoading } = useQuery({
    queryKey: ['businesses', nationalId],
    queryFn: () => getBusinesses(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  if (isLoading) return <LoadingView message={t(lang, 'loading')} />
  const businesses = data ?? []

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      {businesses.length === 0 && <EmptyState message={t(lang, 'noBusinesses')} />}
      {businesses.map(biz => (
        <SectionCard key={biz.id} title={biz.businessName}>
          <InfoRow label={t(lang, 'registrationNumber')} value={biz.registrationNumber} />
          <InfoRow label={t(lang, 'businessType')} value={biz.businessType.replace(/_/g, ' ')} />
          <InfoRow label={t(lang, 'businessStatus')} value={<StatusBadge label={t(lang, biz.status.toLowerCase() as any)} status={biz.status} />} />
          <InfoRow label={t(lang, 'registrationDate')} value={formatDate(biz.registrationDate, lang)} />
          <InfoRow label={t(lang, 'expiryDate')} value={formatDate(biz.expiryDate, lang)} last />
        </SectionCard>
      ))}
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 32 },
})
