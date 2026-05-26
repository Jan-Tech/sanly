// ─── Navigation ──────────────────────────────────────────────────────────────

export type AuthStackParamList = {
  Login: undefined
}

export type ServicesStackParamList = {
  Services: undefined
  Medical: undefined
  License: undefined
  Vehicle: undefined
  Tax: undefined
  Business: undefined
  Property: undefined
  Education: undefined
  Social: undefined
  Pension: undefined
  Customs: undefined
  Court: undefined
  Appointments: undefined
  Banking: undefined
  Signatures: undefined
}

export type ProfileStackParamList = {
  Profile: undefined
  Identity: undefined
  AccessLog: undefined
  Settings: undefined
  Sessions: undefined
}

export type MainTabParamList = {
  HomeTab: undefined
  ServicesTab: undefined
  DocumentsTab: undefined
  NotificationsTab: undefined
  ProfileTab: undefined
}

// ─── Shared ──────────────────────────────────────────────────────────────────

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

// ─── Auth ─────────────────────────────────────────────────────────────────────

export interface LoginResponse {
  status: 'OTP_REQUIRED' | 'PHONE_REQUIRED' | 'SUCCESS'
  sessionToken?: string
  phoneMasked?: string
  attemptsRemaining?: number
  token?: string
  tokenType?: string
  expiresIn?: number
  username?: string
  roles?: string[]
  sessionId?: string
  message?: string
}

export interface SessionInfo {
  sessionId: string
  nationalId: string
  createdAt: string
  lastSeenAt: string
  expiresAt: string
  status: string
  ipAddress: string
  userAgent: string
  currentSession: boolean
}

// ─── Citizen ──────────────────────────────────────────────────────────────────

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

// ─── Medical ──────────────────────────────────────────────────────────────────

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

export type LicenseStatus = 'ACTIVE' | 'SUSPENDED' | 'EXPIRED' | 'REVOKED'

export interface DrivingLicense {
  id: string
  ownerNationalId: string
  licenseNumber: string
  category: string
  status: LicenseStatus
  issuedDate: string
  expiryDate: string
  createdAt: string
}

// ─── Tax ─────────────────────────────────────────────────────────────────────

export type ComplianceStatus = 'COMPLIANT' | 'NON_COMPLIANT' | 'PENDING'
export type FilingStatus = 'PENDING' | 'SUBMITTED' | 'ACCEPTED' | 'REJECTED'

export interface TaxpayerRecord {
  id: string
  citizenNationalId: string
  taxId: string
  taxpayerType: string
  status: string
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

export interface Business {
  id: string
  registrationNumber: string
  businessName: string
  businessType: string
  status: string
  ownerNationalId: string
  ownerFullName: string
  address: string
  registrationDate: string
  expiryDate: string
  createdAt: string
}

// ─── Bridge / Access Log ──────────────────────────────────────────────────────

export interface ExchangeLog {
  id: string
  requestingInstitutionCode: string
  targetInstitutionCode: string
  subjectNationalId: string
  dataType: string
  timestamp: string
  success: boolean
}

// ─── Notifications ────────────────────────────────────────────────────────────

export interface Notification {
  notificationId: string
  eventType: string
  title: string
  body: string
  channel: string
  language: string
  status: 'UNREAD' | 'READ' | 'DELIVERED' | 'FAILED'
  createdAt: string
  readAt: string | null
}
