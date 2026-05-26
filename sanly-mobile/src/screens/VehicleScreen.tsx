import React, { useState } from 'react'
import { ScrollView, View, Text, TouchableOpacity, StyleSheet } from 'react-native'
import { useQuery } from '@tanstack/react-query'
import { Truck, ChevronDown, ChevronUp } from 'lucide-react-native'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { getVehiclesByOwner, getInsuranceByPlate, getInspectionsByPlate } from '../api/vehicle'
import { t } from '../i18n'
import { LoadingView } from '../components/LoadingView'
import { SectionCard } from '../components/SectionCard'
import { InfoRow } from '../components/InfoRow'
import { StatusBadge } from '../components/StatusBadge'
import { EmptyState } from '../components/EmptyState'
import { Colors, Radius, Spacing } from '../constants/theme'
import { formatDate } from '../utils/formatters'

function VehicleCard({ vehicle }: { vehicle: ReturnType<typeof getVehiclesByOwner> extends Promise<infer T> ? T extends (infer U)[] ? U : never : never }) {
  const [expanded, setExpanded] = useState(false)
  const { lang } = useLangStore()

  const { data: insurance } = useQuery({
    queryKey: ['insurance', vehicle.plateNumber],
    queryFn: () => getInsuranceByPlate(vehicle.plateNumber),
    enabled: expanded,
    retry: false,
  })
  const { data: inspections } = useQuery({
    queryKey: ['inspections', vehicle.plateNumber],
    queryFn: () => getInspectionsByPlate(vehicle.plateNumber),
    enabled: expanded,
    retry: false,
  })

  const latestInsurance = insurance?.[0]
  const latestInspection = inspections?.[0]

  return (
    <SectionCard>
      <TouchableOpacity onPress={() => setExpanded(v => !v)} activeOpacity={0.7}>
        <View style={styles.vHeader}>
          <View style={styles.plateWrap}>
            <Text style={styles.plate}>{vehicle.plateNumber}</Text>
          </View>
          <View style={styles.vInfo}>
            <Text style={styles.vTitle}>{vehicle.year} {vehicle.make} {vehicle.model}</Text>
            <StatusBadge label={vehicle.status} status={vehicle.status} size="sm" />
          </View>
          {expanded ? <ChevronUp size={18} color={Colors.textMuted} /> : <ChevronDown size={18} color={Colors.textMuted} />}
        </View>
      </TouchableOpacity>
      {expanded && (
        <>
          <InfoRow label={t(lang, 'vehicleCount')} value={vehicle.vehicleType} />
          <InfoRow label="Fuel" value={vehicle.fuelType} />
          {vehicle.color && <InfoRow label="Color" value={vehicle.color} />}
          {latestInsurance && (
            <InfoRow label={t(lang, 'insuranceStatus')} value={
              <StatusBadge label={latestInsurance.status} status={latestInsurance.status} size="sm" />
            } />
          )}
          {latestInspection && (
            <>
              <InfoRow label={t(lang, 'inspectionStatus')} value={
                <StatusBadge label={latestInspection.result} status={latestInspection.result} size="sm" />
              } />
              <InfoRow label={t(lang, 'nextInspectionDue')} value={formatDate(latestInspection.nextInspectionDue, lang)} last />
            </>
          )}
        </>
      )}
    </SectionCard>
  )
}

export function VehicleScreen() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()
  const { data, isLoading } = useQuery({
    queryKey: ['vehicles', nationalId],
    queryFn: () => getVehiclesByOwner(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  if (isLoading) return <LoadingView message={t(lang, 'loading')} />
  const vehicles = data ?? []

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      {vehicles.length === 0
        ? <EmptyState message={t(lang, 'noVehicles')} icon={<Truck size={24} color={Colors.textMuted} />} />
        : vehicles.map(v => <VehicleCard key={v.vehicleId} vehicle={v} />)
      }
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 32 },
  vHeader: { flexDirection: 'row', alignItems: 'center', gap: Spacing.sm },
  plateWrap: { backgroundColor: '#FEF9C3', borderWidth: 1.5, borderColor: '#CA8A04', borderRadius: Radius.sm, paddingHorizontal: 8, paddingVertical: 4 },
  plate: { fontWeight: '800', fontSize: 13, color: '#92400E', letterSpacing: 1 },
  vInfo: { flex: 1, gap: 4 },
  vTitle: { fontSize: 14, fontWeight: '600', color: Colors.dark },
})
