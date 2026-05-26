import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import ProtectedRoute from './components/ProtectedRoute'
import Layout from './components/layout/Layout'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Identity from './pages/Identity'
import Medical from './pages/Medical'
import License from './pages/License'
import Tax from './pages/Tax'
import Business from './pages/Business'
import AccessLog from './pages/AccessLog'
import Settings from './pages/Settings'
import Notifications from './pages/Notifications'
import Education from './pages/Education'
import Property from './pages/Property'
import Benefits from './pages/Benefits'
import Customs from './pages/Customs'
import Court from './pages/Court'
import Vehicles from './pages/Vehicles'
import Pension from './pages/Pension'
import Signatures from './pages/Signatures'
import Appointments from './pages/Appointments'
import Documents from './pages/Documents'
import Tracker from './pages/Tracker'
import Banking from './pages/Banking'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 2 * 60 * 1000,
      gcTime: 10 * 60 * 1000,
      refetchOnWindowFocus: false,
    },
  },
})

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/identity" element={<Identity />} />
            <Route path="/medical" element={<Medical />} />
            <Route path="/license" element={<License />} />
            <Route path="/tax" element={<Tax />} />
            <Route path="/business" element={<Business />} />
            <Route path="/access-log" element={<AccessLog />} />
            <Route path="/education" element={<Education />} />
            <Route path="/property" element={<Property />} />
            <Route path="/benefits" element={<Benefits />} />
            <Route path="/customs" element={<Customs />} />
            <Route path="/court" element={<Court />} />
            <Route path="/vehicles" element={<Vehicles />} />
            <Route path="/pension" element={<Pension />} />
            <Route path="/appointments" element={<Appointments />} />
            <Route path="/signatures" element={<Signatures />} />
            <Route path="/documents" element={<Documents />} />
            <Route path="/tracker" element={<Tracker />} />
            <Route path="/banking" element={<Banking />} />
            <Route path="/notifications" element={<Notifications />} />
            <Route path="/settings" element={<Settings />} />
          </Route>
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}
