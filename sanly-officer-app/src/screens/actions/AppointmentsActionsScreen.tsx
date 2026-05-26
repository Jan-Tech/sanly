import React from 'react'
import { View, Text, FlatList, TouchableOpacity, StyleSheet, Alert } from 'react-native'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getTodayAppointments, confirmArrival, completeAppointment, markNoShow } from '../../api/appointments'
import { StatusBadge } from '../../components/StatusBadge'
import { LoadingView } from '../../components/LoadingView'
import { Colors, Spacing, Radius, Shadow } from '../../constants/theme'
import { formatDate } from '../../utils/formatters'

export function AppointmentsActionsScreen() {
  const qc = useQueryClient()

  const { data: appointments, isLoading } = useQuery({
    queryKey: ['today-appointments'],
    queryFn: () => getTodayAppointments(),
    retry: false,
    refetchInterval: 60_000,
  })

  const confirmMutation = useMutation({
    mutationFn: (id: string) => confirmArrival(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['today-appointments'] }),
    onError: (e: any) => Alert.alert('Error', e?.response?.data?.message ?? 'Failed'),
  })

  const completeMutation = useMutation({
    mutationFn: (id: string) => completeAppointment(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['today-appointments'] }),
    onError: (e: any) => Alert.alert('Error', e?.response?.data?.message ?? 'Failed'),
  })

  const noShowMutation = useMutation({
    mutationFn: (id: string) => markNoShow(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['today-appointments'] }),
    onError: (e: any) => Alert.alert('Error', e?.response?.data?.message ?? 'Failed'),
  })

  if (isLoading) return <LoadingView message="Loading appointments..." />

  const total = appointments?.length ?? 0
  const completed = appointments?.filter(a => a.status === 'COMPLETED').length ?? 0
  const noShows = appointments?.filter(a => a.status === 'NO_SHOW').length ?? 0

  return (
    <View style={styles.container}>
      {/* Stats bar */}
      <View style={styles.statsBar}>
        <View style={styles.stat}><Text style={styles.statVal}>{total}</Text><Text style={styles.statLbl}>Total</Text></View>
        <View style={styles.stat}><Text style={[styles.statVal, { color: Colors.success }]}>{completed}</Text><Text style={styles.statLbl}>Done</Text></View>
        <View style={styles.stat}><Text style={[styles.statVal, { color: Colors.error }]}>{noShows}</Text><Text style={styles.statLbl}>No Show</Text></View>
        <View style={styles.stat}><Text style={[styles.statVal, { color: Colors.warning }]}>{total - completed - noShows}</Text><Text style={styles.statLbl}>Pending</Text></View>
      </View>

      <FlatList
        data={appointments ?? []}
        keyExtractor={a => a.appointmentId}
        contentContainerStyle={styles.list}
        ListEmptyComponent={<Text style={styles.empty}>No appointments today</Text>}
        renderItem={({ item: a }) => (
          <View style={styles.card}>
            <View style={styles.cardTop}>
              <View style={{ flex: 1 }}>
                <Text style={styles.citizenName}>{a.citizenName}</Text>
                <Text style={styles.service}>{a.serviceType}</Text>
                <Text style={styles.time}>{formatDate(a.scheduledAt)}</Text>
                <Text style={styles.code}>{a.appointmentCode}</Text>
              </View>
              <StatusBadge label={a.status} status={a.status} size="sm" />
            </View>
            {(a.status === 'SCHEDULED' || a.status === 'CONFIRMED') && (
              <View style={styles.actions}>
                {a.status === 'SCHEDULED' && (
                  <TouchableOpacity style={styles.confirmBtn} onPress={() => confirmMutation.mutate(a.appointmentId)}>
                    <Text style={styles.btnText}>✓ Arrived</Text>
                  </TouchableOpacity>
                )}
                {a.status === 'CONFIRMED' && (
                  <TouchableOpacity style={styles.completeBtn} onPress={() => completeMutation.mutate(a.appointmentId)}>
                    <Text style={styles.btnText}>✓ Complete</Text>
                  </TouchableOpacity>
                )}
                <TouchableOpacity style={styles.noShowBtn} onPress={() => noShowMutation.mutate(a.appointmentId)}>
                  <Text style={styles.btnText}>✕ No Show</Text>
                </TouchableOpacity>
              </View>
            )}
          </View>
        )}
      />
    </View>
  )
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  statsBar: { flexDirection: 'row', backgroundColor: Colors.navy, padding: Spacing.lg },
  stat: { flex: 1, alignItems: 'center' },
  statVal: { fontSize: 22, fontWeight: '800', color: '#fff' },
  statLbl: { fontSize: 10, color: 'rgba(255,255,255,0.6)', textTransform: 'uppercase', marginTop: 2 },
  list: { padding: Spacing.lg, gap: Spacing.md },
  card: { backgroundColor: Colors.card, borderRadius: Radius.lg, padding: Spacing.lg, ...Shadow.sm },
  cardTop: { flexDirection: 'row', alignItems: 'flex-start' },
  citizenName: { fontSize: 15, fontWeight: '700', color: Colors.dark },
  service: { fontSize: 13, color: Colors.textSecondary, marginTop: 2 },
  time: { fontSize: 12, color: Colors.textMuted, marginTop: 2 },
  code: { fontSize: 11, color: Colors.textMuted, marginTop: 2, fontVariant: ['tabular-nums'] },
  actions: { flexDirection: 'row', gap: Spacing.sm, marginTop: Spacing.md },
  confirmBtn: { flex: 1, backgroundColor: Colors.info, borderRadius: Radius.sm, padding: Spacing.sm, alignItems: 'center' },
  completeBtn: { flex: 1, backgroundColor: Colors.success, borderRadius: Radius.sm, padding: Spacing.sm, alignItems: 'center' },
  noShowBtn: { flex: 1, backgroundColor: Colors.error, borderRadius: Radius.sm, padding: Spacing.sm, alignItems: 'center' },
  btnText: { color: '#fff', fontWeight: '700', fontSize: 12 },
  empty: { textAlign: 'center', color: Colors.textMuted, marginTop: 80, fontSize: 14 },
})
