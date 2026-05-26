import { useQuery } from '@tanstack/react-query'
import { GraduationCap, Award, BookOpen, ExternalLink, CheckCircle, XCircle } from 'lucide-react'
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer
} from 'recharts'
import { useAuthStore } from '../store/authStore'
import { useLangStore } from '../store/langStore'
import { t } from '../utils/i18n'
import { getEnrollments, getAcademicRecords, getDiplomas } from '../api/education'
import Badge from '../components/ui/Badge'
import { ServiceUnavailable } from '../components/ui/ServiceUnavailable'
import { SkeletonCard } from '../components/ui/Skeleton'

const LEVEL_LABELS: Record<string, string> = {
  PRIMARY: 'Primary',
  SECONDARY: 'Secondary',
  VOCATIONAL_CERTIFICATE: 'Vocational Certificate',
  BACHELORS: "Bachelor's",
  MASTERS: "Master's",
  PHD: 'PhD',
  POSTDOCTORAL: 'Postdoctoral',
}

const HONORS_LABELS: Record<string, string> = {
  NONE: '',
  CUM_LAUDE: 'Cum Laude',
  MAGNA_CUM_LAUDE: 'Magna Cum Laude',
  SUMMA_CUM_LAUDE: 'Summa Cum Laude',
}

const HONORS_COLORS: Record<string, string> = {
  NONE: '',
  CUM_LAUDE: 'text-blue-600 bg-blue-50',
  MAGNA_CUM_LAUDE: 'text-purple-600 bg-purple-50',
  SUMMA_CUM_LAUDE: 'text-yellow-700 bg-yellow-50',
}

export default function Education() {
  const { nationalId } = useAuthStore()
  const { lang } = useLangStore()

  const { data: enrollments, isLoading: enrollmentsLoading, isError: enrollmentsError } = useQuery({
    queryKey: ['enrollments', nationalId],
    queryFn: () => getEnrollments(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: records, isLoading: recordsLoading } = useQuery({
    queryKey: ['academic-records', nationalId],
    queryFn: () => getAcademicRecords(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  const { data: diplomas, isLoading: diplomasLoading, isError: diplomasError } = useQuery({
    queryKey: ['diplomas', nationalId],
    queryFn: () => getDiplomas(nationalId!),
    enabled: !!nationalId,
    retry: false,
  })

  if (enrollmentsError && diplomasError) {
    return <ServiceUnavailable service="Education" />
  }

  // Build GPA chart data — deduplicate by academicYear, parse gpa as float
  const gpaData = (records ?? [])
    .filter(r => r.gpa != null)
    .map(r => ({ year: r.academicYear, gpa: parseFloat(r.gpa!) }))
    .sort((a, b) => a.year.localeCompare(b.year))

  const publicVerifyUrl = (diplomaCode: string) =>
    `/proxy/education/api/v1/education/diplomas/verify/${diplomaCode}`

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-dark flex items-center gap-2">
          <GraduationCap className="w-6 h-6 text-primary" />
          {t(lang, 'education')}
        </h1>
        <p className="text-sm text-gray-400 mt-1">{t(lang, 'educationDesc')}</p>
      </div>

      {/* Diplomas */}
      <div>
        <h2 className="section-title">{t(lang, 'diplomas')}</h2>
        {diplomasLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {[1,2].map(i => <SkeletonCard key={i} />)}
          </div>
        ) : !diplomas || diplomas.length === 0 ? (
          <div className="card text-center py-10 text-gray-400">
            <Award className="w-10 h-10 mx-auto mb-2 opacity-30" />
            <p>{t(lang, 'noDiplomas')}</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {diplomas.map(diploma => (
              <div key={diploma.diplomaId}
                   className={`card border-l-4 ${diploma.status === 'VALID' ? 'border-l-green-500' : 'border-l-red-400'}`}>
                <div className="flex items-start justify-between gap-2">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="font-mono text-xs text-gray-500">{diploma.diplomaCode}</span>
                      {diploma.status === 'VALID' ? (
                        <span className="flex items-center gap-1 text-xs font-medium text-green-600">
                          <CheckCircle className="w-3.5 h-3.5" /> Valid
                        </span>
                      ) : (
                        <span className="flex items-center gap-1 text-xs font-medium text-red-500">
                          <XCircle className="w-3.5 h-3.5" /> Revoked
                        </span>
                      )}
                    </div>
                    <h3 className="font-semibold text-dark mt-1 truncate">{diploma.programName}</h3>
                    <p className="text-sm text-gray-500">
                      {LEVEL_LABELS[diploma.programLevel] || diploma.programLevel}
                      {' · '}{diploma.institutionCode}
                    </p>
                    <p className="text-xs text-gray-400 mt-1">
                      Graduated: {diploma.graduationDate}
                    </p>
                  </div>
                  {diploma.honors !== 'NONE' && (
                    <span className={`text-xs px-2 py-0.5 rounded-full font-medium shrink-0 ${HONORS_COLORS[diploma.honors]}`}>
                      {HONORS_LABELS[diploma.honors]}
                    </span>
                  )}
                </div>
                {diploma.status === 'VALID' && (
                  <a
                    href={publicVerifyUrl(diploma.diplomaCode)}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="mt-3 inline-flex items-center gap-1.5 text-xs text-primary hover:text-primary-dark font-medium"
                  >
                    <ExternalLink className="w-3.5 h-3.5" />
                    Verify / Share with Employer
                  </a>
                )}
                {diploma.status === 'REVOKED' && diploma.revokedReason && (
                  <p className="mt-2 text-xs text-red-400">Reason: {diploma.revokedReason}</p>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* GPA Chart */}
      {gpaData.length > 0 && (
        <div className="card">
          <h2 className="section-title">GPA History</h2>
          <div className="h-56">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={gpaData} margin={{ top: 5, right: 20, bottom: 5, left: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="year" tick={{ fontSize: 11 }} />
                <YAxis domain={[0, 4]} tick={{ fontSize: 11 }} />
                <Tooltip
                  contentStyle={{ borderRadius: '8px', fontSize: '12px' }}
                  formatter={(v: number) => [v.toFixed(2), 'GPA']}
                />
                <Line
                  type="monotone"
                  dataKey="gpa"
                  stroke="#4f46e5"
                  strokeWidth={2}
                  dot={{ r: 4, fill: '#4f46e5' }}
                  activeDot={{ r: 6 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}

      {/* Enrollments */}
      <div>
        <h2 className="section-title">{t(lang, 'enrollments')}</h2>
        {enrollmentsLoading ? (
          <div className="space-y-3">
            {[1,2].map(i => <div key={i} className="skeleton h-16 rounded-lg" />)}
          </div>
        ) : !enrollments || enrollments.length === 0 ? (
          <div className="card text-center py-8 text-gray-400">
            <BookOpen className="w-8 h-8 mx-auto mb-2 opacity-30" />
            <p>{t(lang, 'noEnrollments')}</p>
          </div>
        ) : (
          <div className="card divide-y divide-gray-50">
            {enrollments.map(enrollment => (
              <div key={enrollment.enrollmentId} className="py-4 first:pt-0 last:pb-0 flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-primary/10 flex items-center justify-center shrink-0">
                  <BookOpen className="w-4 h-4 text-primary" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="font-medium text-dark text-sm">
                    {enrollment.programName || 'General Program'}
                  </p>
                  <p className="text-xs text-gray-400 truncate">
                    {enrollment.institutionCode}
                    {enrollment.expectedGraduationYear && ` · Expected ${enrollment.expectedGraduationYear}`}
                  </p>
                  <p className="text-xs text-gray-400">Enrolled: {enrollment.enrollmentDate}</p>
                </div>
                <Badge
                  label={enrollment.status}
                  status={enrollment.status === 'ACTIVE' ? 'ACTIVE'
                        : enrollment.status === 'GRADUATED' ? 'ACTIVE'
                        : enrollment.status === 'DROPPED' ? 'FAIL'
                        : 'PENDING'}
                  size="sm"
                />
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
