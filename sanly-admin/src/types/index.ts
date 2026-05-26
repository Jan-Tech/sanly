// ─── Shared ──────────────────────────────────────────────────────────────────
export interface ApiResponse<T> { success: boolean; message: string | null; data: T }
export interface PageResponse<T> {
  content: T[]; totalElements: number; totalPages: number; number: number; size: number; last: boolean
}

// ─── Citizen Registry ─────────────────────────────────────────────────────────
export type CitizenStatus = 'ACTIVE' | 'INACTIVE' | 'DECEASED' | 'SUSPENDED'
export type Gender = 'MALE' | 'FEMALE'

export interface Citizen {
  id: number; nationalId: string; firstName: string; lastName: string
  dateOfBirth: string; gender: Gender; placeOfBirth: string; street: string
  status: CitizenStatus; photoUrl: string | null; fatherNin: string | null
  motherNin: string | null; createdAt: string
}

export interface AuthResponse { token: string; username: string; roles: string[] }

// ─── Bridge ───────────────────────────────────────────────────────────────────
export type InstitutionStatus = 'ACTIVE' | 'SUSPENDED' | 'PENDING'
export type OperationType = 'QUERY' | 'PUBLISH'
export type ExchangeResult = 'SUCCESS' | 'DENIED' | 'NOT_FOUND' | 'ERROR'
export type AlertSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export type AlertStatus = 'OPEN' | 'REVIEWED' | 'DISMISSED'
export type AnomalyType =
  | 'HIGH_VOLUME_QUERIES' | 'REPEATED_CITIZEN_QUERY' | 'OFF_HOURS_SENSITIVE_ACCESS'
  | 'SELF_QUERY_SUSPICION' | 'BULK_CITIZEN_SCAN' | 'DENIED_REPEATED_ATTEMPT'

export interface Institution {
  institutionCode: string; name: string; description: string | null
  publishableTypes: string[]; status: InstitutionStatus
  createdAt: string; updatedAt: string
}

export interface InstitutionPermission {
  id: number; requestingCode: string; targetCode: string; dataType: string
  grantedBy: string | null; grantedAt: string; active: boolean
}

export interface ExchangeLog {
  id: number; requestingCode: string; targetCode: string | null
  nationalId: string | null; dataType: string; operationType: OperationType
  result: ExchangeResult; responseTimeMs: number | null; details: string | null
  exchangedAt: string; purposeCode: string | null; caseReference: string | null
}

export interface AnomalyAlert {
  alertId: string; institutionCode: string; alertType: AnomalyType
  description: string; severity: AlertSeverity; nationalIdInvolved: string | null
  detectedAt: string; status: AlertStatus; reviewedBy: string | null; reviewedAt: string | null
}

export interface AnomalyAlertSummary {
  totalOpen: number; openBySeverity: Record<string, number>
}

// ─── Medical ─────────────────────────────────────────────────────────────────
export type TestResult = 'PASS' | 'FAIL' | 'PENDING'
export type TestType = 'VISION_TEST' | 'GENERAL_CLEARANCE' | 'MENTAL_HEALTH' | 'SUBSTANCE_ABUSE' | 'CARDIOVASCULAR'

export interface MedicalRecord {
  id: string; nationalId: string; testType: TestType; result: TestResult
  testDate: string; expiryDate: string | null; createdAt: string
}

// ─── DMV ─────────────────────────────────────────────────────────────────────
export type LicenseStatus = 'ACTIVE' | 'SUSPENDED' | 'EXPIRED' | 'REVOKED'

export interface DrivingLicense {
  id: string; ownerNationalId: string; licenseNumber: string
  category: string; status: LicenseStatus; issuedDate: string; expiryDate: string
}

// ─── Tax ────────────────────────────────────────────────────────────────────
export type ComplianceStatus = 'COMPLIANT' | 'NON_COMPLIANT' | 'PENDING'

export interface TaxpayerRecord {
  id: string; citizenNationalId: string; taxId: string
  taxpayerType: string; status: string; complianceStatus: ComplianceStatus
}

// ─── Business ───────────────────────────────────────────────────────────────
export type BusinessStatus = 'ACTIVE' | 'SUSPENDED' | 'EXPIRED' | 'REVOKED'

export interface Business {
  id: string; registrationNumber: string; businessName: string
  businessType: string; status: BusinessStatus; ownerNationalId: string
  registrationDate: string; expiryDate: string
}

// ─── Civil ───────────────────────────────────────────────────────────────────
export interface BirthRecord {
  id: string; certificateNumber: string; childNationalId: string
  childFirstName: string; childLastName: string; dateOfBirth: string; placeOfBirth: string
}

export interface MarriageRecord {
  id: string; certificateNumber: string; spouse1NationalId: string; spouse1FullName: string
  spouse2NationalId: string; spouse2FullName: string; marriageDate: string; status: string
}

// ─── Health ──────────────────────────────────────────────────────────────────
export interface ServiceHealth {
  name: string; url: string; port: number
  healthy: boolean; responseTimeMs: number; checkedAt: string
}
