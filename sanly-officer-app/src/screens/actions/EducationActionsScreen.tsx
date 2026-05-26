import React, { useState } from 'react'
import { View, Text, ScrollView, TextInput, TouchableOpacity, Modal, StyleSheet, Alert, FlatList } from 'react-native'
import { useMutation, useQuery } from '@tanstack/react-query'
import { verifyDiploma, getPendingIntake, issueDiploma } from '../../api/education'
import { SectionCard } from '../../components/SectionCard'
import { StatusBadge } from '../../components/StatusBadge'
import { LoadingView } from '../../components/LoadingView'
import { Colors, Spacing, Radius } from '../../constants/theme'
import { formatDate } from '../../utils/formatters'

export function EducationActionsScreen() {
  const [diplomaCode, setDiplomaCode] = useState('')
  const [diploma, setDiploma] = useState<any>(null)
  const [diplomaLoading, setDiplomaLoading] = useState(false)
  const [issueModal, setIssueModal] = useState(false)
  const [issue, setIssue] = useState({ enrollmentId: '', degree: '', field: '', year: new Date().getFullYear().toString(), gpa: '' })

  const { data: pendingIntake, isLoading } = useQuery({
    queryKey: ['pending-intake'],
    queryFn: getPendingIntake,
    retry: false,
  })

  const issueMutation = useMutation({
    mutationFn: () => issueDiploma({
      enrollmentId: issue.enrollmentId,
      degree: issue.degree,
      field: issue.field,
      graduationYear: parseInt(issue.year),
      gpa: issue.gpa ? parseFloat(issue.gpa) : undefined,
    }),
    onSuccess: () => {
      Alert.alert('Issued', 'Diploma issued successfully.')
      setIssueModal(false)
      setIssue({ enrollmentId: '', degree: '', field: '', year: new Date().getFullYear().toString(), gpa: '' })
    },
    onError: (e: any) => Alert.alert('Error', e?.response?.data?.message ?? 'Failed'),
  })

  async function lookupDiploma() {
    if (!diplomaCode.trim()) return
    setDiplomaLoading(true)
    setDiploma(null)
    try {
      setDiploma(await verifyDiploma(diplomaCode.trim()))
    } catch {
      Alert.alert('Not Found', 'No diploma found for this code.')
    } finally {
      setDiplomaLoading(false)
    }
  }

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      {/* Diploma verification */}
      <SectionCard title="Diploma Verification">
        <View style={styles.lookupRow}>
          <TextInput style={styles.lookupInput} value={diplomaCode} onChangeText={setDiplomaCode} placeholder="TM-DIP-YYYYNNNNNN" placeholderTextColor={Colors.textMuted} autoCapitalize="characters" returnKeyType="search" onSubmitEditing={lookupDiploma} />
          <TouchableOpacity style={styles.lookupBtn} onPress={lookupDiploma} disabled={diplomaLoading}>
            <Text style={styles.lookupBtnText}>{diplomaLoading ? '...' : 'Verify'}</Text>
          </TouchableOpacity>
        </View>
        {diploma && (
          <View style={styles.resultBody}>
            {[
              ['Holder', diploma.holderName],
              ['Institution', diploma.institutionName],
              ['Degree', diploma.degree],
              ['Field', diploma.field],
              ['Year', String(diploma.graduationYear)],
              ['GPA', diploma.gpa ? String(diploma.gpa) : '—'],
            ].map(([l, v]) => (
              <View key={l} style={styles.row}>
                <Text style={styles.rowLabel}>{l}</Text>
                <Text style={styles.rowValue}>{v}</Text>
              </View>
            ))}
            <View style={styles.row}>
              <Text style={styles.rowLabel}>Status</Text>
              <StatusBadge label={diploma.status} status={diploma.status} size="sm" />
            </View>
          </View>
        )}
      </SectionCard>

      {/* Issue Diploma button */}
      <TouchableOpacity style={styles.issueBtn} onPress={() => setIssueModal(true)}>
        <Text style={styles.issueBtnText}>+ Issue Diploma</Text>
      </TouchableOpacity>

      {/* Pending intake queue */}
      <SectionCard title={`Pending School Intake (${pendingIntake?.length ?? 0})`}>
        {isLoading ? (
          <LoadingView />
        ) : !pendingIntake?.length ? (
          <Text style={styles.empty}>No pending intake</Text>
        ) : (
          pendingIntake.slice(0, 10).map((e, i) => (
            <View key={e.enrollmentId} style={[styles.intakeRow, i > 0 && styles.border]}>
              <View style={{ flex: 1 }}>
                <Text style={styles.intakeNin}>{e.nationalId}</Text>
                <Text style={styles.intakeMeta}>{e.institutionName} · {e.program}</Text>
                <Text style={styles.intakeMeta}>{formatDate(e.enrolledAt)}</Text>
              </View>
              <StatusBadge label={e.status} status={e.status} size="sm" />
            </View>
          ))
        )}
      </SectionCard>

      {/* Issue Diploma Modal */}
      <Modal visible={issueModal} animationType="slide" presentationStyle="pageSheet">
        <View style={styles.modal}>
          <View style={styles.modalHeader}>
            <Text style={styles.modalTitle}>Issue Diploma</Text>
            <TouchableOpacity onPress={() => setIssueModal(false)}><Text style={styles.modalClose}>Cancel</Text></TouchableOpacity>
          </View>
          <ScrollView contentContainerStyle={styles.modalContent}>
            {[
              { label: 'Enrollment ID', key: 'enrollmentId', placeholder: 'UUID or enrollment code' },
              { label: 'Degree', key: 'degree', placeholder: 'e.g. Bachelor of Science' },
              { label: 'Field of Study', key: 'field', placeholder: 'e.g. Computer Engineering' },
              { label: 'Graduation Year', key: 'year', placeholder: '2024' },
              { label: 'GPA (optional)', key: 'gpa', placeholder: '3.85' },
            ].map(f => (
              <View key={f.key}>
                <Text style={styles.label}>{f.label}</Text>
                <TextInput style={styles.input} value={(issue as any)[f.key]} onChangeText={(v: string) => setIssue(p => ({ ...p, [f.key]: v }))} placeholder={f.placeholder} placeholderTextColor={Colors.textMuted} keyboardType={['year', 'gpa'].includes(f.key) ? 'decimal-pad' : 'default'} />
              </View>
            ))}
            <TouchableOpacity style={[styles.submitBtn, issueMutation.isPending && styles.disabled]} onPress={() => issueMutation.mutate()} disabled={issueMutation.isPending}>
              <Text style={styles.submitText}>{issueMutation.isPending ? 'Issuing...' : 'Issue Diploma'}</Text>
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
  lookupRow: { flexDirection: 'row', gap: Spacing.sm, padding: Spacing.md },
  lookupInput: { flex: 1, backgroundColor: Colors.background, borderRadius: Radius.sm, borderWidth: 1, borderColor: Colors.border, padding: Spacing.md, fontSize: 14, color: Colors.dark },
  lookupBtn: { backgroundColor: Colors.education, borderRadius: Radius.sm, paddingHorizontal: Spacing.lg, justifyContent: 'center' },
  lookupBtnText: { color: '#fff', fontWeight: '700', fontSize: 13 },
  resultBody: { borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: Colors.border, padding: Spacing.md, gap: 2 },
  row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 5 },
  rowLabel: { fontSize: 12, color: Colors.textSecondary },
  rowValue: { fontSize: 12, fontWeight: '500', color: Colors.dark },
  issueBtn: { backgroundColor: Colors.education, borderRadius: Radius.lg, padding: Spacing.md, alignItems: 'center', marginBottom: Spacing.lg },
  issueBtnText: { color: '#fff', fontWeight: '700', fontSize: 15 },
  intakeRow: { flexDirection: 'row', alignItems: 'center', padding: Spacing.md },
  border: { borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: Colors.border },
  intakeNin: { fontSize: 13, fontWeight: '600', color: Colors.dark },
  intakeMeta: { fontSize: 11, color: Colors.textMuted, marginTop: 1 },
  empty: { padding: Spacing.lg, color: Colors.textMuted, textAlign: 'center' },
  modal: { flex: 1, backgroundColor: Colors.background },
  modalHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: Spacing.lg, backgroundColor: Colors.navy },
  modalTitle: { fontSize: 17, fontWeight: '700', color: '#fff' },
  modalClose: { color: Colors.primary, fontSize: 15, fontWeight: '600' },
  modalContent: { padding: Spacing.lg, paddingBottom: 40 },
  label: { fontSize: 12, fontWeight: '600', color: Colors.textSecondary, marginBottom: Spacing.sm, textTransform: 'uppercase', letterSpacing: 0.5, marginTop: Spacing.md },
  input: { backgroundColor: Colors.card, borderRadius: Radius.md, borderWidth: 1, borderColor: Colors.border, padding: Spacing.md, fontSize: 15, color: Colors.dark },
  submitBtn: { backgroundColor: Colors.education, borderRadius: Radius.lg, padding: Spacing.md, alignItems: 'center', marginTop: Spacing.xl },
  disabled: { opacity: 0.5 },
  submitText: { color: '#fff', fontWeight: '700', fontSize: 15 },
})
