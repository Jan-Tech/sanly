import React, { useState } from 'react'
import { View, Text, ScrollView, TouchableOpacity, StyleSheet, Switch, RefreshControl } from 'react-native'
import { LinearGradient } from 'expo-linear-gradient'
import { useNavigation } from '@react-navigation/native'
import type { BottomTabNavigationProp } from '@react-navigation/bottom-tabs'
import { useQuery } from '@tanstack/react-query'
import {
  UserCheck, QrCode, Search, FileText,
  Activity, AlertTriangle, Calendar,
} from 'lucide-react-native'
import { useAuthStore } from '../../store/authStore'
import { useLangStore } from '../../store/langStore'
import { useRole } from '../../hooks/useRole'
import { useOfflineQueueStore } from '../../store/offlineQueueStore'
import { getScanHistory } from '../../hooks/useScanner'
import { OfficerCard } from '../../components/OfficerCard'
import { ActionButton } from '../../components/ActionButton'
import { scanCodeLabels } from '../../utils/codeParser'
import { timeAgo } from '../../utils/formatters'
import { t } from '../../i18n'
import { Colors, Spacing, Radius, Shadow } from '../../constants/theme'
import type { MainTabParamList } from '../../types'

type NavProp = BottomTabNavigationProp<MainTabParamList>

function greeting(lang: string): string {
  const hour = new Date().getHours()
  if (hour < 12) return lang === 'ru' ? 'Доброе утро' : lang === 'tk' ? 'Hoş irden' : 'Good morning'
  if (hour < 17) return lang === 'ru' ? 'Добрый день' : lang === 'tk' ? 'Günortan' : 'Good afternoon'
  return lang === 'ru' ? 'Добрый вечер' : lang === 'tk' ? 'Agşam' : 'Good evening'
}

export function DashboardScreen() {
  const navigation = useNavigation<NavProp>()
  const officer = useAuthStore(s => s.officer)
  const { lang } = useLangStore()
  const { role, colors, label } = useRole()
  const { queue } = useOfflineQueueStore()
  const [onDuty, setOnDuty] = useState(true)
  const [refreshing, setRefreshing] = useState(false)

  const { data: scanHistory, refetch } = useQuery({
    queryKey: ['scan-history'],
    queryFn: getScanHistory,
    retry: false,
  })

  async function onRefresh() {
    setRefreshing(true)
    await refetch()
    setRefreshing(false)
  }

  const recentScans = (scanHistory ?? []).slice(0, 3)

  // Role-specific quick action configs
  type QuickAction = { icon: React.ReactNode; label: string; tab: keyof MainTabParamList; color: string }
  const quickActions: QuickAction[] = [
    { icon: <QrCode size={22} color={colors.text} />, label: t(lang, 'scan'), tab: 'ScanTab', color: colors.text },
    { icon: <Search size={22} color={Colors.info} />, label: t(lang, 'search'), tab: 'SearchTab', color: Colors.info },
    { icon: <Activity size={22} color={Colors.warning} />, label: t(lang, 'actions'), tab: 'ActionsTab', color: Colors.warning },
    { icon: <Calendar size={22} color={Colors.success} />, label: t(lang, 'appointmentQueue'), tab: 'ActionsTab', color: Colors.success },
  ]

  return (
    <View style={{ flex: 1 }}>
      <LinearGradient colors={[Colors.navyDark, Colors.navy]} style={styles.header}>
        {/* Officer info */}
        {officer && <OfficerCard officer={officer} />}

        {/* Shift toggle */}
        <View style={styles.shiftRow}>
          <View style={[styles.shiftDot, { backgroundColor: onDuty ? Colors.success : Colors.textMuted }]} />
          <Text style={styles.shiftText}>{onDuty ? t(lang, 'onDuty') : t(lang, 'offDuty')}</Text>
          <Switch
            value={onDuty}
            onValueChange={setOnDuty}
            trackColor={{ false: Colors.navyMid, true: Colors.primary }}
            thumbColor="#fff"
            style={{ marginLeft: 'auto' }}
          />
        </View>

        {/* Stats row */}
        <View style={styles.statsRow}>
          {[
            { label: t(lang, 'scansCount'), value: scanHistory?.length ?? 0 },
            { label: t(lang, 'actionsCount'), value: 0 },
            { label: t(lang, 'checksCount'), value: 0 },
          ].map(s => (
            <View key={s.label} style={styles.statItem}>
              <Text style={styles.statValue}>{s.value}</Text>
              <Text style={styles.statLabel}>{s.label}</Text>
            </View>
          ))}
        </View>
      </LinearGradient>

      <ScrollView
        style={styles.scroll}
        contentContainerStyle={styles.content}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={Colors.primary} />}
      >
        {/* Offline queue alert */}
        {queue.length > 0 && (
          <View style={styles.queueAlert}>
            <AlertTriangle size={16} color={Colors.warning} />
            <Text style={styles.queueText}>
              {queue.length} {t(lang, 'offlineQueueItems')} — {t(lang, 'offline')}
            </Text>
          </View>
        )}

        {/* Quick actions */}
        <Text style={styles.sectionTitle}>{t(lang, 'quickActions')}</Text>
        <View style={styles.actionsGrid}>
          {quickActions.map(a => (
            <ActionButton
              key={a.label}
              icon={a.icon}
              label={a.label}
              color={a.color}
              onPress={() => navigation.navigate(a.tab)}
            />
          ))}
        </View>

        {/* Recent scans */}
        <Text style={styles.sectionTitle}>{t(lang, 'recentScans')}</Text>
        <View style={styles.card}>
          {recentScans.length === 0 ? (
            <Text style={styles.empty}>{t(lang, 'noRecentScans')}</Text>
          ) : (
            recentScans.map((s, i) => (
              <View key={s.id} style={[styles.scanRow, i > 0 && styles.scanBorder]}>
                <View style={styles.scanIcon}>
                  <QrCode size={14} color={Colors.primary} />
                </View>
                <View style={{ flex: 1 }}>
                  <Text style={styles.scanCode} numberOfLines={1}>{s.rawCode}</Text>
                  <Text style={styles.scanMeta}>{scanCodeLabels[s.type]} · {timeAgo(s.timestamp)}</Text>
                </View>
              </View>
            ))
          )}
        </View>

        {/* Role badge */}
        <View style={[styles.roleBanner, { backgroundColor: colors.bg }]}>
          <FileText size={16} color={colors.text} />
          <Text style={[styles.roleBannerText, { color: colors.text }]}>{label}</Text>
        </View>
      </ScrollView>
    </View>
  )
}

const styles = StyleSheet.create({
  header: { paddingTop: 56, paddingHorizontal: Spacing.xl, paddingBottom: Spacing.xl },
  shiftRow: { flexDirection: 'row', alignItems: 'center', gap: Spacing.sm, marginTop: Spacing.lg, backgroundColor: 'rgba(255,255,255,0.08)', borderRadius: Radius.md, padding: Spacing.md },
  shiftDot: { width: 8, height: 8, borderRadius: 4 },
  shiftText: { fontSize: 14, fontWeight: '600', color: '#fff' },
  statsRow: { flexDirection: 'row', marginTop: Spacing.lg, gap: Spacing.sm },
  statItem: { flex: 1, alignItems: 'center', backgroundColor: 'rgba(255,255,255,0.08)', borderRadius: Radius.md, padding: Spacing.md },
  statValue: { fontSize: 22, fontWeight: '800', color: '#fff' },
  statLabel: { fontSize: 10, color: 'rgba(255,255,255,0.6)', marginTop: 2, textTransform: 'uppercase', letterSpacing: 0.5 },
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 32 },
  queueAlert: { flexDirection: 'row', alignItems: 'center', gap: Spacing.sm, backgroundColor: Colors.warningBg, borderRadius: Radius.md, padding: Spacing.md, marginBottom: Spacing.lg },
  queueText: { fontSize: 13, color: Colors.warning, fontWeight: '500' },
  sectionTitle: { fontSize: 11, fontWeight: '700', color: Colors.textSecondary, textTransform: 'uppercase', letterSpacing: 0.8, marginBottom: Spacing.sm, marginTop: Spacing.sm },
  actionsGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: Spacing.sm, marginBottom: Spacing.lg },
  card: { backgroundColor: Colors.card, borderRadius: Radius.lg, overflow: 'hidden', ...Shadow.sm, marginBottom: Spacing.lg },
  scanRow: { flexDirection: 'row', alignItems: 'center', gap: Spacing.md, padding: Spacing.md },
  scanBorder: { borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: Colors.border },
  scanIcon: { width: 28, height: 28, borderRadius: Radius.sm, backgroundColor: Colors.primaryBg, justifyContent: 'center', alignItems: 'center' },
  scanCode: { fontSize: 13, fontWeight: '500', color: Colors.dark },
  scanMeta: { fontSize: 11, color: Colors.textMuted, marginTop: 2 },
  empty: { padding: Spacing.lg, color: Colors.textMuted, textAlign: 'center', fontSize: 13 },
  roleBanner: { flexDirection: 'row', alignItems: 'center', gap: Spacing.sm, borderRadius: Radius.md, padding: Spacing.md },
  roleBannerText: { fontSize: 13, fontWeight: '600' },
})
