import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Eye, EyeOff, Globe, Shield } from 'lucide-react'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { t, type Lang } from '../utils/i18n'
import { validateNin } from '../utils/nin'
import { login, verifyOtp, resendOtp } from '../api/registry'

const LANGS: { value: Lang; label: string }[] = [
  { value: 'en', label: 'EN' },
  { value: 'tk', label: 'TK' },
  { value: 'ru', label: 'RU' },
]

const RESEND_COOLDOWN = 60

function parseJwt(token: string): Record<string, unknown> {
  try {
    return JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
  } catch {
    return {}
  }
}

export default function Login() {
  const navigate = useNavigate()
  const { setAuth, setOtpChallenge, clearOtpChallenge, otpStep, sessionToken, phoneMasked } = useAuthStore()
  const { lang, setLang } = useLangStore()

  // Step 1 state
  const [nin, setNin] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)

  // Step 2 state
  const [digits, setDigits] = useState(['', '', '', '', '', ''])
  const digitRefs = useRef<(HTMLInputElement | null)[]>([])
  const [resendCountdown, setResendCountdown] = useState(0)

  // Shared
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  // Resend countdown timer
  useEffect(() => {
    if (otpStep) setResendCountdown(RESEND_COOLDOWN)
  }, [otpStep])

  useEffect(() => {
    if (resendCountdown <= 0) return
    const id = setTimeout(() => setResendCountdown((c) => c - 1), 1000)
    return () => clearTimeout(id)
  }, [resendCountdown])

  // ── Step 1: credentials ───────────────────────────────────────────────────

  const handleCredentials = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    if (!validateNin(nin)) {
      setError('Please enter a valid 11-digit national ID number.')
      return
    }
    setLoading(true)
    try {
      const resp = await login(nin.trim(), password, lang.toUpperCase())
      if (resp.status === 'SUCCESS' && resp.token) {
        const claims = parseJwt(resp.token)
        const nationalId = (claims.nationalId as string) ?? (claims.sub as string) ?? nin.trim()
        setAuth(resp.token, nationalId, resp.roles ?? [], resp.sessionId ?? null)
        navigate('/dashboard', { replace: true })
      } else if (resp.status === 'OTP_REQUIRED' && resp.sessionToken) {
        setOtpChallenge(resp.sessionToken, resp.phoneMasked ?? '')
      } else if (resp.status === 'PHONE_REQUIRED') {
        setError(t(lang, 'phoneRequired'))
      }
    } catch {
      setError(t(lang, 'invalidCredentials'))
    } finally {
      setLoading(false)
    }
  }

  // ── Step 2: OTP verification ──────────────────────────────────────────────

  const handleDigitChange = (i: number, value: string) => {
    const d = value.replace(/\D/g, '').slice(-1)
    const next = [...digits]
    next[i] = d
    setDigits(next)
    if (d && i < 5) digitRefs.current[i + 1]?.focus()
    if (next.every((v) => v !== '')) submitOtp(next.join(''))
  }

  const handleDigitKeyDown = (i: number, e: React.KeyboardEvent) => {
    if (e.key === 'Backspace' && !digits[i] && i > 0) {
      digitRefs.current[i - 1]?.focus()
    }
  }

  const submitOtp = async (code: string) => {
    if (!sessionToken) return
    setError('')
    setLoading(true)
    try {
      const resp = await verifyOtp(sessionToken, code)
      if (resp.token) {
        const claims = parseJwt(resp.token)
        const nationalId = (claims.nationalId as string) ?? (claims.sub as string) ?? nin.trim()
        setAuth(resp.token, nationalId, resp.roles ?? ['ROLE_CITIZEN'], resp.sessionId ?? null)
        navigate('/dashboard', { replace: true })
      }
    } catch (err: unknown) {
      setDigits(['', '', '', '', '', ''])
      digitRefs.current[0]?.focus()
      const msg = (err as { response?: { data?: { message?: string } } })
        ?.response?.data?.message
      setError(msg ?? t(lang, 'otpInvalid'))
    } finally {
      setLoading(false)
    }
  }

  const handleResend = async () => {
    if (!sessionToken || resendCountdown > 0) return
    setError('')
    try {
      await resendOtp(sessionToken, lang.toUpperCase())
      setResendCountdown(RESEND_COOLDOWN)
      setDigits(['', '', '', '', '', ''])
      digitRefs.current[0]?.focus()
    } catch {
      setError('Could not resend. Please try again.')
    }
  }

  // ── Shared panels ─────────────────────────────────────────────────────────

  const LeftPanel = (
    <div className="hidden lg:flex lg:w-2/5 xl:w-1/2 bg-dark flex-col justify-between p-12">
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-xl bg-primary flex items-center justify-center">
          <span className="text-white font-bold text-lg">S</span>
        </div>
        <span className="text-white font-bold text-xl tracking-tight">SANLY</span>
      </div>
      <div>
        <h1 className="text-4xl font-bold text-white leading-tight mb-4">{t(lang, 'tagline')}</h1>
        <p className="text-white/50 text-lg">Türkmenistanyň sanly döwlet hyzmatlary portaly</p>
        <div className="mt-12 grid grid-cols-2 gap-4">
          {[
            { num: '15+', label: 'Government Services' },
            { num: '100%', label: 'Encrypted' },
            { num: '24/7', label: 'Available' },
            { num: '2FA', label: 'Secured' },
          ].map(({ num, label }) => (
            <div key={label} className="bg-white/5 rounded-xl p-4">
              <p className="text-primary-light text-2xl font-bold">{num}</p>
              <p className="text-white/50 text-sm mt-1">{label}</p>
            </div>
          ))}
        </div>
      </div>
      <p className="text-white/20 text-sm">&copy; 2024 Türkmenistanyň Sanly Dolandyryş Ministrligi</p>
    </div>
  )

  const LangSwitcher = (
    <div className="flex items-center justify-end gap-1 mb-8">
      <Globe className="w-4 h-4 text-gray-400 mr-1" />
      {LANGS.map(({ value, label }) => (
        <button
          key={value}
          onClick={() => setLang(value)}
          className={`px-3 py-1 rounded-lg text-xs font-medium transition-colors ${
            lang === value ? 'bg-primary text-white' : 'text-gray-500 hover:bg-gray-100'
          }`}
        >
          {label}
        </button>
      ))}
    </div>
  )

  // ── OTP step ──────────────────────────────────────────────────────────────

  if (otpStep) {
    return (
      <div className="min-h-screen bg-background flex">
        {LeftPanel}
        <div className="flex-1 flex flex-col items-center justify-center p-6 lg:p-12">
          <div className="lg:hidden flex items-center gap-2 mb-8">
            <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center">
              <span className="text-white font-bold">S</span>
            </div>
            <span className="font-bold text-dark text-xl">SANLY</span>
          </div>

          <div className="w-full max-w-sm">
            {LangSwitcher}

            <div className="flex items-center gap-3 mb-6">
              <div className="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center">
                <Shield className="w-5 h-5 text-primary" />
              </div>
              <div>
                <h2 className="text-2xl font-bold text-dark">{t(lang, 'otpTitle')}</h2>
                <p className="text-gray-500 text-sm">
                  {t(lang, 'otpSubtitle')} <span className="font-medium text-dark">{phoneMasked}</span>
                </p>
              </div>
            </div>

            {/* 6 digit boxes */}
            <div className="flex gap-2 mb-6">
              {digits.map((d, i) => (
                <input
                  key={i}
                  ref={(el) => { digitRefs.current[i] = el }}
                  type="text"
                  inputMode="numeric"
                  maxLength={1}
                  value={d}
                  onChange={(e) => handleDigitChange(i, e.target.value)}
                  onKeyDown={(e) => handleDigitKeyDown(i, e)}
                  className="w-full aspect-square text-center text-xl font-bold border-2 rounded-xl
                             border-gray-200 focus:border-primary focus:outline-none transition-colors
                             disabled:opacity-50"
                  disabled={loading}
                  autoFocus={i === 0}
                />
              ))}
            </div>

            {error && (
              <div className="flex items-center gap-2 px-3 py-2.5 rounded-lg bg-red-50 border border-red-100 mb-4">
                <span className="text-sm text-red-600">{error}</span>
              </div>
            )}

            <button
              className="btn-primary w-full py-3 mb-4"
              disabled={loading || digits.some((d) => !d)}
              onClick={() => submitOtp(digits.join(''))}
            >
              {loading ? t(lang, 'verifyingOtp') : t(lang, 'verifyOtp')}
            </button>

            {/* Resend */}
            <div className="text-center text-sm text-gray-500">
              {resendCountdown > 0 ? (
                <span>{t(lang, 'resendIn')} {resendCountdown}s</span>
              ) : (
                <button
                  className="text-primary hover:underline font-medium"
                  onClick={handleResend}
                >
                  {t(lang, 'resendOtp')}
                </button>
              )}
            </div>

            <button
              className="mt-4 w-full text-center text-sm text-gray-400 hover:text-gray-600"
              onClick={() => { clearOtpChallenge(); setError('') }}
            >
              &larr; {t(lang, 'back')}
            </button>
          </div>
        </div>
      </div>
    )
  }

  // ── Step 1: credentials ───────────────────────────────────────────────────

  return (
    <div className="min-h-screen bg-background flex">
      {LeftPanel}
      <div className="flex-1 flex flex-col items-center justify-center p-6 lg:p-12">
        <div className="lg:hidden flex items-center gap-2 mb-8">
          <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center">
            <span className="text-white font-bold">S</span>
          </div>
          <span className="font-bold text-dark text-xl">SANLY</span>
        </div>

        <div className="w-full max-w-sm">
          {LangSwitcher}

          <h2 className="text-2xl font-bold text-dark mb-1">{t(lang, 'signIn')}</h2>
          <p className="text-gray-500 text-sm mb-8">{t(lang, 'tagline')}</p>

          <form onSubmit={handleCredentials} className="space-y-5">
            <div>
              <label className="label">{t(lang, 'nationalId')}</label>
              <input
                type="text"
                inputMode="numeric"
                pattern="\d{11}"
                maxLength={11}
                className="input font-mono tracking-widest"
                placeholder={t(lang, 'nationalIdPlaceholder')}
                value={nin}
                onChange={(e) => setNin(e.target.value.replace(/\D/g, ''))}
                required
              />
            </div>

            <div>
              <label className="label">{t(lang, 'password')}</label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  className="input pr-11"
                  placeholder={t(lang, 'passwordPlaceholder')}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            {error && (
              <div className="flex items-center gap-2 px-3 py-2.5 rounded-lg bg-red-50 border border-red-100">
                <span className="text-sm text-red-600">{error}</span>
              </div>
            )}

            <button type="submit" className="btn-primary w-full py-3" disabled={loading}>
              {loading ? t(lang, 'signingIn') : t(lang, 'signIn')}
            </button>
          </form>

          <div className="mt-6 p-4 rounded-xl bg-primary/5 border border-primary/10">
            <p className="text-xs text-primary font-medium mb-1">TM-NIN Format</p>
            <p className="text-xs text-gray-500">
              Your 11-digit national identification number printed on your ID card.
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
