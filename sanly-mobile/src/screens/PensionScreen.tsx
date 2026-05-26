import React from 'react'
import { ScrollView, StyleSheet } from 'react-native'
import { useQuery } from '@tanstack/react-query'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { getPensionAccount, getPensionEligibility, getPensionContributions } from '../api/pension'
import { t } from '../i18n'
import { LoadingView } from '../components/LoadingView'
import { SectionCard } from '../components/SectionCard'
import { InfoRow } from '../components/InfoRow'
import { StatusBadge } from '../components/StatusBadge'
import { EmptyState } from '../components/EmptyState'
import { Colors, Spacing } from '../constants/theme'
import { formatDate, formatMoney } from '../utils/formatters'

export function PensionScreen() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()
  const opts = { enabled: !!nationalId, retry: false }

  const { data: account, isLoading } = useQuery({ queryKey: ['pension', nationalId], queryFn: () => getPensionAccount(nationalId!), ...opts })
  const { data: eligibility } = useQuery({ queryKey: ['pension-eligibility', nationalId], queryFn: () => getPensionEligibility(nationalId!), ...opts })
  const { data: contributions } = useQuery({ queryKey: ['contributions', account?.accountCode], queryFn: () => getPensionContributions(account!.accountCode), enabled: !!account?.accountCode, retry: false })

  if (isLoading) return <LoadingView message={t(lang, 'loading')} />
  if (!account) return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      <EmptyState message={t(lang, 'noPensionAccount')} />
    </ScrollView>
  )

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      <SectionCard title={t(lang, 'pensionTitle')}>
        <InfoRow label={t(lang, 'status')} value={<StatusBadge label={account.status} status={['ELIGIBLE','PAYING'].includes(account.status) ? 'ACTIVE' : account.status} />} />
        <InfoRow label={t(lang, 'totalContributions')} value={formatMoney(account.totalContributions)} />
        <InfoRow label={t(lang, 'registrationDate')} value={formatDate(account.openedAt, lang)} last />
      </SectionCard>

      {eligibility && (
        <SectionCard title={t(lang, 'eligibleAt')}>
          <InfoRow label={t(lang, 'eligibleAt')} value={formatDate(eligibility.eligibleAt, lang)} />
          <InfoRow label={t(lang, 'yearsRemaining')} value={String(eligibility.yearsRemaining)} />
          <InfoRow label={t(lang, 'projectedMonthly')} value={formatMoney(eligibility.projectedMonthlyAmount)} last />
        </SectionCard>
      )}

      <SectionCard title={t(lang, 'contributionHistory')}>
        {!contributions || contributions.length === 0
          ? <EmptyState message={t(lang, 'noData')} />
          : contributions.slice(0, 12).map((c, i) => (
            <InfoRow key={c.contributionId} label={c.contributionMonth} value={formatMoney(c.totalAmount)} last={i === Math.min(contributions.length, 12) - 1} />
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
