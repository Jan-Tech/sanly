import { vehicleClient } from './client'

export interface VehicleRecord {
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
