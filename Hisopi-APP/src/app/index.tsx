import { Redirect } from 'expo-router'
import { ActivityIndicator, StyleSheet, View } from 'react-native'

import { useAuth } from '@/src/context/AuthContext'
import { colors } from '@/src/theme/colors'

export default function Index() {
  const { authenticated, loading } = useAuth()

  if (loading) {
    return (
      <View style={styles.container}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    )
  }

  return <Redirect href={authenticated ? '/espacos' : '/login'} />
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.background,
  },
})