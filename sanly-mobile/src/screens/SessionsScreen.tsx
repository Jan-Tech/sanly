import React from 'react'
import { View, Text, TouchableOpacity, FlatList, StyleSheet, Alert } from 'react-native'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Smartphone, AlertCircle } from 'lucide-react-native'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { listSessions, revokeSession, revokeOtherSessions } from '../api/registry'
import { t } from '../i18n'
import { LoadingView } from '../components/LoadingView'
import { Colors, Radius, Spacing, Shadow } from '../constants/theme'
import { formatDateTime } from '../utils/formatters'

export function SessionsScreen() {
  const { nationalId, sessionId } = useAuthStore()
  const { lang } = useLangStore()
  const queryClient = useQueryClient()

  const { data: sessions, isLoading } = useQuery({
    queryKey: ['sessions'],
    queryFn: () => listSessions(sessionId ?? undefined),
    retry: false,
  })

  const revokeMutation = useMutation({
    mutationFn: revokeSession,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['sessions'] }),
  })

  const revokeOthersMutation = useMutation({
    mutationFn: () => revokeOtherSessions(sessionId!),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['sessions'] }),
  })

  if (isLoading) return <LoadingView message={t(lang, 'loading')} />

  const others = (sessions ?? []).filter(s => s.sessionId !== sessionId)

  return (
    <View style={styles.flex}>
      {others.length > 0 && (
        <View style={styles.header}>
          <Text style={styles.headerText}>{others.length} other device(s) signed in</Text>
          <TouchableOpacity
            style={styles.revokeAllBtn}
            onPress={() => Alert.alert(
              'Sign Out All',
              'Sign out from all other devices?',
              [
                { text: t(lang, 'cancel'), style: 'cancel' },
                { text: 'Sign Out All', style: 'destructive', onPress: () => revokeOthersMutation.mutate() },
              ]
            )}
          >
            <AlertCircle size={14} color={Colors.error} />
            <Text style={styles.revokeAllText}>{t(lang, 'revokeSession')} All</Text>
          </TouchableOpacity>
        </View>
      )}
      <FlatList
        data={sessions ?? []}
        keyExtractor={s => s.sessionId}
        contentContainerStyle={styles.list}
        renderItem={({ item: s }) => (
          <View style={styles.card}>
            <View style={styles.cardLeft}>
              <View style={[styles.icon, s.sessionId === sessionId && styles.iconActive]}>
                <Smartphone size={18} color={s.sessionId === sessionId ? Colors.primary : Colors.textSecondary} />
              </View>
              <View style={styles.info}>
                <View style={styles.labelRow}>
                  <Text style={styles.device}>{s.userAgent?.split('/')[0] ?? 'Device'}</Text>
                  {s.sessionId === sessionId && (
                    <View style={styles.currentChip}>
                      <Text style={styles.currentText}>{t(lang, 'currentSession')}</Text>
                    </View>
                  )}
                </View>
                <Text style={styles.ip}>{s.ipAddress}</Text>
                <Text style={styles.time}>{t(lang, 'lastSeen')}: {formatDateTime(s.lastSeenAt, lang)}</Text>
              </View>
            </View>
            {s.sessionId !== sessionId && (
              <TouchableOpacity
                style={styles.revokeBtn}
                onPress={() => revokeMutation.mutate(s.sessionId)}
              >
                <Text style={styles.revokeBtnText}>Sign Out</Text>
              </TouchableOpacity>
            )}
          </View>
        )}
      />
    </View>
  )
}

const styles = StyleSheet.create({
  flex: { flex: 1, backgroundColor: Colors.background },
  header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: Spacing.lg, backgroundColor: Colors.warningBg, borderBottomWidth: StyleSheet.hairlineWidth, borderBottomColor: Colors.border },
  headerText: { fontSize: 13, color: '#B45309' },
  revokeAllBtn: { flexDirection: 'row', alignItems: 'center', gap: 4 },
  revokeAllText: { fontSize: 12, color: Colors.error, fontWeight: '600' },
  list: { padding: Spacing.lg, gap: Spacing.md, paddingBottom: 32 },
  card: { backgroundColor: Colors.card, borderRadius: Radius.lg, padding: Spacing.lg, flexDirection: 'row', alignItems: 'center', ...Shadow.sm },
  cardLeft: { flex: 1, flexDirection: 'row', alignItems: 'flex-start', gap: Spacing.md },
  icon: { width: 36, height: 36, borderRadius: Radius.md, backgroundColor: Colors.borderLight, justifyContent: 'center', alignItems: 'center' },
  iconActive: { backgroundColor: Colors.primaryBg },
  info: { flex: 1 },
  labelRow: { flexDirection: 'row', alignItems: 'center', gap: 8, flexWrap: 'wrap' },
  device: { fontSize: 14, fontWeight: '600', color: Colors.dark },
  currentChip: { backgroundColor: Colors.successBg, borderRadius: Radius.full, paddingHorizontal: 6, paddingVertical: 2 },
  currentText: { fontSize: 10, fontWeight: '700', color: Colors.success },
  ip: { fontSize: 12, color: Colors.textMuted, marginTop: 2, fontVariant: ['tabular-nums'] },
  time: { fontSize: 11, color: Colors.textMuted, marginTop: 2 },
  revokeBtn: { borderWidth: 1, borderColor: Colors.error, borderRadius: Radius.sm, paddingHorizontal: 10, paddingVertical: 5 },
  revokeBtnText: { fontSize: 12, color: Colors.error, fontWeight: '600' },
})
