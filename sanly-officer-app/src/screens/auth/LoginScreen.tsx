import React, { useState } from 'react'
import {
  View, Text, TextInput, TouchableOpacity, ScrollView,
  StyleSheet, Alert, KeyboardAvoidingView, Platform,
} from 'react-native'
import { LinearGradient } from 'expo-linear-gradient'
import { Eye, EyeOff, Fingerprint, Shield } from 'lucide-react-native'
import { useAuthStore } from '../../store/authStore'
import { useLangStore } from '../../store/langStore'
import { useBiometrics } from '../../hooks/useBiometric'
import { loginOfficer } from '../../api/registry'
import { t } from '../../i18n'
import type { Lang } from '../../i18n'
import type { Institution } from '../../types'
import { Colors, Spacing, Radius, Shadow } from '../../constants/theme'

const INSTITUTIONS: { code: Institution; labelKey: 'inst_police' | 'inst_dmv' | 'inst_medical' | 'inst_customs' | 'inst_civil' | 'inst_court' | 'inst_education' | 'inst_land' | 'inst_tax' | 'inst_social'; color: string }[] = [
  { code: 'POLICE',    labelKey: 'inst_police',    color: Colors.police },
  { code: 'DMV',       labelKey: 'inst_dmv',       color: Colors.dmv },
  { code: 'MEDICAL',   labelKey: 'inst_medical',   color: Colors.medical },
  { code: 'CUSTOMS',   labelKey: 'inst_customs',   color: Colors.customs },
  { code: 'CIVIL',     labelKey: 'inst_civil',     color: Colors.civil },
  { code: 'COURT',     labelKey: 'inst_court',     color: Colors.court },
  { code: 'EDUCATION', labelKey: 'inst_education', color: Colors.education },
  { code: 'LAND',      labelKey: 'inst_land',      color: Colors.land },
  { code: 'TAX',       labelKey: 'inst_tax',       color: Colors.tax },
  { code: 'SOCIAL',    labelKey: 'inst_social',    color: Colors.social },
]

const LANGS: { code: Lang; label: string }[] = [
  { code: 'en', label: 'EN' },
  { code: 'tk', label: 'TK' },
  { code: 'ru', label: 'RU' },
]

export function LoginScreen() {
  const { setAuth } = useAuthStore()
  const { lang, setLang } = useLangStore()
  const { canUseBiometrics, authenticate } = useBiometrics()

  const [officerId, setOfficerId] = useState('')
  const [password, setPassword] = useState('')
  const [showPass, setShowPass] = useState(false)
  const [institution, setInstitution] = useState<Institution | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleLogin() {
    if (!officerId.trim() || !password.trim()) {
      setError('Please enter your Officer ID and password.')
      return
    }
    setLoading(true)
    setError(null)
    try {
      const res = await loginOfficer(officerId.trim(), password.trim())
      await setAuth(res.token)
    } catch (e: any) {
      setError(e?.response?.data?.message ?? t(lang, 'invalidCredentials'))
    } finally {
      setLoading(false)
    }
  }

  async function handleBiometricLogin() {
    const ok = await authenticate('Authenticate to sign in to SANLY Officer')
    if (ok) {
      // Biometric re-auth: token already stored in SecureStore by authStore.hydrate()
      // If we got here, auth is already set — just navigate
    } else {
      Alert.alert('Biometric Failed', 'Please enter your credentials manually.')
    }
  }

  return (
    <KeyboardAvoidingView style={{ flex: 1 }} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <LinearGradient colors={[Colors.navyDark, Colors.navy, Colors.navyLight]} style={styles.gradient}>
        {/* Header */}
        <View style={styles.header}>
          <View style={styles.logoWrap}>
            <Shield size={28} color={Colors.primary} />
          </View>
          <Text style={styles.appName}>SANLY Officer</Text>
          <Text style={styles.tagline}>Secure Government Field Operations</Text>

          {/* Language selector */}
          <View style={styles.langRow}>
            {LANGS.map(l => (
              <TouchableOpacity
                key={l.code}
                style={[styles.langChip, lang === l.code && styles.langChipActive]}
                onPress={() => setLang(l.code)}
              >
                <Text style={[styles.langText, lang === l.code && styles.langTextActive]}>{l.label}</Text>
              </TouchableOpacity>
            ))}
          </View>
        </View>

        {/* Form */}
        <ScrollView style={styles.formScroll} contentContainerStyle={styles.form} keyboardShouldPersistTaps="handled">
          <Text style={styles.formTitle}>{t(lang, 'officerLogin')}</Text>

          {/* Institution selector */}
          <Text style={styles.fieldLabel}>{t(lang, 'institution')}</Text>
          <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.institutionScroll} contentContainerStyle={styles.institutionRow}>
            {INSTITUTIONS.map(inst => (
              <TouchableOpacity
                key={inst.code}
                style={[styles.instChip, institution === inst.code && { backgroundColor: inst.color, borderColor: inst.color }]}
                onPress={() => setInstitution(prev => prev === inst.code ? null : inst.code)}
              >
                <Text style={[styles.instText, institution === inst.code && styles.instTextActive]}>
                  {t(lang, inst.labelKey)}
                </Text>
              </TouchableOpacity>
            ))}
          </ScrollView>

          {/* Officer ID */}
          <Text style={styles.fieldLabel}>{t(lang, 'officerId')}</Text>
          <View style={styles.inputWrap}>
            <TextInput
              style={styles.input}
              value={officerId}
              onChangeText={setOfficerId}
              placeholder="e.g. officer.annamuradov"
              placeholderTextColor={Colors.textMuted}
              autoCapitalize="none"
              autoCorrect={false}
              returnKeyType="next"
            />
          </View>

          {/* Password */}
          <Text style={styles.fieldLabel}>{t(lang, 'password')}</Text>
          <View style={styles.inputWrap}>
            <TextInput
              style={[styles.input, { flex: 1 }]}
              value={password}
              onChangeText={setPassword}
              placeholder="••••••••"
              placeholderTextColor={Colors.textMuted}
              secureTextEntry={!showPass}
              returnKeyType="go"
              onSubmitEditing={handleLogin}
            />
            <TouchableOpacity onPress={() => setShowPass(v => !v)} style={styles.eyeBtn}>
              {showPass ? <EyeOff size={18} color={Colors.textMuted} /> : <Eye size={18} color={Colors.textMuted} />}
            </TouchableOpacity>
          </View>

          {error && <Text style={styles.error}>{error}</Text>}

          <TouchableOpacity
            style={[styles.loginBtn, loading && styles.loginBtnDisabled]}
            onPress={handleLogin}
            disabled={loading}
          >
            <Text style={styles.loginBtnText}>{loading ? t(lang, 'loggingIn') : t(lang, 'login')}</Text>
          </TouchableOpacity>

          {canUseBiometrics && (
            <TouchableOpacity style={styles.bioBtn} onPress={handleBiometricLogin}>
              <Fingerprint size={20} color={Colors.primary} />
              <Text style={styles.bioBtnText}>{t(lang, 'loginWithBiometrics')}</Text>
            </TouchableOpacity>
          )}

          <Text style={styles.disclaimer}>
            This application is for authorised SANLY government personnel only. Unauthorized access is prohibited.
          </Text>
        </ScrollView>
      </LinearGradient>
    </KeyboardAvoidingView>
  )
}

const styles = StyleSheet.create({
  gradient: { flex: 1 },
  header: { paddingTop: 60, paddingHorizontal: Spacing.xl, paddingBottom: Spacing.xl, alignItems: 'center' },
  logoWrap: { width: 64, height: 64, borderRadius: 32, backgroundColor: Colors.navyMid, justifyContent: 'center', alignItems: 'center', marginBottom: Spacing.md, borderWidth: 2, borderColor: Colors.primary },
  appName: { fontSize: 24, fontWeight: '800', color: '#fff', letterSpacing: 1 },
  tagline: { fontSize: 12, color: 'rgba(255,255,255,0.5)', marginTop: 4, letterSpacing: 0.5 },
  langRow: { flexDirection: 'row', gap: Spacing.sm, marginTop: Spacing.lg },
  langChip: { paddingHorizontal: 14, paddingVertical: 6, borderRadius: Radius.full, borderWidth: 1, borderColor: 'rgba(255,255,255,0.2)' },
  langChipActive: { backgroundColor: Colors.primary, borderColor: Colors.primary },
  langText: { fontSize: 12, fontWeight: '600', color: 'rgba(255,255,255,0.6)' },
  langTextActive: { color: '#fff' },
  formScroll: { flex: 1 },
  form: { backgroundColor: Colors.background, borderTopLeftRadius: 24, borderTopRightRadius: 24, padding: Spacing.xl, paddingBottom: 48 },
  formTitle: { fontSize: 20, fontWeight: '700', color: Colors.dark, marginBottom: Spacing.xl },
  fieldLabel: { fontSize: 12, fontWeight: '600', color: Colors.textSecondary, marginBottom: Spacing.sm, textTransform: 'uppercase', letterSpacing: 0.5 },
  institutionScroll: { marginBottom: Spacing.lg },
  institutionRow: { gap: Spacing.sm, paddingVertical: 2 },
  instChip: { paddingHorizontal: 12, paddingVertical: 8, borderRadius: Radius.full, borderWidth: 1.5, borderColor: Colors.border, backgroundColor: Colors.card },
  instText: { fontSize: 12, fontWeight: '600', color: Colors.textSecondary },
  instTextActive: { color: '#fff' },
  inputWrap: { flexDirection: 'row', alignItems: 'center', backgroundColor: Colors.card, borderRadius: Radius.md, borderWidth: 1, borderColor: Colors.border, paddingHorizontal: Spacing.md, marginBottom: Spacing.lg, ...Shadow.sm },
  input: { flex: 1, height: 48, fontSize: 15, color: Colors.dark },
  eyeBtn: { padding: Spacing.sm },
  error: { color: Colors.error, fontSize: 13, marginBottom: Spacing.md },
  loginBtn: { backgroundColor: Colors.navy, borderRadius: Radius.lg, height: 52, justifyContent: 'center', alignItems: 'center', ...Shadow.md },
  loginBtnDisabled: { opacity: 0.6 },
  loginBtnText: { color: '#fff', fontWeight: '700', fontSize: 16, letterSpacing: 0.5 },
  bioBtn: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: Spacing.sm, marginTop: Spacing.lg, padding: Spacing.md },
  bioBtnText: { color: Colors.primary, fontWeight: '600', fontSize: 14 },
  disclaimer: { marginTop: Spacing.xl, fontSize: 11, color: Colors.textMuted, textAlign: 'center', lineHeight: 16 },
})
