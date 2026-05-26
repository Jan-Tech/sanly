import { useState, useCallback } from 'react'
import AsyncStorage from '@react-native-async-storage/async-storage'
import { parseCode } from '../utils/codeParser'
import type { ScanHistoryItem } from '../types'

const HISTORY_KEY = 'officer_scan_history'
const MAX_HISTORY = 20

export function useScanner() {
  const [scanned, setScanned] = useState(false)
  const [lastCode, setLastCode] = useState<string | null>(null)

  const processBarcode = useCallback(async (rawCode: string) => {
    if (scanned) return null
    setScanned(true)
    setLastCode(rawCode)

    const parsed = parseCode(rawCode)

    // Append to history
    const historyRaw = await AsyncStorage.getItem(HISTORY_KEY)
    const history: ScanHistoryItem[] = historyRaw ? JSON.parse(historyRaw) : []
    const item: ScanHistoryItem = {
      id: Math.random().toString(36).slice(2),
      rawCode,
      type: parsed.type,
      timestamp: new Date().toISOString(),
    }
    const updated = [item, ...history].slice(0, MAX_HISTORY)
    await AsyncStorage.setItem(HISTORY_KEY, JSON.stringify(updated))

    return parsed
  }, [scanned])

  const resetScanner = useCallback(() => {
    setScanned(false)
    setLastCode(null)
  }, [])

  return { scanned, lastCode, processBarcode, resetScanner }
}

export async function getScanHistory(): Promise<ScanHistoryItem[]> {
  try {
    const raw = await AsyncStorage.getItem(HISTORY_KEY)
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

export async function clearScanHistory(): Promise<void> {
  await AsyncStorage.removeItem(HISTORY_KEY)
}
