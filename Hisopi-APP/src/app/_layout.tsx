
import { Stack } from 'expo-router'

import { AuthProvider } from '@/src/context/AuthContext'
import { AccessibilityProvider } from '@/src/context/AccessibilityContext'

export default function RootLayout() {
  return (
    <AccessibilityProvider>
      <AuthProvider>
        <Stack screenOptions={{ headerShown: false }} />
      </AuthProvider>
    </AccessibilityProvider>
  )
}
