// ─── Shared ─────────────────────────────────────────────────────────────────

export interface ApiResponse<T> {
  success: boolean
  message: string | null
  data: T
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

// ─── Citizen Registry ────────────────────────────────────────────────────────

export type Gender = 'MALE' | 'FEMALE'
export type CitizenStatus = 'ACTIVE' | 'INACTIVE' | 'DECEASED'

export interface Citizen {
  id: number
  nationalId: string
  firstName: string
  lastName: string
  dateOfBirth: string
  gender: Gender
  placeOfBirth: string
  street: string
  status: CitizenStatus
  photoUrl: string | null
  fatherNin: string | null
  motherNin: string | null
  createdAt: string
}

export interface AuthResponse {
  token: string
  username: string
  roles: string[]
}

// ─── Medical ─────────────────────────────────────────────────────────────────

export type TestType = 'VISION_TEST' | 'GENERAL_CLEARANCE' | 'MENTAL_HEALTH' | 'SUBSTANCE_ABUSE' | 'CARDIOVASCULAR'
export type TestResult = 'PASS' | 'FAIL' | 'PENDING'

export interface MedicalRecord {
  id: string
  nationalId: string
  testType: TestType
  result: TestResult
  clinicId: string
  notes: string | null
  testDate: string
  expiryDate: string | null
  bridgePublished: boolean
  createdAt: string
}

// ─── DMV ──────────────────────────────────────────────────────────────────────

export type LicenseCategory = 'A' | 'B' | 'C' | 'D' | 'E'
export type LicenseStatus = 'ACTIVE' | 'SUSPENDED' | 'EXPIRED' | 'REVOKED'
export type ApplicationStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export interface DrivingLicense {
  id: string
  ownerNationalId: string
  licenseNumber: string
  category: LicenseCategory
  status: LicenseStatus
  issuedDate: string
  expiryDate: string
  createdAt: string
}

export interface LicenseApplication {
  id: string
  applicantNationalId: string
  category: LicenseCategory
  status: ApplicationStatus
  rejectionReason: string | null
  createdAt: string
}

// ─── Tax ─────────────────────────────────────────────────────────────────────

export type TaxpayerType = 'INDIVIDUAL' | 'BUSINESS'
export type TaxpayerStatus = 'ACTIVE' | 'SUSPENDED' | 'DEREGISTERED'
export type ComplianceStatus = 'COMPLIANT' | 'NON_COMPLIANT' | 'PENDING'
export type FilingStatus = 'PENDING' | 'SUBMITTED' | 'ACCEPTED' | 'REJECTED'

export interface TaxpayerRecord {
  id: string
  citizenNationalId: string
  taxId: string
  taxpayerType: TaxpayerType
  status: TaxpayerStatus
  complianceStatus: ComplianceStatus
  registrationDate: string
}

export interface TaxFiling {
  id: string
  taxId: string
  taxYear: number
  status: FilingStatus
  declaredIncome: string | null
  taxDue: string | null
  taxPaid: string | null
  submittedAt: string | null
  processedAt: string | null
  createdAt: string
}

// ─── Business ────────────────────────────────────────────────────────────────

export type BusinessType = 'SOLE_PROPRIETORSHIP' | 'LLC' | 'JSC' | 'PARTNERSHIP'
export type BusinessStatus = 'ACTIVE' | 'SUSPENDED' | 'EXPIRED' | 'REVOKED'

export interface Business {
  id: string
  registrationNumber: string
  businessName: string
  businessType: BusinessType
  status: BusinessStatus
  ownerNationalId: string
  ownerFullName: string
  address: string
  registrationDate: string
  expiryDate: string
  createdAt: string
}

// ─── Civil ───────────────────────────────────────────────────────────────────

export type MarriageStatus = 'ACTIVE' | 'DISSOLVED'

export interface BirthRecord {
  id: string
  certificateNumber: string
  childNationalId: string
  childFirstName: string
  childLastName: string
  dateOfBirth: string
  placeOfBirth: string
  fatherNationalId: string | null
  fatherFullName: string | null
  motherNationalId: string | null
  motherFullName: string | null
  createdAt: string
}

export interface MarriageRecord {
  id: string
  certificateNumber: string
  spouse1NationalId: string
  spouse1FullName: string
  spouse2NationalId: string
  spouse2FullName: string
  marriageDate: string
  status: MarriageStatus
  dissolutionDate: string | null
  createdAt: string
}

export interface DeathRecord {
  id: string
  certificateNumber: string
  deceasedNationalId: string
  deceasedFullName: string
  dateOfDeath: string
  placeOfDeath: string
  deathCause: string | null
  createdAt: string
}

// ─── Bridge / Access Log ─────────────────────────────────────────────────────

export interface ExchangeLog {
  id: string
  requestingInstitutionCode: string
  targetInstitutionCode: string
  subjectNationalId: string
  dataType: string
  timestamp: string
  success: boolean
}
