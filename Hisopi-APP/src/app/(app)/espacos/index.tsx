import { router, useFocusEffect } from 'expo-router'
import { useMemo, useState, useCallback } from 'react'
import { useAccessibility } from '@/src/context/AccessibilityContext'
import {
  AccessibilityInfo,
  ActivityIndicator,
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native'

import { useAuth } from '@/src/context/AuthContext'
import { useResponsive } from '@/src/hooks/useResponsive'
import {
  listarEspacosDoUsuario,
  type EspacoComPapel,
} from '@/src/services/espacoService'

const PAPEL_LABEL: Record<string, string> = {
  DONO: 'Dono',
  ADMIN: 'Admin',
  GERENTE: 'Gerente',
  OPERADOR: 'Operador',
}

export default function EspacosScreen() {
  const { colors, fontScale } = useAccessibility()
  const styles = useMemo(() => createStyles(colors, fontScale), [colors, fontScale])
  
  const { usuario, logout } = useAuth()
  const { isDesktop } = useResponsive()

  const [espacos, setEspacos] = useState<EspacoComPapel[]>([])
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  const load = useCallback(async () => {
    if (!usuario) return

    try {
      setErro(null)
      const data = await listarEspacosDoUsuario()
      setEspacos(data)
    } catch (error) {
      console.log('Erro ao carregar espaços:', error)
      const mensagem = 'Não foi possível carregar seus espaços. Tente novamente.'
      setErro(mensagem)
      AccessibilityInfo.announceForAccessibility(mensagem)
    } finally {
      setLoading(false)
      setRefreshing(false)
    }
  }, [usuario])

  const sair = useCallback(async () => {
    try {
      await logout()
      router.replace('/login')
    } catch (error) {
      console.error('Erro ao sair da conta:', error)
    }
  }, [logout])

  useFocusEffect(
    useCallback(() => {
      load()
    }, [load])
  )

  const onRefresh = useCallback(() => {
    setRefreshing(true)
    load()
  }, [load])

  const abrirNovoEspaco = () => router.push('/espacos/novo')

  if (loading) {
    return (
      <View
        style={styles.center}
        accessible
        accessibilityRole="progressbar"
        accessibilityLabel="Carregando espaços"
        accessibilityLiveRegion="polite"
      >
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    )
  }

  return (
    <View style={styles.screen}>
      <View style={[styles.header, isDesktop && styles.headerDesktop]}>
        <View>
          <Text
            style={styles.title}
            accessibilityRole="header"
            maxFontSizeMultiplier={1.5}
          >
            Seus espaços
          </Text>
          <Text style={styles.subtitle} maxFontSizeMultiplier={1.5}>
            Escolha um espaço para gerenciar, ou crie um novo.
          </Text>
        </View>

        <Pressable
          style={({ pressed }) => [
            styles.newButton,
            pressed && styles.pressed,
          ]}
          onPress={abrirNovoEspaco}
          accessibilityRole="button"
          accessibilityLabel="Criar novo espaço"
          accessibilityHint="Abre a tela de criação de espaço"
          hitSlop={8}
        >
          <Text style={styles.newButtonText} maxFontSizeMultiplier={1.4}>
            + Novo espaço
          </Text>
        </Pressable>

        <Pressable
          style={({ pressed }) => [
            styles.profileButton,
            pressed && styles.pressed,
          ]}
          onPress={() => router.push('/perfil')}
          accessibilityRole="button"
          accessibilityLabel="Abrir meu perfil"
          accessibilityHint="Abre suas configurações de perfil e acessibilidade"
          hitSlop={8}
        >
          <Text style={styles.profileButtonText}>
            Meu perfil
          </Text>
        </Pressable>

        <Pressable
          style={({ pressed }) => [
            styles.profileButton,
            pressed && styles.pressed,
          ]}
          onPress={sair}
          accessibilityRole="button"
          accessibilityLabel="Sair da conta"
          accessibilityHint="Encerra sua sessão e retorna para o login"
          hitSlop={8}
        >
          <Text style={styles.profileButtonText}>Sair</Text>
        </Pressable>
      </View>

      {erro && (
        <View
          style={[styles.erroBox, isDesktop && styles.erroBoxDesktop]}
          accessibilityRole="alert"
          accessibilityLiveRegion="assertive"
        >
          <Text style={styles.erroText} maxFontSizeMultiplier={1.5}>
            {erro}
          </Text>
          <Pressable
            onPress={onRefresh}
            style={({ pressed }) => [
              styles.erroButton,
              pressed && styles.pressed,
            ]}
            accessibilityRole="button"
            accessibilityLabel="Tentar carregar os espaços novamente"
            hitSlop={8}
          >
            <Text style={styles.erroButtonText} maxFontSizeMultiplier={1.4}>
              Tentar novamente
            </Text>
          </Pressable>
        </View>
      )}

      {espacos.length === 0 && !erro ? (
        <View style={styles.empty}>
          <View
            style={styles.emptyIcon}
            accessibilityElementsHidden
            importantForAccessibility="no-hide-descendants"
          >
            <Text style={styles.emptyIconText}>+</Text>
          </View>
          <Text
            style={styles.emptyTitle}
            accessibilityRole="header"
            maxFontSizeMultiplier={1.5}
          >
            Nenhum espaço ainda
          </Text>
          <Text style={styles.emptyText} maxFontSizeMultiplier={1.5}>
            Crie um espaço pessoal para controlar sua despensa, ou um
            espaço de organização para o seu negócio.
          </Text>
          <Pressable
            style={({ pressed }) => [
              styles.emptyButton,
              pressed && styles.pressed,
            ]}
            onPress={abrirNovoEspaco}
            accessibilityRole="button"
            accessibilityLabel="Criar meu primeiro espaço"
            accessibilityHint="Abre a tela de criação de espaço"
            hitSlop={8}
          >
            <Text style={styles.emptyButtonText} maxFontSizeMultiplier={1.4}>
              Criar meu primeiro espaço
            </Text>
          </Pressable>
        </View>
      ) : (
        <FlatList
          data={espacos}
          key={isDesktop ? 'desktop' : 'mobile'}
          keyExtractor={(item) => String(item.id)}
          numColumns={isDesktop ? 3 : 1}
          columnWrapperStyle={isDesktop ? styles.row : undefined}
          contentContainerStyle={[
            styles.list,
            isDesktop && styles.listDesktop,
          ]}
          refreshing={refreshing}
          onRefresh={onRefresh}
          accessibilityLabel={`Lista de espaços, ${espacos.length} ${
            espacos.length === 1 ? 'item' : 'itens'
          }`}
          renderItem={({ item }) => {
            const ehOrg = item.tipo === 'ORGANIZACAO'
            const tipoLabel = ehOrg ? 'Organização' : 'Pessoal'
            const papelLabel = PAPEL_LABEL[item.papel] ?? item.papel

            return (
              <Pressable
                style={({ pressed }) => [
                  styles.card,
                  isDesktop && styles.cardDesktop,
                  pressed && styles.pressed,
                ]}
                onPress={() => router.push(`/espacos/${item.id}`)}
                accessibilityRole="button"
                accessibilityLabel={`${item.nome}, espaço ${tipoLabel}, seu papel: ${papelLabel}`}
                accessibilityHint="Abre o espaço"
              >
                <View
                  style={[styles.cardIcon, ehOrg && styles.cardIconOrg]}
                  accessibilityElementsHidden
                  importantForAccessibility="no-hide-descendants"
                >
                  <Text style={styles.cardIconText}>{ehOrg ? 'O' : 'P'}</Text>
                </View>

                <Text
                  style={styles.cardNome}
                  numberOfLines={1}
                  maxFontSizeMultiplier={1.4}
                >
                  {item.nome}
                </Text>

                <Text style={styles.cardTipo} maxFontSizeMultiplier={1.4}>
                  {tipoLabel}
                </Text>

                <View style={styles.papelBadge}>
                  <Text
                    style={styles.papelBadgeText}
                    maxFontSizeMultiplier={1.3}
                  >
                    {papelLabel}
                  </Text>
                </View>
              </Pressable>
            )
          }}
        />
      )}
    </View>
  )
}

const MIN_TOUCH = 44

function createStyles(
  colors: typeof import('@/src/theme/colors').colors,
  fontScale: number,
) {
  return StyleSheet.create({
    screen: {
      flex: 1,
      backgroundColor: colors.background,
      padding: 20,
    },
    center: {
      flex: 1,
      alignItems: 'center',
      justifyContent: 'center',
      backgroundColor: colors.background,
    },
    pressed: {
      opacity: 0.85,
    },
    header: {
      marginBottom: 24,
      gap: 12,
    },
    headerDesktop: {
      flexDirection: 'row',
      justifyContent: 'space-between',
      alignItems: 'flex-end',
      maxWidth: 1000,
      width: '100%',
      alignSelf: 'center',
    },
    title: {
      fontSize: 26 * fontScale,
      fontWeight: '800',
      color: colors.text,
    },
    subtitle: {
      fontSize: 14 * fontScale,
      color: colors.textMuted,
      marginTop: 4,
    },
    profileButton: {
      minHeight: 44,
      justifyContent: 'center',
      alignSelf: 'flex-start',
      paddingVertical: 12,
      paddingHorizontal: 18,
      borderRadius: 12,
      borderWidth: 1,
      borderColor: colors.border,
      backgroundColor: colors.accentSoft,
    },
    profileButtonText: {
      color: colors.text,
      fontWeight: '700',
      fontSize: 14 * fontScale,
    },
    newButton: {
      backgroundColor: colors.primary,
      minHeight: 44,
      justifyContent: 'center',
      paddingVertical: 12,
      paddingHorizontal: 18,
      borderRadius: 12,
      marginTop: 16,
      alignSelf: 'flex-start',
    },
    newButtonText: {
      color: '#FFFFFF',
      fontWeight: '700',
      fontSize: 14 * fontScale,
    },
    erroBox: {
      backgroundColor: colors.dangerSoft,
      borderWidth: 1,
      borderColor: colors.danger,
      borderRadius: 12,
      padding: 14,
      marginBottom: 16,
    },
    erroBoxDesktop: {
      maxWidth: 1000,
      width: '100%',
      alignSelf: 'center',
    },
    erroText: {
      color: colors.danger,
      fontSize: 14 * fontScale,
      fontWeight: '600',
    },
    erroButton: {
      minHeight: 44,
      justifyContent: 'center',
      alignSelf: 'flex-start',
      marginTop: 8,
    },
    erroButtonText: {
      color: colors.danger,
      fontSize: 14 * fontScale,
      fontWeight: '800',
      textDecorationLine: 'underline',
    },
    list: {
      paddingBottom: 24,
    },
    listDesktop: {
      maxWidth: 1000,
      width: '100%',
      alignSelf: 'center',
    },
    row: {
      gap: 16,
      justifyContent: 'flex-start',
    },
    card: {
      backgroundColor: colors.surface,
      borderRadius: 18,
      padding: 20,
      borderWidth: 1,
      borderColor: colors.border,
      marginBottom: 16,
      minHeight: 44,
    },
    cardDesktop: {
      flex: 1,
      maxWidth: '32%',
    },
    cardIcon: {
      width: 52,
      height: 52,
      borderRadius: 14,
      backgroundColor: colors.accentSoft,
      alignItems: 'center',
      justifyContent: 'center',
      marginBottom: 14,
    },
    cardIconOrg: {
      backgroundColor: colors.surface,
      borderWidth: 1,
      borderColor: colors.border,
    },
    cardIconText: {
      fontSize: 24 * fontScale,
      color: colors.text,
    },
    cardNome: {
      fontSize: 17 * fontScale,
      fontWeight: '700',
      color: colors.text,
    },
    cardTipo: {
      fontSize: 13 * fontScale,
      color: colors.textMuted,
      marginTop: 2,
      marginBottom: 12,
    },
    papelBadge: {
      alignSelf: 'flex-start',
      backgroundColor: colors.background,
      borderWidth: 1,
      borderColor: colors.border,
      paddingHorizontal: 10,
      paddingVertical: 4,
      borderRadius: 999,
    },
    papelBadgeText: {
      fontSize: 12 * fontScale,
      fontWeight: '700',
      color: colors.text,
    },
    empty: {
      flex: 1,
      alignItems: 'center',
      justifyContent: 'center',
      paddingHorizontal: 32,
    },
    emptyIcon: {
      width: 64,
      height: 64,
      borderRadius: 32,
      backgroundColor: colors.accentSoft,
      alignItems: 'center',
      justifyContent: 'center',
      marginBottom: 20,
    },
    emptyIconText: {
      fontSize: 32 * fontScale,
      color: colors.text,
      fontWeight: '800',
    },
    emptyTitle: {
      fontSize: 18 * fontScale,
      fontWeight: '800',
      color: colors.text,
      marginBottom: 8,
    },
    emptyText: {
      fontSize: 14 * fontScale,
      color: colors.textMuted,
      textAlign: 'center',
      lineHeight: 21 * fontScale,
      marginBottom: 24,
    },
    emptyButton: {
      backgroundColor: colors.primary,
      minHeight: 44,
      justifyContent: 'center',
      paddingVertical: 14,
      paddingHorizontal: 24,
      borderRadius: 12,
    },
    emptyButtonText: {
      color: '#FFFFFF',
      fontWeight: '700',
      fontSize: 15 * fontScale,
    },
  })
}