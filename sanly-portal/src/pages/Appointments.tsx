import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  CalendarDays, Clock, MapPin, ChevronRight, Star, X, CheckCircle2,
  Building2, Stethoscope, Car, Scale, Home, HeartHandshake, Package, Receipt,
  GraduationCap, PiggyBank, Shield, Briefcase, Pill,
} from 'lucide-react'
import { useLangStore } from '../store/langStore'
import { t } from '../utils/i18n'
import {
  getOffices, getServiceTypes, getAvailableSlots, getNextAvailableDates,
  bookAppointment, getMyAppointments, cancelAppointment, rateAppointment,
  type Office, type ServiceType, type SlotResponse, type Appointment,
} from '../api/appointments'
import Badge from '../components/ui/Badge'

// ─── Institution icons + labels ───────────────────────────────────────────────

const INSTITUTION_INFO: Record<string, { icon: React.ReactNode; label: string; color: string }> = {
  DMV:              { icon: <Car className="w-6 h-6" />,          label: 'DMV',            color: 'text-blue-600 bg-blue-50' },
  CIVIL_REGISTRY:   { icon: <Building2 className="w-6 h-6" />,    label: 'Civil Registry', color: 'text-green-600 bg-green-50' },
  TAX:              { icon: <Receipt className="w-6 h-6" />,       label: 'Tax Authority',  color: 'text-orange-600 bg-orange-50' },
  MEDICAL:          { icon: <Stethoscope className="w-6 h-6" />,   label: 'Medical',        color: 'text-red-600 bg-red-50' },
  COURT:            { icon: <Scale className="w-6 h-6" />,         label: 'Court',          color: 'text-purple-600 bg-purple-50' },
  LAND_REGISTRY:    { icon: <Home className="w-6 h-6" />,          label: 'Land Registry',  color: 'text-yellow-600 bg-yellow-50' },
  SOCIAL_SERVICES:  { icon: <HeartHandshake className="w-6 h-6" />,label: 'Social Services',color: 'text-pink-600 bg-pink-50' },
  CUSTOMS:          { icon: <Package className="w-6 h-6" />,       label: 'Customs',        color: 'text-teal-600 bg-teal-50' },
  EDUCATION:        { icon: <GraduationCap className="w-6 h-6" />, label: 'Education',      color: 'text-indigo-600 bg-indigo-50' },
  PENSION:          { icon: <PiggyBank className="w-6 h-6" />,     label: 'Pension',        color: 'text-cyan-600 bg-cyan-50' },
  POLICE:           { icon: <Shield className="w-6 h-6" />,        label: 'Police',         color: 'text-gray-600 bg-gray-100' },
  BUSINESS_REGISTRY:{ icon: <Briefcase className="w-6 h-6" />,     label: 'Business',       color: 'text-emerald-600 bg-emerald-50' },
  PHARMACY:         { icon: <Pill className="w-6 h-6" />,          label: 'Pharmacy',       color: 'text-rose-600 bg-rose-50' },
  OTHER:            { icon: <Building2 className="w-6 h-6" />,     label: 'Other',          color: 'text-gray-500 bg-gray-50' },
}

const INSTITUTION_TYPES = Object.keys(INSTITUTION_INFO)

function statusColor(status: string) {
  if (status === 'BOOKED' || status === 'CONFIRMED') return 'ACTIVE'
  if (status === 'COMPLETED') return 'COMPLIANT'
  if (status === 'CANCELLED') return 'SUSPENDED'
  return 'PENDING'
}

function formatDate(d: string) { return new Date(d).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }) }
function formatTime(t: string) { return t.slice(0, 5) }

// ─── Add 7 days of dates for the date picker ─────────────────────────────────

function getDateRange(availableDates: string[]): { date: string; label: string; available: boolean }[] {
  const today = new Date(); today.setHours(0, 0, 0, 0)
  const availableSet = new Set(availableDates)
  const result = []
  for (let i = 1; i <= 30; i++) {
    const d = new Date(today); d.setDate(today.getDate() + i)
    const iso = d.toISOString().split('T')[0]
    result.push({
      date: iso,
      label: d.toLocaleDateString('en-GB', { weekday: 'short', day: '2-digit', month: 'short' }),
      available: availableSet.has(iso) || availableDates.length === 0,
    })
  }
  return result
}

// ─── Component ────────────────────────────────────────────────────────────────

export default function Appointments() {
  const { lang } = useLangStore()
  const qc = useQueryClient()

  // Booking flow state
  const [step, setStep] = useState<0 | 1 | 2 | 3 | 4 | 5 | 6>(0) // 0=list, 1=type, 2=office, 3=service, 4=date, 5=time, 6=confirm
  const [selectedType, setSelectedType] = useState('')
  const [selectedOffice, setSelectedOffice] = useState<Office | null>(null)
  const [selectedService, setSelectedService] = useState<ServiceType | null>(null)
  const [selectedDate, setSelectedDate] = useState('')
  const [selectedSlot, setSelectedSlot] = useState<SlotResponse | null>(null)
  const [notes, setNotes] = useState('')
  const [bookingResult, setBookingResult] = useState<Appointment | null>(null)
  const [bookError, setBookError] = useState('')

  // Cancel/rate modals
  const [cancelTarget, setCancelTarget] = useState<Appointment | null>(null)
  const [cancelReason, setCancelReason] = useState('')
  const [rateTarget, setRateTarget] = useState<Appointment | null>(null)
  const [rating, setRating] = useState(0)
  const [feedback, setFeedback] = useState('')

  // Queries
  const { data: aptPage, isLoading: aptsLoading } = useQuery({
    queryKey: ['my-appointments'],
    queryFn: () => getMyAppointments(0, 20),
    enabled: step === 0,
  })

  const { data: offices = [] } = useQuery({
    queryKey: ['offices', selectedType],
    queryFn: () => getOffices({ institutionType: selectedType || undefined }),
    enabled: step === 2,
  })

  const { data: services = [] } = useQuery({
    queryKey: ['services', selectedOffice?.officeCode],
    queryFn: () => getServiceTypes(selectedOffice!.officeCode),
    enabled: step === 3 && !!selectedOffice,
  })

  const { data: availableDates = [] } = useQuery({
    queryKey: ['next-dates', selectedOffice?.officeCode, selectedService?.serviceTypeId],
    queryFn: () => getNextAvailableDates(selectedOffice!.officeCode, selectedService!.serviceTypeId),
    enabled: step === 4 && !!selectedOffice && !!selectedService,
  })

  const { data: slots = [] } = useQuery({
    queryKey: ['slots', selectedOffice?.officeCode, selectedDate, selectedService?.serviceTypeId],
    queryFn: () => getAvailableSlots(selectedOffice!.officeCode, selectedDate, selectedService!.serviceTypeId),
    enabled: step === 5 && !!selectedOffice && !!selectedDate && !!selectedService,
  })

  const bookMutation = useMutation({
    mutationFn: () => bookAppointment({
      officeCode: selectedOffice!.officeCode,
      serviceTypeId: selectedService!.serviceTypeId,
      appointmentDate: selectedDate,
      slotTime: selectedSlot!.slotTime,
      notes: notes || undefined,
    }),
    onSuccess: (data) => { setBookingResult(data); setStep(6); qc.invalidateQueries({ queryKey: ['my-appointments'] }) },
    onError: (err: unknown) => {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      setBookError(msg ?? 'Booking failed. Please try again.')
    },
  })

  const cancelMutation = useMutation({
    mutationFn: () => cancelAppointment(cancelTarget!.appointmentCode, cancelReason),
    onSuccess: () => { setCancelTarget(null); setCancelReason(''); qc.invalidateQueries({ queryKey: ['my-appointments'] }) },
  })

  const rateMutation = useMutation({
    mutationFn: () => rateAppointment(rateTarget!.appointmentCode, rating, feedback || undefined),
    onSuccess: () => { setRateTarget(null); setRating(0); setFeedback(''); qc.invalidateQueries({ queryKey: ['my-appointments'] }) },
  })

  const apts = aptPage?.content ?? []
  const upcoming = apts.filter(a => ['BOOKED','CONFIRMED'].includes(a.status))
  const history  = apts.filter(a => !['BOOKED','CONFIRMED'].includes(a.status))

  function resetFlow() {
    setStep(0); setSelectedType(''); setSelectedOffice(null); setSelectedService(null)
    setSelectedDate(''); setSelectedSlot(null); setNotes(''); setBookingResult(null); setBookError('')
  }

  // ─── Steps ───────────────────────────────────────────────────────────────────

  if (step === 6 && bookingResult) {
    return (
      <div className="space-y-6 max-w-lg">
        <div className="card text-center py-8">
          <CheckCircle2 className="w-16 h-16 text-green-500 mx-auto mb-4" />
          <h2 className="text-xl font-bold text-dark mb-1">Appointment Booked!</h2>
          <p className="text-gray-500 text-sm mb-6">Your appointment has been confirmed.</p>
          <div className="bg-gray-50 rounded-xl p-4 text-left space-y-2 mb-6">
            {[
              ['Code', bookingResult.appointmentCode],
              ['Office', bookingResult.officeName],
              ['Service', bookingResult.serviceName],
              ['Date', formatDate(bookingResult.appointmentDate)],
              ['Time', formatTime(bookingResult.slotTime)],
            ].map(([k, v]) => (
              <div key={k} className="flex justify-between text-sm">
                <span className="text-gray-500">{k}</span>
                <span className="font-medium text-dark">{v}</span>
              </div>
            ))}
          </div>
          {selectedService?.requiresDocuments && (
            <div className="bg-amber-50 border border-amber-100 rounded-xl p-3 text-left mb-4">
              <p className="text-xs font-medium text-amber-700 mb-1">Please bring:</p>
              <p className="text-xs text-amber-600">{selectedService.requiresDocuments}</p>
            </div>
          )}
          <button className="btn-primary w-full" onClick={resetFlow}>Back to My Appointments</button>
        </div>
      </div>
    )
  }

  if (step === 1) {
    return (
      <div className="space-y-4 max-w-2xl">
        <div className="flex items-center gap-3">
          <button className="btn-ghost" onClick={resetFlow}><X className="w-4 h-4" /></button>
          <h2 className="font-semibold text-dark">Select Institution Type</h2>
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
          {INSTITUTION_TYPES.map((type) => {
            const info = INSTITUTION_INFO[type]
            return (
              <button key={type} onClick={() => { setSelectedType(type); setStep(2) }}
                className="card hover:shadow-card-hover transition-shadow text-left group">
                <div className={`w-10 h-10 rounded-xl flex items-center justify-center mb-3 ${info.color}`}>
                  {info.icon}
                </div>
                <p className="text-sm font-medium text-dark group-hover:text-primary transition-colors">{info.label}</p>
              </button>
            )
          })}
        </div>
      </div>
    )
  }

  if (step === 2) {
    return (
      <div className="space-y-4 max-w-lg">
        <div className="flex items-center gap-3">
          <button className="btn-ghost" onClick={() => setStep(1)}><ChevronRight className="w-4 h-4 rotate-180" /></button>
          <h2 className="font-semibold text-dark">Select Office — {INSTITUTION_INFO[selectedType]?.label}</h2>
        </div>
        {offices.length === 0 ? (
          <p className="text-sm text-gray-400 text-center py-8">No offices available for this institution type.</p>
        ) : (
          <div className="space-y-2">
            {offices.map((o) => (
              <button key={o.officeCode} onClick={() => { setSelectedOffice(o); setStep(3) }}
                className="card w-full text-left hover:shadow-card-hover transition-shadow group">
                <div className="flex items-start justify-between">
                  <div>
                    <p className="font-medium text-dark group-hover:text-primary">{o.name}</p>
                    <p className="text-xs text-gray-400 flex items-center gap-1 mt-0.5">
                      <MapPin className="w-3 h-3" />{o.region} · {o.address}
                    </p>
                    {o.phone && <p className="text-xs text-gray-400">{o.phone}</p>}
                  </div>
                  <ChevronRight className="w-4 h-4 text-gray-300 group-hover:text-primary" />
                </div>
              </button>
            ))}
          </div>
        )}
      </div>
    )
  }

  if (step === 3) {
    return (
      <div className="space-y-4 max-w-lg">
        <div className="flex items-center gap-3">
          <button className="btn-ghost" onClick={() => setStep(2)}><ChevronRight className="w-4 h-4 rotate-180" /></button>
          <h2 className="font-semibold text-dark">Select Service — {selectedOffice?.name}</h2>
        </div>
        {services.length === 0 ? (
          <p className="text-sm text-gray-400 text-center py-8">No services available at this office.</p>
        ) : (
          <div className="space-y-2">
            {services.map((s) => (
              <button key={s.serviceTypeId} onClick={() => { setSelectedService(s); setStep(4) }}
                className="card w-full text-left hover:shadow-card-hover transition-shadow group">
                <div className="flex items-start justify-between">
                  <div>
                    <p className="font-medium text-dark group-hover:text-primary">{s.serviceName}</p>
                    {s.description && <p className="text-xs text-gray-500 mt-0.5">{s.description}</p>}
                    <div className="flex items-center gap-3 mt-1">
                      <span className="text-xs text-gray-400 flex items-center gap-1">
                        <Clock className="w-3 h-3" />{s.durationMinutes} min
                      </span>
                    </div>
                    {s.requiresDocuments && (
                      <p className="text-xs text-amber-600 mt-1">Bring: {s.requiresDocuments}</p>
                    )}
                  </div>
                  <ChevronRight className="w-4 h-4 text-gray-300 group-hover:text-primary shrink-0" />
                </div>
              </button>
            ))}
          </div>
        )}
      </div>
    )
  }

  if (step === 4) {
    const dateRange = getDateRange(availableDates)
    return (
      <div className="space-y-4 max-w-lg">
        <div className="flex items-center gap-3">
          <button className="btn-ghost" onClick={() => setStep(3)}><ChevronRight className="w-4 h-4 rotate-180" /></button>
          <h2 className="font-semibold text-dark">Select Date</h2>
        </div>
        <p className="text-sm text-gray-500">Green dates have available slots.</p>
        <div className="grid grid-cols-3 sm:grid-cols-4 gap-2">
          {dateRange.slice(0, 28).map(({ date, label, available }) => (
            <button
              key={date}
              disabled={!available}
              onClick={() => { setSelectedDate(date); setStep(5) }}
              className={`p-2.5 rounded-xl border-2 text-xs font-medium transition-all ${
                selectedDate === date
                  ? 'border-primary bg-primary text-white'
                  : available
                    ? 'border-green-200 bg-green-50 text-green-700 hover:border-green-400'
                    : 'border-gray-100 bg-gray-50 text-gray-300 cursor-not-allowed'
              }`}
            >
              {label}
            </button>
          ))}
        </div>
      </div>
    )
  }

  if (step === 5) {
    return (
      <div className="space-y-4 max-w-lg">
        <div className="flex items-center gap-3">
          <button className="btn-ghost" onClick={() => setStep(4)}><ChevronRight className="w-4 h-4 rotate-180" /></button>
          <h2 className="font-semibold text-dark">Select Time — {formatDate(selectedDate)}</h2>
        </div>
        {slots.length === 0 ? (
          <p className="text-sm text-gray-400 text-center py-8">No slots available for this date. Please choose another date.</p>
        ) : (
          <div className="grid grid-cols-3 sm:grid-cols-4 gap-2">
            {slots.map((slot) => (
              <button
                key={slot.slotTime}
                disabled={!slot.available}
                onClick={() => { setSelectedSlot(slot); setStep(6 as 0) }}
                className={`p-2.5 rounded-xl border-2 text-sm font-medium transition-all ${
                  selectedSlot?.slotTime === slot.slotTime
                    ? 'border-primary bg-primary text-white'
                    : slot.available
                      ? 'border-green-200 bg-green-50 text-green-700 hover:border-green-400'
                      : 'border-gray-100 bg-gray-50 text-gray-300 cursor-not-allowed'
                }`}
              >
                {formatTime(slot.slotTime)}
                {slot.available && slot.spotsRemaining <= 2 && (
                  <span className="block text-[10px] text-amber-600">{slot.spotsRemaining} left</span>
                )}
              </button>
            ))}
          </div>
        )}
        {/* Confirm section appears after time selected */}
        {selectedSlot && (
          <div className="card mt-4 space-y-4">
            <h3 className="font-semibold text-dark">Confirm Appointment</h3>
            <div className="space-y-1 text-sm">
              {[
                ['Office', selectedOffice?.name],
                ['Service', selectedService?.serviceName],
                ['Date', formatDate(selectedDate)],
                ['Time', formatTime(selectedSlot.slotTime)],
                ['Duration', `${selectedService?.durationMinutes} min`],
              ].map(([k, v]) => (
                <div key={k} className="flex justify-between">
                  <span className="text-gray-500">{k}</span>
                  <span className="font-medium text-dark">{v}</span>
                </div>
              ))}
            </div>
            {selectedService?.requiresDocuments && (
              <div className="bg-amber-50 border border-amber-100 rounded-xl p-3">
                <p className="text-xs font-medium text-amber-700 mb-1">Please bring:</p>
                <p className="text-xs text-amber-600">{selectedService.requiresDocuments}</p>
              </div>
            )}
            <div>
              <label className="label">Notes (optional)</label>
              <textarea className="input min-h-[60px]" value={notes} onChange={(e) => setNotes(e.target.value)} maxLength={500} />
            </div>
            {bookError && <p className="text-sm text-red-500">{bookError}</p>}
            <button className="btn-primary w-full" disabled={bookMutation.isPending} onClick={() => bookMutation.mutate()}>
              {bookMutation.isPending ? 'Booking...' : 'Confirm Booking'}
            </button>
          </div>
        )}
      </div>
    )
  }

  // ─── Main view (step 0) ────────────────────────────────────────────────────

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="page-title flex items-center gap-2">
            <CalendarDays className="w-6 h-6 text-primary" />Appointments
          </h1>
          <p className="text-sm text-gray-500 mt-1">Book government appointments online. Skip the queue.</p>
        </div>
        <button className="btn-primary flex items-center gap-2" onClick={() => setStep(1)}>
          <CalendarDays className="w-4 h-4" />Book Appointment
        </button>
      </div>

      {/* Upcoming */}
      <div className="card">
        <h2 className="section-title">Upcoming Appointments</h2>
        {aptsLoading ? (
          <div className="space-y-3">{[1,2].map(i => <div key={i} className="skeleton h-20 rounded-xl" />)}</div>
        ) : upcoming.length === 0 ? (
          <p className="text-sm text-gray-400 py-4 text-center">No upcoming appointments. Book one above!</p>
        ) : (
          <div className="space-y-3">
            {upcoming.map((a) => {
              const info = INSTITUTION_INFO[a.officeCode] // fallback
              const canCancel = new Date(`${a.appointmentDate}T${a.slotTime}`).getTime() - Date.now() > 2 * 3600 * 1000
              return (
                <div key={a.appointmentId} className="p-4 rounded-xl border-2 border-primary/20 bg-primary/5 space-y-3">
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <p className="font-semibold text-dark">{a.officeName}</p>
                        <Badge label={a.status} status={statusColor(a.status)} size="sm" />
                      </div>
                      <p className="text-sm text-gray-600 mt-0.5">{a.serviceName}</p>
                      <div className="flex items-center gap-4 mt-1 text-xs text-gray-400">
                        <span className="flex items-center gap-1"><CalendarDays className="w-3 h-3" />{formatDate(a.appointmentDate)}</span>
                        <span className="flex items-center gap-1"><Clock className="w-3 h-3" />{formatTime(a.slotTime)}</span>
                      </div>
                      <p className="text-xs font-mono text-gray-400 mt-1">{a.appointmentCode}</p>
                    </div>
                    {canCancel && (
                      <button className="text-xs text-red-500 hover:underline shrink-0"
                        onClick={() => { setCancelTarget(a); setCancelReason('') }}>
                        Cancel
                      </button>
                    )}
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </div>

      {/* History */}
      {history.length > 0 && (
        <div className="card">
          <h2 className="section-title">Past Appointments</h2>
          <div className="space-y-2">
            {history.map((a) => (
              <div key={a.appointmentId} className="p-3 rounded-xl border border-gray-100 bg-gray-50">
                <div className="flex items-start justify-between gap-2">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <p className="text-sm font-medium text-dark">{a.officeName}</p>
                      <Badge label={a.status} status={statusColor(a.status)} size="sm" />
                    </div>
                    <p className="text-xs text-gray-500">{a.serviceName} · {formatDate(a.appointmentDate)}</p>
                    {a.citizenRating && (
                      <div className="flex items-center gap-0.5 mt-1">
                        {[1,2,3,4,5].map(n => (
                          <Star key={n} className={`w-3 h-3 ${n <= a.citizenRating! ? 'text-yellow-400 fill-yellow-400' : 'text-gray-200 fill-gray-200'}`} />
                        ))}
                      </div>
                    )}
                  </div>
                  {a.status === 'COMPLETED' && !a.citizenRating && (
                    <button className="text-xs text-primary hover:underline shrink-0"
                      onClick={() => { setRateTarget(a); setRating(0); setFeedback('') }}>
                      <Star className="w-3 h-3 inline mr-0.5" />Rate
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Cancel modal */}
      {cancelTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
          <div className="bg-white rounded-2xl p-6 shadow-xl w-full max-w-md">
            <h3 className="font-semibold text-dark mb-2">Cancel Appointment</h3>
            <p className="text-sm text-gray-500 mb-3">{cancelTarget.officeName} · {formatDate(cancelTarget.appointmentDate)} at {formatTime(cancelTarget.slotTime)}</p>
            <label className="label">Reason</label>
            <textarea className="input min-h-[80px] mb-4" value={cancelReason} onChange={(e) => setCancelReason(e.target.value)} placeholder="Why are you cancelling?" />
            <div className="flex gap-2 justify-end">
              <button className="btn-secondary" onClick={() => setCancelTarget(null)}>Keep</button>
              <button className="bg-red-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-red-700 disabled:opacity-50"
                disabled={!cancelReason.trim() || cancelMutation.isPending}
                onClick={() => cancelMutation.mutate()}>
                {cancelMutation.isPending ? 'Cancelling...' : 'Cancel Appointment'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Rate modal */}
      {rateTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
          <div className="bg-white rounded-2xl p-6 shadow-xl w-full max-w-md">
            <h3 className="font-semibold text-dark mb-2">Rate Your Experience</h3>
            <p className="text-sm text-gray-500 mb-4">{rateTarget.officeName} · {rateTarget.serviceName}</p>
            <div className="flex gap-1 mb-4 justify-center">
              {[1,2,3,4,5].map(n => (
                <button key={n} onClick={() => setRating(n)}>
                  <Star className={`w-8 h-8 transition-colors ${n <= rating ? 'text-yellow-400 fill-yellow-400' : 'text-gray-200 fill-gray-200'}`} />
                </button>
              ))}
            </div>
            <textarea className="input min-h-[80px] mb-4" value={feedback} onChange={(e) => setFeedback(e.target.value)} placeholder="Optional feedback..." maxLength={500} />
            <div className="flex gap-2 justify-end">
              <button className="btn-secondary" onClick={() => setRateTarget(null)}>Skip</button>
              <button className="btn-primary" disabled={rating === 0 || rateMutation.isPending} onClick={() => rateMutation.mutate()}>
                {rateMutation.isPending ? 'Submitting...' : 'Submit Rating'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
