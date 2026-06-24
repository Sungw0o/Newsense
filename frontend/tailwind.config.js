/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#ebfbf2',
          100: '#d2f6e1',
          200: '#a5ecc3',
          300: '#79e2a5',
          400: '#4cd887',
          500: '#3ad07b', // Success green from wireframe
          600: '#22b461',
          700: '#19884a',
          800: '#115c32',
          900: '#08301a',
        },
        primary: {
          50: '#e6f3ff',
          100: '#cce7ff',
          200: '#99ceff',
          300: '#66b4ff',
          400: '#339bff',
          500: '#0084ff', // Vibrant blue from wireframe
          600: '#006fe6',
          700: '#005acc',
          800: '#0045b3',
          900: '#003099',
        },
        secondary: {
          50: '#f0f9ff',
          100: '#e0f2fe',
          200: '#bae6fd',
          300: '#7dd3fc',
          400: '#38bdf8',
          500: '#0ea5e9', // sky blue
          600: '#0284c7',
          700: '#0369a1',
          800: '#075985',
          900: '#0c4a6e',
        },
        accent: {
          50: '#fff0f0',
          100: '#ffd1d1',
          200: '#ffa3a3',
          300: '#ff8686',
          400: '#ff6a6a', // Warning/Danger red from wireframe
          500: '#f55252',
          600: '#d63a3a',
          700: '#b82b2b',
          800: '#991d1d',
          900: '#7a1010',
        },
        dark: {
          950: '#07090f',       // Main dark background
          900: '#0c0f19',
          800: '#121726',
          700: '#1b2238',
          600: '#27314f',
          card: 'rgba(15, 23, 42, 0.45)',       // Glassmorphism card bg
          border: 'rgba(255, 255, 255, 0.08)', // Glassmorphism border
          glow: 'rgba(0, 132, 255, 0.15)',     // Blue glow effect
        },
        slate: {
          50: '#f8fafc',
          100: '#f1f5f9',
          200: '#e2e8f0',
          300: '#cbd5e1',
          400: '#94a3b8',
          500: '#64748b',
          600: '#475569',
          700: '#334155',
          800: '#1e293b',
          900: '#0f172a',
          950: '#020617',
        }
      },
      fontFamily: {
        sans: ['Inter', 'Pretendard', 'Noto Sans KR', 'system-ui', 'sans-serif'],
        display: ['Fustat', 'Inter', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'ui-monospace', 'monospace'],
      },
      scale: {
        '102': '1.02',
      },
      boxShadow: {
        'premium': '0 10px 30px -10px rgba(0, 0, 0, 0.5), 0 1px 3px rgba(0, 0, 0, 0.3)',
        'premium-hover': '0 20px 40px -15px rgba(0, 0, 0, 0.7), 0 1px 10px rgba(0, 0, 0, 0.4)',
        'glass-glow': '0 0 25px rgba(0, 132, 255, 0.15)',
        'glass-glow-hover': '0 0 35px rgba(0, 132, 255, 0.25)',
      },
      keyframes: {
        float: {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%': { transform: 'translateY(-10px)' },
        },
        'pulse-glow': {
          '0%, 100%': { opacity: '0.5', transform: 'scale(1)' },
          '50%': { opacity: '0.8', transform: 'scale(1.05)' },
        }
      },
      animation: {
        float: 'float 6s ease-in-out infinite',
        'pulse-glow': 'pulse-glow 4s ease-in-out infinite',
      }
    },
  },
  plugins: [],
}
