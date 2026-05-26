import React from 'react'
import { ScrollView, View, Text, TouchableOpacity, StyleSheet } from 'react-native'
import { useQuery } from '@tanstack/react-query'
import { useLangStore } from '../store/langStore'
import { getPendingConsents, getConsentHistory } from '../api/banking'
import { t } from '../i18n'
import { LoadingView } from '../components/LoadingView'
import { SectionCard } from '../components/SectionCard'
import { InfoRow } from '../components/InfoRow'
import { StatusBadge } from '../components/StatusBadge'
import { EmptyState } from '../components/EmptyState'
import { Colors, Radius, Spacing } from '../constants/theme'
import { formatDate } from '../utils/formatters'

export function BankingScreen() {
  const { lang } = useLangStore()

  const { data: pending, isLoading } = useQuery({ queryKey: ['consents'], queryFn: getPendingConsents, retry: false, refetchInterval: 30_000 })
  const { data: history } = useQuery({ queryKey: ['consent-history'], queryFn: getConsentHistory, retry: false })

  if (isLoading) return <LoadingView message={t(lang, 'loading')} />

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      <SectionCard title={t(lang, 'pendingConsents')}>
        {!pending || pending.length === 0
          ? <EmptyState message={t(lang, 'noPendingConsents')} />
          : pending.map((c, i) => (
            <View key={c.consentId} style={[styles.consentRow, i > 0 && styles.borderTop]}>
              <View style={styles.consentInfo}>
                <Text style={styles.bankName}>{c.bankName}</Text>
                <Text style={styles.purpose} numberOfLines={2}>{c.purpose}</Text>
                <Text style={styles.expires}>Expires: {formatDate(c.expiresAt, lang)}</Text>
                <View style={styles.scopeRow}>
                  {c.requestedScopes.map(s => (
                    <View key={s} style={styles.scopeChip}>
                      <Text style={styles.scopeText}>{s.replace(/_/g, ' ')}</Text>
                    </View>
                  ))}
                </View>
              </View>
            </View>
          ))
        }
      </SectionCard>

      <SectionCard title="Consent History">
        {!history || history.length === 0
          ? <EmptyState message={t(lang, 'noData')} />
          : history.slice(0, 10).map((c, i) => (
            <InfoRow key={c.consentId} label={c.bankName} value={<StatusBadge label={c.status} status={c.status === 'APPROVED' ? 'ACTIVE' : c.status === 'REJECTED' ? 'REVOKED' : 'PENDING'} size="sm" />} last={i === Math.min(history.length, 10) - 1} />
          ))
        }
      </SectionCard>
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 32 },
  consentRow: { paddingVertical: 12 },
  borderTop: { borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: Colors.border },
  consentInfo: {},
  bankName: { fontSize: 15, fontWeight: '700', color: Colors.dark, marginBottom: 4 },
  purpose: { fontSize: 13, color: Colors.textSecondary, marginBottom: 4 },
  expires: { fontSize: 11, color: Colors.textMuted, marginBottom: 8 },
  scopeRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 4 },
  scopeChip: { backgroundColor: Colors.infoBg, borderRadius: Radius.full, paddingHorizontal: 8, paddingVertical: 3 },
  scopeText: { fontSize: 10, color: Colors.info, fontWeight: '600' },
})
