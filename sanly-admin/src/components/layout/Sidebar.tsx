import { NavLink } from 'react-router-dom'
import {
  LayoutDashboard, Users, Building2, Shield, FileSearch,
  AlertTriangle, Activity, Settings, X, ChevronRight, Bell,
  GraduationCap, Award, BookOpen, Home, ArrowLeftRight, HeartHandshake, Coins, List, Package, Anchor, Scale, Gavel, Truck, Clock, PiggyBank, FileSignature, CalendarDays, FileText, BarChart2, GitBranch,
} from 'lucide-react'

const navItems = [
  { to: '/dashboard',             icon: LayoutDashboard, label: 'Dashboard'          },
  { to: '/citizens',              icon: Users,            label: 'Citizens'           },
  { to: '/institutions',          icon: Building2,        label: 'Bridge Institutions'},
  { to: '/permissions',           icon: Shield,           label: 'Permissions'        },
  { to: '/audit',                 icon: FileSearch,       label: 'Audit Log'          },
  { to: '/anomalies',             icon: AlertTriangle,    label: 'Anomaly Alerts'     },
  { to: '/education/institutions',icon: GraduationCap,   label: 'Edu: Institutions'  },
  { to: '/education/diplomas',    icon: Award,            label: 'Edu: Diplomas'      },
  { to: '/education/queue',       icon: BookOpen,         label: 'Edu: Enroll Queue'  },
  { to: '/land/properties',       icon: Home,             label: 'Land: Lookup'       },
  { to: '/land/transfers',        icon: ArrowLeftRight,   label: 'Land: Transfers'    },
  { to: '/social/programs',      icon: List,             label: 'Social: Programs'   },
  { to: '/social/claims',        icon: HeartHandshake,   label: 'Social: Claims'     },
  { to: '/social/payments',      icon: Coins,            label: 'Social: Payments'   },
  { to: '/customs/declarations', icon: Package,          label: 'Customs: Decls'     },
  { to: '/customs/ports',        icon: Anchor,           label: 'Customs: Ports'     },
  { to: '/court/cases',          icon: Scale,            label: 'Court: Cases'       },
  { to: '/court/fines',          icon: Gavel,            label: 'Court: Fines'       },
  { to: '/vehicle/registry',     icon: Truck,            label: 'Vehicle: Registry'  },
  { to: '/vehicle/transfers',    icon: ArrowLeftRight,   label: 'Vehicle: Transfers' },
  { to: '/vehicle/inspections',  icon: Clock,            label: 'Vehicle: Inspections'},
  { to: '/pension/retirement',   icon: PiggyBank,        label: 'Pension: Retirement'},
  { to: '/pension/payments',     icon: Coins,            label: 'Pension: Payments'  },
  { to: '/signatures',           icon: FileSignature,    label: 'Signatures'         },
  { to: '/appointments',         icon: CalendarDays,     label: 'Appointments'       },
  { to: '/documents',            icon: FileText,         label: 'Documents'          },
  { to: '/analytics',           icon: BarChart2,        label: 'Analytics'          },
  { to: '/banking',             icon: Building2,        label: 'Banking API'        },
  { to: '/data-requests',        icon: FileSearch,       label: 'Data Requests'      },
  { to: '/notifications',         icon: Bell,             label: 'Notifications'      },
  { to: '/architecture',          icon: GitBranch,        label: 'Architecture'       },
  { to: '/health',                icon: Activity,         label: 'Service Health'     },
  { to: '/settings',              icon: Settings,         label: 'Settings'           },
]

interface SidebarProps { open: boolean; onClose: () => void }

export default function Sidebar({ open, onClose }: SidebarProps) {
  return (
    <>
      {open && <div className="fixed inset-0 z-20 bg-black/40 lg:hidden" onClick={onClose} />}
      <aside className={`
        fixed inset-y-0 left-0 z-30 w-60 flex flex-col bg-dark text-white
        transform transition-transform duration-300 ease-in-out
        lg:relative lg:translate-x-0 lg:z-auto
        ${open ? 'translate-x-0' : '-translate-x-full'}
      `}>
        {/* Logo */}
        <div className="flex items-center justify-between px-5 py-4 border-b border-white/8">
          <div className="flex items-center gap-2.5">
            <div className="w-7 h-7 rounded-lg bg-primary flex items-center justify-center">
              <span className="text-white font-bold text-xs">S</span>
            </div>
            <div>
              <p className="font-bold text-white text-sm leading-none">SANLY</p>
              <p className="text-white/35 text-[9px] uppercase tracking-widest mt-0.5">Administration</p>
            </div>
          </div>
          <button onClick={onClose} className="lg:hidden text-white/50 hover:text-white">
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Nav */}
        <nav className="flex-1 overflow-y-auto py-3 px-2.5">
          <ul className="space-y-0.5">
            {navItems.map(({ to, icon: Icon, label }) => (
              <li key={to}>
                <NavLink to={to} onClick={onClose}
                  className={({ isActive }) =>
                    `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors
                     ${isActive ? 'bg-primary text-white' : 'text-white/55 hover:bg-white/8 hover:text-white'}`
                  }>
                  {({ isActive }) => (
                    <>
                      <Icon className="w-4 h-4 shrink-0" />
                      <span className="flex-1">{label}</span>
                      {isActive && <ChevronRight className="w-3 h-3 opacity-50" />}
                    </>
                  )}
                </NavLink>
              </li>
            ))}
          </ul>
        </nav>

        <div className="px-5 py-3 border-t border-white/8">
          <p className="text-white/25 text-[10px]">SANLY Admin &copy; 2024</p>
        </div>
      </aside>
    </>
  )
}
