import { useState } from 'react'
import { registryClient } from '../api/client'
import { useAuthStore } from '../store/authStore'

const DATA_TYPES = [
  { name: 'VISION_TEST', desc: 'Eye examination results for driving eligibility' },
  { name: 'MEDICAL_CLEARANCE', desc: 'General medical fitness clearance (sensitive)' },
  { name: 'CRIMINAL_RECORD', desc: 'Criminal history records (sensitive)' },
  { name: 'TAX_STATUS', desc: 'Tax compliance and registration status' },
  { name: 'DRIVING_LICENSE', desc: 'Driving license issuance and status' },
  { name: 'BUSINESS_REGISTRATION', desc: 'Business entity registration' },
  { name: 'BIRTH_RECORD', desc: 'Birth certificate data' },
  { name: 'MARRIAGE_RECORD', desc: 'Marriage certificate data' },
  { name: 'DEATH_RECORD', desc: 'Death registration data' },
]

const PURPOSE_CODES = [
  { code: 'TRAFFIC_STOP', desc: 'Roadside traffic stop verification' },
  { code: 'CRIMINAL_INVESTIGATION', desc: 'Active criminal investigation' },
  { code: 'COURT_ORDER', desc: 'Court-ordered data access' },
  { code: 'ROUTINE_CHECK', desc: 'Routine administrative check (sensitive types require case ref)' },
  { code: 'BORDER_CONTROL', desc: 'Border crossing verification' },
  { code: 'EMERGENCY', desc: 'Emergency response situation' },
  { code: 'LICENSE_ISSUANCE', desc: 'Processing a license application' },
  { code: 'TAX_AUDIT', desc: 'Tax compliance audit' },
  { code: 'BUSINESS_VERIFICATION', desc: 'Business registration verification' },
  { code: 'CIVIL_REGISTRATION', desc: 'Civil record registration process' },
  { code: 'MEDICAL_CLEARANCE', desc: 'Medical clearance processing' },
  { code: 'OTHER', desc: 'Other purpose (justification required)' },
]

export default function Settings() {
  const { username } = useAuthStore()
  const [currentPwd, setCurrentPwd] = useState('')
  const [newPwd, setNewPwd] = useState('')
  const [confirmPwd, setConfirmPwd] = useState('')
  const [pwdError, setPwdError] = useState('')
  const [pwdSuccess, setPwdSuccess] = useState(false)
  const [loading, setLoading] = useState(false)

  const handlePwdChange = async (e: React.FormEvent) => {
    e.preventDefault()
    setPwdError('')
    setPwdSuccess(false)
    if (newPwd.length < 8) { setPwdError('New password must be at least 8 characters.'); return }
    if (newPwd !== confirmPwd) { setPwdError('Passwords do not match.'); return }
    setLoading(true)
    try {
      await registryClient.patch(`/api/v1/admin/password`, { currentPassword: currentPwd, newPassword: newPwd })
      setPwdSuccess(true)
      setCurrentPwd(''); setNewPwd(''); setConfirmPwd('')
    } catch {
      setPwdError('Could not update password. Check your current password.')
    } finally { setLoading(false) }
  }

  return (
    <div className="space-y-6 max-w-2xl">
      {/* Platform info */}
      <div className="card">
        <p className="section-title">Platform Information</p>
        <div className="space-y-2 text-sm">
          <div className="flex gap-3"><span className="text-gray-400 w-36">Logged in as</span><span className="font-medium">{username}</span></div>
          <div className="flex gap-3"><span className="text-gray-400 w-36">Role</span><span className="font-mono text-xs bg-primary/10 text-primary px-2 py-0.5 rounded">ROLE_ADMIN</span></div>
          <div className="flex gap-3"><span className="text-gray-400 w-36">Version</span><span className="font-medium">SANLY Admin v1.0</span></div>
          <div className="flex gap-3"><span className="text-gray-400 w-36">Environment</span><span className="font-medium">Development</span></div>
        </div>
      </div>

      {/* Change password */}
      <div className="card">
        <p className="section-title">Change Password</p>
        <form onSubmit={handlePwdChange} className="space-y-4 max-w-sm">
          {[
            { label: 'Current Password', value: currentPwd, set: setCurrentPwd },
            { label: 'New Password', value: newPwd, set: setNewPwd },
            { label: 'Confirm Password', value: confirmPwd, set: setConfirmPwd },
          ].map(({ label, value, set }) => (
            <div key={label}>
              <label className="label">{label}</label>
              <input type="password" className="input" value={value} onChange={(e) => set(e.target.value)} required minLength={label.includes('New') ? 8 : 1} />
            </div>
          ))}
          {pwdError && <p className="text-sm text-red-600">{pwdError}</p>}
          {pwdSuccess && <p className="text-sm text-green-600">Password updated successfully.</p>}
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Saving…' : 'Update Password'}
          </button>
        </form>
      </div>

      {/* Data types reference */}
      <div className="card">
        <p className="section-title">Bridge Data Types</p>
        <table className="w-full text-sm">
          <thead><tr className="border-b border-gray-100"><th className="text-left py-2 text-xs text-gray-400 font-medium uppercase">Type</th><th className="text-left py-2 text-xs text-gray-400 font-medium uppercase pl-4">Description</th></tr></thead>
          <tbody className="divide-y divide-gray-50">
            {DATA_TYPES.map(({ name, desc }) => (
              <tr key={name} className="hover:bg-gray-50/50">
                <td className="py-2 font-mono text-xs text-primary font-semibold whitespace-nowrap">{name}</td>
                <td className="py-2 pl-4 text-gray-600 text-xs">{desc}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Purpose codes reference */}
      <div className="card">
        <p className="section-title">Query Purpose Codes</p>
        <p className="text-xs text-gray-500 mb-3">Every bridge query must include one of these purpose codes. CRIMINAL_RECORD and MEDICAL_CLEARANCE also require a case reference.</p>
        <table className="w-full text-sm">
          <thead><tr className="border-b border-gray-100"><th className="text-left py-2 text-xs text-gray-400 font-medium uppercase">Code</th><th className="text-left py-2 text-xs text-gray-400 font-medium uppercase pl-4">Description</th></tr></thead>
          <tbody className="divide-y divide-gray-50">
            {PURPOSE_CODES.map(({ code, desc }) => (
              <tr key={code} className="hover:bg-gray-50/50">
                <td className="py-2 font-mono text-xs text-dark font-semibold whitespace-nowrap">{code}</td>
                <td className="py-2 pl-4 text-gray-600 text-xs">{desc}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
