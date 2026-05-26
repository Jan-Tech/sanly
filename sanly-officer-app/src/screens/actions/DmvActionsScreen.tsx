import React, { useState } from 'react'
import { View, Text, ScrollView, TextInput, TouchableOpacity, Modal, StyleSheet, Alert, FlatList } from 'react-native'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { verifyLicense, getPendingApplications, approveApplication, rejectApplication } from '../../api/dmv'
import { SectionCard } from '../../components/SectionCard'
import { StatusBadge } from '../../components/StatusBadge'
import { LoadingView } from '../../components/LoadingView'
import { Colors, Spacing, Radius, Shadow } from '../../constants/theme'
import { formatDate } from '../../utils/formatters'

export function DmvActionsScreen() {
  const qc = useQueryClient()
  const [lookupNum, setLookupNum] = useState('')
  const [licenseData, setLicenseData] = useState<any>(null)
  const [lookupLoading, setLookupLoading] = useState(false)
  const [selectedApp, setSelectedApp] = useState<any>(null)
  const [rejectReason, setRejectReason] = useState('')

  const { data: applications, isLoading } = useQuery({
    queryKey: ['pending-applications'],
    queryFn: getPendingApplications,
    retry: false,
  })

  const approveMutation = useMutation({
    mutationFn: (id: string) => approveApplication(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['pending-applications'] }); setSelectedApp(null) },
    onError: (e: any) => Alert.alert('Error', e?.response?.data?.message ?? 'Failed'),
  })

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => rejectApplication(id, reason),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['pending-applications'] }); setSelectedApp(null) },
    onError: (e: any) => Alert.alert('Error', e?.response?.data?.message ?? 'Failed'),
  })

  async function handleLookup() {
    if (!lookupNum.trim()) return
    setLookupLoading(true)
    setLicenseData(null)
    try {
      const res = await verifyLicense(lookupNum.trim())
      setLicenseData(res)
    } catch (e: any) {
      Alert.alert('Not Found', 'No license found for this number.')
    } finally {
      setLookupLoading(false)
    }
  }

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      {/* License Lookup */}
      <SectionCard title="License Lookup">
        <View style={styles.lookupRow}>
          <TextInput
            style={styles.lookupInput}
            value={lookupNum}
            onChangeText={setLookupNum}
            placeholder="TM-DL-YYYYNNNNNN"
            placeholderTextColor={Colors.textMuted}
            autoCapitalize="characters"
            returnKeyType="search"
            onSubmitEditing={handleLookup}
          />
          <TouchableOpacity style={styles.lookupBtn} onPress={handleLookup} disabled={lookupLoading}>
            <Text style={styles.lookupBtnText}>{lookupLoading ? '...' : 'Lookup'}</Text>
          </TouchableOpacity>
        </View>
        {licenseData && (
          <View style={styles.licenseResult}>
            <View style={styles.licenseRow}>
              <Text style={styles.licenseLabel}>Holder</Text>
              <Text style={styles.licenseValue}>{licenseData.holderName}</Text>
            </View>
            <View style={styles.licenseRow}>
              <Text style={styles.licenseLabel}>Categories</Text>
              <Text style={styles.licenseValue}>{(licenseData.categories ?? []).join(', ')}</Text>
            </View>
            <View style={styles.licenseRow}>
              <Text style={styles.licenseLabel}>Expires</Text>
              <Text style={styles.licenseValue}>{formatDate(licenseData.expiryDate)}</Text>
            </View>
            <View style={styles.licenseRow}>
              <Text style={styles.licenseLabel}>Status</Text>
              <StatusBadge label={licenseData.status} status={licenseData.status} size="sm" />
            </View>
          </View>
        )}
      </SectionCard>

      {/* Pending Applications */}
      <SectionCard title={`Pending Applications (${applications?.length ?? 0})`}>
        {isLoading ? (
          <LoadingView />
        ) : !applications?.length ? (
          <Text style={styles.empty}>No pending applications</Text>
        ) : (
          applications.map((app, i) => (
            <TouchableOpacity
              key={app.applicationId}
              style={[styles.appRow, i > 0 && styles.border]}
              onPress={() => setSelectedApp(app)}
            >
              <View style={{ flex: 1 }}>
                <Text style={styles.appName}>{app.applicantName}</Text>
                <Text style={styles.appMeta}>{app.category} · Submitted {formatDate(app.submittedAt)}</Text>
                {app.visionCheckResult && <Text style={styles.appVision}>Vision: {app.visionCheckResult}</Text>}
              </View>
              <Text style={styles.appArrow}>›</Text>
            </TouchableOpacity>
          ))
        )}
      </SectionCard>

      {/* Application Detail Modal */}
      <Modal visible={!!selectedApp} animationType="slide" presentationStyle="pageSheet">
        {selectedApp && (
          <View style={styles.modal}>
            <View style={styles.modalHeader}>
              <Text style={styles.modalTitle}>Application Detail</Text>
              <TouchableOpacity onPress={() => setSelectedApp(null)}>
                <Text style={styles.modalClose}>Close</Text>
              </TouchableOpacity>
            </View>
            <ScrollView contentContainerStyle={styles.modalContent}>
              {[
                ['Applicant', selectedApp.applicantName],
                ['National ID', selectedApp.nationalId],
                ['Category', selectedApp.category],
                ['Submitted', formatDate(selectedApp.submittedAt)],
                ['Vision Check', selectedApp.visionCheckResult ?? 'Not available'],
              ].map(([l, v]) => (
                <View key={l} style={styles.detailRow}>
                  <Text style={styles.detailLabel}>{l}</Text>
                  <Text style={styles.detailValue}>{v}</Text>
                </View>
              ))}

              <Text style={styles.label}>Rejection Reason (if rejecting)</Text>
              <TextInput style={styles.input} value={rejectReason} onChangeText={setRejectReason} placeholder="Enter reason..." placeholderTextColor={Colors.textMuted} />

              <View style={styles.actionRow}>
                <TouchableOpacity
                  style={[styles.actionBtn, styles.approveBtn, approveMutation.isPending && styles.disabledBtn]}
                  onPress={() => approveMutation.mutate(selectedApp.applicationId)}
                  disabled={approveMutation.isPending}
                >
                  <Text style={styles.actionBtnText}>✓ Approve</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={[styles.actionBtn, styles.rejectBtn, rejectMutation.isPending && styles.disabledBtn]}
                  onPress={() => {
                    if (!rejectReason.trim()) { Alert.alert('Required', 'Enter rejection reason'); return }
                    rejectMutation.mutate({ id: selectedApp.applicationId, reason: rejectReason })
                  }}
                  disabled={rejectMutation.isPending}
                >
                  <Text style={styles.actionBtnText}>✕ Reject</Text>
                </TouchableOpacity>
              </View>
            </ScrollView>
          </View>
        )}
      </Modal>
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 40 },
  lookupRow: { flexDirection: 'row', gap: Spacing.sm, padding: Spacing.md },
  lookupInput: { flex: 1, backgroundColor: Colors.background, borderRadius: Radius.sm, borderWidth: 1, borderColor: Colors.border, padding: Spacing.md, fontSize: 14, color: Colors.dark },
  lookupBtn: { backgroundColor: Colors.dmv, borderRadius: Radius.sm, paddingHorizontal: Spacing.lg, justifyContent: 'center' },
  lookupBtnText: { color: '#fff', fontWeight: '700', fontSize: 13 },
  licenseResult: { borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: Colors.border, padding: Spacing.md, gap: Spacing.sm },
  licenseRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  licenseLabel: { fontSize: 13, color: Colors.textSecondary },
  licenseValue: { fontSize: 13, fontWeight: '500', color: Colors.dark },
  appRow: { flexDirection: 'row', alignItems: 'center', padding: Spacing.md },
  border: { borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: Colors.border },
  appName: { fontSize: 14, fontWeight: '600', color: Colors.dark },
  appMeta: { fontSize: 11, color: Colors.textMuted, marginTop: 2 },
  appVision: { fontSize: 11, color: Colors.info, marginTop: 2 },
  appArrow: { fontSize: 20, color: Colors.textMuted },
  empty: { padding: Spacing.lg, color: Colors.textMuted, textAlign: 'center' },
  modal: { flex: 1, backgroundColor: Colors.background },
  modalHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: Spacing.lg, backgroundColor: Colors.navy },
  modalTitle: { fontSize: 17, fontWeight: '700', color: '#fff' },
  modalClose: { color: Colors.primary, fontSize: 15, fontWeight: '600' },
  modalContent: { padding: Spacing.lg, paddingBottom: 40 },
  detailRow: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: Spacing.sm, borderBottomWidth: StyleSheet.hairlineWidth, borderBottomColor: Colors.border },
  detailLabel: { fontSize: 13, color: Colors.textSecondary },
  detailValue: { fontSize: 13, fontWeight: '500', color: Colors.dark, textAlign: 'right', flex: 1, marginLeft: Spacing.md },
  label: { fontSize: 12, fontWeight: '600', color: Colors.textSecondary, marginTop: Spacing.lg, marginBottom: Spacing.sm, textTransform: 'uppercase', letterSpacing: 0.5 },
  input: { backgroundColor: Colors.card, borderRadius: Radius.md, borderWidth: 1, borderColor: Colors.border, padding: Spacing.md, fontSize: 15, color: Colors.dark },
  actionRow: { flexDirection: 'row', gap: Spacing.md, marginTop: Spacing.xl },
  actionBtn: { flex: 1, borderRadius: Radius.lg, padding: Spacing.md, alignItems: 'center' },
  approveBtn: { backgroundColor: Colors.success },
  rejectBtn: { backgroundColor: Colors.error },
  disabledBtn: { opacity: 0.5 },
  actionBtnText: { color: '#fff', fontWeight: '700', fontSize: 15 },
})
