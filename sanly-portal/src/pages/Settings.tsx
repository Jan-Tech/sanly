import { useState, useEffect } from 'react'
import { useLangStore } from '../store/langStore'
import { t, type Lang } from '../utils/i18n'
import { registryClient } from '../api/client'
import { useAuthStore } from '../store/authStore'
import { listSessions, revokeSession, revokeOtherSessions, type SessionInfo } from '../api/registry'
import { Monitor, Smartphone, Trash2, FileText, Download, Plus, Shield } from 'lucide-react'

// ─── Types ────────────────────────────────────────────────────────────────────

interface DataRequest {
  requestId: string
  requestCode: string
  requestType: string
  affectedService: string
  description: string
  status: 'PENDING' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED' | 'PARTIALLY_APPROVED'
  submittedAt: string
  resolutionDescription?: string
  hasExport?: boolean
}

const REQUEST_TYPES = [
  { value: 'DELETE_MEDICAL_RECORDS', label: 'Delete Medical Records' },
  { value: 'DELETE_CRIMINAL_RECORD', label: 'Delete Criminal Record' },
  { value: 'CORRECT_PERSONAL_INFO', label: 'Correct Personal Information' },
  { value: 'DELETE_AUDIT_LOG_ENTRIES', label: 'Delete Audit Log Entries' },
  { value: 'REVIEW_DATA_ACCESS', label: 'Review Data Access History' },
  { value: 'EXPORT_MY_DATA', label: 'Export My Data' },
  { value: 'DELETE_ACCOUNT', label: 'Delete Account' },
  { value: 'OTHER', label: 'Other' },
]

const SERVICES = [
  'CITIZEN_REGISTRY','MEDICAL','DMV','POLICE','TAX','BUSINESS',
  'CIVIL','EDUCATION','LAND','SOCIAL','CUSTOMS','COURT','VEHICLE','PENSION','SIGNATURE','ALL',
]

const STATUS_COLOR: Record<string, string> = {
  PENDING: 'bg-yellow-50 text-yellow-700',
  UNDER_REVIEW: 'bg-blue-50 text-blue-700',
  APPROVED: 'bg-green-50 text-green-700',
  REJECTED: 'bg-red-50 text-red-700',
  PARTIALLY_APPROVED: 'bg-purple-50 text-purple-700',
}

const LANGS: { value: Lang; label: string; native: string }[] = [
  { value: 'en', label: 'English', native: 'English' },
  { value: 'tk', label: 'Türkmen', native: 'Türkmen dili' },
  { value: 'ru', label: 'Русский', native: 'Русский язык' },
]

// ─── Helpers ─────────────────────────────────────────────────────────────────

function relativeTime(dateStr: string): string {
  const diff = Date.now() - new Date(dateStr).getTime()
  const minutes = Math.floor(diff / 60_000)
  if (minutes < 1) return 'just now'
  if (minutes < 60) return `${minutes}m ago`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}h ago`
  return `${Math.floor(hours / 24)}d ago`
}

// ─── Component ───────────────────────────────────────────────────────────────

export default function Settings() {
  const { lang, setLang } = useLangStore()
  const { nationalId, sessionId } = useAuthStore()

  // Sessions
  const [sessions, setSessions] = useState<SessionInfo[]>([])
  const [sessionsLoading, setSessionsLoading] = useState(false)

  useEffect(() => {
    setSessionsLoading(true)
    listSessions(sessionId ?? undefined)
      .then(setSessions)
      .catch(() => setSessions([]))
      .finally(() => setSessionsLoading(false))
  }, [sessionId])

  const handleRevokeSession = async (id: string) => {
    try { await revokeSession(id); setSessions((prev) => prev.filter((s) => s.sessionId !== id)) }
    catch { /* ignore */ }
  }
  const handleRevokeOthers = async () => {
    if (!sessionId) return
    try { await revokeOtherSessions(sessionId); setSessions((prev) => prev.filter((s) => s.sessionId === sessionId)) }
    catch { /* ignore */ }
  }

  // Password change
  const [currentPwd, setCurrentPwd] = useState('')
  const [newPwd, setNewPwd] = useState('')
  const [confirmPwd, setConfirmPwd] = useState('')
  const [pwdError, setPwdError] = useState('')
  const [pwdSuccess, setPwdSuccess] = useState(false)
  const [pwdLoading, setPwdLoading] = useState(false)

  const handlePasswordChange = async (e: React.FormEvent) => {
    e.preventDefault(); setPwdError(''); setPwdSuccess(false)
    if (newPwd.length < 8) { setPwdError('New password must be at least 8 characters.'); return }
    if (newPwd !== confirmPwd) { setPwdError('Passwords do not match.'); return }
    setPwdLoading(true)
    try {
      await registryClient.patch(`/api/v1/citizens/${nationalId}/password`, { currentPassword: currentPwd, newPassword: newPwd })
      setPwdSuccess(true); setCurrentPwd(''); setNewPwd(''); setConfirmPwd('')
    } catch { setPwdError('Could not update password. Please check your current password.') }
    finally { setPwdLoading(false) }
  }

  // Data requests
  const [dataRequests, setDataRequests] = useState<DataRequest[]>([])
  const [drLoading, setDrLoading] = useState(false)
  const [showSubmitModal, setShowSubmitModal] = useState(false)
  const [drType, setDrType] = useState('EXPORT_MY_DATA')
  const [drService, setDrService] = useState('ALL')
  const [drDescription, setDrDescription] = useState('')
  const [drSubmitting, setDrSubmitting] = useState(false)
  const [drError, setDrError] = useState('')

  useEffect(() => {
    if (!nationalId) return
    setDrLoading(true)
    registryClient.get<{ data: { content: DataRequest[] } }>('/api/v1/data-requests/my', { params: { page: 0, size: 20 } })
      .then(r => setDataRequests(r.data.data.content ?? []))
      .catch(() => setDataRequests([]))
      .finally(() => setDrLoading(false))
  }, [nationalId])

  const handleSubmitRequest = async () => {
    if (!drDescription.trim() || drDescription.length < 10) { setDrError('Description must be at least 10 characters.'); return }
    setDrSubmitting(true); setDrError('')
    try {
      const res = await registryClient.post<{ data: DataRequest }>('/api/v1/data-requests', {
        requestType: drType, affectedService: drService, description: drDescription,
      })
      setDataRequests(prev => [res.data.data, ...prev])
      setShowSubmitModal(false); setDrDescription(''); setDrType('EXPORT_MY_DATA'); setDrService('ALL')
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      setDrError(msg ?? 'Failed to submit request. You may have reached the monthly limit (3 per month).')
    } finally { setDrSubmitting(false) }
  }

  const handleExportDownload = async (requestCode: string) => {
    try {
      const res = await registryClient.get(`/api/v1/data-requests/my/${requestCode}/export`, { responseType: 'blob' })
      const url = URL.createObjectURL(res.data)
      const a = document.createElement('a'); a.href = url; a.download = `data-export-${requestCode}.json`
      a.click(); URL.revokeObjectURL(url)
    } catch { /* ignore */ }
  }

  // ─── Render ─────────────────────────────────────────────────────────────────

  const currentSess = sessions.find(s => s.sessionId === sessionId)
  const otherSessions = sessions.filter(s => s.sessionId !== sessionId)

  return (
    <div className="space-y-6 max-w-2xl">
      <h1 className="page-title">{t(lang, 'settingsTitle')}</h1>

      {/* Language preference */}
      <div className="card">
        <h2 className="section-title">{t(lang, 'languagePreference')}</h2>
        <div className="grid grid-cols-3 gap-3">
          {LANGS.map(({ value, label, native }) => (
            <button key={value} onClick={() => setLang(value)}
              className={`px-4 py-3 rounded-xl border-2 text-left transition-all ${
                lang === value ? 'border-primary bg-primary/5 text-primary' : 'border-gray-100 hover:border-gray-200 text-gray-600'
              }`}>
              <p className="font-semibold text-sm">{label}</p>
              <p className="text-xs opacity-60 mt-0.5">{native}</p>
            </button>
          ))}
        </div>
      </div>

      {/* Change password */}
      <div className="card">
        <h2 className="section-title">{t(lang, 'changePassword')}</h2>
        <form onSubmit={handlePasswordChange} className="space-y-4">
          {[
            { label: t(lang, 'currentPassword'), val: currentPwd, set: setCurrentPwd },
            { label: t(lang, 'newPassword'), val: newPwd, set: setNewPwd, min: 8 },
            { label: t(lang, 'confirmPassword'), val: confirmPwd, set: setConfirmPwd },
          ].map(({ label, val, set, min }) => (
            <div key={label}>
              <label className="label">{label}</label>
              <input type="password" className="input" value={val} onChange={(e) => set(e.target.value)} minLength={min} required />
            </div>
          ))}
          {pwdError && <div className="px-3 py-2.5 rounded-lg bg-red-50 border border-red-100"><p className="text-sm text-red-600">{pwdError}</p></div>}
          {pwdSuccess && <div className="px-3 py-2.5 rounded-lg bg-green-50 border border-green-100"><p className="text-sm text-green-600">Password updated successfully.</p></div>}
          <button type="submit" className="btn-primary" disabled={pwdLoading}>{pwdLoading ? 'Saving...' : t(lang, 'saveChanges')}</button>
        </form>
      </div>

      {/* Active sessions */}
      <div className="card">
        <div className="flex items-center justify-between mb-1">
          <h2 className="section-title mb-0 flex items-center gap-2">
            <Shield className="w-4 h-4 text-primary" />{t(lang, 'sessions')}
          </h2>
          {otherSessions.length > 0 && (
            <button className="text-xs text-red-500 hover:underline" onClick={handleRevokeOthers}>
              {t(lang, 'revokeOtherSessions')}
            </button>
          )}
        </div>
        <p className="text-sm text-gray-500 mb-4">{t(lang, 'sessionsDesc')}</p>

        {sessionsLoading ? <p className="text-sm text-gray-400">{t(lang, 'loading')}</p> : (
          <div className="space-y-3">
            {/* Current session — highlighted */}
            {currentSess && (
              <div className="p-3 rounded-xl border-2 border-primary/30 bg-primary/5">
                <div className="flex items-start gap-3">
                  <div className="mt-0.5 text-primary">
                    {/mobile|android|iphone/i.test(currentSess.userAgent ?? '') ? <Smartphone className="w-4 h-4" /> : <Monitor className="w-4 h-4" />}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                      <p className="text-sm font-medium text-dark">{(currentSess as SessionInfo & { deviceName?: string }).deviceName ?? currentSess.userAgent?.split(' ')[0] ?? 'Unknown'}</p>
                      <span className="text-xs text-primary font-medium">{t(lang, 'currentSession')}</span>
                    </div>
                    <p className="text-xs text-gray-400">{currentSess.ipAddress}</p>
                    <p className="text-xs text-gray-400">{t(lang, 'lastSeen')}: {relativeTime(currentSess.lastSeenAt)}</p>
                    {(currentSess as SessionInfo & { lastAction?: string }).lastAction && (
                      <p className="text-xs text-gray-400">Last: {(currentSess as SessionInfo & { lastAction?: string }).lastAction}</p>
                    )}
                  </div>
                </div>
              </div>
            )}

            {/* Other sessions */}
            {otherSessions.length === 0 && !currentSess && <p className="text-sm text-gray-400">{t(lang, 'noData')}</p>}
            {otherSessions.length === 0 && currentSess && <p className="text-xs text-gray-400 text-center py-2">Only this device is signed in.</p>}
            {otherSessions.map((s) => {
              const extended = s as SessionInfo & { deviceName?: string; lastAction?: string }
              return (
                <div key={s.sessionId} className="flex items-start gap-3 p-3 rounded-xl border border-gray-100 bg-gray-50">
                  <div className="mt-0.5 text-gray-400">
                    {/mobile|android|iphone/i.test(s.userAgent ?? '') ? <Smartphone className="w-4 h-4" /> : <Monitor className="w-4 h-4" />}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-dark">{extended.deviceName ?? s.userAgent?.split(' ')[0] ?? 'Unknown'}</p>
                    <p className="text-xs text-gray-400">{s.ipAddress}</p>
                    <p className="text-xs text-gray-400">{t(lang, 'lastSeen')}: {relativeTime(s.lastSeenAt)}</p>
                    {extended.lastAction && <p className="text-xs text-gray-400">Last: {extended.lastAction}</p>}
                  </div>
                  <button className="shrink-0 text-gray-400 hover:text-red-500 transition-colors"
                    title={t(lang, 'revokeSession')} onClick={() => handleRevokeSession(s.sessionId)}>
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              )
            })}
          </div>
        )}
      </div>

      {/* Privacy & Data Requests */}
      <div className="card">
        <div className="flex items-center justify-between mb-1">
          <h2 className="section-title mb-0 flex items-center gap-2">
            <FileText className="w-4 h-4 text-primary" />Privacy & Data
          </h2>
          <div className="flex gap-2">
            <button className="btn-secondary text-xs flex items-center gap-1" onClick={() => {
              setDrType('EXPORT_MY_DATA'); setDrService('ALL'); setDrDescription('I would like a copy of all my data held by SANLY.'); setShowSubmitModal(true)
            }}>
              <Download className="w-3 h-3" />Export My Data
            </button>
            <button className="btn-primary text-xs flex items-center gap-1" onClick={() => { setDrDescription(''); setShowSubmitModal(true) }}>
              <Plus className="w-3 h-3" />New Request
            </button>
          </div>
        </div>
        <p className="text-sm text-gray-500 mb-4">
          You have the right to request correction, deletion, or export of your government data. Officers review each request within 30 days.
        </p>

        {drLoading ? <p className="text-sm text-gray-400">{t(lang, 'loading')}</p> : dataRequests.length === 0 ? (
          <p className="text-sm text-gray-400 py-4 text-center">No data requests submitted yet.</p>
        ) : (
          <div className="space-y-2">
            {dataRequests.map((r) => (
              <div key={r.requestId} className="p-3 rounded-xl border border-gray-100 bg-gray-50">
                <div className="flex items-start justify-between gap-2">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="font-mono text-xs font-semibold text-dark">{r.requestCode}</span>
                      <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${STATUS_COLOR[r.status] ?? 'bg-gray-100 text-gray-600'}`}>
                        {r.status.replace('_', ' ')}
                      </span>
                    </div>
                    <p className="text-xs text-gray-500 mt-0.5">{r.requestType.replace(/_/g, ' ')} · {r.affectedService}</p>
                    <p className="text-xs text-gray-400">{new Date(r.submittedAt).toLocaleDateString()}</p>
                    {r.resolutionDescription && <p className="text-xs text-gray-600 mt-1 italic">{r.resolutionDescription}</p>}
                  </div>
                  {r.hasExport && r.status === 'APPROVED' && (
                    <button className="btn-secondary text-xs flex items-center gap-1 shrink-0" onClick={() => handleExportDownload(r.requestCode)}>
                      <Download className="w-3 h-3" />Download
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Info */}
      <div className="card !p-4 bg-gray-50">
        <p className="text-xs text-gray-400">
          <span className="font-semibold text-gray-500">SANLY Portal v1.0</span><br />
          Your data is encrypted end-to-end using AES-256-GCM. All access through SANLY Bridge is permanently recorded.
        </p>
      </div>

      {/* Submit Request Modal */}
      {showSubmitModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
          <div className="bg-white rounded-2xl p-6 shadow-xl w-full max-w-md">
            <h3 className="font-semibold text-dark mb-4">Submit Data Request</h3>

            <div className="space-y-3">
              <div>
                <label className="label">Request Type</label>
                <select className="input" value={drType} onChange={(e) => setDrType(e.target.value)}>
                  {REQUEST_TYPES.map(({ value, label }) => (
                    <option key={value} value={value}>{label}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="label">Affected Service</label>
                <select className="input" value={drService} onChange={(e) => setDrService(e.target.value)}>
                  {SERVICES.map(s => <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>)}
                </select>
              </div>

              <div>
                <label className="label">Description <span className="text-gray-400 font-normal">(min 10 chars)</span></label>
                <textarea
                  className="input min-h-[100px]"
                  value={drDescription}
                  onChange={(e) => setDrDescription(e.target.value)}
                  placeholder="Describe what you are requesting and why..."
                  maxLength={2000}
                />
              </div>

              <p className="text-xs text-amber-600">
                Note: Some data is retained by law (tax records 7 years, civil records permanently, criminal records 10 years). Officers review all requests individually.
              </p>

              {drError && <p className="text-sm text-red-500">{drError}</p>}
            </div>

            <div className="flex gap-2 justify-end mt-4">
              <button className="btn-secondary" onClick={() => { setShowSubmitModal(false); setDrError('') }}>Cancel</button>
              <button className="btn-primary" disabled={drSubmitting || drDescription.length < 10} onClick={handleSubmitRequest}>
                {drSubmitting ? 'Submitting...' : 'Submit Request'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
