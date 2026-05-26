import React from 'react'
import { View, Text, ScrollView, TouchableOpacity, StyleSheet, Alert, Switch } from 'react-native'
import { LinearGradient } from 'expo-linear-gradient'
import { useNavigation } from '@react-navigation/native'
import type { NativeStackNavigationProp } from '@react-navigation/native-stack'
import { LogOut, Settings, Globe, AlertCircle } from 'lucide-react-native'
import { useAuthStore } from '../../store/authStore'
import { useLangStore } from '../../store/langStore'
import { useRole } from '../../hooks/useRole'
import { useOfflineQueueStore } from '../../store/offlineQueueStore'
import { OfficerCard } from '../../components/OfficerCard'
import { SectionCard } from '../../components/SectionCard'
import { t } from '../../i18n'
import { Colors, Spacing, Radius, Shadow } from '../../constants/theme'
import type { ProfileStackParamList } from '../../types'

type NavProp = NativeStackNavigationProp<ProfileStackParamList>

export function ProfileScreen() {
  const navigation = useNavigation<NavProp>()
  const { officer, clearAuth } = useAuthStore()
  const { lang } = useLangStore()
  const { colors, label } = useRole()
  const { queue } = useOfflineQueueStore()

  function handleSignOut() {
    Alert.alert(
      t(lang, 'signOut'),
      'Are you sure you want to sign out?',
      [
        { text: t(lang, 'cancel'), style: 'cancel' },
        { text: t(lang, 'signOut'), style: 'destructive', onPress: () => clearAuth() },
      ]
    )
  }

  if (!officer) return null

  return (
    <View style={{ flex: 1 }}>
      <LinearGradient colors={[Colors.navyDark, Colors.navyLight]} style={styles.header}>
        <OfficerCard officer={officer} />
        <View style={[styles.roleTag, { backgroundColor: colors.bg }]}>
          <Text style={[styles.roleTagText, { color: colors.text }]}>{label}</Text>
        </View>
        {officer.region && <Text style={styles.region}>Region: {officer.region}</Text>}
      </LinearGradient>

      <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
        {queue.length > 0 && (
          <View style={styles.queueWarning}>
            <AlertCircle size={16} color={Colors.warning} />
            <Text style={styles.queueText}>{queue.length} {t(lang, 'offlineQueueItems')} pending sync</Text>
          </View>
        )}

        <SectionCard title="Account">
          {officer.badgeNumber && (
            <View style={styles.row}>
              <Text style={styles.rowLabel}>{t(lang, 'badgeNumber')}</Text>
              <Text style={styles.rowValue}>#{officer.badgeNumber}</Text>
            </View>
          )}
          <View style={styles.row}>
            <Text style={styles.rowLabel}>Officer ID</Text>
            <Text style={styles.rowValue}>{officer.officerId}</Text>
          </View>
          <View style={styles.row}>
            <Text style={styles.rowLabel}>Institution</Text>
            <Text style={styles.rowValue}>{officer.institution}</Text>
          </View>
        </SectionCard>

        <SectionCard title="Preferences">
          <TouchableOpacity style={styles.menuItem} onPress={() => navigation.navigate('Settings')}>
            <Globe size={18} color={Colors.primary} />
            <Text style={styles.menuLabel}>{t(lang, 'language')}</Text>
            <Text style={styles.menuValue}>{lang.toUpperCase()}</Text>
          </TouchableOpacity>
          <View style={styles.divider} />
          <TouchableOpacity style={styles.menuItem} onPress={() => navigation.navigate('Settings')}>
            <Settings size={18} color={Colors.primary} />
            <Text style={styles.menuLabel}>{t(lang, 'biometricSettings')}</Text>
          </TouchableOpacity>
        </SectionCard>

        <TouchableOpacity style={styles.signOutBtn} onPress={handleSignOut}>
          <LogOut size={18} color={Colors.error} />
          <Text style={styles.signOutText}>{t(lang, 'signOut')}</Text>
        </TouchableOpacity>

        <Text style={styles.version}>{t(lang, 'appVersion')} 1.0.0 · SANLY Officer</Text>
      </ScrollView>
    </View>
  )
}

const styles = StyleSheet.create({
  header: { paddingTop: 56, paddingHorizontal: Spacing.xl, paddingBottom: Spacing.xl },
  roleTag: { alignSelf: 'flex-start', borderRadius: Radius.full, paddingHorizontal: 12, paddingVertical: 4, marginTop: Spacing.md },
  roleTagText: { fontSize: 12, fontWeight: '700', letterSpacing: 0.5 },
  region: { fontSize: 12, color: 'rgba(255,255,255,0.5)', marginTop: Spacing.sm },
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 40 },
  queueWarning: { flexDirection: 'row', alignItems: 'center', gap: Spacing.sm, backgroundColor: Colors.warningBg, borderRadius: Radius.md, padding: Spacing.md, marginBottom: Spacing.lg },
  queueText: { fontSize: 13, color: Colors.warning, fontWeight: '500' },
  row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: Spacing.sm + 2, paddingHorizontal: Spacing.lg, borderBottomWidth: StyleSheet.hairlineWidth, borderBottomColor: Colors.border },
  rowLabel: { fontSize: 13, color: Colors.textSecondary },
  rowValue: { fontSize: 13, fontWeight: '500', color: Colors.dark },
  menuItem: { flexDirection: 'row', alignItems: 'center', gap: Spacing.md, padding: Spacing.lg },
  menuLabel: { flex: 1, fontSize: 15, fontWeight: '500', color: Colors.dark },
  menuValue: { fontSize: 13, color: Colors.textMuted },
  divider: { height: StyleSheet.hairlineWidth, backgroundColor: Colors.border, marginLeft: Spacing.lg + 18 + Spacing.md },
  signOutBtn: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: Spacing.sm, backgroundColor: Colors.errorBg, borderRadius: Radius.lg, padding: Spacing.lg, marginTop: Spacing.md, ...Shadow.sm },
  signOutText: { fontSize: 15, fontWeight: '700', color: Colors.error },
  version: { textAlign: 'center', fontSize: 11, color: Colors.textMuted, marginTop: Spacing.xl },
})
