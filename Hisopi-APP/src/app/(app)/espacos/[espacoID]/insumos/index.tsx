import { router, useFocusEffect, useLocalSearchParams } from 'expo-router'
import { useCallback, useMemo, useState } from 'react'
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native'

import { useResponsive } from '@/src/hooks/useResponsive'
import { colors } from '@/src/theme/colors'
import {
  listarInsumos,
  listarVencendo,
  type Insumo,
  type LoteInsumo,
} from '@/src/services/insumoService'

type StockLevel = 'ok' | 'low'

function getStockLevel(insumo: Insumo): StockLevel {
  return insumo.estoqueAtual <= insumo.estoqueMinimo ? 'low' : 'ok'
}

export default function InsumosScreen() {
  const { espacoID } = useLocalSearchParams<{ espacoID: string }>()
  const { isDesktop } = useResponsive()

  const [insumos, setInsumos] = useState<Insumo[]>([])
  const [lotesVencendo, setLotesVencendo] = useState<LoteInsumo[]>([])
  const [loading, setLoading] = useState(true)
  const [query, setQuery] = useState('')
  const [filtro, setFiltro] = useState<'todos' | 'baixo'>('todos')

  const load = useCallback(async () => {
    if (!espacoID) return

    try {
      const [dataInsumos, dataVencendo] = await Promise.all([
        listarInsumos(espacoID),
        listarVencendo(espacoID, 7),
      ])
      setInsumos(dataInsumos)
      setLotesVencendo(dataVencendo)
    } catch (error) {
      console.log('Erro ao carregar insumos:', error)
    } finally {
      setLoading(false)
    }
  }, [espacoID])

  useFocusEffect(
    useCallback(() => {
      load()
    }, [load])
  )

  const filtrados = useMemo(() => {
    const q = query.trim().toLowerCase()

    return insumos.filter((i) => {
      if (q && !i.nome.toLowerCase().includes(q)) return false
      if (filtro === 'baixo' && getStockLevel(i) !== 'low') return false
      return true
    })
  }, [insumos, query, filtro])

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
        <Pressable style={styles.backButton} onPress={() =>  router.push(`/espacos/${espacoID}`)}>
          <Text style={styles.backButtonText}>Voltar</Text>
        </Pressable>

        <View style={[styles.header, isDesktop && styles.headerDesktop]}>
          <View>
            <Text style={styles.title}>Insumos</Text>
            <Text style={styles.subtitle}>
              {insumos.length} {insumos.length === 1 ? 'insumo cadastrado' : 'insumos cadastrados'}
            </Text>
          </View>

          <Pressable
            style={styles.newButton}
            onPress={() => router.push(`/espacos/${espacoID}/insumos/adicionar`)}
          >
            <Text style={styles.newButtonText}>+ Novo insumo</Text>
          </Pressable>
        </View>

        {lotesVencendo.length > 0 && (
          <View style={styles.alertBanner}>
            <Text style={styles.alertBannerIcon}>⚠️</Text>
            <Text style={styles.alertBannerText}>
              {lotesVencendo.length}{' '}
              {lotesVencendo.length === 1 ? 'lote vence' : 'lotes vencem'} nos
              próximos 7 dias
            </Text>
          </View>
        )}

        <View style={[styles.toolbar, isDesktop && styles.toolbarDesktop]}>
          <TextInput
            value={query}
            onChangeText={setQuery}
            placeholder="Buscar insumos..."
            placeholderTextColor={colors.textMuted}
            style={styles.search}
          />

          <View style={styles.chips}>
            <Pressable
              style={[styles.chip, filtro === 'todos' && styles.chipActive]}
              onPress={() => setFiltro('todos')}
            >
              <Text
                style={[styles.chipText, filtro === 'todos' && styles.chipTextActive]}
              >
                Todos
              </Text>
            </Pressable>

            <Pressable
              style={[styles.chip, filtro === 'baixo' && styles.chipActive]}
              onPress={() => setFiltro('baixo')}
            >
              <Text
                style={[styles.chipText, filtro === 'baixo' && styles.chipTextActive]}
              >
                Estoque baixo
              </Text>
            </Pressable>
          </View>
        </View>

        {filtrados.length === 0 ? (
          <View style={styles.empty}>
            <Text style={styles.emptyTitle}>Nenhum insumo encontrado</Text>
            <Text style={styles.emptyText}>
              {insumos.length === 0
                ? 'Cadastre seu primeiro insumo para começar a controlar o estoque.'
                : 'Tente ajustar a busca ou o filtro.'}
            </Text>
          </View>
        ) : (
          <FlatList
            data={filtrados}
            key={isDesktop ? 'desktop' : 'mobile'}
            keyExtractor={(item) => String(item.id)}
            numColumns={isDesktop ? 3 : 1}
            columnWrapperStyle={isDesktop ? styles.row : undefined}
            scrollEnabled={false}
            renderItem={({ item }) => {
              const nivel = getStockLevel(item)

              return (
                <Pressable
                  style={[styles.card, isDesktop && styles.cardDesktop]}
                  onPress={() =>
                    router.push(`/espacos/${espacoID}/insumos/${item.id}`)
                  }
                >
                  <Text style={styles.cardNome} numberOfLines={1}>
                    {item.nome}
                  </Text>

                  {item.categoria && (
                    <Text style={styles.cardCategoria}>{item.categoria}</Text>
                  )}

                  <Text style={styles.cardQuantidade}>
                    {item.estoqueAtual} {item.unidadeMedida}
                  </Text>

                  <View
                    style={[
                      styles.badge,
                      nivel === 'low' ? styles.badgeWarning : styles.badgeSuccess,
                    ]}
                  >
                    <Text
                      style={[
                        styles.badgeText,
                        nivel === 'low' ? styles.badgeTextWarning : styles.badgeTextSuccess,
                      ]}
                    >
                      {nivel === 'low' ? 'Estoque baixo' : 'Estoque ok'}
                    </Text>
                  </View>
                </Pressable>
              )
            }}
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

  backButton: {
    alignSelf: 'flex-start',
    backgroundColor: colors.text,
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderRadius: 12,
    marginBottom: 20,
  },

  backButtonText: {
    color: '#fff',
    fontWeight: '700',
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

  alertBanner: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    backgroundColor: colors.warningSoft,
    padding: 12,
    borderRadius: 12,
    marginBottom: 16,
  },

  alertBannerIcon: {
    fontSize: 16,
  },

  alertBannerText: {
    color: colors.warning,
    fontWeight: '700',
    fontSize: 13,
  },

  toolbar: {
    marginBottom: 20,
    gap: 12,
  },

  toolbarDesktop: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },

  search: {
    backgroundColor: colors.surface,
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 12,
    fontSize: 14,
    color: colors.text,
  },

  chips: {
    flexDirection: 'row',
    gap: 8,
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

  cardCategoria: {
    fontSize: 12,
    color: colors.textMuted,
    marginTop: 2,
  },

  cardQuantidade: {
    fontSize: 14,
    color: colors.text,
    marginTop: 8,
    marginBottom: 10,
  },

  badge: {
    alignSelf: 'flex-start',
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 999,
  },

  badgeSuccess: {
    backgroundColor: colors.successSoft,
  },

  badgeWarning: {
    backgroundColor: colors.warningSoft,
  },

  badgeText: {
    fontSize: 11,
    fontWeight: '700',
  },

  badgeTextSuccess: {
    color: colors.success,
  },

  badgeTextWarning: {
    color: colors.warning,
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