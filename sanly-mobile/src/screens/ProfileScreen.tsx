import React from 'react'
import { View, Text, TouchableOpacity, ScrollView, StyleSheet, Alert } from 'react-native'
import { useNavigation } from '@react-navigation/native'
import type { NativeStackNavigationProp } from '@react-navigation/native-stack'
import { useQuery } from '@tanstack/react-query'
import { User, Shield, Settings, LogOut, ChevronRight, Smartphone } from 'lucide-react-native'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { getCitizen } from '../api/registry'
import { t } from '../i18n'
import { Colors, Radius, Shadow, Spacing } from '../constants/theme'
import type { ProfileStackParamList } from '../types'

type NavProp = NativeStackNavigationProp<ProfileStackParamList>

function MenuItem({ icon, label, onPress, danger }: { icon: React.ReactNode; label: string; onPress: () => void; danger?: boolean }) {
  return (
    <TouchableOpacity style={styles.menuItem} onPress={onPress} activeOpacity={0.7}>
      <View style={styles.menuIcon}>{icon}</View>
      <Text style={[styles.menuLabel, danger && { color: Colors.error }]}>{label}</Text>
      <ChevronRight size={16} color={Colors.textMuted} />
    </TouchableOpacity>
  )
}

export function ProfileScreen() {
  const navigation = useNavigation<NavProp>()
  const { nationalId, clearAuth } = useAuthStore()
  const { lang } = useLangStore()

  const { data: citizen } = useQuery({
    queryKey: ['citizen', nationalId],
    queryFn: () => getCitizen(nationalId!),
    enabled: !!nationalId,
    staleTime: 5 * 60_000,
  })

  const handleSignOut = () => {
    Alert.alert(
      t(lang, 'signOut'),
      lang === 'en' ? 'Are you sure you want to sign out?'
        : lang === 'tk' ? 'Çykmak isleýärsiňizmi?'
        : 'Вы уверены, что хотите выйти?',
      [
        { text: t(lang, 'cancel'), style: 'cancel' },
        { text: t(lang, 'signOut'), style: 'destructive', onPress: () => clearAuth() },
      ]
    )
  }

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      {/* Profile card */}
      <View style={styles.profileCard}>
        <View style={styles.avatar}>
          <Text style={styles.avatarText}>{citizen?.firstName?.[0] ?? '?'}</Text>
        </View>
        <View style={styles.profileInfo}>
          <Text style={styles.profileName}>{citizen ? `${citizen.firstName} ${citizen.lastName}` : nationalId}</Text>
          <Text style={styles.profileNin}>{nationalId}</Text>
        </View>
      </View>

      {/* Menu */}
      <View style={styles.menuCard}>
        <MenuItem
          icon={<User size={18} color={Colors.primary} />}
          label={t(lang, 'identity')}
          onPress={() => navigation.navigate('Identity')}
        />
        <View style={styles.divider} />
        <MenuItem
          icon={<Shield size={18} color={Colors.primary} />}
          label={t(lang, 'accessLog')}
          onPress={() => navigation.navigate('AccessLog')}
        />
        <View style={styles.divider} />
        <MenuItem
          icon={<Smartphone size={18} color={Colors.primary} />}
          label={t(lang, 'sessions')}
          onPress={() => navigation.navigate('Sessions')}
        />
        <View style={styles.divider} />
        <MenuItem
          icon={<Settings size={18} color={Colors.primary} />}
          label={t(lang, 'settings')}
          onPress={() => navigation.navigate('Settings')}
        />
      </View>

      <View style={styles.menuCard}>
        <MenuItem
          icon={<LogOut size={18} color={Colors.error} />}
          label={t(lang, 'signOut')}
          onPress={handleSignOut}
          danger
        />
      </View>
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 32 },
  profileCard: { flexDirection: 'row', alignItems: 'center', backgroundColor: Colors.card, borderRadius: Radius.lg, padding: Spacing.lg, marginBottom: Spacing.lg, ...Shadow.md },
  avatar: { width: 56, height: 56, borderRadius: 28, backgroundColor: Colors.primaryBg, justifyContent: 'center', alignItems: 'center', marginRight: Spacing.md },
  avatarText: { fontSize: 22, fontWeight: '700', color: Colors.primary },
  profileInfo: { flex: 1 },
  profileName: { fontSize: 17, fontWeight: '700', color: Colors.dark },
  profileNin: { fontSize: 12, color: Colors.textMuted, marginTop: 2, fontVariant: ['tabular-nums'] },
  menuCard: { backgroundColor: Colors.card, borderRadius: Radius.lg, marginBottom: Spacing.md, ...Shadow.sm, overflow: 'hidden' },
  menuItem: { flexDirection: 'row', alignItems: 'center', padding: Spacing.lg },
  menuIcon: { width: 32, height: 32, borderRadius: Radius.sm, backgroundColor: Colors.primaryBg, justifyContent: 'center', alignItems: 'center', marginRight: Spacing.md },
  menuLabel: { flex: 1, fontSize: 15, fontWeight: '500', color: Colors.dark },
  divider: { height: StyleSheet.hairlineWidth, backgroundColor: Colors.border, marginLeft: Spacing.lg + 32 + Spacing.md },
})
