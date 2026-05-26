import { useRef, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Building2, Shield, CheckCircle2, XCircle, Clock, AlertTriangle, ChevronDown, ChevronRight } from 'lucide-react'
import { useLangStore } from '../store/langStore'
import { t } from '../utils/i18n'
import {
  getPendingConsents, getConsentDetail, approveConsent, rejectConsent, getAccessHistory,
  type ConsentRequest, type BankDataAccess,
} from '../api/banking'
import Badge from '../components/ui/Badge'

const SCOPE_DESC: Record<string, string> = {
  IDENTITY_BASIC: 'Your name, date of birth, and national ID number (masked)',
  IDENTITY_FULL: 'Your full name, date of birth, address, and masked phone number',
  TAX_STATUS: 'Whether your taxes are up-to-date (compliant or non-compliant)',
  TAX_INCOME_CLASS: 'Your income bracket (low/medium/high) — not exact salary',
  CRIMINAL_CLEARANCE: 'Whether you have any criminal record (yes/no only, no details)',
  BUSINESS_OWNERSHIP: 'List of businesses registered under your name',
  PROPERTY_OWNERSHIP: 'Number of properties owned and total value range',
  PENSION_STATUS: 'Your pension account status and estimated monthly pension range',
  EMPLOYMENT_STATUS: 'Whether you have active pension contributions (employment indicator)',
  MEDICAL_CLEARANCE: 'Whether you have any serious medical conditions on record (yes/no only)',
  DRIVING_LICENSE: 'Your driving license status, categories, and expiry date',
}

function scopeLabel(scope: string): string {
  const SCOPE_LABELS: Record<string, string> = {
    IDENTITY_BASIC: 'Basic Identity', IDENTITY_FULL: 'Full Identity',
    TAX_STATUS: 'Tax Status', TAX_INCOME_CLASS: 'Income Classification',
    CRIMINAL_CLEARANCE: 'Criminal Clearance', BUSINESS_OWNERSHIP: 'Business Ownership',
    PROPERTY_OWNERSHIP: 'Property Ownership', PENSION_STATUS: 'Pension Status',
    EMPLOYMENT_STATUS: 'Employment Status', MEDICAL_CLEARANCE: 'Medical Clearance',
    DRIVING_LICENSE: 'Driving License',
  }
  return SCOPE_LABELS[scope] ?? scope.replace(/_/g, ' ')
}

function timeRemaining(expiresAt: string): string {
  const diff = new Date(expiresAt).getTime() - Date.now()
  if (diff <= 0) return 'Expired'
  const mins = Math.floor(diff / 60000)
  const secs = Math.floor((diff % 60000) / 1000)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

function statusBadge(status: string): 'green' | 'yellow' | 'red' | 'gray' {
  if (status === 'APPROVED') return 'green'
  if (status === 'PENDING') return 'yellow'
  if (status === 'REJECTED') return 'red'
  return 'gray'
}

// ── Consent Approval Card ────────────────────────────────────────────────────
function ConsentApprovalCard({ consent }: { consent: ConsentRequest }) {
  const { lang } = useLangStore()
  const qc = useQueryClient()
  const [expanded, setExpanded] = useState(true)
  const [selectedScopes, setSelectedScopes] = useState<string[]>(
    consent.requestedScopes.map(s => s.scope)
  )
  const [otpDigits, setOtpDigits] = useState(['', '', '', '', '', ''])
  const otpRefs = useRef<(HTMLInputElement | null)[]>([])
  const [result, setResult] = useState<'approved' | 'rejected' | null>(null)

  const approveMutation = useMutation({
    mutationFn: () => approveConsent(consent.consentCode, selectedScopes, otpDigits.join('')),
    onSuccess: () => { setResult('approved'); qc.invalidateQueries({ queryKey: ['pending-consents'] }) },
  })
  const rejectMutation = useMutation({
    mutationFn: () => rejectConsent(consent.consentCode),
    onSuccess: () => { setResult('rejected'); qc.invalidateQueries({ queryKey: ['pending-consents'] }) },
  })

  function handleOtpChange(i: number, val: string) {
    const digits = [...otpDigits]
    digits[i] = val.replace(/\D/g, '').slice(-1)
    setOtpDigits(digits)
    if (val && i < 5) otpRefs.current[i + 1]?.focus()
  }

  function handleOtpKeyDown(i: number, e: React.KeyboardEvent) {
    if (e.key === 'Backspace' && !otpDigits[i] && i > 0) otpRefs.current[i - 1]?.focus()
  }

  function toggleScope(scope: string) {
    setSelectedScopes(prev => prev.includes(scope) ? prev.filter(s => s !== scope) : [...prev, scope])
  }

  if (result === 'approved') {
    return (
      <div className="bg-green-50 border border-green-200 rounded-2xl p-5 flex items-center gap-3">
        <CheckCircle2 className="w-6 h-6 text-green-600 shrink-0" />
        <div>
          <p className="font-semibold text-green-800">{t(lang, 'consentApproved')}</p>
          <p className="text-sm text-green-700 mt-0.5">{consent.bankName} — {consent.consentCode}</p>
        </div>
      </div>
    )
  }
  if (result === 'rejected') {
    return (
      <div className="bg-red-50 border border-red-200 rounded-2xl p-5 flex items-center gap-3">
        <XCircle className="w-6 h-6 text-red-600 shrink-0" />
        <p className="font-semibold text-red-800">{t(lang, 'consentRejected')}</p>
      </div>
    )
  }

  return (
    <div className="bg-white border-2 border-yellow-300 rounded-2xl shadow-sm overflow-hidden">
      {/* Header */}
      <div className="flex items-center gap-3 px-5 py-4 bg-yellow-50 border-b border-yellow-200">
        <AlertTriangle className="w-5 h-5 text-yellow-600 shrink-0" />
        <div className="flex-1 min-w-0">
          <p className="font-semibold text-gray-900">{consent.bankName}</p>
          <p className="text-xs text-gray-500 font-mono">{consent.consentCode}</p>
        </div>
        <div className="text-right shrink-0">
          <p className="text-xs text-gray-500">{t(lang, 'consentExpiresIn')}</p>
          <p className="text-sm font-bold text-red-600">{timeRemaining(consent.expiresAt)}</p>
        </div>
      </div>

      <div className="p-5 space-y-4">
        {/* Purpose */}
        <div>
          <p className="text-xs font-medium text-gray-500 mb-1">{t(lang, 'consentPurpose')}</p>
          <p className="text-sm text-gray-800 bg-gray-50 rounded-lg px-3 py-2">{consent.purpose}</p>
        </div>

        {/* Scope selector */}
        <div>
          <p className="text-xs font-medium text-gray-500 mb-2">{t(lang, 'selectScopesToApprove')}</p>
          <div className="space-y-2">
            {consent.requestedScopes.map(scope => (
              <label key={scope.scope} className="flex items-start gap-3 p-3 border border-gray-200 rounded-xl cursor-pointer hover:bg-gray-50">
                <input
                  type="checkbox"
                  checked={selectedScopes.includes(scope.scope)}
                  onChange={() => toggleScope(scope.scope)}
                  className="mt-0.5 w-4 h-4 rounded border-gray-300 text-blue-600"
                />
                <div>
                  <p className="text-sm font-medium text-gray-800">{scopeLabel(scope.scope)}</p>
                  <p className="text-xs text-gray-500 mt-0.5">{SCOPE_DESC[scope.scope] ?? scope.description}</p>
                </div>
              </label>
            ))}
          </div>
        </div>

        {/* OTP */}
        <div>
          <p className="text-xs font-medium text-gray-500 mb-2">{t(lang, 'enterOtpToApprove')}</p>
          <div className="flex gap-2">
            {otpDigits.map((d, i) => (
              <input
                key={i}
                ref={el => { otpRefs.current[i] = el }}
                value={d}
                onChange={e => handleOtpChange(i, e.target.value)}
                onKeyDown={e => handleOtpKeyDown(i, e)}
                maxLength={1}
                inputMode="numeric"
                className="w-11 h-12 text-center text-lg font-bold border-2 border-gray-200 rounded-xl focus:border-blue-500 focus:outline-none"
              />
            ))}
          </div>
        </div>

        {approveMutation.isError && (
          <p className="text-sm text-red-600">Approval failed. Please check your OTP and try again.</p>
        )}

        {/* Actions */}
        <div className="flex gap-3">
          <button
            onClick={() => approveMutation.mutate()}
            disabled={selectedScopes.length === 0 || otpDigits.join('').length < 6 || approveMutation.isPending}
            className="flex-1 py-2.5 bg-blue-600 text-white text-sm font-semibold rounded-xl hover:bg-blue-700 disabled:opacity-50 transition-colors"
          >
            {approveMutation.isPending ? t(lang, 'approvingConsent') : `${t(lang, 'approveConsent')} (${selectedScopes.length})`}
          </button>
          <button
            onClick={() => rejectMutation.mutate()}
            disabled={rejectMutation.isPending}
            className="px-4 py-2.5 border-2 border-red-200 text-red-600 text-sm font-semibold rounded-xl hover:bg-red-50 disabled:opacity-50 transition-colors"
          >
            {rejectMutation.isPending ? t(lang, 'rejectingConsent') : t(lang, 'rejectConsent')}
          </button>
        </div>
      </div>
    </div>
  )
}

// ── Access History Row ───────────────────────────────────────────────────────
function AccessHistoryRow({ access }: { access: BankDataAccess }) {
  const { lang } = useLangStore()
  const [expanded, setExpanded] = useState(false)
  const scopes = Array.isArray(access.scopesAccessed)
    ? access.scopesAccessed
    : JSON.parse(access.scopesAccessed ?? '[]')

  return (
    <div className="border border-gray-100 rounded-xl overflow-hidden">
      <button onClick={() => setExpanded(p => !p)}
        className="w-full flex items-center gap-3 px-4 py-3 hover:bg-gray-50 text-left">
        {expanded ? <ChevronDown className="w-4 h-4 text-gray-400 shrink-0" /> : <ChevronRight className="w-4 h-4 text-gray-400 shrink-0" />}
        <Building2 className="w-4 h-4 text-gray-400 shrink-0" />
        <div className="flex-1 min-w-0">
          <p className="text-sm font-medium text-gray-800">{access.bankName ?? access.bankCode}</p>
          <p className="text-xs text-gray-400">{new Date(access.accessedAt).toLocaleString()}</p>
        </div>
        <Badge color={access.responseStatus === 'SUCCESS' ? 'green' : access.responseStatus === 'PARTIAL' ? 'yellow' : 'red'}>
          {access.responseStatus}
        </Badge>
      </button>
      {expanded && (
        <div className="px-4 pb-3 border-t border-gray-50 bg-gray-50/50">
          <p className="text-xs font-medium text-gray-500 mt-2 mb-1">{t(lang, 'scopesShared')}</p>
          <div className="flex flex-wrap gap-1">
            {scopes.map((s: string) => (
              <span key={s} className="px-2 py-0.5 bg-blue-100 text-blue-700 text-xs rounded-full">{scopeLabel(s)}</span>
            ))}
          </div>
          <p className="text-xs text-gray-400 mt-2 font-mono">{access.consentCode}</p>
        </div>
      )}
    </div>
  )
}

// ── Main Page ────────────────────────────────────────────────────────────────
export default function Banking() {
  const { lang } = useLangStore()

  const { data: pending = [], isLoading: pendingLoading } = useQuery({
    queryKey: ['pending-consents'],
    queryFn: getPendingConsents,
    refetchInterval: 15_000,
  })

  const { data: accesses = [], isLoading: accessLoading } = useQuery({
    queryKey: ['access-history'],
    queryFn: getAccessHistory,
  })

  const hasPending = pending.length > 0

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
          <Building2 className="w-6 h-6 text-blue-600" />
          {t(lang, 'banking')}
        </h1>
        <p className="text-sm text-gray-500 mt-1">{t(lang, 'bankingDesc')}</p>
      </div>

      {/* Pending consent requests — shown prominently */}
      <section>
        <div className="flex items-center gap-2 mb-3">
          <h2 className="text-lg font-semibold text-gray-800">{t(lang, 'pendingConsents')}</h2>
          {hasPending && (
            <span className="px-2 py-0.5 bg-red-100 text-red-700 text-xs font-bold rounded-full">
              {pending.length}
            </span>
          )}
        </div>
        {pendingLoading ? (
          <p className="text-sm text-gray-400">{t(lang, 'loading')}</p>
        ) : pending.length === 0 ? (
          <div className="bg-gray-50 border border-gray-200 rounded-2xl p-6 flex items-center gap-3">
            <Shield className="w-5 h-5 text-gray-400" />
            <p className="text-sm text-gray-500">{t(lang, 'noPendingConsents')}</p>
          </div>
        ) : (
          <div className="space-y-4">
            {pending.map(consent => (
              <ConsentApprovalCard key={consent.consentCode} consent={consent} />
            ))}
          </div>
        )}
      </section>

      {/* Access History */}
      <section>
        <h2 className="text-lg font-semibold text-gray-800 mb-3">{t(lang, 'accessHistory')}</h2>
        {accessLoading ? (
          <p className="text-sm text-gray-400">{t(lang, 'loading')}</p>
        ) : accesses.length === 0 ? (
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
            <p className="text-sm text-gray-400">{t(lang, 'noAccessHistory')}</p>
          </div>
        ) : (
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 space-y-2">
            {accesses.map(a => <AccessHistoryRow key={a.accessId} access={a} />)}
          </div>
        )}
      </section>
    </div>
  )
}
