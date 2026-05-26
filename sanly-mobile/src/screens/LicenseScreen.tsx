import React from 'react'
import { ScrollView, View, Text, StyleSheet } from 'react-native'
import { useQuery } from '@tanstack/react-query'
import { Car } from 'lucide-react-native'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { getLicenses } from '../api/dmv'
import { t } from '../i18n'
import { LoadingView } from '../components/LoadingView'
import { SectionCard } from '../components/SectionCard'
import { InfoRow } from '../components/InfoRow'
import { StatusBadge } from '../components/StatusBadge'
import { EmptyState } from '../components/EmptyState'
import { Colors, Radius, Spacing } from '../constants/theme'
import { formatDate } from '../utils/formatters'

export function LicenseScreen() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()
  const { data, isLoading } = useQuery({
    queryKey: ['licenses', nationalId],
    queryFn: () => getLicenses(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  if (isLoading) return <LoadingView message={t(lang, 'loading')} />
  const licenses = data ?? []

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      {licenses.length === 0 && <EmptyState message={t(lang, 'noLicenseMsg')} icon={<Car size={24} color={Colors.textMuted} />} />}
      {licenses.map(lic => (
        <SectionCard key={lic.id}>
          <View style={styles.licenseCard}>
            <View style={styles.licenseHeader}>
              <View style={styles.categoryBadge}>
                <Text style={styles.categoryText}>{lic.category}</Text>
              </View>
              <StatusBadge label={t(lang, lic.status === 'ACTIVE' ? 'active' : lic.status === 'SUSPENDED' ? 'suspended' : lic.status === 'EXPIRED' ? 'expired' : 'revoked')} status={lic.status} />
            </View>
            <InfoRow label={t(lang, 'licenseNumber')} value={lic.licenseNumber} />
            <InfoRow label={t(lang, 'issuedDate')} value={formatDate(lic.issuedDate, lang)} />
            <InfoRow label={t(lang, 'expiryDate')} value={formatDate(lic.expiryDate, lang)} last />
          </View>
        </SectionCard>
      ))}
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 32 },
  licenseCard: {},
  licenseHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: Spacing.md },
  categoryBadge: { width: 48, height: 48, borderRadius: 24, backgroundColor: Colors.primaryBg, justifyContent: 'center', alignItems: 'center' },
  categoryText: { fontSize: 18, fontWeight: '800', color: Colors.primary },
})
