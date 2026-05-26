import { useState } from 'react'
import { Outlet } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import Sidebar from './Sidebar'
import Navbar from './Navbar'
import { useAuthStore } from '../../store/authStore'
import { getCitizen } from '../../api/registry'

export default function Layout() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const { nationalId } = useAuthStore()

  const { data: citizen } = useQuery({
    queryKey: ['citizen', nationalId],
    queryFn: () => getCitizen(nationalId!),
    enabled: !!nationalId,
    staleTime: 5 * 60 * 1000,
  })

  const citizenName = citizen
    ? `${citizen.firstName} ${citizen.lastName}`
    : undefined

  return (
    <div className="flex h-screen bg-background overflow-hidden">
      <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div className="flex-1 flex flex-col overflow-hidden min-w-0">
        <Navbar onMenuClick={() => setSidebarOpen(true)} citizenName={citizenName} />
        <main className="flex-1 overflow-y-auto">
          <div className="max-w-5xl mx-auto px-4 sm:px-6 py-6">
            <Outlet context={{ citizen }} />
          </div>
        </main>
      </div>
    </div>
  )
}
