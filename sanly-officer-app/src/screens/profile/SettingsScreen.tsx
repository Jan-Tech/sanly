import React from 'react'
import { View, Text, Switch, TouchableOpacity, ScrollView, StyleSheet } from 'react-native'
import { useLangStore } from '../../store/langStore'
import { useBiometrics } from '../../hooks/useBiometric'
import { t } from '../../i18n'
import type { Lang } from '../../i18n'
import { Colors, Radius, Spacing, Shadow } from '../../constants/theme'

const LANGS: { code: Lang; native: string; label: string }[] = [
  { code: 'en', native: 'English', label: 'English' },
  { code: 'tk', native: 'Türkmençe', label: 'Turkmen' },
  { code: 'ru', native: 'Русский', label: 'Russian' },
]

export function SettingsScreen() {
  const { lang, setLang } = useLangStore()
  const { available, enrolled, enabled, toggleBiometrics } = useBiometrics()

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      {/* Language */}
      <Text style={styles.sectionTitle}>{t(lang, 'language')}</Text>
      <View style={styles.card}>
        {LANGS.map((l, i) => (
          <React.Fragment key={l.code}>
            <TouchableOpacity style={styles.langRow} onPress={() => setLang(l.code)} activeOpacity={0.7}>
              <View style={{ flex: 1 }}>
                <Text style={styles.langNative}>{l.native}</Text>
                <Text style={styles.langLabel}>{l.label}</Text>
              </View>
              <View style={[styles.radio, lang === l.code && styles.radioActive]}>
                {lang === l.code && <View style={styles.radioDot} />}
              </View>
            </TouchableOpacity>
            {i < LANGS.length - 1 && <View style={styles.divider} />}
          </React.Fragment>
        ))}
      </View>

      {/* Biometrics */}
      {available && enrolled && (
        <>
          <Text style={styles.sectionTitle}>{t(lang, 'biometricSettings')}</Text>
          <View style={styles.card}>
            <View style={styles.switchRow}>
              <View style={{ flex: 1 }}>
                <Text style={styles.switchLabel}>{t(lang, 'loginWithBiometrics')}</Text>
                <Text style={styles.switchSub}>{enabled ? 'Biometric login is enabled' : 'Biometric login is disabled'}</Text>
              </View>
              <Switch
                value={enabled}
                onValueChange={toggleBiometrics}
                trackColor={{ false: Colors.border, true: Colors.primary }}
                thumbColor="#fff"
              />
            </View>
          </View>
        </>
      )}

      {/* About */}
      <Text style={styles.sectionTitle}>About</Text>
      <View style={styles.card}>
        <View style={styles.aboutRow}>
          <Text style={styles.aboutLabel}>App</Text>
          <Text style={styles.aboutValue}>SANLY Officer</Text>
        </View>
        <View style={styles.divider} />
        <View style={styles.aboutRow}>
          <Text style={styles.aboutLabel}>{t(lang, 'appVersion')}</Text>
          <Text style={styles.aboutValue}>1.0.0</Text>
        </View>
        <View style={styles.divider} />
        <View style={styles.aboutRow}>
          <Text style={styles.aboutLabel}>Platform</Text>
          <Text style={styles.aboutValue}>Sanly e-Gov</Text>
        </View>
      </View>
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 40 },
  sectionTitle: { fontSize: 11, fontWeight: '700', color: Colors.textSecondary, textTransform: 'uppercase', letterSpacing: 0.8, marginBottom: Spacing.sm, marginTop: Spacing.md, paddingLeft: 4 },
  card: { backgroundColor: Colors.card, borderRadius: Radius.lg, overflow: 'hidden', ...Shadow.sm, marginBottom: Spacing.lg },
  langRow: { flexDirection: 'row', alignItems: 'center', padding: Spacing.lg },
  langNative: { fontSize: 15, fontWeight: '500', color: Colors.dark },
  langLabel: { fontSize: 12, color: Colors.textMuted, marginTop: 2 },
  radio: { width: 20, height: 20, borderRadius: 10, borderWidth: 2, borderColor: Colors.border, justifyContent: 'center', alignItems: 'center' },
  radioActive: { borderColor: Colors.primary },
  radioDot: { width: 10, height: 10, borderRadius: 5, backgroundColor: Colors.primary },
  divider: { height: StyleSheet.hairlineWidth, backgroundColor: Colors.border, marginLeft: Spacing.lg },
  switchRow: { flexDirection: 'row', alignItems: 'center', padding: Spacing.lg },
  switchLabel: { fontSize: 15, fontWeight: '500', color: Colors.dark },
  switchSub: { fontSize: 12, color: Colors.textMuted, marginTop: 2 },
  aboutRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: Spacing.lg },
  aboutLabel: { fontSize: 14, color: Colors.textSecondary },
  aboutValue: { fontSize: 14, color: Colors.dark, fontWeight: '500' },
})
