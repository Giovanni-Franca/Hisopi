import { Redirect, Slot } from 'expo-router'
import { ActivityIndicator, StyleSheet, View } from 'react-native'

import { useAuth } from '@/src/context/AuthContext'
import { colors } from '@/src/theme/colors'

export default function AppGroupLayout() {
  const { authenticated, loading } = useAuth()

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    )
  }

  if (!authenticated) {
    return <Redirect href="/login" />
  }

  return <Slot />
}

const styles = StyleSheet.create({
  loadingContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.background,
  },
})