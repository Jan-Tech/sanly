import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
        // All API calls go through the gateway.
        // /proxy/registry/api/v1/... → gateway receives /registry/api/v1/...
        // Gateway routes on /registry/** with StripPrefix=1 → citizen-registry /api/v1/...
        '/proxy': {
          target: env.VITE_GATEWAY_URL || 'http://localhost:8080',
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/proxy/, ''),
        },
      },
    },
  }
})
