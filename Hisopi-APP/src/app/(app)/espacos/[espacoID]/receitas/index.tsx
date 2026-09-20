import { router, useFocusEffect, useLocalSearchParams } from 'expo-router'
import { useCallback, useMemo, useState } from 'react'
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native'

import { useResponsive } from '@/src/hooks/useResponsive'
import { colors } from '@/src/theme/colors'
import { listarReceitas, type Receita, type TipoReceita } from '@/src/services/receitaService'

const TIPO_LABEL: Record<TipoReceita, string> = {
  PRODUTO_VENDA: 'Ficha técnica',
  SUGESTAO_CONSUMO: 'Sugestão de consumo',
}

export default function ReceitasScreen() {
  const { espacoID } = useLocalSearchParams<{ espacoID: string }>()
  const { isDesktop } = useResponsive()

  const [receitas, setReceitas] = useState<Receita[]>([])
  const [loading, setLoading] = useState(true)
  const [filtro, setFiltro] = useState<'todos' | TipoReceita>('todos')

  const load = useCallback(async () => {
    if (!espacoID) return

    try {
      const data = await listarReceitas(espacoID)
      setReceitas(data)
    } catch (error) {
      console.log('Erro ao carregar receitas:', error)
    } finally {
      setLoading(false)
    }
  }, [espacoID])

  useFocusEffect(
    useCallback(() => {
      load()
    }, [load])
  )

  const filtradas = useMemo(() => {
    if (filtro === 'todos') return receitas
    return receitas.filter((r) => r.tipo === filtro)
  }, [receitas, filtro])

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
        <View style={[styles.header, isDesktop && styles.headerDesktop]}>
          <View>
            <Text style={styles.title}>Receitas</Text>
            <Text style={styles.subtitle}>
              {receitas.length} {receitas.length === 1 ? 'receita cadastrada' : 'receitas cadastradas'}
            </Text>
          </View>

          <Pressable
            style={styles.newButton}
            onPress={() => router.push(`/espacos/${espacoID}/receitas/adicionar` as any)}
          >
            <Text style={styles.newButtonText}>+ Nova receita</Text>
          </Pressable>
        </View>

        <View style={styles.chips}>
          <Pressable
            style={[styles.chip, filtro === 'todos' && styles.chipActive]}
            onPress={() => setFiltro('todos')}
          >
            <Text style={[styles.chipText, filtro === 'todos' && styles.chipTextActive]}>
              Todas
            </Text>
          </Pressable>

          <Pressable
            style={[styles.chip, filtro === 'PRODUTO_VENDA' && styles.chipActive]}
            onPress={() => setFiltro('PRODUTO_VENDA')}
          >
            <Text
              style={[styles.chipText, filtro === 'PRODUTO_VENDA' && styles.chipTextActive]}
            >
              Fichas técnicas
            </Text>
          </Pressable>

          <Pressable
            style={[styles.chip, filtro === 'SUGESTAO_CONSUMO' && styles.chipActive]}
            onPress={() => setFiltro('SUGESTAO_CONSUMO')}
          >
            <Text
              style={[styles.chipText, filtro === 'SUGESTAO_CONSUMO' && styles.chipTextActive]}
            >
              Sugestões
            </Text>
          </Pressable>
        </View>

        {filtradas.length === 0 ? (
          <View style={styles.empty}>
            <Text style={styles.emptyTitle}>Nenhuma receita encontrada</Text>
            <Text style={styles.emptyText}>
              {receitas.length === 0
                ? 'Cadastre sua primeira receita para começar a vincular insumos.'
                : 'Tente ajustar o filtro.'}
            </Text>
          </View>
        ) : (
          <FlatList
            data={filtradas}
            key={isDesktop ? 'desktop' : 'mobile'}
            keyExtractor={(item) => String(item.id)}
            numColumns={isDesktop ? 3 : 1}
            columnWrapperStyle={isDesktop ? styles.row : undefined}
            scrollEnabled={false}
            renderItem={({ item }) => (
              <Pressable
                style={[styles.card, isDesktop && styles.cardDesktop]}
                onPress={() =>
                  router.push(`/espacos/${espacoID}/receitas/${item.id}`)
                }
              >
                <Text style={styles.cardNome} numberOfLines={1}>
                  {item.nome}
                </Text>

                {item.modoPreparo && (
                  <Text style={styles.cardPreparo} numberOfLines={2}>
                    {item.modoPreparo}
                  </Text>
                )}

                <View
                  style={[
                    styles.badge,
                    item.tipo === 'PRODUTO_VENDA'
                      ? styles.badgeVenda
                      : styles.badgeSugestao,
                  ]}
                >
                  <Text
                    style={[
                      styles.badgeText,
                      item.tipo === 'PRODUTO_VENDA'
                        ? styles.badgeTextVenda
                        : styles.badgeTextSugestao,
                    ]}
                  >
                    {TIPO_LABEL[item.tipo]}
                  </Text>
                </View>
              </Pressable>
            )}
          />
        )}
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
    maxWidth: 1000,
    width: '100%',
    alignSelf: 'center',
    paddingTop: 32,
  },

  header: {
    marginBottom: 16,
  },

  headerDesktop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-end',
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
  },

  newButton: {
    backgroundColor: colors.primary,
    paddingVertical: 12,
    paddingHorizontal: 18,
    borderRadius: 12,
    marginTop: 16,
    alignSelf: 'flex-start',
  },

  newButtonText: {
    color: '#fff',
    fontWeight: '700',
    fontSize: 14,
  },

  chips: {
    flexDirection: 'row',
    gap: 8,
    marginBottom: 20,
  },

  chip: {
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 999,
    backgroundColor: colors.surface,
    borderWidth: 1,
    borderColor: colors.border,
  },

  chipActive: {
    backgroundColor: colors.text,
    borderColor: colors.text,
  },

  chipText: {
    color: colors.textMuted,
    fontWeight: '600',
    fontSize: 13,
  },

  chipTextActive: {
    color: '#fff',
  },

  row: {
    gap: 16,
    justifyContent: 'flex-start',
  },

  card: {
    backgroundColor: colors.surface,
    borderRadius: 16,
    padding: 16,
    borderWidth: 1,
    borderColor: colors.border,
    marginBottom: 14,
  },

  cardDesktop: {
    flex: 1,
    maxWidth: '32%',
  },

  cardNome: {
    fontSize: 16,
    fontWeight: '700',
    color: colors.text,
  },

  cardPreparo: {
    fontSize: 12,
    color: colors.textMuted,
    marginTop: 6,
    marginBottom: 12,
    lineHeight: 17,
  },

  badge: {
    alignSelf: 'flex-start',
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 999,
    marginTop: 8,
  },

  badgeVenda: {
    backgroundColor: colors.accentSoft,
  },

  badgeSugestao: {
    backgroundColor: colors.successSoft,
  },

  badgeText: {
    fontSize: 11,
    fontWeight: '700',
  },

  badgeTextVenda: {
    color: colors.primary,
  },

  badgeTextSugestao: {
    color: colors.success,
  },

  empty: {
    alignItems: 'center',
    paddingVertical: 60,
  },

  emptyTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: colors.text,
    marginBottom: 6,
  },

  emptyText: {
    fontSize: 14,
    color: colors.textMuted,
    textAlign: 'center',
    paddingHorizontal: 24,
  },
})