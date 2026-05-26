import React, { useState } from 'react'
import { View, Text, ScrollView, TextInput, TouchableOpacity, StyleSheet, Alert } from 'react-native'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { submitTestResult, getTodayRecords } from '../../api/medical'
import { SectionCard } from '../../components/SectionCard'
import { LoadingView } from '../../components/LoadingView'
import { Colors, Spacing, Radius, Shadow } from '../../constants/theme'
import { formatDateTime, formatTestType } from '../../utils/formatters'
import { useOfflineQueueStore } from '../../store/offlineQueueStore'
import type { TestType } from '../../types'

const TEST_TYPES: TestType[] = [
  'BLOOD_TEST', 'URINE_TEST', 'VISION_TEST', 'HEARING_TEST',
  'CHEST_XRAY', 'ECG', 'FULL_PHYSICAL', 'COVID_TEST', 'VACCINATION',
]

const RESULTS = ['PASS', 'FAIL', 'NORMAL', 'ABNORMAL', 'POSITIVE', 'NEGATIVE', 'PENDING']

export function MedicalActionsScreen() {
  const qc = useQueryClient()
  const { enqueue } = useOfflineQueueStore()
  const [nin, setNin] = useState('')
  const [testType, setTestType] = useState<TestType>('BLOOD_TEST')
  const [result, setResult] = useState('PASS')
  const [testDate, setTestDate] = useState(new Date().toISOString().slice(0, 10))
  const [notes, setNotes] = useState('')

  const { data: todayRecords, isLoading } = useQuery({
    queryKey: ['today-medical'],
    queryFn: getTodayRecords,
    retry: false,
  })

  const submitMutation = useMutation({
    mutationFn: () => submitTestResult({ nationalId: nin.trim(), testType, result, testDate, notes: notes || undefined }),
    onSuccess: () => {
      Alert.alert('Submitted', 'Medical record saved successfully.')
      qc.invalidateQueries({ queryKey: ['today-medical'] })
      setNin(''); setNotes('')
    },
    onError: async (e: any) => {
      // Queue for offline retry
      if (!e?.response) {
        await enqueue({
          type: 'MEDICAL_RECORD',
          endpoint: 'medical/api/v1/medical-records',
          method: 'POST',
          payload: { nationalId: nin.trim(), testType, result, testDate, notes },
          description: `Medical record for ${nin} (${formatTestType(testType)})`,
        })
        Alert.alert('Offline', 'Record queued for submission when connection is restored.')
        setNin(''); setNotes('')
      } else {
        Alert.alert('Error', e?.response?.data?.message ?? 'Submission failed')
      }
    },
  })

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      <SectionCard title="Submit Test Result">
        <View style={styles.form}>
          <Text style={styles.label}>Patient National ID</Text>
          <TextInput style={styles.input} value={nin} onChangeText={setNin} placeholder="10-digit NIN" placeholderTextColor={Colors.textMuted} keyboardType="numeric" />

          <Text style={styles.label}>Test Type</Text>
          <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.chipScroll} contentContainerStyle={styles.chipRow}>
            {TEST_TYPES.map(tt => (
              <TouchableOpacity
                key={tt}
                style={[styles.chip, testType === tt && styles.chipActive]}
                onPress={() => setTestType(tt)}
              >
                <Text style={[styles.chipText, testType === tt && styles.chipTextActive]}>{formatTestType(tt)}</Text>
              </TouchableOpacity>
            ))}
          </ScrollView>

          <Text style={styles.label}>Result</Text>
          <View style={styles.resultGrid}>
            {RESULTS.map(r => (
              <TouchableOpacity key={r} style={[styles.resultChip, result === r && styles.resultChipActive]} onPress={() => setResult(r)}>
                <Text style={[styles.resultText, result === r && styles.resultTextActive]}>{r}</Text>
              </TouchableOpacity>
            ))}
          </View>

          <Text style={styles.label}>Test Date</Text>
          <TextInput style={styles.input} value={testDate} onChangeText={setTestDate} placeholder="YYYY-MM-DD" placeholderTextColor={Colors.textMuted} />

          <Text style={styles.label}>Notes (optional)</Text>
          <TextInput style={[styles.input, styles.textarea]} value={notes} onChangeText={setNotes} placeholder="Clinical notes..." placeholderTextColor={Colors.textMuted} multiline numberOfLines={3} />

          <TouchableOpacity
            style={[styles.submitBtn, (submitMutation.isPending || !nin.trim()) && styles.disabledBtn]}
            onPress={() => submitMutation.mutate()}
            disabled={submitMutation.isPending || !nin.trim()}
          >
            <Text style={styles.submitBtnText}>{submitMutation.isPending ? 'Submitting...' : 'Submit Result'}</Text>
          </TouchableOpacity>
        </View>
      </SectionCard>

      <SectionCard title={`Today's Records (${todayRecords?.length ?? 0})`}>
        {isLoading ? (
          <LoadingView />
        ) : !todayRecords?.length ? (
          <Text style={styles.empty}>No records submitted today</Text>
        ) : (
          todayRecords.slice(0, 10).map((r, i) => (
            <View key={r.recordId} style={[styles.recordRow, i > 0 && styles.border]}>
              <Text style={styles.recordType}>{formatTestType(r.testType)}</Text>
              <Text style={styles.recordMeta}>{r.result} · {formatDateTime(r.testDate, 'en')}</Text>
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
  form: { padding: Spacing.md },
  label: { fontSize: 12, fontWeight: '600', color: Colors.textSecondary, marginBottom: Spacing.sm, textTransform: 'uppercase', letterSpacing: 0.5, marginTop: Spacing.md },
  input: { backgroundColor: Colors.background, borderRadius: Radius.sm, borderWidth: 1, borderColor: Colors.border, padding: Spacing.md, fontSize: 14, color: Colors.dark },
  textarea: { height: 80, textAlignVertical: 'top' },
  chipScroll: { marginBottom: Spacing.sm },
  chipRow: { gap: Spacing.sm, paddingBottom: 4 },
  chip: { paddingHorizontal: 12, paddingVertical: 7, borderRadius: Radius.full, borderWidth: 1, borderColor: Colors.border, backgroundColor: Colors.card },
  chipActive: { backgroundColor: Colors.medical, borderColor: Colors.medical },
  chipText: { fontSize: 12, fontWeight: '600', color: Colors.textSecondary },
  chipTextActive: { color: '#fff' },
  resultGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: Spacing.sm, marginBottom: Spacing.sm },
  resultChip: { paddingHorizontal: 12, paddingVertical: 6, borderRadius: Radius.sm, borderWidth: 1, borderColor: Colors.border, backgroundColor: Colors.card },
  resultChipActive: { backgroundColor: Colors.primary, borderColor: Colors.primary },
  resultText: { fontSize: 12, fontWeight: '600', color: Colors.textSecondary },
  resultTextActive: { color: '#fff' },
  submitBtn: { backgroundColor: Colors.medical, borderRadius: Radius.lg, padding: Spacing.md, alignItems: 'center', marginTop: Spacing.lg },
  disabledBtn: { opacity: 0.5 },
  submitBtnText: { color: '#fff', fontWeight: '700', fontSize: 15 },
  recordRow: { padding: Spacing.md },
  border: { borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: Colors.border },
  recordType: { fontSize: 14, fontWeight: '600', color: Colors.dark },
  recordMeta: { fontSize: 11, color: Colors.textMuted, marginTop: 2 },
  empty: { padding: Spacing.lg, color: Colors.textMuted, textAlign: 'center' },
})
