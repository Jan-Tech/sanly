import React from 'react'
import { ScrollView, StyleSheet } from 'react-native'
import { useQuery } from '@tanstack/react-query'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { getDiplomas, getEnrollments } from '../api/education'
import { t } from '../i18n'
import { LoadingView } from '../components/LoadingView'
import { SectionCard } from '../components/SectionCard'
import { InfoRow } from '../components/InfoRow'
import { StatusBadge } from '../components/StatusBadge'
import { EmptyState } from '../components/EmptyState'
import { Colors, Spacing } from '../constants/theme'
import { formatDate } from '../utils/formatters'

export function EducationScreen() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()
  const { data: diplomas, isLoading: dipLoading } = useQuery({
    queryKey: ['diplomas', nationalId],
    queryFn: () => getDiplomas(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })
  const { data: enrollments, isLoading: enrLoading } = useQuery({
    queryKey: ['enrollments', nationalId],
    queryFn: () => getEnrollments(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  if (dipLoading || enrLoading) return <LoadingView message={t(lang, 'loading')} />

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      <SectionCard title={t(lang, 'diplomas')}>
        {!diplomas || diplomas.length === 0
          ? <EmptyState message={t(lang, 'noDiplomas')} />
          : diplomas.map((d, i) => (
            <InfoRow key={d.diplomaId} label={d.programName}
              value={<StatusBadge label={t(lang, d.status === 'VALID' ? 'active' : 'revoked')} status={d.status} size="sm" />}
              last={i === diplomas.length - 1} />
          ))
        }
      </SectionCard>

      <SectionCard title={t(lang, 'enrollments')}>
        {!enrollments || enrollments.length === 0
          ? <EmptyState message={t(lang, 'noEnrollments')} />
          : enrollments.map((e, i) => (
            <InfoRow key={e.enrollmentId} label={`${e.institutionName} — ${e.programName}`}
              value={<StatusBadge label={e.status} status={e.status} size="sm" />}
              last={i === enrollments.length - 1} />
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
