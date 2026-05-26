import { NavLink } from 'react-router-dom'
import {
  LayoutDashboard, User, Stethoscope, Car, Receipt,
  Briefcase, Shield, Settings, X, ChevronRight, Bell, GraduationCap, Home, HeartHandshake, Package, Scale, Truck, PiggyBank, FileSignature, CalendarDays, FileText, Activity, Building2,
} from 'lucide-react'
import { t } from '../../utils/i18n'
import { useLangStore } from '../../store/langStore'

const navItems = [
  { to: '/dashboard',     icon: LayoutDashboard, key: 'dashboard'     as const },
  { to: '/identity',      icon: User,            key: 'identity'      as const },
  { to: '/medical',       icon: Stethoscope,     key: 'medical'       as const },
  { to: '/license',       icon: Car,             key: 'license'       as const },
  { to: '/tax',           icon: Receipt,         key: 'tax'           as const },
  { to: '/business',      icon: Briefcase,       key: 'business'      as const },
  { to: '/education',     icon: GraduationCap,   key: 'education'     as const },
  { to: '/property',      icon: Home,            key: 'property'      as const },
  { to: '/benefits',      icon: HeartHandshake,  key: 'benefits'      as const },
  { to: '/customs',       icon: Package,         key: 'customs'       as const },
  { to: '/court',         icon: Scale,           key: 'court'         as const },
  { to: '/vehicles',      icon: Truck,           key: 'vehicles'      as const },
  { to: '/pension',       icon: PiggyBank,       key: 'pension'       as const },
  { to: '/appointments',  icon: CalendarDays,    key: 'appointments'  as const },
  { to: '/signatures',    icon: FileSignature,   key: 'signatures'    as const },
  { to: '/documents',     icon: FileText,        key: 'documents'     as const },
  { to: '/tracker',       icon: Activity,        key: 'tracker'       as const },
  { to: '/banking',       icon: Building2,       key: 'banking'       as const },
  { to: '/access-log',    icon: Shield,          key: 'accessLog'     as const },
  { to: '/notifications', icon: Bell,            key: 'notifications' as const },
  { to: '/settings',      icon: Settings,        key: 'settings'      as const },
]

interface SidebarProps {
  open: boolean
  onClose: () => void
}

export default function Sidebar({ open, onClose }: SidebarProps) {
  const { lang } = useLangStore()

  return (
    <>
      {/* Mobile overlay */}
      {open && (
        <div
          className="fixed inset-0 z-20 bg-black/40 lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar panel */}
      <aside
        className={`
          fixed inset-y-0 left-0 z-30 w-64 flex flex-col bg-dark text-white
          transform transition-transform duration-300 ease-in-out
          lg:relative lg:translate-x-0 lg:z-auto
          ${open ? 'translate-x-0' : '-translate-x-full'}
        `}
      >
        {/* Logo */}
        <div className="flex items-center justify-between px-6 py-5 border-b border-white/10">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center">
              <span className="text-white font-bold text-sm">S</span>
            </div>
            <div>
              <p className="font-bold text-white text-base leading-none">SANLY</p>
              <p className="text-white/40 text-[10px] uppercase tracking-widest">Portal</p>
            </div>
          </div>
          <button onClick={onClose} className="lg:hidden text-white/60 hover:text-white">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Nav */}
        <nav className="flex-1 overflow-y-auto py-4 px-3">
          <ul className="space-y-0.5">
            {navItems.map(({ to, icon: Icon, key }) => (
              <li key={to}>
                <NavLink
                  to={to}
                  onClick={onClose}
                  className={({ isActive }) =>
                    `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors group
                     ${isActive
                      ? 'bg-primary text-white'
                      : 'text-white/60 hover:bg-white/10 hover:text-white'
                    }`
                  }
                >
                  {({ isActive }) => (
                    <>
                      <Icon className="w-4 h-4 shrink-0" />
                      <span className="flex-1">{t(lang, key)}</span>
                      {isActive && <ChevronRight className="w-3.5 h-3.5 opacity-60" />}
                    </>
                  )}
                </NavLink>
              </li>
            ))}
          </ul>
        </nav>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-white/10">
          <p className="text-white/30 text-xs">SANLY &copy; 2024</p>
          <p className="text-white/20 text-[10px] mt-0.5">Türkmenistan Hökümeti</p>
        </div>
      </aside>
    </>
  )
}
