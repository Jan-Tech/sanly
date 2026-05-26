import { create } from 'zustand'
import AsyncStorage from '@react-native-async-storage/async-storage'

const QUEUE_KEY = 'officer_offline_queue'

export interface QueuedAction {
  id: string
  type: string
  endpoint: string
  method: 'POST' | 'PUT' | 'PATCH'
  payload: Record<string, unknown>
  description: string
  queuedAt: string
  retries: number
}

interface OfflineQueueState {
  queue: QueuedAction[]
  loaded: boolean
  load: () => Promise<void>
  enqueue: (action: Omit<QueuedAction, 'id' | 'queuedAt' | 'retries'>) => Promise<void>
  remove: (id: string) => Promise<void>
  clearAll: () => Promise<void>
}

function uuid(): string {
  return Math.random().toString(36).slice(2) + Date.now().toString(36)
}

export const useOfflineQueueStore = create<OfflineQueueState>((set, get) => ({
  queue: [],
  loaded: false,

  load: async () => {
    try {
      const raw = await AsyncStorage.getItem(QUEUE_KEY)
      const queue: QueuedAction[] = raw ? JSON.parse(raw) : []
      set({ queue, loaded: true })
    } catch {
      set({ loaded: true })
    }
  },

  enqueue: async (action) => {
    const item: QueuedAction = {
      ...action,
      id: uuid(),
      queuedAt: new Date().toISOString(),
      retries: 0,
    }
    const queue = [...get().queue, item]
    set({ queue })
    await AsyncStorage.setItem(QUEUE_KEY, JSON.stringify(queue))
  },

  remove: async (id) => {
    const queue = get().queue.filter(q => q.id !== id)
    set({ queue })
    await AsyncStorage.setItem(QUEUE_KEY, JSON.stringify(queue))
  },

  clearAll: async () => {
    set({ queue: [] })
    await AsyncStorage.removeItem(QUEUE_KEY)
  },
}))
