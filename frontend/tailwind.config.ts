import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        jecrc: {
          red: {
            DEFAULT: '#DE1819',
            50: '#FDF2F2',
            100: '#FDE8E8',
            500: '#DE1819',
            600: '#C8102E',
            700: '#9B1124',
          },
          navy: {
            DEFAULT: '#141B38',
            50: '#F0F3F9',
            100: '#E1E7F3',
            500: '#1E2B58',
            800: '#141B38',
            900: '#0B0F20',
          },
          terracotta: {
            DEFAULT: '#EE785B',
            light: '#F8A892',
            dark: '#C8573C',
          },
          surface: {
            DEFAULT: '#FFFFFF',
            subtle: '#FAFAFA',
            muted: '#F3F4F6',
            border: '#E5E7EB',
          },
          text: {
            primary: '#1F2937',
            secondary: '#4B5563',
            muted: '#9CA3AF',
            inverse: '#FFFFFF',
          },
        },
      },
      fontFamily: {
        sans: ['"Open Sans"', 'Inter', 'system-ui', 'sans-serif'],
        display: ['"Work Sans"', '"Open Sans"', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'monospace'],
      },
      borderRadius: {
        card: '0.625rem', // 10px JECRC standard card treatment
      },
      boxShadow: {
        card: '0 1px 3px 0 rgba(20, 27, 56, 0.08), 0 1px 2px 0 rgba(20, 27, 56, 0.04)',
        elevated: '0 4px 6px -1px rgba(20, 27, 56, 0.1), 0 2px 4px -1px rgba(20, 27, 56, 0.06)',
      },
    },
  },
  plugins: [],
} satisfies Config;
