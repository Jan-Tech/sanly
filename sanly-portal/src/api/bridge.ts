import { bridgeClient } from './client'
import type { ExchangeLog, PageResponse } from '../types'

export async function getAccessLogs(nationalId: string): Promise<ExchangeLog[]> {
  const res = await bridgeClient.get<PageResponse<ExchangeLog>>(
    `/api/v1/exchange/logs`,
    { params: { subjectNationalId: nationalId, size: 200, sort: 'timestamp,desc' } }
  )
  return res.data?.content ?? []
}
