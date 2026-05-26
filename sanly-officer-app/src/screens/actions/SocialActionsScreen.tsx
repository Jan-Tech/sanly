import React, { useState } from 'react'
import { View, Text, ScrollView, TextInput, TouchableOpacity, Modal, StyleSheet, Alert } from 'react-native'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getPendingClaims, processClaim, registerUnemployment } from '../../api/social'
import { SectionCard } from '../../components/SectionCard'
import { StatusBadge } from '../../components/StatusBadge'
import { LoadingView } from '../../components/LoadingView'
import { Colors, Spacing, Radius } from '../../constants/theme'
import { formatDate, formatMoney, maskNin } from '../../utils/formatters'

export function SocialActionsScreen() {
  const qc = useQueryClient()
  const [unempModal, setUnempModal] = useState(false)
  const [unempNin, setUnempNin] = useState('')
  const [unempEmployer, setUnempEmployer] = useState('')
  const [unempReason, setUnempReason] = useState('')

  const { data: claims, isLoading } = useQuery({
    queryKey: ['pending-claims'],
    queryFn: getPendingClaims,
    retry: false,
  })

  const claimMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: 'ACTIVE' | 'REJECTED' }) => processClaim(id, status),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['pending-claims'] }),
    onError: (e: any) => Alert.alert('Error', e?.response?.data?.message ?? 'Failed'),
  })

  const unempMutation = useMutation({
    mutationFn: () => registerUnemployment({ nationalId: unempNin.trim(), lastEmployer: unempEmployer || undefined, reason: unempReason || undefined }),
    onSuccess: () => {
      Alert.alert('Registered', 'Unemployment registration successful.')
      setUnempModal(false)
      setUnempNin(''); setUnempEmployer(''); setUnempReason('')
    },
    onError: (e: any) => Alert.alert('Error', e?.response?.data?.message ?? 'Registration failed'),
  })

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      {/* Register Unemployment button */}
      <TouchableOpacity style={styles.unempBtn} onPress={() => setUnempModal(true)}>
        <Text style={styles.unempBtnText}>+ Register Unemployment</Text>
      </TouchableOpacity>

      {/* Pending claims */}
      <SectionCard title={`Pending Benefit Claims (${claims?.length ?? 0})`}>
        {isLoading ? (
          <LoadingView />
        ) : !claims?.length ? (
          <Text style={styles.empty}>No pending claims</Text>
        ) : (
          claims.map((c, i) => (
            <View key={c.claimId} style={[styles.claimRow, i > 0 && styles.border]}>
              <View style={{ flex: 1 }}>
                <Text style={styles.claimName}>{c.citizenName}</Text>
                <Text style={styles.claimMeta}>{c.benefitType.replace(/_/g, ' ')} · {formatMoney(c.monthlyAmount)}/mo</Text>
                <Text style={styles.claimDate}>Submitted {formatDate(c.submittedAt)}</Text>
              </View>
              <View style={styles.claimActions}>
                <TouchableOpacity style={styles.approveBtn} onPress={() => claimMutation.mutate({ id: c.claimId, status: 'ACTIVE' })}>
                  <Text style={styles.actText}>✓</Text>
                </TouchableOpacity>
                <TouchableOpacity style={styles.rejectBtn} onPress={() => claimMutation.mutate({ id: c.claimId, status: 'REJECTED' })}>
                  <Text style={styles.actText}>✕</Text>
                </TouchableOpacity>
              </View>
            </View>
          ))
        )}
      </SectionCard>

      {/* Unemployment modal */}
      <Modal visible={unempModal} animationType="slide" presentationStyle="pageSheet">
        <View style={styles.modal}>
          <View style={styles.modalHeader}>
            <Text style={styles.modalTitle}>Register Unemployment</Text>
            <TouchableOpacity onPress={() => setUnempModal(false)}><Text style={styles.modalClose}>Cancel</Text></TouchableOpacity>
          </View>
          <ScrollView contentContainerStyle={styles.modalContent}>
            <Text style={styles.label}>National ID</Text>
            <TextInput style={styles.input} value={unempNin} onChangeText={setUnempNin} placeholder="10-digit NIN" placeholderTextColor={Colors.textMuted} keyboardType="numeric" />
            <Text style={styles.label}>Last Employer (optional)</Text>
            <TextInput style={styles.input} value={unempEmployer} onChangeText={setUnempEmployer} placeholder="Company or organization name" placeholderTextColor={Colors.textMuted} />
            <Text style={styles.label}>Reason (optional)</Text>
            <TextInput style={[styles.input, styles.textarea]} value={unempReason} onChangeText={setUnempReason} placeholder="Reason for unemployment..." placeholderTextColor={Colors.textMuted} multiline numberOfLines={3} />
            <TouchableOpacity
              style={[styles.submitBtn, (unempMutation.isPending || !unempNin.trim()) && styles.disabled]}
              onPress={() => unempMutation.mutate()}
              disabled={unempMutation.isPending || !unempNin.trim()}
            >
              <Text style={styles.submitText}>{unempMutation.isPending ? 'Registering...' : 'Register'}</Text>
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
  unempBtn: { backgroundColor: Colors.social, borderRadius: Radius.lg, padding: Spacing.md, alignItems: 'center', marginBottom: Spacing.lg },
  unempBtnText: { color: '#fff', fontWeight: '700', fontSize: 15 },
  claimRow: { flexDirection: 'row', alignItems: 'center', padding: Spacing.md },
  border: { borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: Colors.border },
  claimName: { fontSize: 14, fontWeight: '600', color: Colors.dark },
  claimMeta: { fontSize: 12, color: Colors.textSecondary, marginTop: 2 },
  claimDate: { fontSize: 11, color: Colors.textMuted, marginTop: 1 },
  claimActions: { flexDirection: 'row', gap: Spacing.sm },
  approveBtn: { width: 32, height: 32, borderRadius: Radius.sm, backgroundColor: Colors.success, justifyContent: 'center', alignItems: 'center' },
  rejectBtn: { width: 32, height: 32, borderRadius: Radius.sm, backgroundColor: Colors.error, justifyContent: 'center', alignItems: 'center' },
  actText: { color: '#fff', fontWeight: '800', fontSize: 14 },
  empty: { padding: Spacing.lg, color: Colors.textMuted, textAlign: 'center' },
  modal: { flex: 1, backgroundColor: Colors.background },
  modalHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: Spacing.lg, backgroundColor: Colors.navy },
  modalTitle: { fontSize: 17, fontWeight: '700', color: '#fff' },
  modalClose: { color: Colors.primary, fontSize: 15, fontWeight: '600' },
  modalContent: { padding: Spacing.lg, paddingBottom: 40 },
  label: { fontSize: 12, fontWeight: '600', color: Colors.textSecondary, marginBottom: Spacing.sm, textTransform: 'uppercase', letterSpacing: 0.5, marginTop: Spacing.md },
  input: { backgroundColor: Colors.card, borderRadius: Radius.md, borderWidth: 1, borderColor: Colors.border, padding: Spacing.md, fontSize: 15, color: Colors.dark },
  textarea: { height: 80, textAlignVertical: 'top' },
  submitBtn: { backgroundColor: Colors.social, borderRadius: Radius.lg, padding: Spacing.md, alignItems: 'center', marginTop: Spacing.xl },
  disabled: { opacity: 0.5 },
  submitText: { color: '#fff', fontWeight: '700', fontSize: 15 },
})
