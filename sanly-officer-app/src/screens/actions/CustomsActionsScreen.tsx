import React, { useState } from 'react'
import { View, Text, ScrollView, TextInput, TouchableOpacity, StyleSheet, Alert, FlatList } from 'react-native'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getDeclarationByCode, getPendingDeclarations, recordInspection } from '../../api/customs'
import { SectionCard } from '../../components/SectionCard'
import { StatusBadge } from '../../components/StatusBadge'
import { LoadingView } from '../../components/LoadingView'
import { Colors, Spacing, Radius } from '../../constants/theme'
import { formatDate, formatMoney, maskNin } from '../../utils/formatters'

export function CustomsActionsScreen() {
  const qc = useQueryClient()
  const [code, setCode] = useState('')
  const [declaration, setDeclaration] = useState<any>(null)
  const [lookupLoading, setLookupLoading] = useState(false)
  const [inspNotes, setInspNotes] = useState('')

  const { data: pending, isLoading } = useQuery({
    queryKey: ['pending-declarations'],
    queryFn: getPendingDeclarations,
    retry: false,
  })

  const clearMutation = useMutation({
    mutationFn: ({ id, notes }: { id: string; notes: string }) => recordInspection(id, notes, 'CLEAR'),
    onSuccess: (updated) => {
      setDeclaration(updated)
      qc.invalidateQueries({ queryKey: ['pending-declarations'] })
      Alert.alert('Cleared', 'Declaration cleared for entry.')
    },
    onError: (e: any) => Alert.alert('Error', e?.response?.data?.message ?? 'Failed'),
  })

  const holdMutation = useMutation({
    mutationFn: ({ id, notes }: { id: string; notes: string }) => recordInspection(id, notes, 'HOLD'),
    onSuccess: (updated) => {
      setDeclaration(updated)
      qc.invalidateQueries({ queryKey: ['pending-declarations'] })
      Alert.alert('Held', 'Declaration placed on hold.')
    },
    onError: (e: any) => Alert.alert('Error', e?.response?.data?.message ?? 'Failed'),
  })

  async function handleLookup() {
    if (!code.trim()) return
    setLookupLoading(true)
    setDeclaration(null)
    try {
      const res = await getDeclarationByCode(code.trim())
      setDeclaration(res)
    } catch {
      Alert.alert('Not Found', 'No declaration found for this code.')
    } finally {
      setLookupLoading(false)
    }
  }

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      {/* Declaration Lookup */}
      <SectionCard title="Declaration Lookup">
        <View style={styles.lookupRow}>
          <TextInput style={styles.lookupInput} value={code} onChangeText={setCode} placeholder="Declaration code..." placeholderTextColor={Colors.textMuted} autoCapitalize="characters" returnKeyType="search" onSubmitEditing={handleLookup} />
          <TouchableOpacity style={styles.lookupBtn} onPress={handleLookup} disabled={lookupLoading}>
            <Text style={styles.lookupBtnText}>{lookupLoading ? '...' : 'Lookup'}</Text>
          </TouchableOpacity>
        </View>
        {declaration && (
          <View style={styles.declDetail}>
            {[
              ['Code', declaration.declarationCode],
              ['Declarant NIN', maskNin(declaration.declarantNin)],
              ['Port of Entry', declaration.portOfEntry],
              ['Goods', declaration.goodsDescription],
              ['Total Value', formatMoney(declaration.totalValue, declaration.currency)],
              ['Declared Duties', formatMoney(declaration.declaredDuties)],
            ].map(([l, v]) => (
              <View key={l} style={styles.detailRow}>
                <Text style={styles.detailLabel}>{l}</Text>
                <Text style={styles.detailValue} numberOfLines={2}>{v}</Text>
              </View>
            ))}
            <View style={styles.detailRow}>
              <Text style={styles.detailLabel}>Status</Text>
              <StatusBadge label={declaration.status} status={declaration.status} size="sm" />
            </View>

            {declaration.status === 'PENDING' && (
              <>
                <Text style={styles.label}>Inspection Notes</Text>
                <TextInput style={[styles.input, styles.textarea]} value={inspNotes} onChangeText={setInspNotes} placeholder="Describe inspection findings..." placeholderTextColor={Colors.textMuted} multiline numberOfLines={3} />
                <View style={styles.actionRow}>
                  <TouchableOpacity
                    style={[styles.actionBtn, styles.clearBtn, (clearMutation.isPending) && styles.disabledBtn]}
                    onPress={() => clearMutation.mutate({ id: declaration.declarationId, notes: inspNotes })}
                    disabled={clearMutation.isPending}
                  >
                    <Text style={styles.actionBtnText}>✓ Clear</Text>
                  </TouchableOpacity>
                  <TouchableOpacity
                    style={[styles.actionBtn, styles.holdBtn, holdMutation.isPending && styles.disabledBtn]}
                    onPress={() => holdMutation.mutate({ id: declaration.declarationId, notes: inspNotes })}
                    disabled={holdMutation.isPending}
                  >
                    <Text style={styles.actionBtnText}>⏸ Hold</Text>
                  </TouchableOpacity>
                </View>
              </>
            )}
          </View>
        )}
      </SectionCard>

      {/* Pending Queue */}
      <SectionCard title={`Pending Declarations (${pending?.length ?? 0})`}>
        {isLoading ? (
          <LoadingView />
        ) : !pending?.length ? (
          <Text style={styles.empty}>No pending declarations</Text>
        ) : (
          pending.slice(0, 10).map((d, i) => (
            <TouchableOpacity
              key={d.declarationId}
              style={[styles.pendingRow, i > 0 && styles.border]}
              onPress={() => { setCode(d.declarationCode); setDeclaration(d) }}
            >
              <View style={{ flex: 1 }}>
                <Text style={styles.pendingCode}>{d.declarationCode}</Text>
                <Text style={styles.pendingMeta}>{d.portOfEntry} · {formatDate(d.submittedAt)}</Text>
              </View>
              <StatusBadge label={d.status} status={d.status} size="sm" />
            </TouchableOpacity>
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
  lookupBtn: { backgroundColor: Colors.customs, borderRadius: Radius.sm, paddingHorizontal: Spacing.lg, justifyContent: 'center' },
  lookupBtnText: { color: '#fff', fontWeight: '700', fontSize: 13 },
  declDetail: { borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: Colors.border, padding: Spacing.md, gap: 2 },
  detailRow: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 5 },
  detailLabel: { fontSize: 12, color: Colors.textSecondary, flex: 1 },
  detailValue: { fontSize: 12, fontWeight: '500', color: Colors.dark, flex: 2, textAlign: 'right' },
  label: { fontSize: 12, fontWeight: '600', color: Colors.textSecondary, marginTop: Spacing.md, marginBottom: Spacing.sm, textTransform: 'uppercase', letterSpacing: 0.5 },
  input: { backgroundColor: Colors.background, borderRadius: Radius.sm, borderWidth: 1, borderColor: Colors.border, padding: Spacing.md, fontSize: 14, color: Colors.dark },
  textarea: { height: 80, textAlignVertical: 'top' },
  actionRow: { flexDirection: 'row', gap: Spacing.md, marginTop: Spacing.md },
  actionBtn: { flex: 1, borderRadius: Radius.md, padding: Spacing.md, alignItems: 'center' },
  clearBtn: { backgroundColor: Colors.success },
  holdBtn: { backgroundColor: Colors.warning },
  disabledBtn: { opacity: 0.5 },
  actionBtnText: { color: '#fff', fontWeight: '700', fontSize: 14 },
  pendingRow: { flexDirection: 'row', alignItems: 'center', padding: Spacing.md },
  border: { borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: Colors.border },
  pendingCode: { fontSize: 13, fontWeight: '600', color: Colors.dark },
  pendingMeta: { fontSize: 11, color: Colors.textMuted, marginTop: 2 },
  empty: { padding: Spacing.lg, color: Colors.textMuted, textAlign: 'center' },
})
