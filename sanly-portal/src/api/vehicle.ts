import axios from 'axios'

const vehicleClient = axios.create({ baseURL: '/proxy/vehicle', timeout: 10_000 })
vehicleClient.interceptors.request.use((config) => {
  const token = import.meta.env.VITE_VEHICLE_TOKEN || null
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export interface VehicleRecord {
  vehicleId: string
  plateNumber: string
  vin: string
  make: string
  model: string
  year: number
  color: string | null
  engineVolume: string | null
  fuelType: string
  vehicleType: string
  registeredAt: string
  status: string
}

export interface VehicleOwnership {
  ownershipId: string
  plateNumber: string
  ownerNationalId: string | null
  ownerBusinessNumber: string | null
  ownershipStartDate: string
  ownershipEndDate: string | null
  acquiredVia: string
  status: string
}

export interface VehicleInsurance {
  insuranceId: string
  plateNumber: string
  insuranceCompany: string
  coverageType: string
  validFrom: string
  validUntil: string
  status: string
}

export interface VehicleInspection {
  inspectionId: string
  plateNumber: string
  inspectionDate: string
  nextInspectionDue: string
  result: string
}

export interface VehicleVerify {
  plateNumber: string
  vin: string
  make: string
  model: string
  year: number
  color: string | null
  vehicleType: string
  fuelType: string
  vehicleStatus: string
  ownerNationalId: string | null
  ownerBusinessNumber: string | null
  insuranceStatus: string
  insuranceValidUntil: string | null
  lastInspectionDate: string | null
  nextInspectionDue: string | null
  lastInspectionResult: string | null
}

export async function getVehiclesByOwner(nationalId: string): Promise<VehicleRecord[]> {
  const res = await vehicleClient.get<VehicleRecord[]>(`/api/v1/vehicle/vehicles/owner/${nationalId}`)
  return res.data ?? []
}

export async function getInsuranceByPlate(plateNumber: string): Promise<VehicleInsurance[]> {
  const res = await vehicleClient.get<VehicleInsurance[]>(`/api/v1/vehicle/insurance/vehicle/${plateNumber}`)
  return res.data ?? []
}

export async function getInspectionsByPlate(plateNumber: string): Promise<VehicleInspection[]> {
  const res = await vehicleClient.get<VehicleInspection[]>(`/api/v1/vehicle/inspections/vehicle/${plateNumber}`)
  return res.data ?? []
}

export async function getOwnershipHistory(plateNumber: string): Promise<VehicleOwnership[]> {
  const res = await vehicleClient.get<VehicleOwnership[]>(`/api/v1/vehicle/ownership/vehicle/${plateNumber}`)
  return res.data ?? []
}

export async function verifyVehicle(plateNumber: string): Promise<VehicleVerify> {
  const res = await vehicleClient.get<VehicleVerify>(`/api/v1/vehicle/vehicles/verify/${plateNumber}`)
  return res.data
}
