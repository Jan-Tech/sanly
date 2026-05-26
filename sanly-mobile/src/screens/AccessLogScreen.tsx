import React, { useState } from 'react'
import { View, Text, FlatList, TouchableOpacity, StyleSheet, TextInput } from 'react-native'
import { useQuery } from '@tanstack/react-query'
import { Shield, Building2 } from 'lucide-react-native'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { getAccessLogs } from '../api/bridge'
import { t } from '../i18n'
import { LoadingView } from '../components/LoadingView'
import { EmptyState } from '../components/EmptyState'
import { StatusBadge } from '../components/StatusBadge'
import { Colors, Radius, Spacing } from '../constants/theme'
import { formatDateTime } from '../utils/formatters'
import type { ExchangeLog } from '../types'

export function AccessLogScreen() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()
  const [filter, setFilter] = useState('')

  const { data, isLoading } = useQuery({
    queryKey: ['access-logs', nationalId],
    queryFn: () => getAccessLogs(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  if (isLoading) return <LoadingView message={t(lang, 'loading')} />

  const logs = (data ?? []).filter(l =>
    !filter || l.requestingInstitutionCode.toLowerCase().includes(filter.toLowerCase()) || l.dataType.toLowerCase().includes(filter.toLowerCase())
  )

  return (
    <View style={styles.flex}>
      <View style={styles.infoBar}>
        <Shield size={14} color={Colors.primary} />
        <Text style={styles.infoText} numberOfLines={2}>{t(lang, 'accessLogSubtitle')}</Text>
      </View>

      <TextInput
        style={styles.search}
        value={filter}
        onChangeText={setFilter}
        placeholder="Filter by institution or data type..."
        placeholderTextColor={Colors.textMuted}
      />

      <FlatList
        data={logs}
        keyExtractor={l => l.id}
        renderItem={({ item }) => <LogItem log={item} lang={lang} />}
        ListEmptyComponent={<EmptyState message={t(lang, 'noLogs')} />}
        contentContainerStyle={logs.length === 0 ? styles.emptyContent : undefined}
      />
    </View>
  )
}

function LogItem({ log, lang }: { log: ExchangeLog; lang: string }) {
  return (
    <View style={styles.item}>
      <View style={styles.iconWrap}>
        <Building2 size={16} color={Colors.textSecondary} />
      </View>
      <View style={styles.info}>
        <Text style={styles.institution}>{log.requestingInstitutionCode}</Text>
        <Text style={styles.dataType}>{log.dataType}</Text>
        <Text style={styles.time}>{formatDateTime(log.timestamp, lang as any)}</Text>
      </View>
      <StatusBadge label={log.success ? t(lang as any, 'success') : t(lang as any, 'denied')} status={log.success ? 'ACTIVE' : 'REVOKED'} size="sm" />
    </View>
  )
}

const styles = StyleSheet.create({
  flex: { flex: 1, backgroundColor: Colors.background },
  infoBar: { flexDirection: 'row', alignItems: 'flex-start', gap: 8, padding: Spacing.lg, backgroundColor: Colors.primaryBg, borderBottomWidth: StyleSheet.hairlineWidth, borderBottomColor: Colors.border },
  infoText: { flex: 1, fontSize: 12, color: Colors.primaryDark, lineHeight: 16 },
  search: { backgroundColor: Colors.card, margin: Spacing.md, borderRadius: Radius.md, paddingHorizontal: Spacing.md, paddingVertical: 10, fontSize: 14, color: Colors.text, borderWidth: 1, borderColor: Colors.border },
  item: { flexDirection: 'row', alignItems: 'center', padding: Spacing.lg, borderBottomWidth: StyleSheet.hairlineWidth, borderBottomColor: Colors.border, backgroundColor: Colors.card },
  iconWrap: { width: 32, height: 32, borderRadius: 8, backgroundColor: Colors.borderLight, justifyContent: 'center', alignItems: 'center', marginRight: Spacing.md },
  info: { flex: 1 },
  institution: { fontSize: 13, fontWeight: '600', color: Colors.dark },
  dataType: { fontSize: 12, color: Colors.textSecondary },
  time: { fontSize: 11, color: Colors.textMuted },
  emptyContent: { flex: 1 },
})
