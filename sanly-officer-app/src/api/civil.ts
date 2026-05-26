import { civilClient } from './client'
import type { BirthRecord, MarriageRecord, DeathRecord } from '../types'

export async function registerBirth(data: {
  childNationalId: string
  childFirstName: string
  childLastName: string
  dateOfBirth: string
  placeOfBirth: string
  fatherNationalId?: string
  motherNationalId?: string
}): Promise<BirthRecord> {
  const res = await civilClient.post('/api/v1/births', data)
  return res.data
}

export async function registerMarriage(data: {
  spouse1NationalId: string
  spouse2NationalId: string
  marriageDate: string
  marriagePlace: string
}): Promise<MarriageRecord> {
  const res = await civilClient.post('/api/v1/marriages', data)
  return res.data
}

export async function registerDeath(data: {
  deceasedNationalId: string
  dateOfDeath: string
  placeOfDeath: string
  causeOfDeath?: string
}): Promise<DeathRecord> {
  const res = await civilClient.post('/api/v1/deaths', data)
  return res.data
}

export async function getBirthRecordsByCitizen(nationalId: string): Promise<BirthRecord[]> {
  const res = await civilClient.get('/api/v1/births', { params: { nationalId, size: 10 } })
  return res.data?.content ?? res.data ?? []
}

export async function getMarriageRecordsByCitizen(nationalId: string): Promise<MarriageRecord[]> {
  const res = await civilClient.get('/api/v1/marriages', { params: { nationalId, size: 10 } })
  return res.data?.content ?? res.data ?? []
}
