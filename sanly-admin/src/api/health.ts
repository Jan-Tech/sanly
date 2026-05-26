import axios from 'axios'
import type { ServiceHealth } from '../types'

const SERVICES = [
  { name: 'Citizen Registry', proxy: '/proxy/registry', port: 8080 },
  { name: 'SANLY Bridge',     proxy: '/proxy/bridge',   port: 8081 },
  { name: 'Medical',          proxy: '/proxy/medical',  port: 8082 },
  { name: 'DMV',              proxy: '/proxy/dmv',      port: 8083 },
  { name: 'Police',           proxy: '/proxy/police',   port: 8084 },
  { name: 'Tax',              proxy: '/proxy/tax',      port: 8085 },
  { name: 'Business',         proxy: '/proxy/business', port: 8086 },
  { name: 'Civil Registry',   proxy: '/proxy/civil',    port: 8087 },
]

async function pingService(proxy: string): Promise<{ ok: boolean; ms: number }> {
  const start = Date.now()
  try {
    await axios.get(`${proxy}/v3/api-docs`, { timeout: 5_000 })
    return { ok: true, ms: Date.now() - start }
  } catch {
    return { ok: false, ms: -1 }
  }
}

export async function checkAllServices(): Promise<ServiceHealth[]> {
  const results = await Promise.allSettled(SERVICES.map(async (s) => {
    const { ok, ms } = await pingService(s.proxy)
    return {
      name: s.name,
      url: `http://localhost:${s.port}`,
      port: s.port,
      healthy: ok,
      responseTimeMs: ms,
      checkedAt: new Date().toISOString(),
    } satisfies ServiceHealth
  }))
  return results.map((r, i) =>
    r.status === 'fulfilled' ? r.value : {
      name: SERVICES[i].name, url: `http://localhost:${SERVICES[i].port}`,
      port: SERVICES[i].port, healthy: false, responseTimeMs: -1, checkedAt: new Date().toISOString()
    }
  )
}
