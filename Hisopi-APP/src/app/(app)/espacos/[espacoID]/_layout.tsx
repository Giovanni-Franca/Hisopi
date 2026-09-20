import { router, Slot, usePathname, useLocalSearchParams } from 'expo-router'
import { useEffect, useState } from 'react'
import { ActivityIndicator, Pressable, StyleSheet, Text, View } from 'react-native'

import { useResponsive } from '@/src/hooks/useResponsive'
import { colors } from '@/src/theme/colors'
import { buscarEspaco, type Espaco } from '@/src/services/espacoService'

type NavItem = {
  key: string
  label: string
  getHref: (espacoID: string) => string
}

const NAV_ITEMS: NavItem[] = [
  { key: 'dashboard', label: 'Dashboard', getHref: (id) => `/${id}` },
  { key: 'insumos', label: 'Insumos', getHref: (id) => `/${id}/insumos` },
  { key: 'receitas', label: 'Receitas', getHref: (id) => `/${id}/receitas` },
]

export default function EspacoLayout() {
  const { espacoID } = useLocalSearchParams<{ espacoID: string }>()
  const pathname = usePathname()
  const { isDesktop } = useResponsive()

  const [espaco, setEspaco] = useState<Espaco | null>(null)
  const [loading, setLoading] = useState(true)
  const [erro, setErro] = useState('')

  useEffect(() => {
    async function load() {
      if (!espacoID) {
        setErro('Não foi possível identificar o espaço na URL.')
        setLoading(false)
        return
      }

      try {
        const data = await buscarEspaco(espacoID)
        setEspaco(data)
      } catch (error) {
        console.log('Erro ao carregar espaço:', error)
        setErro('Não foi possível carregar este espaço.')
      } finally {
        setLoading(false)
      }
    }

    load()
  }, [espacoID])

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    )
  }

  if (erro) {
    return (
      <View style={styles.loadingContainer}>
        <Text style={{ color: colors.danger, textAlign: 'center', paddingHorizontal: 24 }}>
          {erro}
        </Text>
      </View>
    )
  }

  return (
    <View style={[styles.screen, isDesktop && styles.screenDesktop]}>
      {/* =====================================================
          NAVEGAÇÃO DO ESPAÇO (sidebar no desktop, barra no mobile)
      ===================================================== */}

      <View style={[styles.nav, isDesktop && styles.navDesktop]}>
        <Pressable
          style={styles.exitButton}
          onPress={() => router.push('/espacos')}
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

        <View style={[styles.navItems, isDesktop && styles.navItemsDesktop]}>
          {NAV_ITEMS.map((item) => {
            const href = item.getHref(espacoID)
            const active = pathname === href

            return (
              <Pressable
                key={item.key}
                style={[styles.navItem, active && styles.navItemActive]}
                onPress={() => router.push(href as any)}
              >
                <Text
                  style={[styles.navItemText, active && styles.navItemTextActive]}
                >
                  {item.label}
                </Text>
              </Pressable>
            )
          })}
        </View>
      </View>

      {/* =====================================================
          CONTEÚDO DA SEÇÃO ATIVA
      ===================================================== */}

      <View style={styles.content}>
        <Slot />
      </View>
    </View>
  )
}

const styles = StyleSheet.create({
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

  /*
   * NAVEGAÇÃO
   *
   * Mobile: barra horizontal no topo, com as abas rolando se
   * necessário.
   * Desktop: vira uma sidebar fixa à esquerda.
   */
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
    marginBottom: 16,
  },

  exitButtonText: {
    fontSize: 13,
    color: colors.textMuted,
    fontWeight: '600',
  },

  espacoInfo: {
    marginBottom: 20,
  },

  espacoNome: {
    fontSize: 18,
    fontWeight: '800',
    color: colors.text,
  },

  espacoTipo: {
    fontSize: 12,
    color: colors.primary,
    fontWeight: '700',
    marginTop: 2,
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
  },

  navItemActive: {
    backgroundColor: colors.accentSoft,
  },

  navItemText: {
    fontSize: 14,
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
})