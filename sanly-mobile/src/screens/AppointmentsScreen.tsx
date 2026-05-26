import React from 'react'
import { ScrollView, View, Text, TouchableOpacity, StyleSheet } from 'react-native'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { CalendarDays } from 'lucide-react-native'
import { useLangStore } from '../store/langStore'
import { getMyAppointments, cancelAppointment } from '../api/appointments'
import { t } from '../i18n'
import { LoadingView } from '../components/LoadingView'
import { SectionCard } from '../components/SectionCard'
import { InfoRow } from '../components/InfoRow'
import { StatusBadge } from '../components/StatusBadge'
import { EmptyState } from '../components/EmptyState'
import { Colors, Radius, Spacing } from '../constants/theme'
import { formatDate } from '../utils/formatters'

export function AppointmentsScreen() {
  const { lang } = useLangStore()
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['appointments'],
    queryFn: () => getMyAppointments(0, 50),
    retry: false,
  })

  const cancelMutation = useMutation({
    mutationFn: ({ code, reason }: { code: string; reason: string }) => cancelAppointment(code, reason),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['appointments'] }),
  })

  if (isLoading) return <LoadingView message={t(lang, 'loading')} />
  const appointments = data?.content ?? []

  const upcoming = appointments.filter(a => ['BOOKED', 'CONFIRMED'].includes(a.status))
  const past = appointments.filter(a => !['BOOKED', 'CONFIRMED'].includes(a.status))

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      {appointments.length === 0 && <EmptyState message={t(lang, 'noAppointments')} icon={<CalendarDays size={24} color={Colors.textMuted} />} />}

      {upcoming.length > 0 && (
        <SectionCard title="Upcoming">
          {upcoming.map((a, i) => (
            <View key={a.appointmentId} style={[styles.apptRow, i > 0 && styles.borderTop]}>
              <View style={styles.apptInfo}>
                <Text style={styles.apptName}>{a.officeName}</Text>
                <Text style={styles.apptService}>{a.serviceName}</Text>
                <Text style={styles.apptDate}>{formatDate(a.appointmentDate, lang)} · {a.slotTime}</Text>
              </View>
              <View style={styles.apptRight}>
                <StatusBadge label={a.status} status={a.status} size="sm" />
                {['BOOKED', 'CONFIRMED'].includes(a.status) && (
                  <TouchableOpacity
                    style={styles.cancelBtn}
                    onPress={() => cancelMutation.mutate({ code: a.appointmentCode, reason: 'Cancelled by citizen' })}
                  >
                    <Text style={styles.cancelText}>{t(lang, 'cancelAppointment')}</Text>
                  </TouchableOpacity>
                )}
              </View>
            </View>
          ))}
        </SectionCard>
      )}

      {past.length > 0 && (
        <SectionCard title="History">
          {past.map((a, i) => (
            <InfoRow key={a.appointmentId} label={a.officeName} value={<StatusBadge label={a.status} status={a.status} size="sm" />} last={i === past.length - 1} />
          ))}
        </SectionCard>
      )}
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 32 },
  apptRow: { paddingVertical: 12, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' },
  borderTop: { borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: Colors.border },
  apptInfo: { flex: 1 },
  apptName: { fontSize: 14, fontWeight: '600', color: Colors.dark },
  apptService: { fontSize: 12, color: Colors.textSecondary, marginTop: 2 },
  apptDate: { fontSize: 11, color: Colors.textMuted, marginTop: 2 },
  apptRight: { alignItems: 'flex-end', gap: 6 },
  cancelBtn: { borderWidth: 1, borderColor: Colors.error, borderRadius: Radius.sm, paddingHorizontal: 8, paddingVertical: 3 },
  cancelText: { fontSize: 11, color: Colors.error, fontWeight: '600' },
})
