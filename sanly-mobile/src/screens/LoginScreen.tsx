import React, { useEffect, useRef, useState } from 'react'
import {
  View, Text, TextInput, TouchableOpacity, StyleSheet,
  KeyboardAvoidingView, Platform, ScrollView, Alert,
} from 'react-native'
import { LinearGradient } from 'expo-linear-gradient'
import { Shield, Eye, EyeOff } from 'lucide-react-native'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { useBiometrics } from '../hooks/useBiometrics'
import { login, verifyOtp, resendOtp } from '../api/registry'
import { t } from '../i18n'
import { Colors, Radius, Spacing } from '../constants/theme'
import type { Lang } from '../i18n'

type Step = 'credentials' | 'otp'

export function LoginScreen() {
  const { setAuth, setOtpChallenge, sessionToken, phoneMasked, otpStep } = useAuthStore()
  const { lang, setLang } = useLangStore()
  const { canUseBiometrics, authenticate } = useBiometrics()

  const [step, setStep] = useState<Step>(otpStep ? 'otp' : 'credentials')
  const [nin, setNin] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [otp, setOtp] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [resendCountdown, setResendCountdown] = useState(0)
  const countdownRef = useRef<ReturnType<typeof setInterval> | null>(null)

  useEffect(() => {
    if (otpStep) setStep('otp')
  }, [otpStep])

  useEffect(() => {
    return () => { if (countdownRef.current) clearInterval(countdownRef.current) }
  }, [])

  const startCountdown = () => {
    setResendCountdown(60)
    countdownRef.current = setInterval(() => {
      setResendCountdown(prev => {
        if (prev <= 1) { clearInterval(countdownRef.current!); return 0 }
        return prev - 1
      })
    }, 1000)
  }

  const handleLogin = async () => {
    if (!nin.trim() || !password.trim()) return
    setLoading(true)
    setError(null)
    try {
      const langCode = lang.toUpperCase()
      const res = await login(nin.trim(), password.trim(), langCode)
      if (res.status === 'SUCCESS' && res.token && res.username) {
        await setAuth(res.token, res.username, res.roles ?? [], res.sessionId)
      } else if (res.status === 'OTP_REQUIRED' && res.sessionToken) {
        setOtpChallenge(res.sessionToken, res.phoneMasked ?? '')
        setStep('otp')
        startCountdown()
      } else if (res.status === 'PHONE_REQUIRED') {
        setError(t(lang, 'phoneRequired'))
      } else {
        setError(res.message ?? t(lang, 'invalidCredentials'))
      }
    } catch (e: any) {
      setError(e.response?.data?.message ?? t(lang, 'invalidCredentials'))
    } finally {
      setLoading(false)
    }
  }

  const handleVerifyOtp = async () => {
    if (!otp.trim() || !sessionToken) return
    setLoading(true)
    setError(null)
    try {
      const res = await verifyOtp(sessionToken, otp.trim())
      if (res.status === 'SUCCESS' && res.token && res.username) {
        await setAuth(res.token, res.username, res.roles ?? [], res.sessionId)
      } else {
        setError(res.message ?? t(lang, 'otpInvalid'))
      }
    } catch (e: any) {
      const msg = e.response?.data?.message ?? t(lang, 'otpInvalid')
      if (msg.toLowerCase().includes('lock')) {
        setError(t(lang, 'otpLocked'))
      } else {
        setError(msg)
      }
    } finally {
      setLoading(false)
    }
  }

  const handleResend = async () => {
    if (resendCountdown > 0 || !sessionToken) return
    try {
      await resendOtp(sessionToken, lang.toUpperCase())
      startCountdown()
    } catch { /* ignore */ }
  }

  const handleBiometric = async () => {
    const success = await authenticate()
    if (!success) Alert.alert('Authentication Failed', 'Could not verify biometrics.')
    // If success, the token is already in the store from hydrate — no action needed
  }

  const langs: Lang[] = ['en', 'tk', 'ru']
  const langLabels: Record<Lang, string> = { en: 'EN', tk: 'TK', ru: 'RU' }

  return (
    <KeyboardAvoidingView style={styles.flex} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <LinearGradient colors={['#0D7377', '#0A5C5F']} style={styles.header}>
        <View style={styles.logoWrap}>
          <Shield size={36} color="#fff" />
        </View>
        <Text style={styles.logoText}>SANLY</Text>
        <Text style={styles.tagline}>{t(lang, 'tagline')}</Text>
        <View style={styles.langRow}>
          {langs.map(l => (
            <TouchableOpacity
              key={l}
              style={[styles.langBtn, lang === l && styles.langBtnActive]}
              onPress={() => setLang(l)}
            >
              <Text style={[styles.langBtnText, lang === l && styles.langBtnTextActive]}>
                {langLabels[l]}
              </Text>
            </TouchableOpacity>
          ))}
        </View>
      </LinearGradient>

      <ScrollView
        style={styles.body}
        contentContainerStyle={styles.bodyContent}
        keyboardShouldPersistTaps="handled"
      >
        {step === 'credentials' ? (
          <>
            <Text style={styles.formTitle}>{t(lang, 'signIn')}</Text>

            <View style={styles.field}>
              <Text style={styles.fieldLabel}>{t(lang, 'nationalId')}</Text>
              <TextInput
                style={styles.input}
                value={nin}
                onChangeText={setNin}
                placeholder={t(lang, 'nationalIdPlaceholder')}
                placeholderTextColor={Colors.textMuted}
                autoCapitalize="characters"
                autoCorrect={false}
              />
            </View>

            <View style={styles.field}>
              <Text style={styles.fieldLabel}>{t(lang, 'password')}</Text>
              <View style={styles.inputRow}>
                <TextInput
                  style={[styles.input, styles.inputFlex]}
                  value={password}
                  onChangeText={setPassword}
                  placeholder={t(lang, 'passwordPlaceholder')}
                  placeholderTextColor={Colors.textMuted}
                  secureTextEntry={!showPassword}
                  autoCapitalize="none"
                  autoCorrect={false}
                  onSubmitEditing={handleLogin}
                />
                <TouchableOpacity onPress={() => setShowPassword(v => !v)} style={styles.eyeBtn}>
                  {showPassword
                    ? <EyeOff size={18} color={Colors.textSecondary} />
                    : <Eye size={18} color={Colors.textSecondary} />}
                </TouchableOpacity>
              </View>
            </View>

            {error && <Text style={styles.error}>{error}</Text>}

            <TouchableOpacity
              style={[styles.primaryBtn, loading && styles.btnDisabled]}
              onPress={handleLogin}
              disabled={loading}
              activeOpacity={0.8}
            >
              <Text style={styles.primaryBtnText}>
                {loading ? t(lang, 'signingIn') : t(lang, 'signIn')}
              </Text>
            </TouchableOpacity>

            {canUseBiometrics && (
              <TouchableOpacity style={styles.biometricBtn} onPress={handleBiometric} activeOpacity={0.7}>
                <Text style={styles.biometricBtnText}>{t(lang, 'biometricTitle')}</Text>
              </TouchableOpacity>
            )}
          </>
        ) : (
          <>
            <Text style={styles.formTitle}>{t(lang, 'otpTitle')}</Text>
            {phoneMasked && (
              <Text style={styles.otpSubtitle}>
                {t(lang, 'otpSubtitle')} {phoneMasked}
              </Text>
            )}

            <View style={styles.field}>
              <Text style={styles.fieldLabel}>{t(lang, 'enterOtp')}</Text>
              <TextInput
                style={[styles.input, styles.otpInput]}
                value={otp}
                onChangeText={setOtp}
                placeholder="• • • • • •"
                placeholderTextColor={Colors.textMuted}
                keyboardType="number-pad"
                maxLength={6}
                textAlign="center"
              />
            </View>

            {error && <Text style={styles.error}>{error}</Text>}

            <TouchableOpacity
              style={[styles.primaryBtn, loading && styles.btnDisabled]}
              onPress={handleVerifyOtp}
              disabled={loading}
              activeOpacity={0.8}
            >
              <Text style={styles.primaryBtnText}>
                {loading ? t(lang, 'verifyingOtp') : t(lang, 'verifyOtp')}
              </Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={[styles.resendBtn, resendCountdown > 0 && styles.resendDisabled]}
              onPress={handleResend}
              disabled={resendCountdown > 0}
            >
              <Text style={[styles.resendText, resendCountdown > 0 && styles.resendTextDisabled]}>
                {resendCountdown > 0 ? `${t(lang, 'resendIn')} ${resendCountdown}s` : t(lang, 'resendOtp')}
              </Text>
            </TouchableOpacity>
          </>
        )}
      </ScrollView>
    </KeyboardAvoidingView>
  )
}

const styles = StyleSheet.create({
  flex: { flex: 1, backgroundColor: Colors.background },
  header: { paddingTop: 64, paddingBottom: 32, alignItems: 'center' },
  logoWrap: { width: 64, height: 64, borderRadius: 20, backgroundColor: 'rgba(255,255,255,0.15)', justifyContent: 'center', alignItems: 'center', marginBottom: 12 },
  logoText: { fontSize: 28, fontWeight: '800', color: '#fff', letterSpacing: 3 },
  tagline: { fontSize: 13, color: 'rgba(255,255,255,0.75)', marginTop: 4, marginBottom: 20 },
  langRow: { flexDirection: 'row', gap: 8 },
  langBtn: { paddingHorizontal: 12, paddingVertical: 5, borderRadius: Radius.full, borderWidth: 1, borderColor: 'rgba(255,255,255,0.4)' },
  langBtnActive: { backgroundColor: '#fff' },
  langBtnText: { fontSize: 12, fontWeight: '600', color: 'rgba(255,255,255,0.85)' },
  langBtnTextActive: { color: Colors.primary },
  body: { flex: 1 },
  bodyContent: { padding: Spacing.xl, paddingTop: Spacing.xxl },
  formTitle: { fontSize: 22, fontWeight: '700', color: Colors.dark, marginBottom: Spacing.xl },
  field: { marginBottom: Spacing.lg },
  fieldLabel: { fontSize: 13, fontWeight: '600', color: Colors.textSecondary, marginBottom: Spacing.xs },
  input: {
    backgroundColor: Colors.card,
    borderWidth: 1.5,
    borderColor: Colors.border,
    borderRadius: Radius.md,
    paddingHorizontal: Spacing.md,
    paddingVertical: 12,
    fontSize: 15,
    color: Colors.text,
  },
  inputRow: { flexDirection: 'row', alignItems: 'center' },
  inputFlex: { flex: 1 },
  eyeBtn: { position: 'absolute', right: 12 },
  otpInput: { fontSize: 22, letterSpacing: 8, fontWeight: '700' },
  otpSubtitle: { fontSize: 14, color: Colors.textSecondary, marginBottom: Spacing.lg, lineHeight: 20 },
  error: { color: Colors.error, fontSize: 13, marginBottom: Spacing.md, backgroundColor: Colors.errorBg, padding: Spacing.md, borderRadius: Radius.md },
  primaryBtn: {
    backgroundColor: Colors.primary,
    borderRadius: Radius.md,
    paddingVertical: 15,
    alignItems: 'center',
    marginTop: Spacing.sm,
  },
  btnDisabled: { opacity: 0.6 },
  primaryBtnText: { color: '#fff', fontSize: 16, fontWeight: '700' },
  biometricBtn: {
    borderWidth: 1.5,
    borderColor: Colors.primary,
    borderRadius: Radius.md,
    paddingVertical: 13,
    alignItems: 'center',
    marginTop: Spacing.md,
  },
  biometricBtnText: { color: Colors.primary, fontSize: 15, fontWeight: '600' },
  resendBtn: { alignItems: 'center', marginTop: Spacing.lg },
  resendDisabled: {},
  resendText: { fontSize: 14, color: Colors.primary, fontWeight: '600' },
  resendTextDisabled: { color: Colors.textMuted },
})
