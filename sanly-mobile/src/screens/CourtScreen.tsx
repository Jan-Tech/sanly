import React from 'react'
import { ScrollView, View, Text, StyleSheet } from 'react-native'
import { useQuery } from '@tanstack/react-query'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { getFinesByCitizen, getCasesByCitizen } from '../api/court'
import { t } from '../i18n'
import { LoadingView } from '../components/LoadingView'
import { SectionCard } from '../components/SectionCard'
import { InfoRow } from '../components/InfoRow'
import { StatusBadge } from '../components/StatusBadge'
import { EmptyState } from '../components/EmptyState'
import { Colors, Radius, Spacing } from '../constants/theme'
import { formatDate, formatMoney } from '../utils/formatters'

export function CourtScreen() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()
  const opts = { enabled: !!nationalId, retry: false }

  const { data: fines, isLoading: finesLoading } = useQuery({ queryKey: ['fines', nationalId], queryFn: () => getFinesByCitizen(nationalId!), ...opts })
  const { data: cases, isLoading: casesLoading } = useQuery({ queryKey: ['cases', nationalId], queryFn: () => getCasesByCitizen(nationalId!), ...opts })

  if (finesLoading || casesLoading) return <LoadingView message={t(lang, 'loading')} />

  const outstanding = (fines ?? []).filter(f => ['OUTSTANDING', 'OVERDUE'].includes(f.status))
  const totalOwed = outstanding.reduce((sum, f) => sum + parseFloat(f.amount || '0'), 0)

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      {outstanding.length > 0 && (
        <View style={styles.alert}>
          <Text style={styles.alertTitle}>Outstanding Fines</Text>
          <Text style={styles.alertAmount}>{totalOwed.toFixed(2)} TMT</Text>
        </View>
      )}

      <SectionCard title={t(lang, 'myFines')}>
        {!fines || fines.length === 0
          ? <EmptyState message={t(lang, 'noFines')} />
          : fines.map((f, i) => (
            <View key={f.fineId} style={[styles.fineRow, i > 0 && styles.borderTop]}>
              <View style={styles.fineLeft}>
                <Text style={styles.fineCode}>{f.fineCode}</Text>
                <Text style={styles.fineType}>{f.fineType.replace(/_/g, ' ')}</Text>
              </View>
              <View style={styles.fineRight}>
                <Text style={styles.fineAmt}>{formatMoney(f.amount)}</Text>
                <StatusBadge label={f.status} status={f.status} size="sm" />
              </View>
            </View>
          ))
        }
      </SectionCard>

      <SectionCard title={t(lang, 'myCases')}>
        {!cases || cases.length === 0
          ? <EmptyState message={t(lang, 'noCases')} />
          : cases.map((c, i) => (
            <InfoRow key={c.caseId} label={c.caseNumber} value={<StatusBadge label={c.status} status={c.status} size="sm" />} last={i === cases.length - 1} />
          ))
        }
      </SectionCard>
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 32 },
  alert: { backgroundColor: Colors.errorBg, borderRadius: Radius.md, padding: Spacing.lg, marginBottom: Spacing.md, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  alertTitle: { fontSize: 14, fontWeight: '600', color: Colors.error },
  alertAmount: { fontSize: 18, fontWeight: '800', color: Colors.error },
  fineRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 10 },
  borderTop: { borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: Colors.border },
  fineLeft: {},
  fineCode: { fontSize: 13, fontWeight: '600', color: Colors.dark },
  fineType: { fontSize: 11, color: Colors.textMuted },
  fineRight: { alignItems: 'flex-end', gap: 4 },
  fineAmt: { fontSize: 14, fontWeight: '700', color: Colors.dark },
})
