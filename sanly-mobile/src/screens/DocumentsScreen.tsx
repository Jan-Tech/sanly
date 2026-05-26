import React, { useState } from 'react'
import { ScrollView, View, Text, TouchableOpacity, StyleSheet } from 'react-native'
import { useQuery } from '@tanstack/react-query'
import { useLangStore } from '../store/langStore'
import { getMyCertificates, getMyTrackedItems } from '../api/documents'
import { t } from '../i18n'
import { LoadingView } from '../components/LoadingView'
import { SectionCard } from '../components/SectionCard'
import { InfoRow } from '../components/InfoRow'
import { StatusBadge } from '../components/StatusBadge'
import { EmptyState } from '../components/EmptyState'
import { Colors, Radius, Spacing } from '../constants/theme'
import { formatDate } from '../utils/formatters'

type Tab = 'certs' | 'tracker'

export function DocumentsScreen() {
  const { lang } = useLangStore()
  const [tab, setTab] = useState<Tab>('certs')

  const { data: certs, isLoading: certLoading } = useQuery({
    queryKey: ['certs'],
    queryFn: getMyCertificates,
    retry: false,
  })
  const { data: tracked, isLoading: trackerLoading } = useQuery({
    queryKey: ['tracker'],
    queryFn: getMyTrackedItems,
    retry: false,
  })

  const isLoading = tab === 'certs' ? certLoading : trackerLoading

  return (
    <View style={styles.flex}>
      <View style={styles.tabs}>
        <TouchableOpacity style={[styles.tab, tab === 'certs' && styles.tabActive]} onPress={() => setTab('certs')}>
          <Text style={[styles.tabText, tab === 'certs' && styles.tabTextActive]}>{t(lang, 'myCertificates')}</Text>
        </TouchableOpacity>
        <TouchableOpacity style={[styles.tab, tab === 'tracker' && styles.tabActive]} onPress={() => setTab('tracker')}>
          <Text style={[styles.tabText, tab === 'tracker' && styles.tabTextActive]}>{t(lang, 'trackerTitle')}</Text>
        </TouchableOpacity>
      </View>

      {isLoading ? (
        <LoadingView message={t(lang, 'loading')} />
      ) : (
        <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
          {tab === 'certs' ? (
            <>
              {!certs || certs.length === 0
                ? <EmptyState message={t(lang, 'noCertificates')} />
                : certs.map((c, i) => (
                  <SectionCard key={c.certificateId} title={c.documentType.replace(/_/g, ' ')}>
                    <InfoRow label="Code" value={c.certificateCode} />
                    <InfoRow label={t(lang, 'issuedDate')} value={formatDate(c.issuedAt, lang)} />
                    <InfoRow label={t(lang, 'expiryDate')} value={formatDate(c.expiresAt, lang)} />
                    <InfoRow label={t(lang, 'status')} value={<StatusBadge label={t(lang, c.status === 'ACTIVE' ? 'active' : c.status === 'EXPIRED' ? 'expired' : 'revoked')} status={c.status} />} last />
                  </SectionCard>
                ))
              }
            </>
          ) : (
            <>
              {!tracked || tracked.length === 0
                ? <EmptyState message={t(lang, 'noTrackedItems')} />
                : tracked.map(item => (
                  <SectionCard key={item.trackingId} title={item.title}>
                    <InfoRow label="Code" value={item.trackingCode} />
                    <InfoRow label="Service" value={item.sourceService} />
                    <InfoRow label={t(lang, 'status')} value={<StatusBadge label={item.currentStatus.replace(/_/g, ' ')} status={item.currentStatus} />} />
                    <InfoRow label="Updated" value={formatDate(item.lastUpdatedAt, lang)} last />
                  </SectionCard>
                ))
              }
            </>
          )}
        </ScrollView>
      )}
    </View>
  )
}

const styles = StyleSheet.create({
  flex: { flex: 1, backgroundColor: Colors.background },
  tabs: { flexDirection: 'row', backgroundColor: Colors.card, borderBottomWidth: StyleSheet.hairlineWidth, borderBottomColor: Colors.border },
  tab: { flex: 1, paddingVertical: 12, alignItems: 'center' },
  tabActive: { borderBottomWidth: 2, borderBottomColor: Colors.primary },
  tabText: { fontSize: 13, fontWeight: '500', color: Colors.textSecondary },
  tabTextActive: { color: Colors.primary, fontWeight: '700' },
  scroll: { flex: 1 },
  content: { padding: Spacing.lg, paddingBottom: 32 },
})
