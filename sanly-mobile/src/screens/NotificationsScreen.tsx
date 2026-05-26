import React from 'react'
import { View, Text, FlatList, TouchableOpacity, StyleSheet } from 'react-native'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Bell, CheckCheck } from 'lucide-react-native'
import { useLangStore } from '../store/langStore'
import { getMyNotifications, markRead, markAllRead } from '../api/notifications'
import { t } from '../i18n'
import { LoadingView } from '../components/LoadingView'
import { EmptyState } from '../components/EmptyState'
import { Colors, Radius, Spacing } from '../constants/theme'
import { formatDateTime } from '../utils/formatters'
import type { Notification } from '../types'

function NotificationItem({ item, onMarkRead }: { item: Notification; onMarkRead: (id: string) => void }) {
  const isUnread = item.status === 'UNREAD'
  return (
    <TouchableOpacity
      style={[styles.item, isUnread && styles.unread]}
      onPress={() => isUnread && onMarkRead(item.notificationId)}
      activeOpacity={0.7}
    >
      <View style={[styles.dot, isUnread && styles.dotActive]} />
      <View style={styles.body}>
        <Text style={[styles.title, isUnread && styles.titleBold]}>{item.title}</Text>
        <Text style={styles.bodyText} numberOfLines={2}>{item.body}</Text>
        <Text style={styles.time}>{formatDateTime(item.createdAt)}</Text>
      </View>
    </TouchableOpacity>
  )
}

export function NotificationsScreen() {
  const { lang } = useLangStore()
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['notifications'],
    queryFn: () => getMyNotifications({ size: 50 }),
    retry: false,
  })

  const markReadMutation = useMutation({
    mutationFn: markRead,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['notifications'] }),
  })

  const markAllMutation = useMutation({
    mutationFn: markAllRead,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['notifications'] }),
  })

  if (isLoading) return <LoadingView message={t(lang, 'loading')} />

  const notifications = data?.content ?? []
  const unreadCount = notifications.filter(n => n.status === 'UNREAD').length

  return (
    <View style={styles.flex}>
      {unreadCount > 0 && (
        <View style={styles.header}>
          <Text style={styles.unreadCount}>{unreadCount} unread</Text>
          <TouchableOpacity onPress={() => markAllMutation.mutate()} style={styles.markAllBtn}>
            <CheckCheck size={14} color={Colors.primary} />
            <Text style={styles.markAllText}>Mark all read</Text>
          </TouchableOpacity>
        </View>
      )}
      <FlatList
        data={notifications}
        keyExtractor={n => n.notificationId}
        renderItem={({ item }) => (
          <NotificationItem item={item} onMarkRead={id => markReadMutation.mutate(id)} />
        )}
        ListEmptyComponent={<EmptyState message={t(lang, 'noData')} icon={<Bell size={24} color={Colors.textMuted} />} />}
        contentContainerStyle={notifications.length === 0 ? styles.empty : undefined}
      />
    </View>
  )
}

const styles = StyleSheet.create({
  flex: { flex: 1, backgroundColor: Colors.background },
  header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingHorizontal: Spacing.lg, paddingVertical: Spacing.sm, backgroundColor: Colors.card, borderBottomWidth: StyleSheet.hairlineWidth, borderBottomColor: Colors.border },
  unreadCount: { fontSize: 13, fontWeight: '600', color: Colors.textSecondary },
  markAllBtn: { flexDirection: 'row', alignItems: 'center', gap: 4 },
  markAllText: { fontSize: 13, color: Colors.primary, fontWeight: '600' },
  item: { flexDirection: 'row', padding: Spacing.lg, borderBottomWidth: StyleSheet.hairlineWidth, borderBottomColor: Colors.border, backgroundColor: Colors.card },
  unread: { backgroundColor: '#F0FAFA' },
  dot: { width: 8, height: 8, borderRadius: 4, backgroundColor: Colors.border, marginTop: 5, marginRight: Spacing.md },
  dotActive: { backgroundColor: Colors.primary },
  body: { flex: 1 },
  title: { fontSize: 14, color: Colors.dark, marginBottom: 3 },
  titleBold: { fontWeight: '700' },
  bodyText: { fontSize: 13, color: Colors.textSecondary, lineHeight: 18, marginBottom: 4 },
  time: { fontSize: 11, color: Colors.textMuted },
  empty: { flex: 1, justifyContent: 'center' },
})
