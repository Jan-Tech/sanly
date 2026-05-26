import { useState } from 'react'
import { X } from 'lucide-react'

interface ServiceDef {
  name: string
  port: number | null
  description: string
  publishes: string[]
  queries: string[]
  features: string[]
}

interface LayerDef {
  id: string
  label: string
  sublabel: string
  tileClass: string
  headerClass: string
  services: ServiceDef[]
}

const layers: LayerDef[] = [
  {
    id: 'gateway',
    label: 'Gateway',
    sublabel: 'Public entry point',
    tileClass: 'border-primary bg-primary/10 hover:bg-primary/20',
    headerClass: 'text-primary',
    services: [
      {
        name: 'API Gateway',
        port: 8080,
        description:
          'Central entry point for all client requests. Routes traffic to upstream services, validates JWT tokens, enforces rate limiting, and provides unified request/response logging across the platform.',
        publishes: [],
        queries: [],
        features: ['JWT validation', 'Rate limiting', 'Request routing', 'Load balancing', 'SSL termination', 'CORS policy'],
      },
    ],
  },
  {
    id: 'foundation',
    label: 'Foundation',
    sublabel: 'Identity & data exchange',
    tileClass: 'border-sky-500 bg-sky-500/10 hover:bg-sky-500/20',
    headerClass: 'text-sky-400',
    services: [
      {
        name: 'citizen-registry',
        port: 8081,
        description:
          'Core identity service. Issues National ID Numbers (NIN), manages biometric enrollment, handles citizen authentication, and maintains the authoritative citizen profile used across all services.',
        publishes: ['CITIZEN_CREATED', 'CITIZEN_UPDATED', 'NIN_ISSUED'],
        queries: [],
        features: ['NIN issuance', 'Biometric enrollment', 'JWT issuance', 'Photo management', 'Death registration lock', 'Role management'],
      },
      {
        name: 'sanly-bridge',
        port: 8082,
        description:
          'Secure inter-service data exchange hub. Institution services publish structured events and query data from other institutions via signed, audited API calls with institution-level access control.',
        publishes: [],
        queries: [],
        features: ['Event publishing', 'Cross-service queries', 'Institution auth', 'Audit logging', 'Data access control', 'Schema validation'],
      },
    ],
  },
  {
    id: 'institution',
    label: 'Institution Services',
    sublabel: '14 domain services — each integrated with sanly-bridge',
    tileClass: 'border-violet-500 bg-violet-500/10 hover:bg-violet-500/20',
    headerClass: 'text-violet-400',
    services: [
      {
        name: 'sanly-police',
        port: 8083,
        description:
          'Criminal record management, citizen background checks, and incident reporting. Law enforcement officers use the field app to submit records and perform real-time citizen checks.',
        publishes: ['CRIMINAL_RECORD', 'WANTED_STATUS', 'BACKGROUND_CHECK'],
        queries: ['CITIZEN_PROFILE', 'LICENSE_DATA', 'VEHICLE_DATA'],
        features: ['Criminal records', 'Background checks', 'Incident reports', 'Wanted list', 'Officer assignments'],
      },
      {
        name: 'sanly-dmv',
        port: 8084,
        description:
          'Driving license issuance, renewal, vehicle registration, and license category management. DMV officers process pending applications in the field app.',
        publishes: ['LICENSE_ISSUED', 'LICENSE_SUSPENDED', 'LICENSE_RENEWED'],
        queries: ['CITIZEN_PROFILE', 'CRIMINAL_RECORD'],
        features: ['License issuance', 'License renewal', 'Category management', 'Suspension tracking', 'Expiry alerts'],
      },
      {
        name: 'sanly-medical',
        port: 8085,
        description:
          'Medical test results, health records, vaccination status, and healthcare provider management. Supports offline queue for field medical officers operating in low-connectivity areas.',
        publishes: ['MEDICAL_RECORD', 'VACCINATION_STATUS', 'TEST_RESULT'],
        queries: ['CITIZEN_PROFILE'],
        features: ['Test results', 'Health records', 'Vaccination registry', 'Provider management', 'Offline queue'],
      },
      {
        name: 'sanly-customs',
        port: 8086,
        description:
          'Import/export declarations, border crossing records, and cargo inspection management. Customs agents process clearance and hold queues via the officer app.',
        publishes: ['DECLARATION_CLEARED', 'DECLARATION_HELD', 'BORDER_CROSSING'],
        queries: ['CITIZEN_PROFILE', 'BUSINESS_DATA', 'CRIMINAL_RECORD'],
        features: ['Declarations', 'Port management', 'Inspection queue', 'Hold/clear workflow', 'Cargo tracking'],
      },
      {
        name: 'sanly-civil',
        port: 8087,
        description:
          'Birth, marriage, and death registration. Issues civil certificates with QR codes. Civil registry officers use the field app with offline queue support for registrations in remote areas.',
        publishes: ['BIRTH_REGISTERED', 'MARRIAGE_REGISTERED', 'DEATH_REGISTERED'],
        queries: ['CITIZEN_PROFILE'],
        features: ['Birth registry', 'Marriage registry', 'Death registry', 'Certificate issuance', 'Offline queue'],
      },
      {
        name: 'sanly-court',
        port: 8088,
        description:
          'Court case management, fine issuance, payment tracking, and legal proceeding records. Court officers confirm payments and manage case queues.',
        publishes: ['FINE_ISSUED', 'CASE_OPENED', 'CASE_CLOSED', 'PAYMENT_RECEIVED'],
        queries: ['CITIZEN_PROFILE', 'CRIMINAL_RECORD', 'BUSINESS_DATA'],
        features: ['Case management', 'Fine issuance', 'Payment tracking', 'Hearing schedule', 'Legal records'],
      },
      {
        name: 'sanly-education',
        port: 8089,
        description:
          'Academic institution management, student enrollment queue, and diploma issuance with QR verification. Education officers process school intake and issue diplomas.',
        publishes: ['DIPLOMA_ISSUED', 'ENROLLMENT_APPROVED'],
        queries: ['CITIZEN_PROFILE'],
        features: ['Institution registry', 'Diploma issuance', 'Enrollment queue', 'Diploma verification', 'QR codes'],
      },
      {
        name: 'sanly-land',
        port: 8090,
        description:
          'Land and property registry, cadastral records, ownership transfer, and property dispute tracking. Land registry officers approve transfer applications.',
        publishes: ['PROPERTY_REGISTERED', 'OWNERSHIP_TRANSFERRED'],
        queries: ['CITIZEN_PROFILE', 'BUSINESS_DATA'],
        features: ['Cadastral registry', 'Ownership records', 'Transfer approvals', 'Property lookup', 'Dispute log'],
      },
      {
        name: 'sanly-tax',
        port: 8091,
        description:
          'Taxpayer registry, tax filing queue, payment records, and compliance monitoring. Tax officers process filing queues and register tax debts.',
        publishes: ['TAX_FILING_ACCEPTED', 'TAX_DEBT_REGISTERED'],
        queries: ['CITIZEN_PROFILE', 'BUSINESS_DATA', 'PROPERTY_DATA'],
        features: ['Taxpayer registry', 'Filing queue', 'Payment records', 'Compliance checks', 'Debt tracking'],
      },
      {
        name: 'sanly-social',
        port: 8092,
        description:
          'Social welfare programs, benefit claims processing, unemployment registration, and payment distribution. Social officers approve claims and register unemployment.',
        publishes: ['BENEFIT_APPROVED', 'UNEMPLOYMENT_REGISTERED'],
        queries: ['CITIZEN_PROFILE', 'TAX_DATA', 'MEDICAL_RECORD'],
        features: ['Benefits registry', 'Claims queue', 'Payment distribution', 'Unemployment reg.', 'Program management'],
      },
      {
        name: 'sanly-vehicle',
        port: 8093,
        description:
          'Vehicle registration, plate assignment, ownership transfers, and insurance compliance tracking. Scannable TM-PLATE-XX-XXX-XXX codes link to vehicle records.',
        publishes: ['VEHICLE_REGISTERED', 'PLATE_ASSIGNED', 'OWNERSHIP_TRANSFERRED'],
        queries: ['CITIZEN_PROFILE', 'CRIMINAL_RECORD', 'LICENSE_DATA'],
        features: ['Vehicle registry', 'Plate management', 'Insurance alerts', 'Transfer approvals', 'Inspection records'],
      },
      {
        name: 'sanly-pension',
        port: 8094,
        description:
          'Pension eligibility assessment, retirement queue processing, and pension payment management. Calculates entitlements from tax contribution history.',
        publishes: ['PENSION_APPROVED', 'RETIREMENT_REGISTERED'],
        queries: ['CITIZEN_PROFILE', 'TAX_DATA'],
        features: ['Eligibility checks', 'Retirement queue', 'Payment schedule', 'Contribution history', 'Beneficiary mgmt'],
      },
      {
        name: 'sanly-banking',
        port: 8095,
        description:
          'Banking institution integration for payment processing, account verification, and financial transaction routing for government fee collection.',
        publishes: ['PAYMENT_CONFIRMED', 'ACCOUNT_VERIFIED'],
        queries: ['CITIZEN_PROFILE', 'TAX_DATA'],
        features: ['Payment processing', 'Account verification', 'Bank integration', 'Transaction routing', 'Balance checks'],
      },
      {
        name: 'sanly-appointments',
        port: 8096,
        description:
          'Citizen appointment booking, slot management, officer assignment, and QR code generation for field check-in. Officers scan TM-APT- codes to retrieve appointment details.',
        publishes: ['APPOINTMENT_BOOKED', 'APPOINTMENT_COMPLETED'],
        queries: ['CITIZEN_PROFILE'],
        features: ['Slot management', 'Booking engine', 'Officer assignment', 'QR check-in', 'Reminder queue'],
      },
    ],
  },
  {
    id: 'platform',
    label: 'Platform Services',
    sublabel: '5 cross-cutting platform capabilities',
    tileClass: 'border-amber-500 bg-amber-500/10 hover:bg-amber-500/20',
    headerClass: 'text-amber-400',
    services: [
      {
        name: 'sanly-signature',
        port: 8100,
        description:
          'Digital signature creation, certificate management, and document signing verification. Provides PKI infrastructure for legally binding electronic signatures on government documents.',
        publishes: ['DOCUMENT_SIGNED'],
        queries: ['CITIZEN_PROFILE'],
        features: ['Key management', 'Certificate issuance', 'Signature verification', 'PKI infrastructure', 'Revocation lists'],
      },
      {
        name: 'sanly-documents',
        port: 8101,
        description:
          'Secure document storage, retrieval, and lifecycle management for all government-issued documents. Provides versioned storage with expiry tracking and citizen access control.',
        publishes: ['DOCUMENT_STORED', 'DOCUMENT_EXPIRED'],
        queries: ['CITIZEN_PROFILE'],
        features: ['Document storage', 'Version control', 'Access control', 'Expiry management', 'Audit trail'],
      },
      {
        name: 'sanly-analytics',
        port: 8102,
        description:
          'Platform-wide analytics aggregation, reporting dashboards, and anomaly detection across all services. Powers the sanly-admin analytics page with real-time event data.',
        publishes: [],
        queries: ['CITIZEN_PROFILE'],
        features: ['Metrics aggregation', 'Anomaly detection', 'Custom reports', 'Real-time dashboards', 'Event replay'],
      },
      {
        name: 'sanly-data-requests',
        port: 8103,
        description:
          'Citizen data access requests, data export, and inter-agency data sharing approval workflows. Enables citizens to request their full data profile and manage consent.',
        publishes: ['DATA_REQUEST_APPROVED'],
        queries: ['CITIZEN_PROFILE'],
        features: ['Access requests', 'Data export', 'Consent management', 'Sharing approvals', 'Audit log'],
      },
      {
        name: 'sanly-notifications',
        port: 8104,
        description:
          'Push notification delivery, SMS routing, and in-app message management for both citizens and officers. Template-driven with delivery tracking and retry logic.',
        publishes: [],
        queries: ['CITIZEN_PROFILE'],
        features: ['Push notifications', 'SMS routing', 'In-app messages', 'Template engine', 'Delivery tracking'],
      },
    ],
  },
  {
    id: 'frontend',
    label: 'Frontend Applications',
    sublabel: '4 client applications',
    tileClass: 'border-emerald-500 bg-emerald-500/10 hover:bg-emerald-500/20',
    headerClass: 'text-emerald-400',
    services: [
      {
        name: 'sanly-portal',
        port: 3000,
        description:
          'Citizen self-service web portal. React + Vite application for citizens to view their profile, apply for services, book appointments, download documents, and track applications.',
        publishes: [],
        queries: [],
        features: ['Citizen profile', 'Service applications', 'Appointment booking', 'Document download', 'Multilingual (en/tk/ru)'],
      },
      {
        name: 'sanly-mobile',
        port: null,
        description:
          'Citizen mobile app built with React Native Expo SDK 52. Mirrors the portal with biometric login, push notification support, QR scanner, and offline document caching.',
        publishes: [],
        queries: [],
        features: ['Biometric login', 'Push notifications', 'QR scanner', 'Offline cache', 'Expo SDK 52'],
      },
      {
        name: 'sanly-officer-app',
        port: null,
        description:
          'Field officer mobile app built with React Native Expo SDK 51. Role-based UI for all 10 institution officer types, full QR/barcode scanner, offline civil/medical queue, and biometric re-auth.',
        publishes: [],
        queries: [],
        features: ['Role-based UI (10 roles)', 'QR/barcode scan', 'Offline queue', 'Biometric login', 'Expo SDK 51'],
      },
      {
        name: 'sanly-admin',
        port: 5173,
        description:
          'Administration dashboard built with React + Vite. Manages citizens, bridge institutions, permissions, audit logs, service-specific queues (education, land, social, customs, court, vehicle, pension), and platform analytics.',
        publishes: [],
        queries: [],
        features: ['Citizen management', 'Institution admin', 'Audit logs', 'Analytics', 'Service health'],
      },
    ],
  },
]

export default function ArchitectureDiagram() {
  const [selected, setSelected] = useState<{ layerId: string; service: ServiceDef } | null>(null)

  function handleSelect(layerId: string, service: ServiceDef) {
    if (selected?.service.name === service.name) {
      setSelected(null)
    } else {
      setSelected({ layerId, service })
    }
  }

  const selectedLayer = selected ? layers.find(l => l.id === selected.layerId) : null

  return (
    <div className="min-h-screen bg-background pb-8">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 px-6 py-5">
        <div className="max-w-7xl mx-auto">
          <h1 className="text-xl font-bold text-dark">System Architecture</h1>
          <p className="text-sm text-gray-500 mt-0.5">
            {layers.reduce((sum, l) => sum + l.services.length, 0)} services across {layers.length} layers — click any tile to view details
          </p>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-6 pt-6 space-y-6">
        {/* Legend */}
        <div className="bg-white rounded-xl border border-gray-200 px-5 py-3 flex flex-wrap items-center gap-x-6 gap-y-2">
          <span className="text-xs font-semibold text-gray-500 uppercase tracking-wider mr-2">Legend</span>
          <LegendItem color="bg-green-500/20 border-green-500/40 text-green-700" label="Publishes to bridge" />
          <LegendItem color="bg-blue-500/20 border-blue-500/40 text-blue-700" label="Queries from bridge" />
          <LegendItem color="bg-gray-100 border-gray-200 text-gray-600" label="Key features" />
          <div className="ml-auto flex flex-wrap gap-x-5 gap-y-1.5">
            <LayerLegendDot color="bg-primary" label="Gateway" />
            <LayerLegendDot color="bg-sky-500" label="Foundation" />
            <LayerLegendDot color="bg-violet-500" label="Institution" />
            <LayerLegendDot color="bg-amber-500" label="Platform" />
            <LayerLegendDot color="bg-emerald-500" label="Frontend" />
          </div>
        </div>

        {/* Layers */}
        {layers.map(layer => (
          <div key={layer.id} className="bg-white rounded-xl border border-gray-200 overflow-hidden">
            {/* Layer header */}
            <div className="px-5 py-3 border-b border-gray-100 flex items-baseline gap-3">
              <h2 className={`text-sm font-bold ${layer.headerClass}`}>{layer.label}</h2>
              <span className="text-xs text-gray-400">{layer.sublabel}</span>
            </div>

            {/* Tiles */}
            <div className="p-4 flex flex-wrap gap-2">
              {layer.services.map(svc => {
                const isActive = selected?.service.name === svc.name
                return (
                  <button
                    key={svc.name}
                    onClick={() => handleSelect(layer.id, svc)}
                    className={`
                      flex flex-col items-start px-3 py-2.5 rounded-lg border text-left transition-all
                      ${layer.tileClass}
                      ${isActive ? 'ring-2 ring-offset-1 ring-current scale-[1.02]' : ''}
                    `}
                  >
                    <span className="text-sm font-semibold text-dark">{svc.name}</span>
                    <span className="text-xs text-gray-400 mt-0.5 font-mono">
                      {svc.port ? `:${svc.port}` : 'mobile'}
                    </span>
                  </button>
                )
              })}
            </div>

            {/* Inline detail panel — shown when the selected service belongs to this layer */}
            {selected && selected.layerId === layer.id && (
              <div className="border-t border-gray-100 bg-gray-50 px-5 py-4">
                <DetailPanel
                  service={selected.service}
                  layerHeaderClass={selectedLayer?.headerClass ?? ''}
                  onClose={() => setSelected(null)}
                />
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}

function DetailPanel({
  service,
  layerHeaderClass,
  onClose,
}: {
  service: ServiceDef
  layerHeaderClass: string
  onClose: () => void
}) {
  return (
    <div>
      <div className="flex items-start justify-between mb-3">
        <div>
          <h3 className={`font-bold text-base ${layerHeaderClass}`}>{service.name}</h3>
          {service.port && (
            <span className="text-xs font-mono text-gray-400">port {service.port}</span>
          )}
        </div>
        <button
          onClick={onClose}
          className="text-gray-400 hover:text-gray-600 transition-colors"
        >
          <X className="w-4 h-4" />
        </button>
      </div>

      <p className="text-sm text-gray-600 mb-4 leading-relaxed max-w-3xl">{service.description}</p>

      <div className="flex flex-wrap gap-6">
        {service.publishes.length > 0 && (
          <BadgeGroup
            title="Publishes to bridge"
            items={service.publishes}
            itemClass="bg-green-500/15 border border-green-500/30 text-green-700"
            titleClass="text-green-700"
          />
        )}

        {service.queries.length > 0 && (
          <BadgeGroup
            title="Queries from bridge"
            items={service.queries}
            itemClass="bg-blue-500/15 border border-blue-500/30 text-blue-700"
            titleClass="text-blue-700"
          />
        )}

        {service.features.length > 0 && (
          <BadgeGroup
            title="Key features"
            items={service.features}
            itemClass="bg-white border border-gray-200 text-gray-600"
            titleClass="text-gray-500"
          />
        )}

        {service.publishes.length === 0 && service.queries.length === 0 && (
          <p className="text-xs text-gray-400 italic">
            Client application — communicates with the API Gateway directly, does not participate in bridge data exchange.
          </p>
        )}
      </div>
    </div>
  )
}

function BadgeGroup({
  title,
  items,
  itemClass,
  titleClass,
}: {
  title: string
  items: string[]
  itemClass: string
  titleClass: string
}) {
  return (
    <div>
      <p className={`text-xs font-semibold uppercase tracking-wider mb-1.5 ${titleClass}`}>{title}</p>
      <div className="flex flex-wrap gap-1.5">
        {items.map(item => (
          <span key={item} className={`text-xs px-2 py-0.5 rounded-full font-medium ${itemClass}`}>
            {item}
          </span>
        ))}
      </div>
    </div>
  )
}

function LegendItem({ color, label }: { color: string; label: string }) {
  return (
    <div className="flex items-center gap-1.5">
      <span className={`text-xs px-2 py-0.5 rounded-full border ${color}`}>EXAMPLE</span>
      <span className="text-xs text-gray-500">{label}</span>
    </div>
  )
}

function LayerLegendDot({ color, label }: { color: string; label: string }) {
  return (
    <div className="flex items-center gap-1.5">
      <span className={`w-2 h-2 rounded-full ${color}`} />
      <span className="text-xs text-gray-500">{label}</span>
    </div>
  )
}
