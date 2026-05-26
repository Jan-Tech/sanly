import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'

interface HourlyData {
  hour: string
  success: number
  denied: number
  total: number
}

interface ExchangeVolumeChartProps {
  data: HourlyData[]
  loading?: boolean
}

export default function ExchangeVolumeChart({ data, loading }: ExchangeVolumeChartProps) {
  if (loading) {
    return <div className="skeleton h-48 rounded-xl" />
  }

  return (
    <ResponsiveContainer width="100%" height={200}>
      <BarChart data={data} barSize={10}>
        <CartesianGrid strokeDasharray="3 3" stroke="#F1F5F9" vertical={false} />
        <XAxis
          dataKey="hour"
          tick={{ fontSize: 10, fill: '#94A3B8' }}
          axisLine={false} tickLine={false}
          interval={3}
        />
        <YAxis tick={{ fontSize: 10, fill: '#94A3B8' }} axisLine={false} tickLine={false} width={28} />
        <Tooltip
          contentStyle={{ fontSize: 12, borderRadius: 8, border: '1px solid #E2E8F0', boxShadow: '0 4px 12px rgba(0,0,0,0.08)' }}
          cursor={{ fill: '#F8FAFC' }}
        />
        <Legend wrapperStyle={{ fontSize: 11, paddingTop: 8 }} />
        <Bar dataKey="success" name="Success" fill="#0D7377" stackId="a" radius={[0, 0, 0, 0]} />
        <Bar dataKey="denied" name="Denied" fill="#DC2626" stackId="a" radius={[2, 2, 0, 0]} />
      </BarChart>
    </ResponsiveContainer>
  )
}
