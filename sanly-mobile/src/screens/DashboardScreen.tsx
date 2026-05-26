import React from 'react'
import {
  View, Text, ScrollView, TouchableOpacity, StyleSheet, RefreshControl,
} from 'react-native'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Car, Receipt, Briefcase, Stethoscope, GraduationCap, Home,
  HeartHandshake, Truck, PiggyBank, Scale, CalendarDays, FileText,
  Shield, ArrowRight, Package, Building2,
} from 'lucide-react-native'
import { useNavigation } from '@react-navigation/native'
import type { BottomTabNavigationProp } from '@react-navigation/bottom-tabs'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { t } from '../i18n'
import { getCitizen } from '../api/registry'
import { getLicenses } from '../api/dmv'
import { getTaxpayer } from '../api/tax'
import { getBusinesses } from '../api/business'
import { getMedicalRecords } from '../api/medical'
import { getAccessLogs } from '../api/bridge'
import { getDiplomas } from '../api/education'
import { getPropertiesByOwner } from '../api/land'
import { getActiveClaims } from '../api/social'
import { getVehiclesByOwner } from '../api/vehicle'
import { getPensionAccount } from '../api/pension'
import { getMyAppointments } from '../api/appointments'
import { getFinesByCitizen } from '../api/court'
import { getMyCertificates } from '../api/documents'
import { getPendingConsents } from '../api/banking'
import { getDeclarationsByDeclarant } from '../api/customs'
import { StatusBadge } from '../components/StatusBadge'
import { Colors, Radius, Shadow, Spacing } from '../constants/theme'
import { formatDateTime } from '../utils/formatters'
import type { MainTabParamList } from '../types'

type NavProp = BottomTabNavigationProp<MainTabParamList>

function SummaryCard({
  icon, label, value, loading, onPress, color = Colors.primaryBg,
}: {
  icon: React.ReactNode; label: string; value: React.ReactNode
  loading?: boolean; onPress: () => void; color?: string
}) {
  return (
    <TouchableOpacity style={styles.card} onPress={onPress} activeOpacity={0.8}>
      <View style={[styles.cardIcon, { backgroundColor: color }]}>{icon}</View>
      <View style={styles.cardBody}>
        {loading
          ? <View style={styles.skeleton} />
          : <View style={styles.cardValue}>{value}</View>}
        <Text style={styles.cardLabel} numberOfLines={1}>{label}</Text>
      </View>
      <ArrowRight size={14} color={Colors.textMuted} />
    </TouchableOpacity>
  )
}

export function DashboardScreen() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()
  const navigation = useNavigation<NavProp>()
  const queryClient = useQueryClient()

  const opts = { enabled: !!nationalId, retry: false }

  const { data: citizen, isLoading: citizenLoading } = useQuery({ queryKey: ['citizen', nationalId], queryFn: () => getCitizen(nationalId!), ...opts, staleTime: 5 * 60_000 })
  const { data: licenses, isLoading: licLoading } = useQuery({ queryKey: ['licenses', nationalId], queryFn: () => getLicenses(nationalId!), ...opts })
  const { data: taxpayer, isLoading: taxLoading } = useQuery({ queryKey: ['taxpayer', nationalId], queryFn: () => getTaxpayer(nationalId!), ...opts })
  const { data: businesses, isLoading: busLoading } = useQuery({ queryKey: ['businesses', nationalId], queryFn: () => getBusinesses(nationalId!), ...opts })
  const { data: medical, isLoading: medLoading } = useQuery({ queryKey: ['medical', nationalId], queryFn: () => getMedicalRecords(nationalId!), ...opts })
  const { data: diplomas, isLoading: dipLoading } = useQuery({ queryKey: ['diplomas', nationalId], queryFn: () => getDiplomas(nationalId!), ...opts })
  const { data: properties, isLoading: propLoading } = useQuery({ queryKey: ['properties', nationalId], queryFn: () => getPropertiesByOwner(nationalId!), ...opts })
  const { data: benefits, isLoading: benLoading } = useQuery({ queryKey: ['benefits', nationalId], queryFn: () => getActiveClaims(nationalId!), ...opts })
  const { data: vehicles, isLoading: vehLoading } = useQuery({ queryKey: ['vehicles', nationalId], queryFn: () => getVehiclesByOwner(nationalId!), ...opts })
  const { data: pension, isLoading: penLoading } = useQuery({ queryKey: ['pension', nationalId], queryFn: () => getPensionAccount(nationalId!), ...opts })
  const { data: aptPage, isLoading: aptLoading } = useQuery({ queryKey: ['appointments-count'], queryFn: () => getMyAppointments(0, 1), ...opts })
  const { data: fines, isLoading: finesLoading } = useQuery({ queryKey: ['fines', nationalId], queryFn: () => getFinesByCitizen(nationalId!), ...opts })
  const { data: certs, isLoading: certLoading } = useQuery({ queryKey: ['certs'], queryFn: getMyCertificates, ...opts })
  const { data: consents, isLoading: consentLoading } = useQuery({ queryKey: ['consents'], queryFn: getPendingConsents, ...opts, refetchInterval: 30_000 })
  const { data: customsDecls } = useQuery({ queryKey: ['customs', nationalId], queryFn: () => getDeclarationsByDeclarant(nationalId!), ...opts })
  const { data: logs, isLoading: logsLoading } = useQuery({ queryKey: ['access-logs', nationalId], queryFn: () => getAccessLogs(nationalId!), ...opts })

  const isRefreshing = citizenLoading
  const onRefresh = () => queryClient.invalidateQueries()

  const activeLicense = licenses?.find(l => l.status === 'ACTIVE')
  const activeBusinesses = (businesses ?? []).filter(b => b.status === 'ACTIVE')
  const overdueOrPending = (fines ?? []).filter(f => ['OUTSTANDING', 'OVERDUE', 'PENDING_VERIFICATION'].includes(f.status))
  const recentLogs = (logs ?? []).slice(0, 5)

  const goServices = () => navigation.navigate('ServicesTab')

  return (
    <ScrollView
      style={styles.scroll}
      contentContainerStyle={styles.content}
      refreshControl={<RefreshControl refreshing={isRefreshing} onRefresh={onRefresh} tintColor={Colors.primary} />}
    >
      {/* Citizen header */}
      <View style={styles.heroCard}>
        <View style={styles.avatar}>
          <Text style={styles.avatarText}>{citizen?.firstName?.[0] ?? '?'}</Text>
        </View>
        <View style={styles.heroInfo}>
          {citizenLoading
            ? <View style={styles.skeletonName} />
            : <>
                <View style={styles.heroNameRow}>
                  <Text style={styles.heroName} numberOfLines={1}>
                    {t(lang, 'welcome')}, {citizen?.firstName}!
                  </Text>
                  {citizen && <StatusBadge label={t(lang, citizen.status === 'ACTIVE' ? 'active' : 'inactive')} status={citizen.status} size="sm" />}
                </View>
                <Text style={styles.heroNin}>{nationalId}</Text>
              </>
          }
        </View>
      </View>

      {/* Summary grid */}
      <Text style={styles.sectionTitle}>{t(lang, 'summaryTitle')}</Text>
      <View style={styles.grid}>
        <SummaryCard icon={<Car size={20} color={Colors.primary} />} label={t(lang, 'licenseStatus')} loading={licLoading}
          value={activeLicense ? <StatusBadge label={t(lang, 'active')} status="ACTIVE" size="sm" /> : <Text style={styles.noData}>{t(lang, 'noLicense')}</Text>}
          onPress={goServices} />
        <SummaryCard icon={<Receipt size={20} color="#059669" />} label={t(lang, 'taxStatus')} loading={taxLoading}
          color="#D1FAE5"
          value={taxpayer ? <StatusBadge label={t(lang, taxpayer.complianceStatus === 'COMPLIANT' ? 'compliant' : taxpayer.complianceStatus === 'NON_COMPLIANT' ? 'nonCompliant' : 'pending')} status={taxpayer.complianceStatus} size="sm" /> : <Text style={styles.noData}>{t(lang, 'notRegistered')}</Text>}
          onPress={goServices} />
        <SummaryCard icon={<Stethoscope size={20} color="#8B5CF6" />} label={t(lang, 'medicalCount')} loading={medLoading}
          color={Colors.purpleBg}
          value={<Text style={styles.countText}>{medical?.length ?? 0}</Text>}
          onPress={goServices} />
        <SummaryCard icon={<Briefcase size={20} color="#F59E0B" />} label={t(lang, 'businessCount')} loading={busLoading}
          color={Colors.warningBg}
          value={<Text style={styles.countText}>{activeBusinesses.length}</Text>}
          onPress={goServices} />
        <SummaryCard icon={<GraduationCap size={20} color="#3B82F6" />} label={t(lang, 'educationCount')} loading={dipLoading}
          color={Colors.infoBg}
          value={<Text style={styles.countText}>{(diplomas ?? []).filter(d => d.status === 'VALID').length}</Text>}
          onPress={goServices} />
        <SummaryCard icon={<Home size={20} color="#10B981" />} label={t(lang, 'propertyCount')} loading={propLoading}
          color={Colors.successBg}
          value={<Text style={styles.countText}>{properties?.length ?? 0}</Text>}
          onPress={goServices} />
        <SummaryCard icon={<HeartHandshake size={20} color="#EC4899" />} label={t(lang, 'benefitCount')} loading={benLoading}
          color="#FCE7F3"
          value={<Text style={styles.countText}>{benefits?.length ?? 0}</Text>}
          onPress={goServices} />
        <SummaryCard icon={<Truck size={20} color="#F97316" />} label={t(lang, 'vehicleCount')} loading={vehLoading}
          color={Colors.orangeBg}
          value={<Text style={styles.countText}>{(vehicles ?? []).filter(v => v.status === 'REGISTERED').length}</Text>}
          onPress={goServices} />
        <SummaryCard icon={<PiggyBank size={20} color="#0EA5E9" />} label={t(lang, 'pensionCount')} loading={penLoading}
          color="#E0F2FE"
          value={pension ? <StatusBadge label={pension.status} status={['ELIGIBLE','PAYING'].includes(pension.status) ? 'ACTIVE' : 'PENDING'} size="sm" /> : <Text style={styles.noData}>—</Text>}
          onPress={goServices} />
        <SummaryCard icon={<Scale size={20} color={Colors.error} />} label={t(lang, 'courtCount')} loading={finesLoading}
          color={Colors.errorBg}
          value={<Text style={[styles.countText, overdueOrPending.length > 0 && { color: Colors.error }]}>{overdueOrPending.length}</Text>}
          onPress={goServices} />
        <SummaryCard icon={<CalendarDays size={20} color={Colors.primary} />} label={t(lang, 'appointmentCount')} loading={aptLoading}
          value={<Text style={styles.countText}>{(aptPage?.content ?? []).length}</Text>}
          onPress={goServices} />
        <SummaryCard icon={<FileText size={20} color="#6366F1" />} label={t(lang, 'documentCount')} loading={certLoading}
          color="#EEF2FF"
          value={<Text style={styles.countText}>{(certs ?? []).filter(c => c.status === 'ACTIVE').length}</Text>}
          onPress={() => navigation.navigate('DocumentsTab')} />
        <SummaryCard icon={<Package size={20} color="#78716C" />} label={t(lang, 'customsCount')} loading={false}
          color="#F5F5F4"
          value={<Text style={styles.countText}>{(customsDecls ?? []).filter(d => ['SUBMITTED','UNDER_REVIEW','HELD'].includes(d.status)).length}</Text>}
          onPress={goServices} />
        <SummaryCard icon={<Building2 size={20} color="#DC2626" />} label={t(lang, 'bankingCount')} loading={consentLoading}
          color={Colors.errorBg}
          value={<View style={styles.row}><Text style={[styles.countText, (consents?.length ?? 0) > 0 && { color: Colors.error }]}>{consents?.length ?? 0}</Text></View>}
          onPress={goServices} />
      </View>

      {/* Recent access log */}
      <View style={styles.recentCard}>
        <View style={styles.recentHeader}>
          <View style={styles.row}>
            <Shield size={16} color={Colors.primary} />
            <Text style={styles.recentTitle}>{t(lang, 'recentActivity')}</Text>
          </View>
          <TouchableOpacity onPress={() => navigation.navigate('ProfileTab')}>
            <Text style={styles.viewAll}>{t(lang, 'viewAll')}</Text>
          </TouchableOpacity>
        </View>
        {logsLoading
          ? [1,2,3].map(i => <View key={i} style={styles.logSkeleton} />)
          : recentLogs.length === 0
          ? <Text style={styles.noLogs}>{t(lang, 'noLogs')}</Text>
          : recentLogs.map(log => (
            <View key={log.id} style={styles.logRow}>
              <View style={styles.logIconWrap}>
                <Building2 size={14} color={Colors.textSecondary} />
              </View>
              <View style={styles.logInfo}>
                <Text style={styles.logInstitution} numberOfLines={1}>{log.requestingInstitutionCode}</Text>
                <Text style={styles.logDataType}>{log.dataType}</Text>
              </View>
              <View style={styles.logRight}>
                <StatusBadge label={log.success ? t(lang, 'success') : t(lang, 'denied')} status={log.success ? 'ACTIVE' : 'REVOKED'} size="sm" />
                <Text style={styles.logTime}>{formatDateTime(log.timestamp, lang)}</Text>
              </View>
            </View>
          ))
        }
      </View>
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 32 },
  heroCard: { flexDirection: 'row', alignItems: 'center', backgroundColor: Colors.card, borderRadius: Radius.lg, padding: Spacing.lg, marginBottom: Spacing.lg, ...Shadow.md },
  avatar: { width: 48, height: 48, borderRadius: 24, backgroundColor: Colors.primaryBg, justifyContent: 'center', alignItems: 'center', marginRight: Spacing.md },
  avatarText: { fontSize: 20, fontWeight: '700', color: Colors.primary },
  heroInfo: { flex: 1 },
  heroNameRow: { flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: 2 },
  heroName: { fontSize: 16, fontWeight: '700', color: Colors.dark, flex: 1 },
  heroNin: { fontSize: 12, color: Colors.textMuted, fontFamily: Platform.OS === 'ios' ? 'Courier' : 'monospace' },
  skeletonName: { height: 16, backgroundColor: Colors.border, borderRadius: 4, width: 140, marginBottom: 6 },
  sectionTitle: { fontSize: 13, fontWeight: '700', color: Colors.textSecondary, textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: Spacing.md },
  grid: { flexDirection: 'row', flexWrap: 'wrap', marginHorizontal: -4, marginBottom: Spacing.lg },
  card: { width: '47%', margin: '1.5%', backgroundColor: Colors.card, borderRadius: Radius.lg, padding: Spacing.md, flexDirection: 'row', alignItems: 'center', ...Shadow.sm },
  cardIcon: { width: 36, height: 36, borderRadius: Radius.md, justifyContent: 'center', alignItems: 'center', marginRight: Spacing.sm },
  cardBody: { flex: 1 },
  cardValue: { marginBottom: 2 },
  cardLabel: { fontSize: 11, color: Colors.textMuted },
  skeleton: { height: 14, backgroundColor: Colors.border, borderRadius: 3, width: 60, marginBottom: 4 },
  countText: { fontSize: 18, fontWeight: '700', color: Colors.dark },
  noData: { fontSize: 11, color: Colors.textMuted },
  row: { flexDirection: 'row', alignItems: 'center', gap: 4 },
  recentCard: { backgroundColor: Colors.card, borderRadius: Radius.lg, padding: Spacing.lg, ...Shadow.md },
  recentHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: Spacing.md },
  recentTitle: { fontSize: 14, fontWeight: '600', color: Colors.dark, marginLeft: 6 },
  viewAll: { fontSize: 13, color: Colors.primary, fontWeight: '600' },
  logRow: { flexDirection: 'row', alignItems: 'center', paddingVertical: 8, borderBottomWidth: StyleSheet.hairlineWidth, borderBottomColor: Colors.border },
  logIconWrap: { width: 30, height: 30, borderRadius: 8, backgroundColor: Colors.borderLight, justifyContent: 'center', alignItems: 'center', marginRight: Spacing.sm },
  logInfo: { flex: 1 },
  logInstitution: { fontSize: 13, fontWeight: '600', color: Colors.dark },
  logDataType: { fontSize: 11, color: Colors.textMuted },
  logRight: { alignItems: 'flex-end', gap: 2 },
  logTime: { fontSize: 10, color: Colors.textMuted, marginTop: 2 },
  logSkeleton: { height: 36, backgroundColor: Colors.border, borderRadius: 6, marginBottom: 8 },
  noLogs: { fontSize: 14, color: Colors.textMuted, textAlign: 'center', paddingVertical: Spacing.lg },
})

// React Native needs Platform for the monospace font
import { Platform } from 'react-native'
