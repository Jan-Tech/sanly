import { useEffect } from 'react'
import NetInfo from '@react-native-community/netinfo'
import axios from 'axios'
import { useOfflineQueueStore } from '../store/offlineQueueStore'
import { useAuthStore } from '../store/authStore'

const GATEWAY = process.env.EXPO_PUBLIC_GATEWAY_URL ?? 'http://localhost:8080'

export function useOfflineQueue() {
  const { queue, load, remove } = useOfflineQueueStore()
  const token = useAuthStore(s => s.token)

  useEffect(() => {
    load()
  }, [])

  // Attempt to flush queue when connectivity returns
  useEffect(() => {
    const unsub = NetInfo.addEventListener(state => {
      if (state.isConnected && queue.length > 0) {
        flushQueue()
      }
    })
    return unsub
  }, [queue, token])

  async function flushQueue() {
    for (const item of queue) {
      try {
        await axios({
          method: item.method,
          url: `${GATEWAY}/${item.endpoint}`,
          data: item.payload,
          headers: token ? { Authorization: `Bearer ${token}` } : undefined,
        })
        await remove(item.id)
      } catch {
        // Leave in queue; will retry on next connectivity event
      }
    }
  }

  return { queue, flushQueue }
}
