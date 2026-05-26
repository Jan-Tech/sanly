import axios from 'axios'

const vehicleClient = axios.create({ baseURL: '/proxy/vehicle', timeout: 10_000 })
vehicleClient.interceptors.request.use((config) => {
  const token = import.meta.env.VITE_VEHICLE_TOKEN || null
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export interface AdminVehicle {
  vehicleId: string
  plateNumber: string
  vin: string
  make: string
  model: string
  year: number
  color: string | null
  fuelType: string
  vehicleType: string
  registeredAt: string
  status: string
}

export interface AdminTransfer {
  applicationId: string
  plateNumber: string
  fromNationalId: string
  toNationalId: string | null
  toBusinessNumber: string | null
  transferType: string
  agreedPrice: string | null
  applicationDate: string
  status: string
  processedByOfficerId: string | null
  processedAt: string | null
  rejectionReason: string | null
}

export interface AdminInsurance {
  insuranceId: string
  plateNumber: string
  insuranceCompany: string
  coverageType: string
  validFrom: string
  validUntil: string
  status: string
}

export interface AdminInspection {
  inspectionId: string
  plateNumber: string
  inspectionDate: string
  nextInspectionDue: string
  result: string
}

export async function getVehiclesByStatus(status: string): Promise<AdminVehicle[]> {
  const res = await vehicleClient.get<AdminVehicle[]>(`/api/v1/vehicle/vehicles?status=${status}`)
  return res.data ?? []
}

export async function getVehicleByPlate(plateNumber: string): Promise<AdminVehicle> {
  const res = await vehicleClient.get<AdminVehicle>(`/api/v1/vehicle/vehicles/${plateNumber}`)
  return res.data
}

export async function updateVehicleStatus(plateNumber: string, status: string): Promise<AdminVehicle> {
  const res = await vehicleClient.patch<AdminVehicle>(`/api/v1/vehicle/vehicles/${plateNumber}/status`, { status })
  return res.data
}

export async function getTransfersByStatus(status: string): Promise<AdminTransfer[]> {
  const res = await vehicleClient.get<AdminTransfer[]>(`/api/v1/vehicle/transfers?status=${status}`)
  return res.data ?? []
}

export async function approveTransfer(applicationId: string): Promise<AdminTransfer> {
  const res = await vehicleClient.post<AdminTransfer>(`/api/v1/vehicle/transfers/${applicationId}/approve`)
  return res.data
}

export async function rejectTransfer(applicationId: string, rejectionReason: string): Promise<AdminTransfer> {
  const res = await vehicleClient.post<AdminTransfer>(
    `/api/v1/vehicle/transfers/${applicationId}/reject`,
    { rejectionReason }
  )
  return res.data
}

export async function getExpiringInsurance(): Promise<AdminInsurance[]> {
  const res = await vehicleClient.get<AdminInsurance[]>('/api/v1/vehicle/insurance?expiringSoon=true')
  return res.data ?? []
}

export async function getInspectionsDue(): Promise<AdminInspection[]> {
  const res = await vehicleClient.get<AdminInspection[]>('/api/v1/vehicle/inspections/due')
  return res.data ?? []
}
