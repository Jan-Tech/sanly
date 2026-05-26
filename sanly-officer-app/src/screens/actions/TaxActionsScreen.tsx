import React, { useState } from 'react'
import { View, Text, ScrollView, TextInput, TouchableOpacity, StyleSheet, Alert } from 'react-native'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getTaxpayerByNin, getFilingsByTaxId, getPendingFilings, processFiling } from '../../api/tax'
import { SectionCard } from '../../components/SectionCard'
import { StatusBadge } from '../../components/StatusBadge'
import { LoadingView } from '../../components/LoadingView'
import { Colors, Spacing, Radius } from '../../constants/theme'
import { formatDate } from '../../utils/formatters'

export function TaxActionsScreen() {
  const qc = useQueryClient()
  const [nin, setNin] = useState('')
  const [taxpayer, setTaxpayer] = useState<any>(null)
  const [filings, setFilings] = useState<any[]>([])
  const [lookupLoading, setLookupLoading] = useState(false)

  const { data: pendingFilings, isLoading } = useQuery({
    queryKey: ['pending-filings'],
    queryFn: getPendingFilings,
    retry: false,
  })

  const processMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: 'ACCEPTED' | 'REJECTED' }) => processFiling(id, status),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['pending-filings'] })
      if (taxpayer) {
        getFilingsByTaxId(taxpayer.taxId).then(setFilings)
      }
      Alert.alert('Processed', 'Filing status updated.')
    },
    onError: (e: any) => Alert.alert('Error', e?.response?.data?.message ?? 'Failed'),
  })

  async function lookupTaxpayer() {
    if (!nin.trim()) return
    setLookupLoading(true)
    setTaxpayer(null)
    setFilings([])
    try {
      const tp = await getTaxpayerByNin(nin.trim())
      setTaxpayer(tp)
      const f = await getFilingsByTaxId(tp.taxId)
      setFilings(f)
    } catch (e: any) {
      Alert.alert('Not Found', 'No taxpayer found for this NIN.')
    } finally {
      setLookupLoading(false)
    }
  }

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      {/* Taxpayer lookup */}
      <SectionCard title="Taxpayer Lookup">
        <View style={styles.lookupRow}>
          <TextInput style={styles.lookupInput} value={nin} onChangeText={setNin} placeholder="10-digit NIN" placeholderTextColor={Colors.textMuted} keyboardType="numeric" returnKeyType="search" onSubmitEditing={lookupTaxpayer} />
          <TouchableOpacity style={styles.lookupBtn} onPress={lookupTaxpayer} disabled={lookupLoading}>
            <Text style={styles.lookupBtnText}>{lookupLoading ? '...' : 'Lookup'}</Text>
          </TouchableOpacity>
        </View>
        {taxpayer && (
          <View style={styles.resultBody}>
            {[
              ['Tax ID', taxpayer.taxId],
              ['Name', taxpayer.fullName],
              ['Type', taxpayer.taxpayerType],
              ['Registered', formatDate(taxpayer.registeredAt)],
            ].map(([l, v]) => (
              <View key={l} style={styles.row}>
                <Text style={styles.rowLabel}>{l}</Text>
                <Text style={styles.rowValue}>{v}</Text>
              </View>
            ))}
            <View style={styles.row}>
              <Text style={styles.rowLabel}>Status</Text>
              <StatusBadge label={taxpayer.status} status={taxpayer.status} size="sm" />
            </View>
          </View>
        )}
      </SectionCard>

      {/* Filings for looked-up taxpayer */}
      {filings.length > 0 && (
        <SectionCard title={`Filings (${filings.length})`}>
          {filings.map((f, i) => (
            <View key={f.filingId} style={[styles.filingRow, i > 0 && styles.border]}>
              <View style={{ flex: 1 }}>
                <Text style={styles.filingYear}>Year {f.year}</Text>
                <Text style={styles.filingDate}>{formatDate(f.submittedAt)}</Text>
              </View>
              <View style={{ alignItems: 'flex-end', gap: 4 }}>
                <StatusBadge label={f.filingStatus} status={f.filingStatus} size="sm" />
                {f.filingStatus === 'SUBMITTED' && (
                  <View style={styles.filingActions}>
                    <TouchableOpacity style={styles.acceptBtn} onPress={() => processMutation.mutate({ id: f.filingId, status: 'ACCEPTED' })}>
                      <Text style={styles.actionText}>✓</Text>
                    </TouchableOpacity>
                    <TouchableOpacity style={styles.rejectBtn} onPress={() => processMutation.mutate({ id: f.filingId, status: 'REJECTED' })}>
                      <Text style={styles.actionText}>✕</Text>
                    </TouchableOpacity>
                  </View>
                )}
              </View>
            </View>
          ))}
        </SectionCard>
      )}

      {/* Pending filings queue */}
      <SectionCard title={`Pending Filings Queue (${pendingFilings?.length ?? 0})`}>
        {isLoading ? (
          <LoadingView />
        ) : !pendingFilings?.length ? (
          <Text style={styles.empty}>No pending filings</Text>
        ) : (
          pendingFilings.slice(0, 15).map((f, i) => (
            <View key={f.filingId} style={[styles.filingRow, i > 0 && styles.border]}>
              <View style={{ flex: 1 }}>
                <Text style={styles.filingYear}>{f.taxId} · Year {f.year}</Text>
                <Text style={styles.filingDate}>Submitted {formatDate(f.submittedAt)}</Text>
              </View>
              <View style={styles.filingActions}>
                <TouchableOpacity style={styles.acceptBtn} onPress={() => processMutation.mutate({ id: f.filingId, status: 'ACCEPTED' })}>
                  <Text style={styles.actionText}>✓</Text>
                </TouchableOpacity>
                <TouchableOpacity style={styles.rejectBtn} onPress={() => processMutation.mutate({ id: f.filingId, status: 'REJECTED' })}>
                  <Text style={styles.actionText}>✕</Text>
                </TouchableOpacity>
              </View>
            </View>
          ))
        )}
      </SectionCard>
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 40 },
  lookupRow: { flexDirection: 'row', gap: Spacing.sm, padding: Spacing.md },
  lookupInput: { flex: 1, backgroundColor: Colors.background, borderRadius: Radius.sm, borderWidth: 1, borderColor: Colors.border, padding: Spacing.md, fontSize: 14, color: Colors.dark },
  lookupBtn: { backgroundColor: Colors.tax, borderRadius: Radius.sm, paddingHorizontal: Spacing.lg, justifyContent: 'center' },
  lookupBtnText: { color: '#fff', fontWeight: '700', fontSize: 13 },
  resultBody: { borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: Colors.border, padding: Spacing.md, gap: 2 },
  row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 5 },
  rowLabel: { fontSize: 12, color: Colors.textSecondary },
  rowValue: { fontSize: 12, fontWeight: '500', color: Colors.dark },
  filingRow: { flexDirection: 'row', alignItems: 'center', padding: Spacing.md },
  border: { borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: Colors.border },
  filingYear: { fontSize: 13, fontWeight: '600', color: Colors.dark },
  filingDate: { fontSize: 11, color: Colors.textMuted, marginTop: 1 },
  filingActions: { flexDirection: 'row', gap: Spacing.sm },
  acceptBtn: { width: 28, height: 28, borderRadius: Radius.sm, backgroundColor: Colors.success, justifyContent: 'center', alignItems: 'center' },
  rejectBtn: { width: 28, height: 28, borderRadius: Radius.sm, backgroundColor: Colors.error, justifyContent: 'center', alignItems: 'center' },
  actionText: { color: '#fff', fontWeight: '800', fontSize: 13 },
  empty: { padding: Spacing.lg, color: Colors.textMuted, textAlign: 'center' },
})
