import { router, useFocusEffect, useLocalSearchParams } from 'expo-router'
import { useCallback, useState } from 'react'
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native'

import { useResponsive } from '@/src/hooks/useResponsive'
import { colors } from '@/src/theme/colors'
import {
  listarInsumos,
  listarBaixoEstoque,
  listarVencendo,
  type Insumo,
  type LoteInsumo,
} from '@/src/services/insumoService'

function diasAteVencimento(dataValidade: string) {
  const hoje = new Date()
  const validade = new Date(dataValidade)
  const diffMs = validade.getTime() - hoje.getTime()
  return Math.ceil(diffMs / (1000 * 60 * 60 * 24))
}

export default function EspacoDashboardScreen() {
  const { espacoID } = useLocalSearchParams<{ espacoID: string }>()
  const { isDesktop } = useResponsive()

  const [totalInsumos, setTotalInsumos] = useState(0)
  const [baixoEstoque, setBaixoEstoque] = useState<Insumo[]>([])
  const [vencendo, setVencendo] = useState<LoteInsumo[]>([])
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    if (!espacoID) return

    try {
      const [todos, dataBaixoEstoque, dataVencendo] = await Promise.all([
        listarInsumos(espacoID),
        listarBaixoEstoque(espacoID),
        listarVencendo(espacoID, 7),
      ])

      setTotalInsumos(todos.length)
      setBaixoEstoque(dataBaixoEstoque)
      setVencendo(dataVencendo)
    } catch (error) {
      console.log('Erro ao carregar dashboard:', error)
    } finally {
      setLoading(false)
    }
  }, [espacoID])

  useFocusEffect(
    useCallback(() => {
      load()
    }, [load])
  )

  if (loading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    )
  }

  return (
    <View style={styles.screen}>
      <View style={[styles.content, isDesktop && styles.contentDesktop]}>
        <Text style={styles.title}>Visão geral</Text>
        <Text style={styles.subtitle}>
          Resumo do estoque e dos alertas deste espaço.
        </Text>

        {/* =====================================================
            cards
        ===================================================== */}

        <View style={[styles.statsRow, isDesktop && styles.statsRowDesktop]}>
          <Pressable
            style={[styles.statCard, isDesktop && styles.statCardDesktop]}
            onPress={() => router.push(`/espacos/${espacoID}/insumos`)}
          >
            <Text style={styles.statValue}>{totalInsumos}</Text>
            <Text style={styles.statLabel}>Insumos cadastrados</Text>
          </Pressable>

          <Pressable
            style={[
              styles.statCard,
              isDesktop && styles.statCardDesktop,
              baixoEstoque.length > 0 && styles.statCardWarning,
            ]}
            onPress={() => router.push(`/espacos/${espacoID}/insumos`)}
          >
            <Text
              style={[
                styles.statValue,
                baixoEstoque.length > 0 && styles.statValueWarning,
              ]}
            >
              {baixoEstoque.length}
            </Text>
            <Text style={styles.statLabel}>Com estoque baixo</Text>
          </Pressable>

          <Pressable
            style={[
              styles.statCard,
              isDesktop && styles.statCardDesktop,
              vencendo.length > 0 && styles.statCardWarning,
            ]}
            onPress={() => router.push(`/espacos/${espacoID}/insumos`)}
          >
            <Text
              style={[
                styles.statValue,
                vencendo.length > 0 && styles.statValueWarning,
              ]}
            >
              {vencendo.length}
            </Text>
            <Text style={styles.statLabel}>Lotes vencendo em 7 dias</Text>
          </Pressable>
        </View>

        {/* =====================================================
            LISTAS DE ALERTA
        ===================================================== */}

        <View style={[styles.panels, isDesktop && styles.panelsDesktop]}>
          <View style={[styles.panel, isDesktop && styles.panelDesktop]}>
            <Text style={styles.panelTitle}>Vencendo em breve</Text>

            {vencendo.length === 0 ? (
              <Text style={styles.panelEmpty}>
                Nenhum lote vencendo nos próximos 7 dias.
              </Text>
            ) : (
              vencendo.slice(0, 5).map((lote) => {
                const dias = diasAteVencimento(lote.dataValidade)

                return (
                  <View key={lote.id} style={styles.panelRow}>
                    <Text style={styles.panelRowNome} numberOfLines={1}>
                      {lote.insumo?.nome ?? 'Insumo'}
                    </Text>
                    <Text style={styles.panelRowInfo}>
                      {dias <= 0 ? 'Vencido' : `Vence em ${dias}d`}
                    </Text>
                  </View>
                )
              })
            )}
          </View>

          <View style={[styles.panel, isDesktop && styles.panelDesktop]}>
            <Text style={styles.panelTitle}>Estoque baixo</Text>

            {baixoEstoque.length === 0 ? (
              <Text style={styles.panelEmpty}>
                Nenhum insumo abaixo do estoque mínimo.
              </Text>
            ) : (
              baixoEstoque.slice(0, 5).map((insumo) => (
                <View key={insumo.id} style={styles.panelRow}>
                  <Text style={styles.panelRowNome} numberOfLines={1}>
                    {insumo.nome}
                  </Text>
                  <Text style={styles.panelRowInfo}>
                    {insumo.estoqueAtual} {insumo.unidadeMedida}
                  </Text>
                </View>
              ))
            )}
          </View>
        </View>
      </View>
    </View>
  )
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: colors.background,
  },

  center: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.background,
  },

  content: {
    padding: 20,
  },

  contentDesktop: {
    maxWidth: 900,
    width: '100%',
    alignSelf: 'center',
    paddingTop: 32,
  },

  title: {
    fontSize: 26,
    fontWeight: '800',
    color: colors.text,
  },

  subtitle: {
    fontSize: 14,
    color: colors.textMuted,
    marginTop: 4,
    marginBottom: 24,
  },

  statsRow: {
    gap: 12,
    marginBottom: 24,
  },

  statsRowDesktop: {
    flexDirection: 'row',
  },

  statCard: {
    backgroundColor: colors.surface,
    borderRadius: 16,
    padding: 20,
    borderWidth: 1,
    borderColor: colors.border,
    marginBottom: 12,
  },

  statCardDesktop: {
    flex: 1,
    marginBottom: 0,
  },

  statCardWarning: {
    borderColor: colors.warning,
    backgroundColor: colors.warningSoft,
  },

  statValue: {
    fontSize: 32,
    fontWeight: '800',
    color: colors.text,
  },

  statValueWarning: {
    color: colors.warning,
  },

  statLabel: {
    fontSize: 13,
    color: colors.textMuted,
    marginTop: 4,
  },

  panels: {
    gap: 16,
  },

  panelsDesktop: {
    flexDirection: 'row',
  },

  panel: {
    backgroundColor: colors.surface,
    borderRadius: 16,
    padding: 20,
    borderWidth: 1,
    borderColor: colors.border,
  },

  panelDesktop: {
    flex: 1,
  },

  panelTitle: {
    fontSize: 15,
    fontWeight: '700',
    color: colors.text,
    marginBottom: 14,
  },

  panelEmpty: {
    fontSize: 13,
    color: colors.textMuted,
  },

  panelRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: colors.background,
  },

  panelRowNome: {
    fontSize: 14,
    color: colors.text,
    flex: 1,
    marginRight: 8,
  },

  panelRowInfo: {
    fontSize: 12,
    fontWeight: '700',
    color: colors.secondary,
  },
})