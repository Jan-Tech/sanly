import React from 'react'
import { ScrollView, StyleSheet } from 'react-native'
import { useQuery } from '@tanstack/react-query'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { getCitizen } from '../api/registry'
import { t } from '../i18n'
import { LoadingView } from '../components/LoadingView'
import { ErrorView } from '../components/ErrorView'
import { SectionCard } from '../components/SectionCard'
import { InfoRow } from '../components/InfoRow'
import { StatusBadge } from '../components/StatusBadge'
import { Colors, Spacing } from '../constants/theme'
import { formatDate } from '../utils/formatters'

export function IdentityScreen() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()
  const { data: citizen, isLoading, isError, refetch } = useQuery({
    queryKey: ['citizen', nationalId],
    queryFn: () => getCitizen(nationalId!),
    enabled: !!nationalId,
    staleTime: 5 * 60_000,
  })

  if (isLoading) return <LoadingView message={t(lang, 'loading')} />
  if (isError || !citizen) return <ErrorView message={t(lang, 'serviceUnavailableMsg')} onRetry={refetch} retryLabel={t(lang, 'retry')} />

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      <SectionCard title={t(lang, 'identityTitle')}>
        <InfoRow label={t(lang, 'nin')} value={citizen.nationalId} />
        <InfoRow label={t(lang, 'fullName')} value={`${citizen.firstName} ${citizen.lastName}`} />
        <InfoRow label={t(lang, 'dateOfBirth')} value={formatDate(citizen.dateOfBirth, lang)} />
        <InfoRow label={t(lang, 'gender')} value={t(lang, citizen.gender === 'MALE' ? 'male' : 'female')} />
        <InfoRow label={t(lang, 'placeOfBirth')} value={citizen.placeOfBirth} />
        <InfoRow label={t(lang, 'address')} value={citizen.street} />
        <InfoRow label={t(lang, 'status')} value={
          <StatusBadge label={t(lang, citizen.status === 'ACTIVE' ? 'active' : citizen.status === 'DECEASED' ? 'deceased' : 'inactive')} status={citizen.status} />
        } last />
      </SectionCard>

      {(citizen.fatherNin || citizen.motherNin) && (
        <SectionCard title={t(lang, 'familyLinks')}>
          {citizen.fatherNin && <InfoRow label={t(lang, 'father')} value={citizen.fatherNin} />}
          {citizen.motherNin && <InfoRow label={t(lang, 'mother')} value={citizen.motherNin} last />}
        </SectionCard>
      )}
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 32 },
})
