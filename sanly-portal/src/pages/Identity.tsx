import { useQuery } from '@tanstack/react-query'
import { User, Calendar, MapPin, Home, Users } from 'lucide-react'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { t } from '../utils/i18n'
import { getCitizen } from '../api/registry'
import { getBirthRecord, getMarriageRecords } from '../api/civil'
import Badge from '../components/ui/Badge'
import ServiceUnavailable from '../components/ui/ServiceUnavailable'
import { SkeletonCard } from '../components/ui/Skeleton'
import { formatDate } from '../utils/date'
import { displayNin } from '../utils/nin'

function InfoRow({ label, value, icon }: { label: string; value: React.ReactNode; icon?: React.ReactNode }) {
  return (
    <div className="flex items-start gap-3 py-3 border-b border-gray-50 last:border-0">
      {icon && <div className="w-4 h-4 text-gray-400 mt-0.5 shrink-0">{icon}</div>}
      <div className="flex-1 min-w-0">
        <p className="text-xs text-gray-400 font-medium uppercase tracking-wide">{label}</p>
        <p className="text-sm text-dark font-medium mt-0.5">{value ?? '—'}</p>
      </div>
    </div>
  )
}

export default function Identity() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()

  const { data: citizen, isLoading: citizenLoading, isError: citizenError, refetch: refetchCitizen } = useQuery({
    queryKey: ['citizen', nationalId],
    queryFn: () => getCitizen(nationalId!),
    enabled: !!nationalId,
    staleTime: 5 * 60 * 1000,
  })

  const { data: birthRecord, isLoading: birthLoading, isError: birthError } = useQuery({
    queryKey: ['birth-record', nationalId],
    queryFn: () => getBirthRecord(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: marriageRecords, isLoading: marriageLoading } = useQuery({
    queryKey: ['marriage-records', nationalId],
    queryFn: () => getMarriageRecords(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const activeMarriage = marriageRecords?.find((m) => m.status === 'ACTIVE')
  const mySpouse = activeMarriage
    ? (activeMarriage.spouse1NationalId === nationalId
        ? { name: activeMarriage.spouse2FullName, nin: activeMarriage.spouse2NationalId }
        : { name: activeMarriage.spouse1FullName, nin: activeMarriage.spouse1NationalId })
    : null

  return (
    <div className="space-y-6">
      <h1 className="page-title">{t(lang, 'identityTitle')}</h1>

      {/* Citizen record card */}
      {citizenLoading ? (
        <SkeletonCard />
      ) : citizenError ? (
        <ServiceUnavailable onRetry={refetchCitizen} />
      ) : citizen ? (
        <div className="card">
          {/* Header */}
          <div className="flex items-center gap-4 pb-4 border-b border-gray-100 mb-2">
            <div className="w-16 h-16 rounded-2xl bg-primary/10 flex items-center justify-center text-primary text-2xl font-bold shrink-0">
              {citizen.firstName[0]}
            </div>
            <div>
              <h2 className="text-xl font-bold text-dark">{citizen.firstName} {citizen.lastName}</h2>
              <p className="text-sm text-gray-400 font-mono mt-0.5">{displayNin(citizen.nationalId)}</p>
              <div className="mt-1">
                <Badge
                  label={t(lang, citizen.status === 'ACTIVE' ? 'active' : citizen.status === 'DECEASED' ? 'deceased' : 'inactive')}
                  status={citizen.status}
                />
              </div>
            </div>
          </div>

          <div className="grid sm:grid-cols-2 gap-x-8">
            <div>
              <InfoRow label={t(lang, 'dateOfBirth')} value={formatDate(citizen.dateOfBirth, lang)} icon={<Calendar />} />
              <InfoRow label={t(lang, 'gender')} value={citizen.gender === 'MALE' ? t(lang, 'male') : t(lang, 'female')} icon={<User />} />
              <InfoRow label={t(lang, 'placeOfBirth')} value={citizen.placeOfBirth} icon={<MapPin />} />
            </div>
            <div>
              <InfoRow label={t(lang, 'address')} value={citizen.street} icon={<Home />} />
              <InfoRow label={t(lang, 'nationalId')} value={<span className="font-mono">{displayNin(citizen.nationalId)}</span>} />
            </div>
          </div>
        </div>
      ) : null}

      {/* Family */}
      <div className="card">
        <h2 className="section-title flex items-center gap-2">
          <Users className="w-5 h-5 text-primary" />
          {t(lang, 'familyLinks')}
        </h2>
        <div className="grid sm:grid-cols-3 gap-4">
          {[
            { label: t(lang, 'father'), nin: citizen?.fatherNin },
            { label: t(lang, 'mother'), nin: citizen?.motherNin },
            { label: t(lang, 'spouse'), nin: mySpouse?.nin, name: mySpouse?.name },
          ].map(({ label, nin: memberNin, name }) => (
            <div key={label} className="bg-gray-50 rounded-xl p-4">
              <p className="text-xs text-gray-400 font-medium uppercase tracking-wide mb-2">{label}</p>
              {memberNin ? (
                <>
                  <p className="text-sm font-semibold text-dark">{name ?? '—'}</p>
                  <p className="text-xs text-gray-400 font-mono mt-0.5">{displayNin(memberNin)}</p>
                </>
              ) : (
                <p className="text-sm text-gray-300">—</p>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Civil records */}
      <div className="grid sm:grid-cols-2 gap-4">
        {/* Birth certificate */}
        <div className="card">
          <h2 className="section-title">{t(lang, 'birthCertificate')}</h2>
          {birthLoading ? (
            <div className="space-y-2">
              <div className="skeleton h-4 w-full rounded" />
              <div className="skeleton h-4 w-2/3 rounded" />
            </div>
          ) : birthError || !birthRecord ? (
            <p className="text-sm text-gray-400">{t(lang, 'noData')}</p>
          ) : (
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">{t(lang, 'certificateNumber')}</span>
                <span className="font-mono font-medium text-dark">{birthRecord.certificateNumber}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">{t(lang, 'issuedOn')}</span>
                <span className="text-dark">{formatDate(birthRecord.createdAt, lang)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">{t(lang, 'placeOfBirth')}</span>
                <span className="text-dark">{birthRecord.placeOfBirth}</span>
              </div>
            </div>
          )}
        </div>

        {/* Marriage certificate */}
        <div className="card">
          <h2 className="section-title">{t(lang, 'marriageCertificate')}</h2>
          {marriageLoading ? (
            <div className="space-y-2">
              <div className="skeleton h-4 w-full rounded" />
              <div className="skeleton h-4 w-2/3 rounded" />
            </div>
          ) : !activeMarriage ? (
            <p className="text-sm text-gray-400">{t(lang, 'noData')}</p>
          ) : (
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">{t(lang, 'certificateNumber')}</span>
                <span className="font-mono font-medium text-dark">{activeMarriage.certificateNumber}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">{t(lang, 'issuedOn')}</span>
                <span className="text-dark">{formatDate(activeMarriage.marriageDate, lang)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">Status</span>
                <Badge label={t(lang, activeMarriage.status === 'ACTIVE' ? 'active' : 'dissolved')} status={activeMarriage.status} size="sm" />
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
