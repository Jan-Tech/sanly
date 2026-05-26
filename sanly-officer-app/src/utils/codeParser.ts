import type { ParsedCode, ScanCodeType } from '../types'

export function parseCode(rawCode: string): ParsedCode {
  const code = rawCode.trim().toUpperCase()

  // TM-NIN-XXXXXXXXXX (10-digit national ID)
  if (/^TM-NIN-\d{10}$/.test(code))
    return { type: 'NIN', rawCode, value: code.slice(7) }

  // TM-DL-YYYYNNNNNN (driving license)
  if (/^TM-DL-\d{10}$/.test(code))
    return { type: 'LICENSE', rawCode, value: code }

  // TM-BUS-YYYYNNNNNN (business registration)
  if (/^TM-BUS-\d{10}$/.test(code))
    return { type: 'BUSINESS', rawCode, value: code }

  // TM-APT-* (appointment)
  if (/^TM-APT-/.test(code))
    return { type: 'APPOINTMENT', rawCode, value: code }

  // TM-FINE-* (court fine)
  if (/^TM-FINE-/.test(code))
    return { type: 'FINE', rawCode, value: code }

  // TM-CASE-* (court case)
  if (/^TM-CASE-/.test(code))
    return { type: 'CASE', rawCode, value: code }

  // TM-CAD-YYYYNNNNNN (cadastral / land)
  if (/^TM-CAD-\d{10}$/.test(code))
    return { type: 'CADASTRAL', rawCode, value: code }

  // TM-DIP-YYYYNNNNNN (diploma)
  if (/^TM-DIP-\d{10}$/.test(code))
    return { type: 'DIPLOMA', rawCode, value: code }

  // TM-BIRTH-YYYYNNNNNN (birth certificate)
  if (/^TM-BIRTH-\d{10}$/.test(code))
    return { type: 'BIRTH_CERT', rawCode, value: code }

  // TM-MARR-YYYYNNNNNN (marriage certificate)
  if (/^TM-MARR-\d{10}$/.test(code))
    return { type: 'MARR_CERT', rawCode, value: code }

  // TM-DEATH-YYYYNNNNNN (death certificate)
  if (/^TM-DEATH-\d{10}$/.test(code))
    return { type: 'DEATH_CERT', rawCode, value: code }

  // TM-PLATE-XX-XXX or raw plate (vehicle)
  if (/^TM-PLATE-/.test(code) || /^\d{2}-[A-Z]{2,3}-\d{3,4}$/.test(code))
    return { type: 'PLATE', rawCode, value: code.replace('TM-PLATE-', '') }

  // Plain 10-digit number — treat as NIN
  if (/^\d{10}$/.test(code))
    return { type: 'NIN', rawCode, value: code }

  return { type: 'UNKNOWN', rawCode, value: code }
}

export const scanCodeLabels: Record<ScanCodeType, string> = {
  NIN: 'National ID',
  LICENSE: 'Driving License',
  BUSINESS: 'Business Registration',
  APPOINTMENT: 'Appointment',
  FINE: 'Court Fine',
  CASE: 'Court Case',
  PLATE: 'Vehicle Plate',
  CADASTRAL: 'Property (Cadastral)',
  DIPLOMA: 'Diploma',
  BIRTH_CERT: 'Birth Certificate',
  MARR_CERT: 'Marriage Certificate',
  DEATH_CERT: 'Death Certificate',
  UNKNOWN: 'Unknown Code',
}
