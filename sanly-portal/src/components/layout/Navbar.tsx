import { useState, useRef, useEffect } from 'react'
import { Menu, LogOut, Bell } from 'lucide-react'
import { useNavigate, Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '../../store/authStore'
import { useLangStore } from '../../store/langStore'
import { t, type Lang } from '../../utils/i18n'
import { displayNin } from '../../utils/nin'
import { getUnreadCount, getRecentNotifications, markRead, type Notification } from '../../api/notifications'

interface NavbarProps {
  onMenuClick: () => void
  citizenName?: string
}

const LANGS: { value: Lang; label: string }[] = [
  { value: 'en', label: 'EN' },
  { value: 'tk', label: 'TK' },
  { value: 'ru', label: 'RU' },
]

function timeAgo(iso: string): string {
  const diff = Date.now() - new Date(iso).getTime()
  const m = Math.floor(diff / 60000)
  if (m < 1) return 'just now'
  if (m < 60) return `${m}m ago`
  const h = Math.floor(m / 60)
  if (h < 24) return `${h}h ago`
  return `${Math.floor(h / 24)}d ago`
}

export default function Navbar({ onMenuClick, citizenName }: NavbarProps) {
  const { nationalId, clearAuth } = useAuthStore()
  const { lang, setLang } = useLangStore()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [bellOpen, setBellOpen] = useState(false)
  const bellRef = useRef<HTMLDivElement>(null)

  const { data: countData } = useQuery({
    queryKey: ['notifications-count'],
    queryFn: getUnreadCount,
    enabled: !!nationalId,
    refetchInterval: 60_000,
  })

  const { data: recent } = useQuery({
    queryKey: ['notifications-recent'],
    queryFn: getRecentNotifications,
    enabled: !!nationalId && bellOpen,
    staleTime: 30_000,
  })

  const { mutate: doMarkRead } = useMutation({
    mutationFn: markRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications-count'] })
      queryClient.invalidateQueries({ queryKey: ['notifications-recent'] })
    },
  })

  // Close dropdown on outside click
  useEffect(() => {
    function handleClick(e: MouseEvent) {
      if (bellRef.current && !bellRef.current.contains(e.target as Node)) {
        setBellOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [])

  const unreadCount = countData?.count ?? 0

  const handleLogout = () => {
    clearAuth()
    navigate('/login')
  }

  return (
    <header className="h-14 bg-white border-b border-gray-100 flex items-center px-4 gap-4 shrink-0">
      <button
        onClick={onMenuClick}
        className="lg:hidden p-1.5 rounded-lg hover:bg-gray-100 text-gray-500"
      >
        <Menu className="w-5 h-5" />
      </button>

      <div className="flex-1" />

      {/* Lang switcher */}
      <div className="flex items-center gap-1 bg-gray-100 rounded-lg p-0.5">
        {LANGS.map(({ value, label }) => (
          <button
            key={value}
            onClick={() => setLang(value)}
            className={`px-2.5 py-1 rounded-md text-xs font-medium transition-colors ${
              lang === value ? 'bg-white text-dark shadow-sm' : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            {label}
          </button>
        ))}
      </div>

      {/* Notification bell */}
      {nationalId && (
        <div className="relative" ref={bellRef}>
          <button
            onClick={() => setBellOpen(v => !v)}
            className="relative p-2 rounded-lg hover:bg-gray-100 text-gray-500 transition-colors"
          >
            <Bell className="w-5 h-5" />
            {unreadCount > 0 && (
              <span className="absolute top-1 right-1 min-w-[16px] h-4 px-0.5 bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center leading-none">
                {unreadCount > 99 ? '99+' : unreadCount}
              </span>
            )}
          </button>

          {bellOpen && (
            <div className="absolute right-0 top-12 w-80 bg-white rounded-xl shadow-xl border border-gray-100 z-50 overflow-hidden">
              <div className="px-4 py-3 border-b border-gray-100 flex items-center justify-between">
                <span className="font-semibold text-sm text-dark">Notifications</span>
                {unreadCount > 0 && (
                  <span className="text-xs text-gray-400">{unreadCount} unread</span>
                )}
              </div>

              <div className="max-h-72 overflow-y-auto divide-y divide-gray-50">
                {!recent || recent.length === 0 ? (
                  <p className="text-sm text-gray-400 text-center py-6">No notifications</p>
                ) : (
                  recent.map((n: Notification) => (
                    <div
                      key={n.notificationId}
                      className={`px-4 py-3 cursor-pointer hover:bg-gray-50 transition-colors ${
                        n.status === 'UNREAD' ? 'bg-primary/5' : ''
                      }`}
                      onClick={() => {
                        if (n.status === 'UNREAD') doMarkRead(n.notificationId)
                      }}
                    >
                      <div className="flex items-start gap-2">
                        {n.status === 'UNREAD' && (
                          <div className="mt-1.5 w-2 h-2 rounded-full bg-primary shrink-0" />
                        )}
                        <div className={n.status === 'UNREAD' ? '' : 'ml-4'}>
                          <p className="text-sm font-medium text-dark leading-snug">{n.title}</p>
                          <p className="text-xs text-gray-500 mt-0.5 line-clamp-2">{n.body}</p>
                          <p className="text-[10px] text-gray-400 mt-1">{timeAgo(n.createdAt)}</p>
                        </div>
                      </div>
                    </div>
                  ))
                )}
              </div>

              <div className="px-4 py-2.5 border-t border-gray-100">
                <Link
                  to="/notifications"
                  onClick={() => setBellOpen(false)}
                  className="text-xs text-primary font-medium hover:underline"
                >
                  View all notifications →
                </Link>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Citizen info */}
      {nationalId && (
        <div className="flex items-center gap-3">
          <div className="text-right hidden sm:block">
            <p className="text-sm font-medium text-dark leading-none">{citizenName ?? '—'}</p>
            <p className="text-xs text-gray-400 mt-0.5">{displayNin(nationalId)}</p>
          </div>
          <div className="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center text-primary font-semibold text-sm">
            {citizenName ? citizenName[0].toUpperCase() : '?'}
          </div>
        </div>
      )}

      <button
        onClick={handleLogout}
        className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm text-gray-500 hover:bg-gray-100 hover:text-dark transition-colors"
        title={t(lang, 'signOut')}
      >
        <LogOut className="w-4 h-4" />
        <span className="hidden sm:block">{t(lang, 'signOut')}</span>
      </button>
    </header>
  )
}
