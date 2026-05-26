import type { Config } from 'tailwindcss'

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#0D7377',
          dark: '#0a5c60',
          light: '#14a9ae',
          50: '#f0fafa',
          100: '#d0f0f1',
        },
        dark: '#1A1A2E',
        background: '#F4F8F9',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        card: '0 1px 3px 0 rgba(0,0,0,0.07), 0 1px 2px -1px rgba(0,0,0,0.07)',
        'card-hover': '0 4px 12px 0 rgba(0,0,0,0.10)',
      },
    },
  },
  plugins: [],
} satisfies Config
