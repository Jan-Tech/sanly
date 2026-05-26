import React, { useState } from 'react'
import { View, Text, ScrollView, TextInput, TouchableOpacity, StyleSheet, Alert } from 'react-native'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { verifyProperty, getPendingTransfers, approveTransfer, rejectTransfer } from '../../api/land'
import { SectionCard } from '../../components/SectionCard'
import { StatusBadge } from '../../components/StatusBadge'
import { LoadingView } from '../../components/LoadingView'
import { Colors, Spacing, Radius } from '../../constants/theme'
import { formatDate, formatMoney, maskNin } from '../../utils/formatters'

export function LandActionsScreen() {
  const qc = useQueryClient()
  const [cadCode, setCadCode] = useState('')
  const [property, setProperty] = useState<any>(null)
  const [propLoading, setPropLoading] = useState(false)
  const [selectedTransfer, setSelectedTransfer] = useState<any>(null)
  const [rejectReason, setRejectReason] = useState('')

  const { data: transfers, isLoading } = useQuery({
    queryKey: ['pending-transfers'],
    queryFn: getPendingTransfers,
    retry: false,
  })

  const approveMutation = useMutation({
    mutationFn: (id: string) => approveTransfer(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['pending-transfers'] }); setSelectedTransfer(null); Alert.alert('Approved', 'Transfer approved.') },
    onError: (e: any) => Alert.alert('Error', e?.response?.data?.message ?? 'Failed'),
  })

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => rejectTransfer(id, reason),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['pending-transfers'] }); setSelectedTransfer(null); Alert.alert('Rejected', 'Transfer rejected.') },
    onError: (e: any) => Alert.alert('Error', e?.response?.data?.message ?? 'Failed'),
  })

  async function lookupProperty() {
    if (!cadCode.trim()) return
    setPropLoading(true)
    setProperty(null)
    try {
      setProperty(await verifyProperty(cadCode.trim()))
    } catch {
      Alert.alert('Not Found', 'No property found for this code.')
    } finally {
      setPropLoading(false)
    }
  }

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      {/* Property lookup */}
      <SectionCard title="Property Lookup">
        <View style={styles.lookupRow}>
          <TextInput style={styles.lookupInput} value={cadCode} onChangeText={setCadCode} placeholder="TM-CAD-YYYYNNNNNN" placeholderTextColor={Colors.textMuted} autoCapitalize="characters" returnKeyType="search" onSubmitEditing={lookupProperty} />
          <TouchableOpacity style={styles.lookupBtn} onPress={lookupProperty} disabled={propLoading}>
            <Text style={styles.lookupBtnText}>{propLoading ? '...' : 'Lookup'}</Text>
          </TouchableOpacity>
        </View>
        {property && (
          <View style={styles.resultBody}>
            {[
              ['Cadastral Code', property.cadastralCode],
              ['Address', property.address],
              ['Type', property.propertyType],
              ['Area', `${property.area} m²`],
              ['Owner NIN', maskNin(property.ownerNationalId)],
            ].map(([l, v]) => (
              <View key={l} style={styles.row}>
                <Text style={styles.rowLabel}>{l}</Text>
                <Text style={styles.rowValue}>{v}</Text>
              </View>
            ))}
            <View style={styles.row}>
              <Text style={styles.rowLabel}>Status</Text>
              <StatusBadge label={property.status} status={property.status} size="sm" />
            </View>
          </View>
        )}
      </SectionCard>

      {/* Pending transfers */}
      <SectionCard title={`Pending Transfers (${transfers?.length ?? 0})`}>
        {isLoading ? (
          <LoadingView />
        ) : !transfers?.length ? (
          <Text style={styles.empty}>No pending transfers</Text>
        ) : (
          transfers.map((tr, i) => (
            <TouchableOpacity key={tr.applicationId} style={[styles.transferRow, i > 0 && styles.border]} onPress={() => { setSelectedTransfer(tr); setRejectReason('') }}>
              <View style={{ flex: 1 }}>
                <Text style={styles.transferCode}>{tr.cadastralCode}</Text>
                <Text style={styles.transferMeta}>{maskNin(tr.currentOwnerNin)} → {maskNin(tr.newOwnerNin)}</Text>
                <Text style={styles.transferMeta}>{formatMoney(tr.agreedPrice)} · {formatDate(tr.submittedAt)}</Text>
                {tr.taxCheckResult && (
                  <Text style={[styles.taxAlert, tr.taxCheckResult === 'NON_COMPLIANT' && styles.taxAlertRed]}>
                    Tax: {tr.taxCheckResult}
                  </Text>
                )}
              </View>
              <StatusBadge label={tr.status} status={tr.status} size="sm" />
            </TouchableOpacity>
          ))
        )}
      </SectionCard>

      {/* Transfer detail inline */}
      {selectedTransfer && (
        <SectionCard title="Transfer Details">
          <View style={styles.resultBody}>
            {[
              ['Property', selectedTransfer.cadastralCode],
              ['Current Owner NIN', maskNin(selectedTransfer.currentOwnerNin)],
              ['New Owner NIN', maskNin(selectedTransfer.newOwnerNin)],
              ['Agreed Price', formatMoney(selectedTransfer.agreedPrice)],
              ['Tax Check', selectedTransfer.taxCheckResult ?? 'Pending'],
              ['Submitted', formatDate(selectedTransfer.submittedAt)],
            ].map(([l, v]) => (
              <View key={l} style={styles.row}>
                <Text style={styles.rowLabel}>{l}</Text>
                <Text style={styles.rowValue}>{v}</Text>
              </View>
            ))}
            <Text style={styles.label}>Rejection Reason (if rejecting)</Text>
            <TextInput style={styles.input} value={rejectReason} onChangeText={setRejectReason} placeholder="Required if rejecting..." placeholderTextColor={Colors.textMuted} />
            <View style={styles.actionRow}>
              <TouchableOpacity style={[styles.approveBtn, approveMutation.isPending && styles.disabled]} onPress={() => approveMutation.mutate(selectedTransfer.applicationId)} disabled={approveMutation.isPending}>
                <Text style={styles.actionBtnText}>✓ Approve</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.rejectBtn, rejectMutation.isPending && styles.disabled]}
                onPress={() => {
                  if (!rejectReason.trim()) { Alert.alert('Required', 'Enter rejection reason'); return }
                  rejectMutation.mutate({ id: selectedTransfer.applicationId, reason: rejectReason })
                }}
                disabled={rejectMutation.isPending}
              >
                <Text style={styles.actionBtnText}>✕ Reject</Text>
              </TouchableOpacity>
            </View>
          </View>
        </SectionCard>
      )}
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 40 },
  lookupRow: { flexDirection: 'row', gap: Spacing.sm, padding: Spacing.md },
  lookupInput: { flex: 1, backgroundColor: Colors.background, borderRadius: Radius.sm, borderWidth: 1, borderColor: Colors.border, padding: Spacing.md, fontSize: 14, color: Colors.dark },
  lookupBtn: { backgroundColor: Colors.land, borderRadius: Radius.sm, paddingHorizontal: Spacing.lg, justifyContent: 'center' },
  lookupBtnText: { color: '#fff', fontWeight: '700', fontSize: 13 },
  resultBody: { borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: Colors.border, padding: Spacing.md, gap: 2 },
  row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 5 },
  rowLabel: { fontSize: 12, color: Colors.textSecondary, flex: 1 },
  rowValue: { fontSize: 12, fontWeight: '500', color: Colors.dark, flex: 2, textAlign: 'right' },
  transferRow: { flexDirection: 'row', alignItems: 'center', padding: Spacing.md },
  border: { borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: Colors.border },
  transferCode: { fontSize: 13, fontWeight: '600', color: Colors.dark },
  transferMeta: { fontSize: 11, color: Colors.textMuted, marginTop: 1 },
  taxAlert: { fontSize: 11, color: Colors.success, marginTop: 2, fontWeight: '600' },
  taxAlertRed: { color: Colors.error },
  empty: { padding: Spacing.lg, color: Colors.textMuted, textAlign: 'center' },
  label: { fontSize: 12, fontWeight: '600', color: Colors.textSecondary, marginTop: Spacing.md, marginBottom: Spacing.sm, textTransform: 'uppercase', letterSpacing: 0.5 },
  input: { backgroundColor: Colors.background, borderRadius: Radius.sm, borderWidth: 1, borderColor: Colors.border, padding: Spacing.md, fontSize: 14, color: Colors.dark },
  actionRow: { flexDirection: 'row', gap: Spacing.md, marginTop: Spacing.md },
  approveBtn: { flex: 1, backgroundColor: Colors.success, borderRadius: Radius.md, padding: Spacing.md, alignItems: 'center' },
  rejectBtn: { flex: 1, backgroundColor: Colors.error, borderRadius: Radius.md, padding: Spacing.md, alignItems: 'center' },
  disabled: { opacity: 0.5 },
  actionBtnText: { color: '#fff', fontWeight: '700', fontSize: 14 },
})
