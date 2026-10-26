
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react'
import AsyncStorage from '@react-native-async-storage/async-storage'

import { colors as baseColors } from '@/src/theme/colors'

type FontSize = 'normal' | 'large' | 'extraLarge'
type AppColors = typeof baseColors

type AccessibilityContextType = {
  highContrast: boolean
  setHighContrast: (enabled: boolean) => void
  fontSize: FontSize
  setFontSize: (size: FontSize) => void
  fontScale: number
  colors: AppColors
  ready: boolean
}

const CONTRAST_KEY = '@hisopi:highContrast'
const FONT_SIZE_KEY = '@hisopi:fontSize'

const highContrastColors: AppColors = {
  primary: '#000000',
  primaryPressed: '#222222',
  secondary: '#000000',
  accent: '#FFFF00',
  accentSoft: '#FFFACD',
  text: '#000000',
  textMuted: '#333333',
  background: '#FFFFFF',
  surface: '#FFFFFF',
  border: '#000000',
  danger: '#B00020',
  dangerSoft: '#FFE5E5',
  success: '#005A2B',
  successSoft: '#D7FFE6',
  warning: '#664400',
  warningSoft: '#FFF2B2',
}

const fontScales: Record<FontSize, number> = {
  normal: 1,
  large: 1.2,
  extraLarge: 1.4,
}

const AccessibilityContext =
  createContext<AccessibilityContextType | undefined>(undefined)

export function AccessibilityProvider({
  children,
}: {
  children: React.ReactNode
}) {
  const [highContrast, setHighContrastState] = useState(false)
  const [fontSize, setFontSizeState] = useState<FontSize>('normal')
  const [ready, setReady] = useState(false)

  useEffect(() => {
    async function loadPreferences() {
      try {
        const [savedContrast, savedFontSize] = await Promise.all([
          AsyncStorage.getItem(CONTRAST_KEY),
          AsyncStorage.getItem(FONT_SIZE_KEY),
        ])

        setHighContrastState(savedContrast === 'true')

        if (
          savedFontSize === 'normal' ||
          savedFontSize === 'large' ||
          savedFontSize === 'extraLarge'
        ) {
          setFontSizeState(savedFontSize)
        }
      } catch (error) {
        console.warn('Não foi possível carregar as preferências de acessibilidade.', error)
      } finally {
        setReady(true)
      }
    }

    loadPreferences()
  }, [])

  const setHighContrast = useCallback((enabled: boolean) => {
    setHighContrastState(enabled)

    AsyncStorage.setItem(CONTRAST_KEY, String(enabled)).catch((error) => {
      console.warn('Não foi possível salvar o contraste.', error)
    })
  }, [])

  const setFontSize = useCallback((size: FontSize) => {
    setFontSizeState(size)

    AsyncStorage.setItem(FONT_SIZE_KEY, size).catch((error) => {
      console.warn('Não foi possível salvar o tamanho da fonte.', error)
    })
  }, [])

  const value = useMemo(
    () => ({
      highContrast,
      setHighContrast,
      fontSize,
      setFontSize,
      fontScale: fontScales[fontSize],
      colors: highContrast ? highContrastColors : baseColors,
      ready,
    }),
    [highContrast, setHighContrast, fontSize, setFontSize, ready],
  )

  return (
    <AccessibilityContext.Provider value={value}>
      {children}
    </AccessibilityContext.Provider>
  )
}

export function useAccessibility() {
  const context = useContext(AccessibilityContext)

  if (!context) {
    throw new Error(
      'useAccessibility deve ser utilizado dentro de AccessibilityProvider.',
    )
  }

  return context
}
