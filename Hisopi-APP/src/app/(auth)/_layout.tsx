import { Redirect, Slot } from 'expo-router'
import { ActivityIndicator, View, StyleSheet } from 'react-native'

import { useAuth } from '@/src/context/AuthContext'
import { colors } from '@/src/theme/colors'

export default function AuthGroupLayout() {
  const { authenticated, loading } = useAuth()

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    )
  }

  // Já autenticado: não faz sentido ver login/cadastro de novo.
  // TODO: ajustar destino quando a rota de seleção de espaço existir
  // (ex: '/espacos').
  if (authenticated) {
    return <Redirect href="/" />
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