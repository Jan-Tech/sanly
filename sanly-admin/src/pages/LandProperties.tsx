import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Home, Search } from 'lucide-react'
import { getPropertyDetail, getTransfersByProperty, getValuationsByProperty } from '../api/land'
import Badge from '../components/ui/Badge'
import Modal from '../components/ui/Modal'

const TYPE_LABELS: Record<string, string> = {
  RESIDENTIAL_APARTMENT: 'Apartment', RESIDENTIAL_HOUSE: 'House', COMMERCIAL: 'Commercial',
  AGRICULTURAL: 'Agricultural', INDUSTRIAL: 'Industrial', LAND_PLOT: 'Land Plot',
  GARAGE: 'Garage', OTHER: 'Other',
}

function PropertyLookup() {
  const [cadInput, setCadInput] = useState('')
  const [searchCode, setSearchCode] = useState('')
  const [showHistory, setShowHistory] = useState(false)

  const { data: detail, isLoading, isError } = useQuery({
    queryKey: ['land-property', searchCode],
    queryFn: () => getPropertyDetail(searchCode),
    enabled: !!searchCode,
    retry: false,
  })

  const { data: transfers } = useQuery({
    queryKey: ['land-transfers-prop', searchCode],
    queryFn: () => getTransfersByProperty(searchCode),
    enabled: !!searchCode && showHistory,
  })

  const { data: valuations } = useQuery({
    queryKey: ['land-valuations', searchCode],
    queryFn: () => getValuationsByProperty(searchCode),
    enabled: !!searchCode && showHistory,
  })

  return (
    <div className="space-y-4">
      <div className="flex gap-2">
        <input
          type="text"
          placeholder="Enter cadastral number (e.g. TM-CAD-2024000001)"
          value={cadInput}
          onChange={e => setCadInput(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && setSearchCode(cadInput.trim())}
          className="input flex-1"
        />
        <button
          onClick={() => setSearchCode(cadInput.trim())}
          className="btn-primary flex items-center gap-2"
        >
          <Search className="w-4 h-4" /> Search
        </button>
      </div>

      {isLoading && <div className="skeleton h-32 rounded-xl" />}
      {isError && <p className="text-sm text-red-500">Property not found: {searchCode}</p>}

      {detail && (
        <div className="bg-white rounded-xl border border-gray-100 p-5 space-y-4">
          {/* Property header */}
          <div className="flex items-start justify-between gap-4">
            <div>
              <div className="flex items-center gap-2 flex-wrap mb-1">
                <span className="font-mono text-sm font-semibold text-dark">{detail.property.cadastralNumber}</span>
                <Badge label={TYPE_LABELS[detail.property.propertyType] || detail.property.propertyType} status="ACTIVE" size="sm" />
                <Badge label={detail.property.status} status={detail.property.status === 'REGISTERED' ? 'ACTIVE' : 'PENDING'} size="sm" />
              </div>
              <p className="text-sm text-gray-600">{detail.property.address}</p>
              <p className="text-xs text-gray-400">{detail.property.region}{detail.property.area ? ` · ${detail.property.area} m²` : ''}</p>
            </div>
            <button
              onClick={() => setShowHistory(!showHistory)}
              className="text-xs text-primary font-medium hover:text-primary-dark"
            >
              {showHistory ? 'Hide history' : 'Show history'}
            </button>
          </div>

          {/* Owners */}
          <div>
            <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-2">Current Owners</p>
            <div className="space-y-1.5">
              {detail.owners.map((o: any) => (
                <div key={o.ownershipId} className="flex items-center gap-3 text-sm">
                  <span className="font-mono text-xs text-gray-600 w-36 shrink-0">{o.ownerNationalId}</span>
                  <span className="text-xs text-gray-500">{o.ownershipShare}% · {o.acquiredVia} · {o.acquiredAt}</span>
                </div>
              ))}
              {detail.owners.length === 0 && <p className="text-xs text-gray-400">No active owners</p>}
            </div>
          </div>

          {/* Transfer history */}
          {showHistory && transfers && transfers.length > 0 && (
            <div>
              <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-2">Transfer History</p>
              <div className="space-y-1.5">
                {transfers.map(t => (
                  <div key={t.applicationId} className="flex items-center gap-3 text-xs">
                    <span className="text-gray-500 font-mono">{t.fromNationalId} → {t.toNationalId}</span>
                    <span className="text-gray-400">{t.transferType} · {t.applicationDate}</span>
                    <Badge label={t.status} status={t.status === 'APPROVED' ? 'ACTIVE' : t.status === 'REJECTED' ? 'FAIL' : 'PENDING'} size="sm" />
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Valuations */}
          {showHistory && valuations && valuations.length > 0 && (
            <div>
              <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-2">Valuations</p>
              <div className="space-y-1.5">
                {valuations.map(v => (
                  <div key={v.valuationId} className="flex items-center gap-3 text-xs text-gray-500">
                    <span className="font-semibold text-dark">{v.valuationAmount}</span>
                    <span>{v.valuationDate}</span>
                    <span className="text-gray-400">{v.purpose}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  )
}

export default function LandProperties() {
  return (
    <div className="space-y-5">
      <div className="flex items-center gap-2">
        <Home className="w-5 h-5 text-primary" />
        <h1 className="text-xl font-bold text-dark">Property Lookup</h1>
      </div>
      <p className="text-sm text-gray-500">
        Search any property by cadastral number to view ownership, transfer history, and valuations.
      </p>
      <PropertyLookup />
    </div>
  )
}
