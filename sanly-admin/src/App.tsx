import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import ProtectedRoute from './components/ProtectedRoute'
import Layout from './components/layout/Layout'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Citizens from './pages/Citizens'
import CitizenDetail from './pages/CitizenDetail'
import Institutions from './pages/Institutions'
import Permissions from './pages/Permissions'
import AuditLog from './pages/AuditLog'
import Anomalies from './pages/Anomalies'
import Health from './pages/Health'
import Settings from './pages/Settings'
import NotificationsAdmin from './pages/NotificationsAdmin'
import EducationInstitutions from './pages/EducationInstitutions'
import EducationDiplomas from './pages/EducationDiplomas'
import EducationPendingQueue from './pages/EducationPendingQueue'
import LandProperties from './pages/LandProperties'
import LandTransfers from './pages/LandTransfers'
import SocialPrograms from './pages/SocialPrograms'
import SocialClaimsQueue from './pages/SocialClaimsQueue'
import SocialPayments from './pages/SocialPayments'
import CustomsDeclarations from './pages/CustomsDeclarations'
import CustomsPorts from './pages/CustomsPorts'
import CourtCases from './pages/CourtCases'
import CourtFinesQueue from './pages/CourtFinesQueue'
import VehicleRegistry from './pages/VehicleRegistry'
import VehicleTransfers from './pages/VehicleTransfers'
import VehicleInsuranceAlerts from './pages/VehicleInsuranceAlerts'
import PensionRetirementQueue from './pages/PensionRetirementQueue'
import PensionPayments from './pages/PensionPayments'
import SignatureAdmin from './pages/SignatureAdmin'
import DataRequestsAdmin from './pages/DataRequestsAdmin'
import AppointmentsAdmin from './pages/AppointmentsAdmin'
import DocumentsAdmin from './pages/DocumentsAdmin'
import AnalyticsAdmin from './pages/AnalyticsAdmin'
import BankingAdmin from './pages/BankingAdmin'
import ArchitectureDiagram from './pages/ArchitectureDiagram'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { staleTime: 60_000, gcTime: 5 * 60_000, refetchOnWindowFocus: false, retry: false },
  },
})

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route element={<ProtectedRoute><Layout /></ProtectedRoute>} path="/">
            <Route path="/dashboard"            element={<Dashboard />} />
            <Route path="/citizens"             element={<Citizens />} />
            <Route path="/citizens/:nationalId" element={<CitizenDetail />} />
            <Route path="/institutions"         element={<Institutions />} />
            <Route path="/permissions"          element={<Permissions />} />
            <Route path="/audit"                element={<AuditLog />} />
            <Route path="/anomalies"            element={<Anomalies />} />
            <Route path="/education/institutions" element={<EducationInstitutions />} />
            <Route path="/education/diplomas"    element={<EducationDiplomas />} />
            <Route path="/education/queue"       element={<EducationPendingQueue />} />
            <Route path="/land/properties"       element={<LandProperties />} />
            <Route path="/land/transfers"        element={<LandTransfers />} />
            <Route path="/social/programs"       element={<SocialPrograms />} />
            <Route path="/social/claims"         element={<SocialClaimsQueue />} />
            <Route path="/social/payments"       element={<SocialPayments />} />
            <Route path="/customs/declarations"  element={<CustomsDeclarations />} />
            <Route path="/customs/ports"         element={<CustomsPorts />} />
            <Route path="/court/cases"           element={<CourtCases />} />
            <Route path="/court/fines"           element={<CourtFinesQueue />} />
            <Route path="/vehicle/registry"      element={<VehicleRegistry />} />
            <Route path="/vehicle/transfers"     element={<VehicleTransfers />} />
            <Route path="/vehicle/inspections"   element={<VehicleInsuranceAlerts />} />
            <Route path="/pension/retirement"    element={<PensionRetirementQueue />} />
            <Route path="/pension/payments"      element={<PensionPayments />} />
            <Route path="/signatures"            element={<SignatureAdmin />} />
            <Route path="/appointments"           element={<AppointmentsAdmin />} />
            <Route path="/documents"             element={<DocumentsAdmin />} />
            <Route path="/analytics"             element={<AnalyticsAdmin />} />
            <Route path="/banking"               element={<BankingAdmin />} />
            <Route path="/data-requests"         element={<DataRequestsAdmin />} />
            <Route path="/notifications"         element={<NotificationsAdmin />} />
            <Route path="/architecture"          element={<ArchitectureDiagram />} />
            <Route path="/health"               element={<Health />} />
            <Route path="/settings"             element={<Settings />} />
          </Route>
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}
