import React from 'react'
import { View, Text, ScrollView, TouchableOpacity, StyleSheet } from 'react-native'
import { useRoute, useNavigation } from '@react-navigation/native'
import type { NativeStackNavigationProp, RouteProp } from '@react-navigation/native-stack'
import {
  User, CreditCard, Car, Building2, MapPin,
  GraduationCap, AlertTriangle, Scale, CalendarCheck,
} from 'lucide-react-native'
import { StatusBadge } from '../../components/StatusBadge'
import { InfoRow } from '../../components/InfoRow'
import { SectionCard } from '../../components/SectionCard'
import { Colors, Spacing, Radius, Shadow } from '../../constants/theme'
import { formatDate, formatMoney, maskNin } from '../../utils/formatters'
import { scanCodeLabels } from '../../utils/codeParser'
import type { ScanStackParamList, ScanCodeType } from '../../types'

type RouteProps = RouteProp<ScanStackParamList, 'ScanResultDetail'>
type NavProp = NativeStackNavigationProp<ScanStackParamList>

const iconForType: Record<ScanCodeType, React.ReactNode> = {
  NIN:        <User size={22} color={Colors.primary} />,
  LICENSE:    <CreditCard size={22} color={Colors.dmv} />,
  BUSINESS:   <Building2 size={22} color={Colors.primary} />,
  APPOINTMENT:<CalendarCheck size={22} color={Colors.success} />,
  FINE:       <AlertTriangle size={22} color={Colors.error} />,
  CASE:       <Scale size={22} color={Colors.court} />,
  PLATE:      <Car size={22} color={Colors.police} />,
  CADASTRAL:  <MapPin size={22} color={Colors.land} />,
  DIPLOMA:    <GraduationCap size={22} color={Colors.education} />,
  BIRTH_CERT: <User size={22} color={Colors.civil} />,
  MARR_CERT:  <User size={22} color={Colors.civil} />,
  DEATH_CERT: <User size={22} color={Colors.textMuted} />,
  UNKNOWN:    <AlertTriangle size={22} color={Colors.warning} />,
}

function renderData(type: ScanCodeType, data: any) {
  if (!data) return <Text style={styles.noData}>No data available</Text>

  switch (type) {
    case 'NIN':
      return (
        <SectionCard title="Citizen">
          <InfoRow label="Name" value={`${data.firstName} ${data.lastName}`} />
          <InfoRow label="National ID" value={data.nationalId} />
          <InfoRow label="Date of Birth" value={formatDate(data.dateOfBirth)} />
          <InfoRow label="Gender" value={data.gender} />
          <InfoRow label="Status" value={<StatusBadge label={data.status} status={data.status} size="sm" />} last />
        </SectionCard>
      )

    case 'LICENSE':
      return (
        <SectionCard title="Driving License">
          <InfoRow label="License №" value={data.licenseNumber} />
          <InfoRow label="Holder" value={data.holderName} />
          <InfoRow label="Categories" value={(data.categories ?? []).join(', ')} />
          <InfoRow label="Issued" value={formatDate(data.issueDate)} />
          <InfoRow label="Expires" value={formatDate(data.expiryDate)} />
          <InfoRow label="Status" value={<StatusBadge label={data.status} status={data.status} size="sm" />} last />
        </SectionCard>
      )

    case 'APPOINTMENT':
      return (
        <SectionCard title="Appointment">
          <InfoRow label="Code" value={data.appointmentCode} />
          <InfoRow label="Citizen" value={data.citizenName} />
          <InfoRow label="Service" value={data.serviceType} />
          <InfoRow label="Office" value={data.officeName} />
          <InfoRow label="Scheduled" value={formatDate(data.scheduledAt)} />
          <InfoRow label="Status" value={<StatusBadge label={data.status} status={data.status} size="sm" />} last />
        </SectionCard>
      )

    case 'FINE':
      return (
        <SectionCard title="Court Fine">
          <InfoRow label="Fine Code" value={data.fineCode} />
          <InfoRow label="Citizen" value={data.citizenName} />
          <InfoRow label="Offense" value={data.offenseType} />
          <InfoRow label="Amount" value={formatMoney(data.fineAmount)} />
          <InfoRow label="Due Date" value={formatDate(data.dueDate)} />
          <InfoRow label="Status" value={<StatusBadge label={data.status} status={data.status} size="sm" />} last />
        </SectionCard>
      )

    case 'DIPLOMA':
      return (
        <SectionCard title="Diploma">
          <InfoRow label="Code" value={data.diplomaCode} />
          <InfoRow label="Holder" value={data.holderName} />
          <InfoRow label="Institution" value={data.institutionName} />
          <InfoRow label="Degree" value={data.degree} />
          <InfoRow label="Field" value={data.field} />
          <InfoRow label="Year" value={String(data.graduationYear)} />
          <InfoRow label="Status" value={<StatusBadge label={data.status} status={data.status} size="sm" />} last />
        </SectionCard>
      )

    case 'CADASTRAL':
      return (
        <SectionCard title="Property">
          <InfoRow label="Cadastral Code" value={data.cadastralCode} />
          <InfoRow label="Address" value={data.address} />
          <InfoRow label="Type" value={data.propertyType} />
          <InfoRow label="Area" value={`${data.area} m²`} />
          <InfoRow label="Status" value={<StatusBadge label={data.status} status={data.status} size="sm" />} last />
        </SectionCard>
      )

    default:
      return (
        <SectionCard title="Result">
          <Text style={styles.rawData}>{JSON.stringify(data, null, 2)}</Text>
        </SectionCard>
      )
  }
}

export function ScanResultScreen() {
  const { params } = useRoute<RouteProps>()
  const navigation = useNavigation<NavProp>()
  const { code, type, data } = params

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      {/* Header card */}
      <View style={styles.headerCard}>
        <View style={[styles.typeIcon, { backgroundColor: Colors.primaryBg }]}>
          {iconForType[type]}
        </View>
        <View style={{ flex: 1 }}>
          <Text style={styles.typeLabel}>{scanCodeLabels[type]}</Text>
          <Text style={styles.codeText} numberOfLines={1}>{code}</Text>
        </View>
      </View>

      {renderData(type, data)}

      <TouchableOpacity style={styles.backBtn} onPress={() => navigation.goBack()}>
        <Text style={styles.backBtnText}>← Scan Another</Text>
      </TouchableOpacity>
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 40 },
  headerCard: { flexDirection: 'row', alignItems: 'center', gap: Spacing.md, backgroundColor: Colors.card, borderRadius: Radius.lg, padding: Spacing.lg, marginBottom: Spacing.lg, ...Shadow.md },
  typeIcon: { width: 48, height: 48, borderRadius: Radius.md, justifyContent: 'center', alignItems: 'center' },
  typeLabel: { fontSize: 12, fontWeight: '700', color: Colors.textSecondary, textTransform: 'uppercase', letterSpacing: 0.5 },
  codeText: { fontSize: 14, fontWeight: '600', color: Colors.dark, marginTop: 2, fontVariant: ['tabular-nums'] },
  noData: { padding: Spacing.lg, color: Colors.textMuted, textAlign: 'center' },
  rawData: { padding: Spacing.md, fontSize: 11, color: Colors.textSecondary, fontFamily: 'monospace' },
  backBtn: { marginTop: Spacing.lg, padding: Spacing.md, alignItems: 'center' },
  backBtnText: { color: Colors.primary, fontWeight: '600', fontSize: 14 },
})
