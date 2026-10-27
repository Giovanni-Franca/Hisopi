
import { router, Slot, usePathname, useLocalSearchParams } from 'expo-router'
import { useEffect, useMemo, useState } from 'react'
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native'

import { useResponsive } from '@/src/hooks/useResponsive'
import { useAccessibility } from '@/src/context/AccessibilityContext'
import { buscarEspaco, type Espaco } from '@/src/services/espacoService'

type NavItem = {
  key: string
  label: string
  getHref: (espacoID: string) => string
  onlyOrg?: boolean
}

const NAV_ITEMS: NavItem[] = [
  { key: 'dashboard', label: 'Dashboard', getHref: (id) => `/espacos/${id}` },
  { key: 'insumos', label: 'Insumos', getHref: (id) => `/espacos/${id}/insumos` },
  { key: 'receitas', label: 'Receitas', getHref: (id) => `/espacos/${id}/receitas` },
  { key: 'membros', label: 'Membros', getHref: (id) => `/espacos/${id}/membros`, onlyOrg: true },
  { key: 'relatorios', label: 'Relatórios', getHref: (id) => `/espacos/${id}/relatorios` },
]

export default function EspacoLayout() {
  const { espacoID } = useLocalSearchParams<{ espacoID: string }>()
  const pathname = usePathname()
  const { isDesktop } = useResponsive()
  const { colors, fontScale } = useAccessibility()

  const [espaco, setEspaco] = useState<Espaco | null>(null)
  const [loading, setLoading] = useState(true)
  const [erro, setErro] = useState('')

  const styles = useMemo(
    () =>
      StyleSheet.create({
        loadingContainer: {
          flex: 1,
          alignItems: 'center',
          justifyContent: 'center',
          backgroundColor: colors.background,
        },
        screen: {
          flex: 1,
          backgroundColor: colors.background,
        },
        screenDesktop: {
          flexDirection: 'row',
        },
        nav: {
          backgroundColor: colors.surface,
          borderBottomWidth: 1,
          borderBottomColor: colors.border,
          paddingTop: 16,
          paddingHorizontal: 16,
          paddingBottom: 8,
        },
        navDesktop: {
          width: 240,
          borderBottomWidth: 0,
          borderRightWidth: 1,
          borderRightColor: colors.border,
          paddingTop: 24,
          paddingHorizontal: 20,
        },
        exitButton: {
          minHeight: 44,
          justifyContent: 'center',
          marginBottom: 12,
        },
        exitButtonText: {
          fontSize: 13 * fontScale,
          color: colors.textMuted,
          fontWeight: '600',
        },
        espacoInfo: {
          marginBottom: 16,
        },
        espacoNome: {
          fontSize: 18 * fontScale,
          fontWeight: '800',
          color: colors.text,
        },
        espacoTipo: {
          fontSize: 12 * fontScale,
          color: colors.primary,
          fontWeight: '700',
          marginTop: 2,
        },
        profileButton: {
          minHeight: 44,
          justifyContent: 'center',
          alignSelf: 'flex-start',
          backgroundColor: colors.accentSoft,
          borderWidth: 1,
          borderColor: colors.border,
          borderRadius: 10,
          paddingHorizontal: 14,
          paddingVertical: 10,
          marginBottom: 16,
        },
        profileButtonText: {
          color: colors.text,
          fontSize: 14 * fontScale,
          fontWeight: '700',
        },
        navItems: {
          flexDirection: 'row',
          gap: 8,
        },
        navItemsDesktop: {
          flexDirection: 'column',
        },
        navItem: {
          paddingVertical: 10,
          paddingHorizontal: 14,
          borderRadius: 10,
          minHeight: 44,
          justifyContent: 'center',
        },
        navItemActive: {
          backgroundColor: colors.accentSoft,
        },
        navItemText: {
          fontSize: 14 * fontScale,
          fontWeight: '600',
          color: colors.textMuted,
        },
        navItemTextActive: {
          color: colors.primary,
          fontWeight: '700',
        },
        content: {
          flex: 1,
        },
        errorText: {
          color: colors.danger,
          textAlign: 'center',
          paddingHorizontal: 24,
          fontSize: 14 * fontScale,
        },
      }),
    [colors, fontScale],
  )

  useEffect(() => {
    let active = true

    async function load() {
      if (!espacoID) {
        setErro('Não foi possível identificar o espaço na URL.')
        setLoading(false)
        return
      }

      setLoading(true)
      setErro('')

      try {
        const data = await buscarEspaco(espacoID)

        if (active) {
          setEspaco(data)
        }
      } catch (error) {
        console.log('Erro ao carregar espaço:', error)

        if (active) {
          setErro('Não foi possível carregar este espaço.')
        }
      } finally {
        if (active) {
          setLoading(false)
        }
      }
    }

    load()

    return () => {
      active = false
    }
  }, [espacoID])

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator
          size="large"
          color={colors.primary}
          accessibilityLabel="Carregando espaço"
        />
      </View>
    )
  }

  if (erro) {
    return (
      <View style={styles.loadingContainer}>
        <Text
          style={styles.errorText}
          accessibilityRole="alert"
          accessibilityLiveRegion="assertive"
        >
          {erro}
        </Text>
        <Pressable
          style={styles.profileButton}
          onPress={() => router.push('/perfil')}
          accessibilityRole="button"
          accessibilityLabel="Abrir meu perfil"
        >
          <Text style={styles.profileButtonText}>Meu perfil</Text>
        </Pressable>
      </View>
    )
  }

  return (
    <View style={[styles.screen, isDesktop && styles.screenDesktop]}>
      <View style={[styles.nav, isDesktop && styles.navDesktop]}>
        <Pressable
          style={styles.exitButton}
          onPress={() => router.push('/espacos')}
          accessibilityRole="button"
          accessibilityLabel="Trocar de espaço"
        >
          <Text style={styles.exitButtonText}>← Trocar de espaço</Text>
        </Pressable>

        <View style={styles.espacoInfo}>
          <Text style={styles.espacoNome} numberOfLines={1}>
            {espaco?.nome ?? 'Carregando...'}
          </Text>
          <Text style={styles.espacoTipo}>
            {espaco?.tipo === 'ORGANIZACAO' ? 'Organização' : 'Pessoal'}
          </Text>
        </View>

        <Pressable
          style={styles.profileButton}
          onPress={() => router.push('/perfil')}
          accessibilityRole="button"
          accessibilityLabel="Abrir meu perfil"
          accessibilityHint="Abre suas configurações de perfil e acessibilidade"
        >
          <Text style={styles.profileButtonText}>Meu perfil</Text>
        </Pressable>

        <View style={[styles.navItems, isDesktop && styles.navItemsDesktop]}>
          {NAV_ITEMS.filter(
            (item) => !item.onlyOrg || espaco?.tipo === 'ORGANIZACAO',
          ).map((item) => {
            const href = item.getHref(espacoID)
            const active = pathname === href

            return (
              <Pressable
                key={item.key}
                style={[styles.navItem, active && styles.navItemActive]}
                onPress={() => router.push(href as any)}
                accessibilityRole="button"
                accessibilityState={{ selected: active }}
                accessibilityLabel={item.label}
              >
                <Text
                  style={[
                    styles.navItemText,
                    active && styles.navItemTextActive,
                  ]}
                >
                  {item.label}
                </Text>
              </Pressable>
            )
          })}
        </View>
      </View>

      <View style={styles.content}>
        <Slot />
      </View>
    </View>
  )
}
