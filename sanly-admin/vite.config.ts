import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  return {
    plugins: [react()],
    server: {
      port: 5174,
      proxy: {
        // All API calls go through the gateway.
        // /proxy/bridge/api/v1/... → gateway receives /bridge/api/v1/...
        // Gateway routes on /bridge/** with StripPrefix=1 → sanly-bridge /api/v1/...
        '/proxy': {
          target: env.VITE_GATEWAY_URL || 'http://localhost:8080',
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/proxy/, ''),
        },
      },
    },
  }
})
