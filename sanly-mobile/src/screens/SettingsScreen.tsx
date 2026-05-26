import React from 'react'
import { View, Text, Switch, TouchableOpacity, ScrollView, StyleSheet } from 'react-native'
import { useLangStore } from '../store/langStore'
import { useBiometrics } from '../hooks/useBiometrics'
import { t } from '../i18n'
import type { Lang } from '../i18n'
import { Colors, Radius, Shadow, Spacing } from '../constants/theme'

export function SettingsScreen() {
  const { lang, setLang } = useLangStore()
  const { available, enrolled, enabled, toggleBiometrics } = useBiometrics()

  const langs: { code: Lang; label: string; native: string }[] = [
    { code: 'en', label: 'English', native: 'English' },
    { code: 'tk', label: 'Turkmen', native: 'Türkmençe' },
    { code: 'ru', label: 'Russian', native: 'Русский' },
  ]

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      {/* Language */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>{t(lang, 'languagePreference')}</Text>
        <View style={styles.card}>
          {langs.map((l, i) => (
            <React.Fragment key={l.code}>
              <TouchableOpacity style={styles.langRow} onPress={() => setLang(l.code)} activeOpacity={0.7}>
                <View style={styles.langInfo}>
                  <Text style={styles.langLabel}>{l.native}</Text>
                  <Text style={styles.langSub}>{l.label}</Text>
                </View>
                <View style={[styles.radio, lang === l.code && styles.radioActive]}>
                  {lang === l.code && <View style={styles.radioDot} />}
                </View>
              </TouchableOpacity>
              {i < langs.length - 1 && <View style={styles.divider} />}
            </React.Fragment>
          ))}
        </View>
      </View>

      {/* Biometrics */}
      {available && enrolled && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>{t(lang, 'biometricTitle')}</Text>
          <View style={styles.card}>
            <View style={styles.switchRow}>
              <View style={styles.switchInfo}>
                <Text style={styles.switchLabel}>{t(lang, 'enableBiometrics')}</Text>
                <Text style={styles.switchSub}>{enabled ? t(lang, 'biometricsEnabled') : t(lang, 'biometricsDisabled')}</Text>
              </View>
              <Switch
                value={enabled}
                onValueChange={toggleBiometrics}
                trackColor={{ false: Colors.border, true: Colors.primary }}
                thumbColor="#fff"
              />
            </View>
          </View>
        </View>
      )}

      {/* About */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>About</Text>
        <View style={styles.card}>
          <View style={styles.aboutRow}>
            <Text style={styles.aboutLabel}>Version</Text>
            <Text style={styles.aboutValue}>1.0.0</Text>
          </View>
          <View style={styles.divider} />
          <View style={styles.aboutRow}>
            <Text style={styles.aboutLabel}>Platform</Text>
            <Text style={styles.aboutValue}>Sanly e-Gov</Text>
          </View>
        </View>
      </View>
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 32 },
  section: { marginBottom: Spacing.xl },
  sectionTitle: { fontSize: 12, fontWeight: '700', color: Colors.textSecondary, textTransform: 'uppercase', letterSpacing: 0.8, marginBottom: Spacing.sm, paddingLeft: 4 },
  card: { backgroundColor: Colors.card, borderRadius: Radius.lg, overflow: 'hidden', ...Shadow.sm },
  langRow: { flexDirection: 'row', alignItems: 'center', padding: Spacing.lg },
  langInfo: { flex: 1 },
  langLabel: { fontSize: 15, fontWeight: '500', color: Colors.dark },
  langSub: { fontSize: 12, color: Colors.textMuted, marginTop: 2 },
  radio: { width: 20, height: 20, borderRadius: 10, borderWidth: 2, borderColor: Colors.border, justifyContent: 'center', alignItems: 'center' },
  radioActive: { borderColor: Colors.primary },
  radioDot: { width: 10, height: 10, borderRadius: 5, backgroundColor: Colors.primary },
  divider: { height: StyleSheet.hairlineWidth, backgroundColor: Colors.border, marginLeft: Spacing.lg },
  switchRow: { flexDirection: 'row', alignItems: 'center', padding: Spacing.lg },
  switchInfo: { flex: 1 },
  switchLabel: { fontSize: 15, fontWeight: '500', color: Colors.dark },
  switchSub: { fontSize: 12, color: Colors.textMuted, marginTop: 2 },
  aboutRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: Spacing.lg },
  aboutLabel: { fontSize: 14, color: Colors.textSecondary },
  aboutValue: { fontSize: 14, color: Colors.dark, fontWeight: '500' },
})
