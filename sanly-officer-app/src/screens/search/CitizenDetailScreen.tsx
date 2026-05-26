import React from 'react'
import { View, Text, ScrollView, StyleSheet } from 'react-native'
import { useRoute } from '@react-navigation/native'
import type { RouteProp } from '@react-navigation/native-stack'
import { useQuery } from '@tanstack/react-query'
import { getCitizen } from '../../api/registry'
import { StatusBadge } from '../../components/StatusBadge'
import { InfoRow } from '../../components/InfoRow'
import { SectionCard } from '../../components/SectionCard'
import { LoadingView } from '../../components/LoadingView'
import { ErrorView } from '../../components/ErrorView'
import { useLangStore } from '../../store/langStore'
import { formatDate } from '../../utils/formatters'
import { Colors, Spacing, Radius } from '../../constants/theme'
import type { SearchStackParamList } from '../../types'

type RouteProps = RouteProp<SearchStackParamList, 'CitizenDetail'>

export function CitizenDetailScreen() {
  const { lang } = useLangStore()
  const { params: { nationalId } } = useRoute<RouteProps>()

  const { data: citizen, isLoading, error, refetch } = useQuery({
    queryKey: ['citizen-detail', nationalId],
    queryFn: () => getCitizen(nationalId),
    retry: false,
  })

  if (isLoading) return <LoadingView message="Loading citizen..." />
  if (error || !citizen) return <ErrorView message="Failed to load citizen record." onRetry={refetch} />

  const initials = `${citizen.firstName?.[0] ?? ''}${citizen.lastName?.[0] ?? ''}`.toUpperCase()

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      {/* Header card */}
      <View style={styles.heroCard}>
        <View style={styles.avatar}>
          <Text style={styles.avatarText}>{initials}</Text>
        </View>
        <Text style={styles.heroName}>{citizen.firstName} {citizen.lastName}</Text>
        <Text style={styles.heroNin}>{citizen.nationalId}</Text>
        <StatusBadge label={citizen.status} status={citizen.status} />
      </View>

      <SectionCard title="Identity">
        <InfoRow label="National ID" value={citizen.nationalId} />
        <InfoRow label="First Name" value={citizen.firstName} />
        <InfoRow label="Last Name" value={citizen.lastName} />
        <InfoRow label="Date of Birth" value={formatDate(citizen.dateOfBirth)} />
        <InfoRow label="Gender" value={citizen.gender} />
        <InfoRow label="Marital Status" value={citizen.maritalStatus ?? '—'} last />
      </SectionCard>

      {(citizen.fatherNin || citizen.motherNin) && (
        <SectionCard title="Family Links">
          {citizen.fatherNin && <InfoRow label="Father NIN" value={citizen.fatherNin} />}
          {citizen.motherNin && <InfoRow label="Mother NIN" value={citizen.motherNin} last />}
        </SectionCard>
      )}
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 40 },
  heroCard: { backgroundColor: Colors.navy, borderRadius: Radius.lg, padding: Spacing.xl, alignItems: 'center', marginBottom: Spacing.lg },
  avatar: { width: 72, height: 72, borderRadius: 36, backgroundColor: Colors.primaryBg, justifyContent: 'center', alignItems: 'center', marginBottom: Spacing.md },
  avatarText: { fontSize: 26, fontWeight: '800', color: Colors.primary },
  heroName: { fontSize: 20, fontWeight: '800', color: '#fff' },
  heroNin: { fontSize: 13, color: 'rgba(255,255,255,0.6)', marginTop: 4, marginBottom: Spacing.md, fontVariant: ['tabular-nums'] },
})
