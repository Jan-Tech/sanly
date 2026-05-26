import { useRef, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { CheckCircle2, XCircle, FileSignature, Upload, Shield, QrCode, Trash2 } from 'lucide-react'
import { useLangStore } from '../store/langStore'
import { t } from '../utils/i18n'
import {
  getMySignatures, signDocument, revokeSignature,
  generateActionOtp, verifyDocument, verifyByCode, getQrCode,
  type SignatureRecord, type VerifyResponse,
} from '../api/signature'
import Badge from '../components/ui/Badge'

const MAX_FILE_BYTES = 10 * 1024 * 1024

export default function Signatures() {
  const { lang } = useLangStore()
  const qc = useQueryClient()

  // ── Sign flow state ──────────────────────────────────────────────────────
  const [signFile, setSignFile] = useState<File | null>(null)
  const [purpose, setPurpose] = useState('')
  const [otpSent, setOtpSent] = useState(false)
  const [signDigits, setSignDigits] = useState(['', '', '', '', '', ''])
  const signDigitRefs = useRef<(HTMLInputElement | null)[]>([])
  const [signResult, setSignResult] = useState<SignatureRecord | null>(null)
  const [signError, setSignError] = useState('')
  const [qrUrl, setQrUrl] = useState<string | null>(null)
  const [qrModal, setQrModal] = useState(false)

  // ── Revoke state ──────────────────────────────────────────────────────────
  const [revokeCode, setRevokeCode] = useState('')
  const [revokeReason, setRevokeReasonVal] = useState('')
  const [revokeModal, setRevokeModal] = useState(false)

  // ── Verify state ──────────────────────────────────────────────────────────
  const [verifyFile, setVerifyFile] = useState<File | null>(null)
  const [verifyCode, setVerifyCode] = useState('')
  const [verifyResult, setVerifyResult] = useState<VerifyResponse | null>(null)
  const [verifyError, setVerifyError] = useState('')

  const fileRef = useRef<HTMLInputElement>(null)
  const verifyFileRef = useRef<HTMLInputElement>(null)

  const { data: sigPage, isLoading } = useQuery({
    queryKey: ['my-signatures'],
    queryFn: () => getMySignatures(0, 20),
  })
  const signatures = sigPage?.content ?? []

  // ── Send OTP ──────────────────────────────────────────────────────────────
  const otpMutation = useMutation({
    mutationFn: () => generateActionOtp(lang.toUpperCase()),
    onSuccess: () => { setOtpSent(true); setSignError('') },
    onError: () => setSignError('Could not send OTP. Please try again.'),
  })

  // ── Sign document ─────────────────────────────────────────────────────────
  const signMutation = useMutation({
    mutationFn: () => {
      if (!signFile) throw new Error('No file')
      return signDocument(signFile, purpose, signDigits.join(''))
    },
    onSuccess: (data) => {
      setSignResult(data)
      setSignDigits(['', '', '', '', '', ''])
      setOtpSent(false)
      qc.invalidateQueries({ queryKey: ['my-signatures'] })
    },
    onError: (err: unknown) => {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      setSignError(msg ?? 'Signing failed. Please try again.')
      setSignDigits(['', '', '', '', '', ''])
    },
  })

  // ── Revoke ────────────────────────────────────────────────────────────────
  const revokeMutation = useMutation({
    mutationFn: () => revokeSignature(revokeCode, revokeReason),
    onSuccess: () => {
      setRevokeModal(false)
      setRevokeReasonVal('')
      qc.invalidateQueries({ queryKey: ['my-signatures'] })
    },
  })

  // ── Verify ────────────────────────────────────────────────────────────────
  const verifyMutation = useMutation({
    mutationFn: () => {
      if (verifyFile) return verifyDocument(verifyFile, verifyCode)
      return verifyByCode(verifyCode)
    },
    onSuccess: (data) => { setVerifyResult(data); setVerifyError('') },
    onError: () => { setVerifyError('Verification failed. Please check the code and document.') },
  })

  const handleSignDigit = (i: number, val: string) => {
    const d = val.replace(/\D/g, '').slice(-1)
    const next = [...signDigits]; next[i] = d; setSignDigits(next)
    if (d && i < 5) signDigitRefs.current[i + 1]?.focus()
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="page-title flex items-center gap-2">
          <FileSignature className="w-6 h-6 text-primary" />
          {t(lang, 'signatures')}
        </h1>
        <p className="text-sm text-gray-500 mt-1">{t(lang, 'signaturesDesc')}</p>
      </div>

      {/* ── Sign a Document ────────────────────────────────────────────── */}
      <div className="card">
        <h2 className="section-title">{t(lang, 'signDocument')}</h2>
        <p className="text-sm text-gray-500 mb-4">{t(lang, 'signDocumentDesc')}</p>

        {signResult ? (
          <div className="rounded-xl bg-green-50 border border-green-100 p-5 space-y-3">
            <div className="flex items-center gap-2 text-green-700 font-semibold">
              <CheckCircle2 className="w-5 h-5" />
              {t(lang, 'signSuccess')}
            </div>
            <div>
              <p className="text-xs text-gray-400">{t(lang, 'signatureCode')}</p>
              <p className="font-mono font-bold text-dark text-lg">{signResult.signatureCode}</p>
            </div>
            <div className="flex gap-2">
              <button
                className="btn-primary text-sm"
                onClick={async () => {
                  const url = await getQrCode(signResult.signatureCode)
                  setQrUrl(url)
                  setQrModal(true)
                }}
              >
                <QrCode className="w-4 h-4 mr-1 inline" />{t(lang, 'viewQr')}
              </button>
              <button className="btn-secondary text-sm" onClick={() => setSignResult(null)}>
                Sign Another
              </button>
            </div>
          </div>
        ) : (
          <div className="space-y-4">
            {/* File upload */}
            <div
              className="border-2 border-dashed border-gray-200 rounded-xl p-6 text-center cursor-pointer hover:border-primary/40 transition-colors"
              onClick={() => fileRef.current?.click()}
              onDragOver={(e) => e.preventDefault()}
              onDrop={(e) => {
                e.preventDefault()
                const f = e.dataTransfer.files[0]
                if (f && f.size <= MAX_FILE_BYTES) setSignFile(f)
              }}
            >
              <Upload className="w-8 h-8 text-gray-300 mx-auto mb-2" />
              {signFile ? (
                <p className="text-sm font-medium text-dark">{signFile.name} ({(signFile.size / 1024).toFixed(1)} KB)</p>
              ) : (
                <p className="text-sm text-gray-400">{t(lang, 'dropDocumentHere')} (max 10MB)</p>
              )}
              <input ref={fileRef} type="file" className="hidden" onChange={(e) => {
                const f = e.target.files?.[0]
                if (f && f.size <= MAX_FILE_BYTES) setSignFile(f)
                else if (f) setSignError('File exceeds 10MB limit.')
              }} />
            </div>

            {/* Purpose */}
            <div>
              <label className="label">{t(lang, 'purposeLabel')}</label>
              <input
                type="text"
                className="input"
                placeholder={t(lang, 'purposePlaceholder')}
                value={purpose}
                onChange={(e) => setPurpose(e.target.value)}
                maxLength={500}
              />
            </div>

            {/* OTP */}
            {!otpSent ? (
              <button
                className="btn-secondary w-full"
                disabled={!signFile || !purpose.trim() || otpMutation.isPending}
                onClick={() => { setSignError(''); otpMutation.mutate() }}
              >
                {otpMutation.isPending ? 'Sending...' : t(lang, 'sendOtpToPhone')}
              </button>
            ) : (
              <div>
                <label className="label">{t(lang, 'enterOtp')}</label>
                <div className="flex gap-2 mb-3">
                  {signDigits.map((d, i) => (
                    <input
                      key={i}
                      ref={(el) => { signDigitRefs.current[i] = el }}
                      type="text" inputMode="numeric" maxLength={1}
                      value={d}
                      onChange={(e) => handleSignDigit(i, e.target.value)}
                      onKeyDown={(e) => { if (e.key === 'Backspace' && !d && i > 0) signDigitRefs.current[i-1]?.focus() }}
                      className="w-full aspect-square text-center text-xl font-bold border-2 rounded-xl border-gray-200 focus:border-primary focus:outline-none"
                      autoFocus={i === 0}
                    />
                  ))}
                </div>
                <button
                  className="btn-primary w-full"
                  disabled={signDigits.some(d => !d) || signMutation.isPending}
                  onClick={() => signMutation.mutate()}
                >
                  {signMutation.isPending ? t(lang, 'signingDocument') : t(lang, 'signDocumentBtn')}
                </button>
              </div>
            )}

            {signError && (
              <div className="px-3 py-2.5 rounded-lg bg-red-50 border border-red-100">
                <p className="text-sm text-red-600">{signError}</p>
              </div>
            )}
          </div>
        )}
      </div>

      {/* ── My Signatures ──────────────────────────────────────────────── */}
      <div className="card">
        <h2 className="section-title">{t(lang, 'mySignatures')}</h2>
        {isLoading ? (
          <div className="space-y-3">{[1,2,3].map(i => <div key={i} className="skeleton h-14 rounded-lg" />)}</div>
        ) : signatures.length === 0 ? (
          <p className="text-sm text-gray-400 py-4 text-center">{t(lang, 'noSignatures')}</p>
        ) : (
          <div className="space-y-3">
            {signatures.map((s) => (
              <div key={s.signatureId} className="flex items-start gap-3 p-3 rounded-xl border border-gray-100 bg-gray-50">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 flex-wrap">
                    <span className="font-mono text-sm font-semibold text-dark">{s.signatureCode}</span>
                    <Badge label={s.status} status={s.status === 'VALID' ? 'ACTIVE' : 'SUSPENDED'} size="sm" />
                  </div>
                  <p className="text-xs text-gray-500 truncate mt-0.5">{s.documentName}</p>
                  <p className="text-xs text-gray-400">{s.purpose}</p>
                  <p className="text-xs text-gray-400">{new Date(s.signedAt).toLocaleString()}</p>
                </div>
                <div className="flex gap-1 shrink-0">
                  <button
                    className="p-1.5 rounded-lg text-gray-400 hover:text-primary hover:bg-primary/5"
                    title={t(lang, 'viewQr')}
                    onClick={async () => {
                      const url = await getQrCode(s.signatureCode)
                      setQrUrl(url)
                      setQrModal(true)
                    }}
                  >
                    <QrCode className="w-4 h-4" />
                  </button>
                  {s.status === 'VALID' && (
                    <button
                      className="p-1.5 rounded-lg text-gray-400 hover:text-red-500 hover:bg-red-50"
                      title={t(lang, 'revokeSignature')}
                      onClick={() => { setRevokeCode(s.signatureCode); setRevokeModal(true) }}
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* ── Verify Tool ────────────────────────────────────────────────── */}
      <div className="card">
        <h2 className="section-title flex items-center gap-2">
          <Shield className="w-4 h-4 text-primary" />{t(lang, 'verifyTool')}
        </h2>
        <p className="text-sm text-gray-500 mb-4">{t(lang, 'verifyToolDesc')}</p>

        <div className="space-y-3">
          <div
            className="border-2 border-dashed border-gray-200 rounded-xl p-4 text-center cursor-pointer hover:border-primary/40 transition-colors"
            onClick={() => verifyFileRef.current?.click()}
          >
            <Upload className="w-6 h-6 text-gray-300 mx-auto mb-1" />
            {verifyFile
              ? <p className="text-sm text-dark">{verifyFile.name}</p>
              : <p className="text-xs text-gray-400">{t(lang, 'dropDocumentHere')} (optional)</p>}
            <input ref={verifyFileRef} type="file" className="hidden" onChange={(e) => setVerifyFile(e.target.files?.[0] ?? null)} />
          </div>

          <input
            type="text"
            className="input font-mono"
            placeholder="TM-SIG-YYYYNNNNNN"
            value={verifyCode}
            onChange={(e) => setVerifyCode(e.target.value.toUpperCase())}
          />

          <button
            className="btn-primary w-full"
            disabled={!verifyCode.trim() || verifyMutation.isPending}
            onClick={() => { setVerifyResult(null); setVerifyError(''); verifyMutation.mutate() }}
          >
            {verifyMutation.isPending ? t(lang, 'verifying') : t(lang, 'verifyBtn')}
          </button>

          {verifyError && <p className="text-sm text-red-500">{verifyError}</p>}

          {verifyResult && (
            <div className={`rounded-xl p-4 border ${verifyResult.valid ? 'bg-green-50 border-green-100' : 'bg-red-50 border-red-100'}`}>
              <div className={`flex items-center gap-2 font-semibold mb-3 ${verifyResult.valid ? 'text-green-700' : 'text-red-700'}`}>
                {verifyResult.valid
                  ? <><CheckCircle2 className="w-5 h-5" />{t(lang, 'signatureValid')}</>
                  : <><XCircle className="w-5 h-5" />{t(lang, 'signatureInvalid')}</>}
              </div>
              {verifyResult.valid && (
                <div className="space-y-1 text-sm">
                  <p><span className="text-gray-500">{t(lang, 'signedBy')}:</span> <span className="font-medium">{verifyResult.signerName ?? verifyResult.signerNationalId}</span></p>
                  <p><span className="text-gray-500">{t(lang, 'signedAt')}:</span> {verifyResult.signedAt ? new Date(verifyResult.signedAt).toLocaleString() : '—'}</p>
                  <p><span className="text-gray-500">{t(lang, 'signaturePurpose')}:</span> {verifyResult.purpose}</p>
                  {!verifyResult.documentResubmitted && (
                    <p className="text-xs text-amber-600 mt-2">Note: Document not re-submitted — hash not re-verified.</p>
                  )}
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* ── QR Modal ───────────────────────────────────────────────────── */}
      {qrModal && qrUrl && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50" onClick={() => setQrModal(false)}>
          <div className="bg-white rounded-2xl p-6 shadow-xl text-center" onClick={(e) => e.stopPropagation()}>
            <p className="font-semibold text-dark mb-4">Verification QR Code</p>
            <img src={qrUrl} alt="QR Code" className="w-64 h-64 mx-auto" />
            <div className="flex gap-2 mt-4 justify-center">
              <a href={qrUrl} download="signature-qr.png" className="btn-primary text-sm">{t(lang, 'downloadQr')}</a>
              <button className="btn-secondary text-sm" onClick={() => setQrModal(false)}>Close</button>
            </div>
          </div>
        </div>
      )}

      {/* ── Revoke Modal ───────────────────────────────────────────────── */}
      {revokeModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
          <div className="bg-white rounded-2xl p-6 shadow-xl w-full max-w-md">
            <h3 className="font-semibold text-dark mb-2">{t(lang, 'revokeSignature')} {revokeCode}</h3>
            <p className="text-sm text-amber-600 mb-4">{t(lang, 'revokeWarning')}</p>
            <label className="label">{t(lang, 'revokeReason')}</label>
            <textarea
              className="input min-h-[80px] mb-4"
              value={revokeReason}
              onChange={(e) => setRevokeReasonVal(e.target.value)}
              placeholder="Enter reason..."
            />
            <div className="flex gap-2 justify-end">
              <button className="btn-secondary" onClick={() => setRevokeModal(false)}>Cancel</button>
              <button
                className="bg-red-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-red-700 disabled:opacity-50"
                disabled={!revokeReason.trim() || revokeMutation.isPending}
                onClick={() => revokeMutation.mutate()}
              >
                {revokeMutation.isPending ? 'Revoking...' : t(lang, 'confirmRevoke')}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
