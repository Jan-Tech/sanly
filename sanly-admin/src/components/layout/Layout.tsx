import { useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import Sidebar from './Sidebar'
import TopBar from './TopBar'

const PAGE_TITLES: Record<string, string> = {
  '/dashboard': 'Dashboard',
  '/citizens': 'Citizens',
  '/institutions': 'Institutions',
  '/permissions': 'Permissions',
  '/audit': 'Exchange Audit Log',
  '/anomalies': 'Anomaly Alerts',
  '/health': 'Service Health',
  '/settings': 'Settings',
}

export default function Layout() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const { pathname } = useLocation()
  const title = pathname.startsWith('/citizens/') ? 'Citizen Detail' : PAGE_TITLES[pathname]

  return (
    <div className="flex h-screen bg-background overflow-hidden">
      <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div className="flex-1 flex flex-col overflow-hidden min-w-0">
        <TopBar onMenuClick={() => setSidebarOpen(true)} title={title} />
        <main className="flex-1 overflow-y-auto">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 py-5">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  )
}
