import React from 'react'
import { ScrollView, StyleSheet } from 'react-native'
import { useQuery } from '@tanstack/react-query'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { getTaxpayer, getFilings } from '../api/tax'
import { t } from '../i18n'
import { LoadingView } from '../components/LoadingView'
import { SectionCard } from '../components/SectionCard'
import { InfoRow } from '../components/InfoRow'
import { StatusBadge } from '../components/StatusBadge'
import { EmptyState } from '../components/EmptyState'
import { Colors, Spacing } from '../constants/theme'
import { formatDate } from '../utils/formatters'

export function TaxScreen() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()

  const { data: taxpayer, isLoading } = useQuery({
    queryKey: ['taxpayer', nationalId],
    queryFn: () => getTaxpayer(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: filings } = useQuery({
    queryKey: ['filings', taxpayer?.taxId],
    queryFn: () => getFilings(taxpayer!.taxId),
    enabled: !!taxpayer?.taxId,
    retry: false,
  })

  if (isLoading) return <LoadingView message={t(lang, 'loading')} />
  if (!taxpayer) return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      <EmptyState message={t(lang, 'noTaxRecord')} />
    </ScrollView>
  )

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      <SectionCard title={t(lang, 'taxTitle')}>
        <InfoRow label={t(lang, 'taxId')} value={taxpayer.taxId} />
        <InfoRow label={t(lang, 'taxpayerType')} value={taxpayer.taxpayerType} />
        <InfoRow label={t(lang, 'taxpayerStatus')} value={<StatusBadge label={t(lang, taxpayer.status === 'ACTIVE' ? 'active' : taxpayer.status === 'SUSPENDED' ? 'suspended' : 'inactive')} status={taxpayer.status} />} />
        <InfoRow label={t(lang, 'complianceStatus')} value={<StatusBadge label={t(lang, taxpayer.complianceStatus === 'COMPLIANT' ? 'compliant' : taxpayer.complianceStatus === 'NON_COMPLIANT' ? 'nonCompliant' : 'pending')} status={taxpayer.complianceStatus} />} />
        <InfoRow label={t(lang, 'registrationDate')} value={formatDate(taxpayer.registrationDate, lang)} last />
      </SectionCard>

      <SectionCard title={t(lang, 'filingHistory')}>
        {!filings || filings.length === 0
          ? <EmptyState message={t(lang, 'noData')} />
          : filings.map((f, i) => (
            <InfoRow key={f.id} label={String(f.taxYear)} value={<StatusBadge label={t(lang, f.status.toLowerCase() as any)} status={f.status} />} last={i === filings.length - 1} />
          ))
        }
      </SectionCard>
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 32 },
})
