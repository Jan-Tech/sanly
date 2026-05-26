import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { FileText, Download, CheckCircle2, XCircle, Search } from 'lucide-react'
import { useLangStore } from '../store/langStore'
import { t } from '../utils/i18n'
import {
  getMyCertificates, generateCertificate, downloadCertificatePdf, verifyCertificate,
  type CertificateRecord, type CertificateVerifyResponse,
} from '../api/documents'
import Badge from '../components/ui/Badge'

const STATUS_COLORS: Record<string, 'green' | 'gray' | 'red'> = {
  ACTIVE: 'green',
  EXPIRED: 'gray',
  REVOKED: 'red',
}

function statusLabel(status: string, lang: string) {
  if (status === 'ACTIVE') return t(lang as any, 'certActive')
  if (status === 'EXPIRED') return t(lang as any, 'certExpired')
  if (status === 'REVOKED') return t(lang as any, 'certRevoked')
  return status
}

export default function Documents() {
  const { lang } = useLangStore()
  const qc = useQueryClient()

  // ── Generate form state ───────────────────────────────────────────────────
  const [docType, setDocType] = useState('')
  const [sourceCode, setSourceCode] = useState('')
  const [generating, setGenerating] = useState(false)
  const [generateError, setGenerateError] = useState('')

  // ── Verify state ──────────────────────────────────────────────────────────
  const [verifyCode, setVerifyCode] = useState('')
  const [verifyResult, setVerifyResult] = useState<CertificateVerifyResponse | null>(null)
  const [verifyError, setVerifyError] = useState('')
  const [verifying, setVerifying] = useState(false)

  const { data: certs = [], isLoading } = useQuery({
    queryKey: ['my-certificates'],
    queryFn: getMyCertificates,
  })

  async function handleGenerate() {
    if (!docType.trim()) return
    setGenerating(true)
    setGenerateError('')
    try {
      await generateCertificate(docType.trim(), sourceCode.trim() || undefined)
      qc.invalidateQueries({ queryKey: ['my-certificates'] })
      qc.invalidateQueries({ queryKey: ['my-certificates-count'] })
      setDocType('')
      setSourceCode('')
    } catch (err: any) {
      setGenerateError(err?.response?.data?.message ?? 'Request failed. Please try again.')
    } finally {
      setGenerating(false)
    }
  }

  async function handleVerify() {
    if (!verifyCode.trim()) return
    setVerifying(true)
    setVerifyResult(null)
    setVerifyError('')
    try {
      const result = await verifyCertificate(verifyCode.trim())
      setVerifyResult(result)
    } catch {
      setVerifyError(t(lang, 'certInvalid'))
    } finally {
      setVerifying(false)
    }
  }

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
          <FileText className="w-6 h-6 text-blue-600" />
          {t(lang, 'documents')}
        </h1>
        <p className="text-sm text-gray-500 mt-1">{t(lang, 'documentsDesc')}</p>
      </div>

      {/* ── Generate Certificate ── */}
      <section className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">{t(lang, 'requestCertificate')}</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">{t(lang, 'certDocType')}</label>
            <input
              value={docType}
              onChange={e => setDocType(e.target.value)}
              placeholder={t(lang, 'certDocTypePlaceholder')}
              className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">{t(lang, 'certSourceCode')}</label>
            <input
              value={sourceCode}
              onChange={e => setSourceCode(e.target.value)}
              placeholder={t(lang, 'certSourceCodePlaceholder')}
              className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>
        {generateError && <p className="text-sm text-red-600 mt-2">{generateError}</p>}
        <p className="text-xs text-gray-400 mt-3">
          The certificate will be generated and downloaded as a PDF. It will also appear in your list below.
        </p>
        <button
          onClick={handleGenerate}
          disabled={!docType.trim() || generating}
          className="mt-4 px-5 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors flex items-center gap-2"
        >
          <Download className="w-4 h-4" />
          {generating ? t(lang, 'requestingCert') : t(lang, 'requestCert')}
        </button>
      </section>

      {/* ── My Certificates ── */}
      <section className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">{t(lang, 'myCertificates')}</h2>
        {isLoading ? (
          <p className="text-sm text-gray-400">{t(lang, 'loading')}</p>
        ) : certs.length === 0 ? (
          <p className="text-sm text-gray-400">{t(lang, 'noCertificates')}</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-gray-500 border-b border-gray-100">
                  <th className="pb-2 font-medium">{t(lang, 'certCode')}</th>
                  <th className="pb-2 font-medium">{t(lang, 'certDocType')}</th>
                  <th className="pb-2 font-medium">{t(lang, 'certIssuedAt')}</th>
                  <th className="pb-2 font-medium">{t(lang, 'certExpiresAt')}</th>
                  <th className="pb-2 font-medium">{t(lang, 'trackingStatus')}</th>
                  <th className="pb-2 font-medium"></th>
                </tr>
              </thead>
              <tbody>
                {certs.map(cert => (
                  <tr key={cert.certificateId} className="border-b border-gray-50 hover:bg-gray-50">
                    <td className="py-2 font-mono text-xs text-blue-700">{cert.certificateCode}</td>
                    <td className="py-2">{cert.documentType.replace(/_/g, ' ')}</td>
                    <td className="py-2 text-gray-500">{new Date(cert.issuedAt).toLocaleDateString()}</td>
                    <td className="py-2 text-gray-500">{new Date(cert.expiresAt).toLocaleDateString()}</td>
                    <td className="py-2">
                      <Badge color={STATUS_COLORS[cert.status] ?? 'gray'}>
                        {statusLabel(cert.status, lang)}
                      </Badge>
                    </td>
                    <td className="py-2">
                      {cert.status === 'ACTIVE' && (
                        <button
                          onClick={() => downloadCertificatePdf(cert.certificateCode)}
                          className="flex items-center gap-1 text-xs text-blue-600 hover:text-blue-700"
                        >
                          <Download className="w-3 h-3" />
                          {t(lang, 'downloadCert')}
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {/* ── Verify a Certificate ── */}
      <section className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
        <h2 className="text-lg font-semibold text-gray-800 mb-1">{t(lang, 'verifyCert')}</h2>
        <p className="text-sm text-gray-500 mb-4">{t(lang, 'verifyCertDesc')}</p>
        <div className="flex gap-3">
          <input
            value={verifyCode}
            onChange={e => setVerifyCode(e.target.value)}
            placeholder="TM-CERT-2026000001"
            className="flex-1 border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 font-mono"
          />
          <button
            onClick={handleVerify}
            disabled={verifying || !verifyCode.trim()}
            className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors flex items-center gap-1"
          >
            <Search className="w-4 h-4" />
            {verifying ? t(lang, 'certVerifying') : t(lang, 'verifyCertBtn')}
          </button>
        </div>

        {verifyResult && (
          <div className={`mt-4 p-4 rounded-xl border ${verifyResult.valid ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'}`}>
            <div className="flex items-center gap-2 mb-2">
              {verifyResult.valid
                ? <CheckCircle2 className="w-5 h-5 text-green-600" />
                : <XCircle className="w-5 h-5 text-red-600" />
              }
              <span className={`font-semibold ${verifyResult.valid ? 'text-green-800' : 'text-red-800'}`}>
                {verifyResult.valid ? t(lang, 'certValid') : t(lang, 'certInvalid')}
              </span>
            </div>
            {verifyResult.valid && (
              <dl className="grid grid-cols-2 gap-x-4 gap-y-1 text-sm text-green-900">
                {verifyResult.documentType && <><dt className="font-medium">{t(lang, 'certDocType')}</dt><dd>{verifyResult.documentType.replace(/_/g, ' ')}</dd></>}
                {verifyResult.holderName && <><dt className="font-medium">Holder</dt><dd>{verifyResult.holderName}</dd></>}
                {verifyResult.holderNationalId && <><dt className="font-medium">NIN</dt><dd className="font-mono">{verifyResult.holderNationalId}</dd></>}
                {verifyResult.issuedAt && <><dt className="font-medium">{t(lang, 'certIssuedAt')}</dt><dd>{new Date(verifyResult.issuedAt).toLocaleDateString()}</dd></>}
                {verifyResult.expiresAt && <><dt className="font-medium">{t(lang, 'certExpiresAt')}</dt><dd>{new Date(verifyResult.expiresAt).toLocaleDateString()}</dd></>}
              </dl>
            )}
            {verifyResult.message && !verifyResult.valid && <p className="text-sm text-red-700 mt-1">{verifyResult.message}</p>}
          </div>
        )}
        {verifyError && (
          <div className="mt-4 p-4 rounded-xl bg-red-50 border border-red-200 flex items-center gap-2">
            <XCircle className="w-5 h-5 text-red-600" />
            <span className="text-sm text-red-800">{verifyError}</span>
          </div>
        )}
      </section>
    </div>
  )
}
