// ─── Officer & Auth ──────────────────────────────────────────────────────────

export type OfficerRole =
  | 'ROLE_POLICE'
  | 'ROLE_DMV'
  | 'ROLE_MEDICAL'
  | 'ROLE_CUSTOMS'
  | 'ROLE_CIVIL'
  | 'ROLE_COURT'
  | 'ROLE_EDUCATION'
  | 'ROLE_LAND'
  | 'ROLE_TAX'
  | 'ROLE_SOCIAL'
  | 'ROLE_ADMIN'

export type Institution =
  | 'POLICE' | 'DMV' | 'MEDICAL' | 'CUSTOMS' | 'CIVIL'
  | 'COURT' | 'EDUCATION' | 'LAND' | 'TAX' | 'SOCIAL' | 'ADMIN'

export interface Officer {
  officerId: string
  officerName: string
  role: OfficerRole
  institution: Institution
  badgeNumber?: string
  region?: string
}

// ─── Navigation ───────────────────────────────────────────────────────────────

export type AuthStackParamList = {
  Login: undefined
}

export type ScanStackParamList = {
  Scanner: undefined
  ScanHistory: undefined
  ScanResultDetail: { code: string; type: ScanCodeType; data: unknown }
}

export type SearchStackParamList = {
  Search: undefined
  CitizenDetail: { nationalId: string }
}

export type ActionsStackParamList = {
  RoleActionsHome: undefined
  CivilForm: { formType: 'BIRTH' | 'MARRIAGE' | 'DEATH' }
  DmvApplicationDetail: { applicationId: string }
  CourtFineDetail: { fineId: string }
  CourtCaseDetail: { caseId: string }
  LandTransferDetail: { applicationId: string }
  TaxFilingDetail: { filingId: string }
}

export type ProfileStackParamList = {
  Profile: undefined
  Settings: undefined
}

export type MainTabParamList = {
  HomeTab: undefined
  ScanTab: undefined
  SearchTab: undefined
  ActionsTab: undefined
  ProfileTab: undefined
}

// ─── Scan codes ───────────────────────────────────────────────────────────────

export type ScanCodeType =
  | 'NIN'
  | 'LICENSE'
  | 'BUSINESS'
  | 'APPOINTMENT'
  | 'FINE'
  | 'CASE'
  | 'PLATE'
  | 'CADASTRAL'
  | 'DIPLOMA'
  | 'BIRTH_CERT'
  | 'MARR_CERT'
  | 'DEATH_CERT'
  | 'UNKNOWN'

export interface ParsedCode {
  type: ScanCodeType
  rawCode: string
  value: string
}

export interface ScanHistoryItem {
  id: string
  rawCode: string
  type: ScanCodeType
  timestamp: string
  summary?: string
}

// ─── Citizens ─────────────────────────────────────────────────────────────────

export interface Citizen {
  nationalId: string
  firstName: string
  lastName: string
  dateOfBirth: string
  gender: string
  status: 'ACTIVE' | 'DECEASED' | 'SUSPENDED'
  fatherNin?: string
  motherNin?: string
  maritalStatus?: string
  address?: string
}

// ─── Police ───────────────────────────────────────────────────────────────────

export interface CitizenCheck {
  checkId: string
  nationalId: string
  checkType: 'DRIVING_LICENSE' | 'TAX_STATUS' | 'FULL_CHECK'
  results: Record<string, unknown>
  checkedAt: string
  officerId: string
}

export interface CriminalRecord {
  recordId: string
  nationalId: string
  offenseType: string
  offenseDate: string
  verdict: string
  sentenceDescription?: string
  courtName: string
  caseNumber: string
  createdAt: string
}

// ─── DMV ──────────────────────────────────────────────────────────────────────

export interface DrivingLicense {
  licenseId: string
  licenseNumber: string
  nationalId: string
  holderName: string
  categories: string[]
  issueDate: string
  expiryDate: string
  status: 'VALID' | 'EXPIRED' | 'SUSPENDED' | 'REVOKED'
  restrictions?: string
}

export interface LicenseApplication {
  applicationId: string
  nationalId: string
  applicantName: string
  category: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  submittedAt: string
  visionCheckResult?: string
}

// ─── Medical ──────────────────────────────────────────────────────────────────

export type TestType =
  | 'BLOOD_TEST' | 'URINE_TEST' | 'VISION_TEST' | 'HEARING_TEST'
  | 'CHEST_XRAY' | 'ECG' | 'FULL_PHYSICAL' | 'COVID_TEST' | 'VACCINATION'

export interface MedicalRecord {
  recordId: string
  nationalId: string
  testType: TestType
  result: string
  testDate: string
  notes?: string
  clinicName: string
  doctorName: string
}

// ─── Customs ──────────────────────────────────────────────────────────────────

export type DeclarationStatus = 'PENDING' | 'CLEARED' | 'HELD' | 'INSPECTED' | 'REJECTED'

export interface CustomsDeclaration {
  declarationId: string
  declarationCode: string
  declarantNin: string
  declarantName: string
  portOfEntry: string
  goodsDescription: string
  totalValue: number
  currency: string
  declaredDuties: number
  status: DeclarationStatus
  submittedAt: string
  inspectedAt?: string
  inspectionNotes?: string
}

// ─── Civil ────────────────────────────────────────────────────────────────────

export interface BirthRecord {
  recordId: string
  certificateNumber: string
  childNationalId: string
  childFirstName: string
  childLastName: string
  dateOfBirth: string
  placeOfBirth: string
  fatherNationalId?: string
  motherNationalId?: string
  registeredAt: string
}

export interface MarriageRecord {
  recordId: string
  certificateNumber: string
  spouse1NationalId: string
  spouse2NationalId: string
  marriageDate: string
  marriagePlace: string
  status: 'ACTIVE' | 'DISSOLVED'
  registeredAt: string
}

export interface DeathRecord {
  recordId: string
  certificateNumber: string
  deceasedNationalId: string
  dateOfDeath: string
  placeOfDeath: string
  causeOfDeath?: string
  registeredAt: string
}

// ─── Court ────────────────────────────────────────────────────────────────────

export interface CourtFine {
  fineId: string
  fineCode: string
  nationalId: string
  citizenName: string
  offenseType: string
  fineAmount: number
  issuedDate: string
  dueDate: string
  status: 'OUTSTANDING' | 'PAID' | 'OVERDUE' | 'DISPUTED'
  paidAt?: string
}

export interface CourtCase {
  caseId: string
  caseNumber: string
  nationalId: string
  citizenName: string
  caseType: string
  status: 'OPEN' | 'CLOSED' | 'APPEALED' | 'SCHEDULED'
  filedDate: string
  nextHearingDate?: string
  judgeName?: string
  verdict?: string
}

// ─── Education ────────────────────────────────────────────────────────────────

export interface Diploma {
  diplomaId: string
  diplomaCode: string
  nationalId: string
  holderName: string
  institutionCode: string
  institutionName: string
  degree: string
  field: string
  graduationYear: number
  gpa?: number
  status: 'VALID' | 'REVOKED'
  issuedAt: string
}

export interface Enrollment {
  enrollmentId: string
  nationalId: string
  institutionCode: string
  institutionName: string
  program: string
  status: 'PENDING' | 'ACTIVE' | 'GRADUATED' | 'DROPPED' | 'TRANSFERRED'
  enrolledAt: string
  expectedGraduation?: string
}

// ─── Land ─────────────────────────────────────────────────────────────────────

export interface Property {
  propertyId: string
  cadastralCode: string
  address: string
  area: number
  propertyType: string
  status: 'ACTIVE' | 'UNDER_TRANSFER' | 'DISPUTED' | 'FORECLOSED'
  ownerNationalId: string
  ownerName: string
  valuationAmount?: number
  registeredAt: string
}

export interface TransferApplication {
  applicationId: string
  propertyId: string
  cadastralCode: string
  currentOwnerNin: string
  newOwnerNin: string
  agreedPrice: number
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  submittedAt: string
  taxCheckResult?: string
}

// ─── Tax ──────────────────────────────────────────────────────────────────────

export interface TaxpayerRecord {
  recordId: string
  taxId: string
  nationalId: string
  fullName: string
  taxpayerType: 'INDIVIDUAL' | 'BUSINESS'
  status: 'ACTIVE' | 'SUSPENDED' | 'DEREGISTERED'
  registeredAt: string
}

export interface TaxFiling {
  filingId: string
  taxId: string
  year: number
  filingStatus: 'PENDING' | 'SUBMITTED' | 'ACCEPTED' | 'REJECTED'
  submittedAt?: string
  processedAt?: string
}

// ─── Social ───────────────────────────────────────────────────────────────────

export type BenefitType = 'UNEMPLOYMENT' | 'DISABILITY' | 'CHILD_BENEFIT' | 'PENSION_SUPPLEMENT' | 'HOUSING_ASSISTANCE'

export interface BenefitClaim {
  claimId: string
  nationalId: string
  citizenName: string
  benefitType: BenefitType
  status: 'PENDING' | 'ACTIVE' | 'REJECTED' | 'EXPIRED'
  startDate?: string
  endDate?: string
  monthlyAmount: number
  submittedAt: string
}

export interface UnemploymentRecord {
  recordId: string
  nationalId: string
  registeredAt: string
  status: 'REGISTERED' | 'PLACED' | 'EXPIRED'
  lastEmployer?: string
  reason?: string
}

// ─── Appointments ─────────────────────────────────────────────────────────────

export interface Appointment {
  appointmentId: string
  appointmentCode: string
  nationalId: string
  citizenName: string
  officeCode: string
  officeName: string
  serviceType: string
  scheduledAt: string
  status: 'SCHEDULED' | 'CONFIRMED' | 'COMPLETED' | 'NO_SHOW' | 'CANCELLED'
  notes?: string
}

// ─── Bridge ───────────────────────────────────────────────────────────────────

export interface BridgeQueryResult {
  dataType: string
  subjectNationalId: string
  publisherCode: string
  recordRef: string
  data: Record<string, unknown>
  publishedAt: string
}
