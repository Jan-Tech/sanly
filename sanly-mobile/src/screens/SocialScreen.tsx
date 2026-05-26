import React from 'react'
import { ScrollView, StyleSheet } from 'react-native'
import { useQuery } from '@tanstack/react-query'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { getAllClaims, getPayments, getUnemploymentStatus } from '../api/social'
import { t } from '../i18n'
import { LoadingView } from '../components/LoadingView'
import { SectionCard } from '../components/SectionCard'
import { InfoRow } from '../components/InfoRow'
import { StatusBadge } from '../components/StatusBadge'
import { EmptyState } from '../components/EmptyState'
import { Colors, Spacing } from '../constants/theme'
import { formatDate, formatMoney } from '../utils/formatters'

export function SocialScreen() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()
  const opts = { enabled: !!nationalId, retry: false }

  const { data: claims, isLoading } = useQuery({ queryKey: ['claims-all', nationalId], queryFn: () => getAllClaims(nationalId!), ...opts })
  const { data: payments } = useQuery({ queryKey: ['payments', nationalId], queryFn: () => getPayments(nationalId!), ...opts })
  const { data: unemployment } = useQuery({ queryKey: ['unemployment', nationalId], queryFn: () => getUnemploymentStatus(nationalId!), ...opts })

  if (isLoading) return <LoadingView message={t(lang, 'loading')} />

  const active = (claims ?? []).filter(c => ['ACTIVE', 'PENDING'].includes(c.status))
  const recent = (payments ?? []).slice(0, 5)

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      <SectionCard title={t(lang, 'activeBenefits')}>
        {active.length === 0
          ? <EmptyState message={t(lang, 'noActiveBenefits')} />
          : active.map((c, i) => (
            <InfoRow key={c.claimId} label={c.programCode} value={<StatusBadge label={c.status} status={c.status} size="sm" />} last={i === active.length - 1} />
          ))
        }
      </SectionCard>

      <SectionCard title={t(lang, 'paymentHistory')}>
        {recent.length === 0
          ? <EmptyState message={t(lang, 'noPayments')} />
          : recent.map((p, i) => (
            <InfoRow key={p.paymentId} label={p.paymentPeriod} value={formatMoney(p.amount)} last={i === recent.length - 1} />
          ))
        }
      </SectionCard>

      {unemployment && (
        <SectionCard title={t(lang, 'unemploymentStatus')}>
          <InfoRow label={t(lang, 'status')} value={<StatusBadge label={unemployment.status} status={unemployment.status} />} />
          <InfoRow label={t(lang, 'registrationDate')} value={formatDate(unemployment.registeredAt, lang)} />
          <InfoRow label="Reason" value={unemployment.reason.replace(/_/g, ' ')} last />
        </SectionCard>
      )}
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 32 },
})
