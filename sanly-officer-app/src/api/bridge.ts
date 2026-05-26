import { bridgeClient } from './client'

export type DataType =
  | 'VISION_TEST' | 'MEDICAL_CLEARANCE' | 'CRIMINAL_RECORD' | 'TAX_STATUS'
  | 'DRIVING_LICENSE' | 'BUSINESS_REGISTRATION' | 'EDUCATION_DIPLOMA'
  | 'PROPERTY_RECORD' | 'BIRTH_RECORD' | 'MARRIAGE_RECORD' | 'DEATH_RECORD'

export async function queryBridgeData(
  nationalId: string,
  dataType: DataType,
  purposeCode = 'IDENTITY_VERIFICATION',
  caseReference?: string
) {
  const res = await bridgeClient.get(`/api/v1/exchange/data/${nationalId}/${dataType}`, {
    params: { purposeCode, caseReference },
  })
  return res.data
}
