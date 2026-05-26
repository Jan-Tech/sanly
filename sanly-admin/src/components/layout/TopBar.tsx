import { Menu, LogOut, User } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'

interface TopBarProps {
  onMenuClick: () => void
  title?: string
}

export default function TopBar({ onMenuClick, title }: TopBarProps) {
  const { username, clearAuth } = useAuthStore()
  const navigate = useNavigate()

  const handleLogout = () => { clearAuth(); navigate('/login') }

  return (
    <header className="h-13 bg-white border-b border-gray-100 flex items-center px-4 gap-3 shrink-0">
      <button onClick={onMenuClick} className="lg:hidden p-1.5 rounded-lg hover:bg-gray-100 text-gray-500">
        <Menu className="w-5 h-5" />
      </button>
      {title && <h1 className="font-semibold text-dark text-base">{title}</h1>}
      <div className="flex-1" />
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-2 text-sm">
          <div className="w-7 h-7 rounded-full bg-primary/10 flex items-center justify-center">
            <User className="w-3.5 h-3.5 text-primary" />
          </div>
          <span className="text-dark font-medium hidden sm:block">{username}</span>
          <span className="text-xs text-gray-400 hidden sm:block">ROLE_ADMIN</span>
        </div>
        <button onClick={handleLogout}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm text-gray-500 hover:bg-gray-100 hover:text-dark transition-colors">
          <LogOut className="w-4 h-4" />
          <span className="hidden sm:block">Sign out</span>
        </button>
      </div>
    </header>
  )
}
