import React, { useState } from 'react'
import {
  View, Text, ScrollView, TextInput, TouchableOpacity,
  Modal, StyleSheet, Alert,
} from 'react-native'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { UserCheck, FileWarning, AlertTriangle } from 'lucide-react-native'
import { runCitizenCheck, createCriminalRecord, getTodayChecks } from '../../api/police'
import { SectionCard } from '../../components/SectionCard'
import { StatusBadge } from '../../components/StatusBadge'
import { LoadingView } from '../../components/LoadingView'
import { Colors, Spacing, Radius, Shadow } from '../../constants/theme'
import { formatDateTime, maskNin } from '../../utils/formatters'

type CheckType = 'DRIVING_LICENSE' | 'TAX_STATUS' | 'FULL_CHECK'

export function PoliceActionsScreen() {
  const qc = useQueryClient()
  const [checkModal, setCheckModal] = useState(false)
  const [recordModal, setRecordModal] = useState(false)
  const [nationalId, setNationalId] = useState('')
  const [checkType, setCheckType] = useState<CheckType>('FULL_CHECK')
  const [caseRef, setCaseRef] = useState('')
  const [checkResult, setCheckResult] = useState<any>(null)

  // Criminal record form state
  const [crNin, setCrNin] = useState('')
  const [offense, setOffense] = useState('')
  const [offenseDate, setOffenseDate] = useState('')
  const [verdict, setVerdict] = useState('')
  const [sentence, setSentence] = useState('')
  const [courtName, setCourtName] = useState('')
  const [caseNumber, setCaseNumber] = useState('')

  const { data: todayChecks, isLoading } = useQuery({
    queryKey: ['today-checks'],
    queryFn: getTodayChecks,
    retry: false,
  })

  const checkMutation = useMutation({
    mutationFn: () => runCitizenCheck(nationalId.trim(), checkType, 'TRAFFIC_STOP', caseRef || undefined),
    onSuccess: (data) => {
      setCheckResult(data)
      qc.invalidateQueries({ queryKey: ['today-checks'] })
    },
    onError: (e: any) => Alert.alert('Check Failed', e?.response?.data?.message ?? 'Request failed'),
  })

  const recordMutation = useMutation({
    mutationFn: () => createCriminalRecord({ nationalId: crNin.trim(), offenseType: offense, offenseDate, verdict, sentenceDescription: sentence, courtName, caseNumber }),
    onSuccess: () => {
      Alert.alert('Success', 'Criminal record submitted.')
      setRecordModal(false)
      setCrNin(''); setOffense(''); setOffenseDate(''); setVerdict(''); setSentence(''); setCourtName(''); setCaseNumber('')
    },
    onError: (e: any) => Alert.alert('Error', e?.response?.data?.message ?? 'Submission failed'),
  })

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      {/* Quick action buttons */}
      <View style={styles.btnGrid}>
        <TouchableOpacity style={[styles.bigBtn, { borderColor: Colors.police }]} onPress={() => { setCheckModal(true); setCheckResult(null) }}>
          <UserCheck size={24} color={Colors.police} />
          <Text style={[styles.bigBtnText, { color: Colors.police }]}>Run Citizen Check</Text>
        </TouchableOpacity>
        <TouchableOpacity style={[styles.bigBtn, { borderColor: Colors.error }]} onPress={() => setRecordModal(true)}>
          <FileWarning size={24} color={Colors.error} />
          <Text style={[styles.bigBtnText, { color: Colors.error }]}>Criminal Record</Text>
        </TouchableOpacity>
      </View>

      {/* Today's checks */}
      <SectionCard title={`Today's Checks (${todayChecks?.length ?? 0})`}>
        {isLoading ? (
          <LoadingView />
        ) : !todayChecks?.length ? (
          <Text style={styles.empty}>No checks today</Text>
        ) : (
          todayChecks.map((c, i) => (
            <View key={c.checkId} style={[styles.checkRow, i > 0 && styles.border]}>
              <View>
                <Text style={styles.checkNin}>{maskNin(c.nationalId)}</Text>
                <Text style={styles.checkMeta}>{c.checkType} · {formatDateTime(c.checkedAt, 'en')}</Text>
              </View>
            </View>
          ))
        )}
      </SectionCard>

      {/* Run Check Modal */}
      <Modal visible={checkModal} animationType="slide" presentationStyle="pageSheet">
        <View style={styles.modal}>
          <View style={styles.modalHeader}>
            <Text style={styles.modalTitle}>Citizen Check</Text>
            <TouchableOpacity onPress={() => setCheckModal(false)}>
              <Text style={styles.modalClose}>Done</Text>
            </TouchableOpacity>
          </View>
          <ScrollView style={{ flex: 1 }} contentContainerStyle={styles.modalContent}>
            <Text style={styles.label}>National ID</Text>
            <TextInput style={styles.input} value={nationalId} onChangeText={setNationalId} placeholder="10-digit NIN" placeholderTextColor={Colors.textMuted} keyboardType="numeric" />

            <Text style={styles.label}>Check Type</Text>
            {(['FULL_CHECK', 'DRIVING_LICENSE', 'TAX_STATUS'] as CheckType[]).map(ct => (
              <TouchableOpacity key={ct} style={[styles.radioRow, checkType === ct && styles.radioActive]} onPress={() => setCheckType(ct)}>
                <View style={[styles.radioCircle, checkType === ct && styles.radioCircleActive]} />
                <Text style={styles.radioText}>{ct.replace(/_/g, ' ')}</Text>
              </TouchableOpacity>
            ))}

            <Text style={styles.label}>Case Reference (optional)</Text>
            <TextInput style={styles.input} value={caseRef} onChangeText={setCaseRef} placeholder="e.g. TM-CASE-2024001234" placeholderTextColor={Colors.textMuted} />

            <TouchableOpacity
              style={[styles.submitBtn, checkMutation.isPending && styles.submitBtnDisabled]}
              onPress={() => checkMutation.mutate()}
              disabled={checkMutation.isPending || !nationalId.trim()}
            >
              <Text style={styles.submitBtnText}>{checkMutation.isPending ? 'Running...' : 'Run Check'}</Text>
            </TouchableOpacity>

            {checkResult && (
              <View style={styles.resultBox}>
                <Text style={styles.resultTitle}>Check Result</Text>
                <Text style={styles.resultText}>{JSON.stringify(checkResult.results, null, 2)}</Text>
              </View>
            )}
          </ScrollView>
        </View>
      </Modal>

      {/* Criminal Record Modal */}
      <Modal visible={recordModal} animationType="slide" presentationStyle="pageSheet">
        <View style={styles.modal}>
          <View style={styles.modalHeader}>
            <Text style={styles.modalTitle}>Criminal Record</Text>
            <TouchableOpacity onPress={() => setRecordModal(false)}>
              <Text style={styles.modalClose}>Cancel</Text>
            </TouchableOpacity>
          </View>
          <ScrollView style={{ flex: 1 }} contentContainerStyle={styles.modalContent}>
            {[
              { label: 'National ID', value: crNin, setter: setCrNin, placeholder: '10-digit NIN' },
              { label: 'Offense Type', value: offense, setter: setOffense, placeholder: 'e.g. Traffic Violation' },
              { label: 'Offense Date (YYYY-MM-DD)', value: offenseDate, setter: setOffenseDate, placeholder: '2024-01-15' },
              { label: 'Verdict', value: verdict, setter: setVerdict, placeholder: 'e.g. GUILTY' },
              { label: 'Court Name', value: courtName, setter: setCourtName, placeholder: 'e.g. Ashgabat City Court' },
              { label: 'Case Number', value: caseNumber, setter: setCaseNumber, placeholder: 'TM-CASE-...' },
              { label: 'Sentence Description (optional)', value: sentence, setter: setSentence, placeholder: 'Details...' },
            ].map(f => (
              <View key={f.label}>
                <Text style={styles.label}>{f.label}</Text>
                <TextInput style={styles.input} value={f.value} onChangeText={f.setter} placeholder={f.placeholder} placeholderTextColor={Colors.textMuted} />
              </View>
            ))}

            <TouchableOpacity
              style={[styles.submitBtn, recordMutation.isPending && styles.submitBtnDisabled]}
              onPress={() => recordMutation.mutate()}
              disabled={recordMutation.isPending}
            >
              <Text style={styles.submitBtnText}>{recordMutation.isPending ? 'Submitting...' : 'Submit Record'}</Text>
            </TouchableOpacity>
          </ScrollView>
        </View>
      </Modal>
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 40 },
  btnGrid: { flexDirection: 'row', gap: Spacing.md, marginBottom: Spacing.lg },
  bigBtn: { flex: 1, borderWidth: 1.5, borderRadius: Radius.lg, padding: Spacing.lg, alignItems: 'center', gap: Spacing.sm, backgroundColor: Colors.card, ...Shadow.sm },
  bigBtnText: { fontSize: 13, fontWeight: '700', textAlign: 'center' },
  checkRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: Spacing.md },
  border: { borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: Colors.border },
  checkNin: { fontSize: 14, fontWeight: '600', color: Colors.dark },
  checkMeta: { fontSize: 11, color: Colors.textMuted, marginTop: 2 },
  empty: { padding: Spacing.lg, color: Colors.textMuted, textAlign: 'center' },
  modal: { flex: 1, backgroundColor: Colors.background },
  modalHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: Spacing.lg, backgroundColor: Colors.navy },
  modalTitle: { fontSize: 17, fontWeight: '700', color: '#fff' },
  modalClose: { color: Colors.primary, fontSize: 15, fontWeight: '600' },
  modalContent: { padding: Spacing.lg, paddingBottom: 40 },
  label: { fontSize: 12, fontWeight: '600', color: Colors.textSecondary, marginBottom: Spacing.sm, textTransform: 'uppercase', letterSpacing: 0.5, marginTop: Spacing.md },
  input: { backgroundColor: Colors.card, borderRadius: Radius.md, borderWidth: 1, borderColor: Colors.border, padding: Spacing.md, fontSize: 15, color: Colors.dark, ...Shadow.sm },
  radioRow: { flexDirection: 'row', alignItems: 'center', gap: Spacing.md, padding: Spacing.md, borderRadius: Radius.md, borderWidth: 1, borderColor: Colors.border, marginBottom: Spacing.sm, backgroundColor: Colors.card },
  radioActive: { borderColor: Colors.police, backgroundColor: Colors.policeBg },
  radioCircle: { width: 18, height: 18, borderRadius: 9, borderWidth: 2, borderColor: Colors.border },
  radioCircleActive: { borderColor: Colors.police, backgroundColor: Colors.police },
  radioText: { fontSize: 14, color: Colors.dark, fontWeight: '500' },
  submitBtn: { backgroundColor: Colors.navy, borderRadius: Radius.lg, padding: Spacing.md, alignItems: 'center', marginTop: Spacing.xl },
  submitBtnDisabled: { opacity: 0.5 },
  submitBtnText: { color: '#fff', fontWeight: '700', fontSize: 15 },
  resultBox: { backgroundColor: Colors.card, borderRadius: Radius.md, padding: Spacing.md, marginTop: Spacing.lg },
  resultTitle: { fontSize: 13, fontWeight: '700', color: Colors.dark, marginBottom: Spacing.sm },
  resultText: { fontSize: 11, color: Colors.textSecondary, fontFamily: 'monospace' },
})
