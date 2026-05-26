import React, { useState } from 'react'
import { View, Text, ScrollView, TextInput, TouchableOpacity, Modal, StyleSheet, Alert } from 'react-native'
import { useMutation } from '@tanstack/react-query'
import { getFineByCode, getCaseByNumber, confirmFinePayment } from '../../api/court'
import { SectionCard } from '../../components/SectionCard'
import { StatusBadge } from '../../components/StatusBadge'
import { Colors, Spacing, Radius } from '../../constants/theme'
import { formatDate, formatMoney, maskNin } from '../../utils/formatters'

export function CourtActionsScreen() {
  const [fineCode, setFineCode] = useState('')
  const [caseNum, setCaseNum] = useState('')
  const [fine, setFine] = useState<any>(null)
  const [caseRecord, setCaseRecord] = useState<any>(null)
  const [fineLoading, setFineLoading] = useState(false)
  const [caseLoading, setCaseLoading] = useState(false)
  const [paymentRef, setPaymentRef] = useState('')
  const [payModal, setPayModal] = useState(false)

  const payMutation = useMutation({
    mutationFn: () => confirmFinePayment(fine.fineId, paymentRef || undefined),
    onSuccess: (updated) => {
      setFine(updated)
      setPayModal(false)
      Alert.alert('Confirmed', 'Fine payment recorded.')
    },
    onError: (e: any) => Alert.alert('Error', e?.response?.data?.message ?? 'Failed'),
  })

  async function lookupFine() {
    if (!fineCode.trim()) return
    setFineLoading(true)
    setFine(null)
    try {
      setFine(await getFineByCode(fineCode.trim()))
    } catch {
      Alert.alert('Not Found', 'No fine found for this code.')
    } finally {
      setFineLoading(false)
    }
  }

  async function lookupCase() {
    if (!caseNum.trim()) return
    setCaseLoading(true)
    setCaseRecord(null)
    try {
      setCaseRecord(await getCaseByNumber(caseNum.trim()))
    } catch {
      Alert.alert('Not Found', 'No case found for this number.')
    } finally {
      setCaseLoading(false)
    }
  }

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      {/* Fine lookup */}
      <SectionCard title="Fine Lookup">
        <View style={styles.lookupRow}>
          <TextInput style={styles.lookupInput} value={fineCode} onChangeText={setFineCode} placeholder="TM-FINE-..." placeholderTextColor={Colors.textMuted} autoCapitalize="characters" returnKeyType="search" onSubmitEditing={lookupFine} />
          <TouchableOpacity style={styles.lookupBtn} onPress={lookupFine} disabled={fineLoading}>
            <Text style={styles.lookupBtnText}>{fineLoading ? '...' : 'Lookup'}</Text>
          </TouchableOpacity>
        </View>
        {fine && (
          <View style={styles.resultBody}>
            {[
              ['Fine Code', fine.fineCode],
              ['Citizen', fine.citizenName],
              ['NIN', maskNin(fine.nationalId)],
              ['Offense', fine.offenseType],
              ['Amount', formatMoney(fine.fineAmount)],
              ['Issued', formatDate(fine.issuedDate)],
              ['Due Date', formatDate(fine.dueDate)],
            ].map(([l, v]) => (
              <View key={l} style={styles.row}>
                <Text style={styles.rowLabel}>{l}</Text>
                <Text style={styles.rowValue}>{v}</Text>
              </View>
            ))}
            <View style={styles.row}>
              <Text style={styles.rowLabel}>Status</Text>
              <StatusBadge label={fine.status} status={fine.status} size="sm" />
            </View>
            {fine.status === 'OUTSTANDING' && (
              <TouchableOpacity style={styles.payBtn} onPress={() => setPayModal(true)}>
                <Text style={styles.payBtnText}>✓ Confirm Payment</Text>
              </TouchableOpacity>
            )}
          </View>
        )}
      </SectionCard>

      {/* Case lookup */}
      <SectionCard title="Case Lookup">
        <View style={styles.lookupRow}>
          <TextInput style={styles.lookupInput} value={caseNum} onChangeText={setCaseNum} placeholder="TM-CASE-..." placeholderTextColor={Colors.textMuted} autoCapitalize="characters" returnKeyType="search" onSubmitEditing={lookupCase} />
          <TouchableOpacity style={styles.lookupBtn} onPress={lookupCase} disabled={caseLoading}>
            <Text style={styles.lookupBtnText}>{caseLoading ? '...' : 'Lookup'}</Text>
          </TouchableOpacity>
        </View>
        {caseRecord && (
          <View style={styles.resultBody}>
            {[
              ['Case №', caseRecord.caseNumber],
              ['Citizen', caseRecord.citizenName],
              ['Type', caseRecord.caseType],
              ['Filed', formatDate(caseRecord.filedDate)],
              ['Next Hearing', formatDate(caseRecord.nextHearingDate)],
              ['Judge', caseRecord.judgeName ?? '—'],
              ['Verdict', caseRecord.verdict ?? 'Pending'],
            ].map(([l, v]) => (
              <View key={l} style={styles.row}>
                <Text style={styles.rowLabel}>{l}</Text>
                <Text style={styles.rowValue}>{v}</Text>
              </View>
            ))}
            <View style={styles.row}>
              <Text style={styles.rowLabel}>Status</Text>
              <StatusBadge label={caseRecord.status} status={caseRecord.status} size="sm" />
            </View>
          </View>
        )}
      </SectionCard>

      {/* Payment confirmation modal */}
      <Modal visible={payModal} animationType="slide" presentationStyle="formSheet">
        <View style={styles.modal}>
          <View style={styles.modalHeader}>
            <Text style={styles.modalTitle}>Confirm Payment</Text>
            <TouchableOpacity onPress={() => setPayModal(false)}><Text style={styles.modalClose}>Cancel</Text></TouchableOpacity>
          </View>
          <View style={styles.modalContent}>
            {fine && <Text style={styles.payAmount}>Amount: {formatMoney(fine.fineAmount)}</Text>}
            <Text style={styles.label}>Payment Reference (optional)</Text>
            <TextInput style={styles.input} value={paymentRef} onChangeText={setPaymentRef} placeholder="Bank transfer ref, receipt #..." placeholderTextColor={Colors.textMuted} />
            <TouchableOpacity
              style={[styles.confirmBtn, payMutation.isPending && styles.disabledBtn]}
              onPress={() => payMutation.mutate()}
              disabled={payMutation.isPending}
            >
              <Text style={styles.confirmBtnText}>{payMutation.isPending ? 'Processing...' : 'Confirm Payment'}</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 40 },
  lookupRow: { flexDirection: 'row', gap: Spacing.sm, padding: Spacing.md },
  lookupInput: { flex: 1, backgroundColor: Colors.background, borderRadius: Radius.sm, borderWidth: 1, borderColor: Colors.border, padding: Spacing.md, fontSize: 14, color: Colors.dark },
  lookupBtn: { backgroundColor: Colors.court, borderRadius: Radius.sm, paddingHorizontal: Spacing.lg, justifyContent: 'center' },
  lookupBtnText: { color: '#fff', fontWeight: '700', fontSize: 13 },
  resultBody: { borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: Colors.border, padding: Spacing.md, gap: 2 },
  row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 5 },
  rowLabel: { fontSize: 12, color: Colors.textSecondary },
  rowValue: { fontSize: 12, fontWeight: '500', color: Colors.dark },
  payBtn: { backgroundColor: Colors.success, borderRadius: Radius.md, padding: Spacing.md, alignItems: 'center', marginTop: Spacing.md },
  payBtnText: { color: '#fff', fontWeight: '700', fontSize: 14 },
  modal: { flex: 1, backgroundColor: Colors.background },
  modalHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: Spacing.lg, backgroundColor: Colors.navy },
  modalTitle: { fontSize: 17, fontWeight: '700', color: '#fff' },
  modalClose: { color: Colors.primary, fontSize: 15, fontWeight: '600' },
  modalContent: { padding: Spacing.lg },
  payAmount: { fontSize: 20, fontWeight: '800', color: Colors.dark, marginBottom: Spacing.lg, textAlign: 'center' },
  label: { fontSize: 12, fontWeight: '600', color: Colors.textSecondary, marginBottom: Spacing.sm, textTransform: 'uppercase', letterSpacing: 0.5 },
  input: { backgroundColor: Colors.card, borderRadius: Radius.md, borderWidth: 1, borderColor: Colors.border, padding: Spacing.md, fontSize: 15, color: Colors.dark },
  confirmBtn: { backgroundColor: Colors.success, borderRadius: Radius.lg, padding: Spacing.md, alignItems: 'center', marginTop: Spacing.xl },
  disabledBtn: { opacity: 0.5 },
  confirmBtnText: { color: '#fff', fontWeight: '700', fontSize: 15 },
})
