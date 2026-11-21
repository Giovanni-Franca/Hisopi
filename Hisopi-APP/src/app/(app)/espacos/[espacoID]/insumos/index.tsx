import { router, useFocusEffect, useLocalSearchParams } from 'expo-router'
import { useCallback, useMemo, useState } from 'react'
import { useAccessibility } from '@/src/context/AccessibilityContext'
import { SelectInput } from '@/src/components/ui/SelectInput'
import {
  ActivityIndicator,
  FlatList,
  Modal,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
  ScrollView
} from 'react-native'

import { useResponsive } from '@/src/hooks/useResponsive'
import {
  listarInsumos,
  listarVencendo,
  desativarInsumo,
  editarInsumo,
  labelCategoria,
  CATEGORIAS, 
  Categoria,
  UNIDADES_MEDIDA,
  UnidadeMedida,
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

  const { colors, fontScale } = useAccessibility()

  const styles = useMemo(
    () => createStyles(colors, fontScale),
    [colors, fontScale],
  )

  const [insumos, setInsumos] = useState<Insumo[]>([])
  const [lotesVencendo, setLotesVencendo] = useState<LoteInsumo[]>([])
  const [loading, setLoading] = useState(true)
  const [query, setQuery] = useState('')
  const [filtro, setFiltro] = useState<'todos' | 'baixo'>('todos')
  const [insumoParaRemover, setInsumoParaRemover] = useState<Insumo | null>(null)
  const [removendo, setRemovendo] = useState(false)
  const [erroRemocao, setErroRemocao] = useState<string | null>(null)
  const [menuAbertoId, setMenuAbertoId] = useState<number | null>(null)
  const [insumoParaEditar, setInsumoParaEditar] = useState<Insumo | null>(null)
  const [unidadeMedida, setUnidadeMedida] = useState<UnidadeMedida | null>(null)
  const [editCategoria, setEditCategoria] = useState<Categoria | null>(null)  
  const [editNome, setEditNome] = useState('')
  const [editEstoqueMin, setEditEstoqueMin] = useState('')
  const [editCusto, setEditCusto] = useState('')
  const [salvando, setSalvando] = useState(false)
  const [erroEdicao, setErroEdicao] = useState<string | null>(null)

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

  const fecharModalRemocao = () => {
    if (removendo) return
    setInsumoParaRemover(null)
    setErroRemocao(null)
  }

  const confirmarRemocao = async () => {
    if (!insumoParaRemover || !espacoID) return

    try {
      setRemovendo(true)
      setErroRemocao(null)
      await desativarInsumo(espacoID, insumoParaRemover.id)
      setInsumoParaRemover(null)
      await load()
    } catch (error) {
      console.log('Erro ao remover insumo:', error)
      setErroRemocao('Não foi possível remover o insumo. Tente novamente.')
    } finally {
      setRemovendo(false)
    }
  }

  const abrirEdicao = (item: Insumo) => {
    setMenuAbertoId(null)
    setInsumoParaEditar(item)
    setEditNome(item.nome)
    setUnidadeMedida((item.unidadeMedida as UnidadeMedida) ?? null)
    setEditCategoria((item.categoria as Categoria) ?? null)
    setEditEstoqueMin(String(item.estoqueMinimo ?? 0))
    setEditCusto(String(item.custoUnitario ?? 0))
    setErroEdicao(null)
  }

  const abrirRemocao = (item: Insumo) => {
    setMenuAbertoId(null)
    setInsumoParaRemover(item)
  }

  const fecharModalEdicao = () => {
    if (salvando) return
    setInsumoParaEditar(null)
    setErroEdicao(null)
  }

  const parseNumero = (valor: string) =>
    Number(valor.trim().replace(',', '.'))

  const salvarEdicao = async () => {
    if (!insumoParaEditar || !espacoID) return

    const estoqueMinimo = parseNumero(editEstoqueMin)
    const custoUnitario = parseNumero(editCusto)

    if (!editNome.trim()) {
      setErroEdicao('Informe o nome do insumo.')
      return
    }
    if (!unidadeMedida) {
      setErroEdicao('Selecione a unidade de medida.')
      return
    }
    if (Number.isNaN(estoqueMinimo) || Number.isNaN(custoUnitario)) {
      setErroEdicao('Estoque mínimo e custo devem ser números válidos.')
      return
    }

    try {
      setSalvando(true)
      setErroEdicao(null)
      await editarInsumo(espacoID, insumoParaEditar.id, {
        nome: editNome.trim(),
        categoria: editCategoria,
        unidadeMedida,
        estoqueMinimo,
        custoUnitario,
      } as any)
      setInsumoParaEditar(null)
      await load()
    } catch (error) {
      console.log('Erro ao editar insumo:', error)
      setErroEdicao('Não foi possível salvar as alterações. Tente novamente.')
    } finally {
      setSalvando(false)
    }
  }

  if (loading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    )
  }

  return (
    <ScrollView style={styles.screen} keyboardShouldPersistTaps="handled">
      <View style={[styles.content, isDesktop && styles.contentDesktop]}>

        <View style={[styles.header, isDesktop && styles.headerDesktop]}>
          <View>
            <Text style={styles.title}>Insumos</Text>
            <Text style={styles.subtitle}>
              {insumos.length} {insumos.length === 1 ? 'insumo cadastrado' : 'insumos cadastrados'}
            </Text>
          </View>

          <Pressable
            style={styles.newButton}
            onPress={() => router.push(`/espacos/${espacoID}/insumos/adicionar` as any)}
          >
            <Text style={styles.newButtonText}>+ Novo insumo</Text>
          </Pressable>
        </View>

        {lotesVencendo.length > 0 && (
          <View style={styles.alertBanner}>
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
            scrollEnabled={true}
            renderItem={({ item }) => {
              const nivel = getStockLevel(item)

              return (
                <Pressable
                  style={[
                    styles.card,
                    isDesktop && styles.cardDesktop,
                    menuAbertoId === item.id && styles.cardMenuAberto,
                  ]}
                  onPress={() => {
                    setMenuAbertoId(null)
                    router.push(`/espacos/${espacoID}/insumos/${item.id}` as any)
                  }}
                >
                  <Pressable
                    style={styles.menuButton}
                    hitSlop={8}
                    onPress={() =>
                      setMenuAbertoId(menuAbertoId === item.id ? null : item.id)
                    }
                    accessibilityRole="button"
                    accessibilityLabel={`Opções do insumo ${item.nome}`}
                  >
                    <Text style={styles.menuButtonText}>⋮</Text>
                  </Pressable>

                  {menuAbertoId === item.id && (
                    <View style={styles.dropdown}>
                      <Pressable style={styles.dropdownItem} onPress={() => abrirEdicao(item)}>
                        <Text style={styles.dropdownText}>Editar</Text>
                      </Pressable>

                      <View style={styles.dropdownDivider} />

                      <Pressable style={styles.dropdownItem} onPress={() => abrirRemocao(item)}>
                        <Text style={[styles.dropdownText, styles.dropdownTextDanger]}>
                          Remover
                        </Text>
                      </Pressable>
                    </View>
                  )}

                  <Text style={styles.cardNome} numberOfLines={1}>
                    {item.nome}
                  </Text>

                  {item.categoria && (
                    <Text style={styles.cardCategoria}>{labelCategoria(item.categoria)}</Text>
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
      <Modal
        visible={!!insumoParaRemover}
        transparent
        animationType="fade"
        onRequestClose={fecharModalRemocao}
      >
        <Pressable style={styles.modalOverlay} onPress={fecharModalRemocao}>
          <Pressable style={styles.modalBox} onPress={() => {}}>
            <Text style={styles.modalTitle}>Remover insumo?</Text>
            <Text style={styles.modalText}>
              “{insumoParaRemover?.nome}” será removido da lista de insumos.
            </Text>

            {erroRemocao && <Text style={styles.modalError}>{erroRemocao}</Text>}

            <View style={styles.modalActions}>
              <Pressable
                style={styles.modalCancel}
                onPress={fecharModalRemocao}
                disabled={removendo}
              >
                <Text style={styles.modalCancelText}>Cancelar</Text>
              </Pressable>

              <Pressable
                style={[styles.modalDelete, removendo && { opacity: 0.6 }]}
                onPress={confirmarRemocao}
                disabled={removendo}
              >
                {removendo ? (
                  <ActivityIndicator size="small" color="#fff" />
                ) : (
                  <Text style={styles.modalDeleteText}>Remover</Text>
                )}
              </Pressable>
            </View>
          </Pressable>
        </Pressable>
      </Modal>
      <Modal
        visible={!!insumoParaEditar}
        transparent
        animationType="fade"
        onRequestClose={fecharModalEdicao}
      >
        <Pressable style={styles.modalOverlay} onPress={fecharModalEdicao}>
          <Pressable style={styles.modalBox} onPress={() => {}}>
            <Text style={styles.modalTitle}>Editar insumo</Text>

            <Text style={styles.modalLabel}>Nome</Text>
            <TextInput
              value={editNome}
              onChangeText={setEditNome}
              style={styles.modalInput}
              placeholder="Nome do insumo"
              placeholderTextColor={colors.textMuted}
            />

            <SelectInput
              label="Unidade de medida"
              placeholder="Selecione a unidade"
              options={UNIDADES_MEDIDA}
              value={unidadeMedida}
              onChange={setUnidadeMedida}
              allowClear={false}
            />

            <Text style={styles.modalLabel}>Estoque mínimo</Text>
            <TextInput
              value={editEstoqueMin}
              onChangeText={setEditEstoqueMin}
              style={styles.modalInput}
              keyboardType="decimal-pad"
              placeholderTextColor={colors.textMuted}
            />

            <Text style={styles.modalLabel}>Custo unitário (R$)</Text>
            <TextInput
              value={editCusto}
              onChangeText={setEditCusto}
              style={styles.modalInput}
              keyboardType="decimal-pad"
              placeholderTextColor={colors.textMuted}
            />

            {erroEdicao && <Text style={styles.modalError}>{erroEdicao}</Text>}

            <View style={styles.modalActions}>
              <Pressable
                style={styles.modalCancel}
                onPress={fecharModalEdicao}
                disabled={salvando}
              >
                <Text style={styles.modalCancelText}>Cancelar</Text>
              </Pressable>

              <Pressable
                style={[styles.modalSave, salvando && { opacity: 0.6 }]}
                onPress={salvarEdicao}
                disabled={salvando}
              >
                {salvando ? (
                  <ActivityIndicator size="small" color="#fff" />
                ) : (
                  <Text style={styles.modalSaveText}>Salvar</Text>
                )}
              </Pressable>
            </View>
          </Pressable>
        </Pressable>
      </Modal>
    </ScrollView>
  )
}

function createStyles(
  colors: typeof import('@/src/theme/colors').colors,
  fontScale: number,
) {
  return StyleSheet.create({
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
      flexDirection: 'row',
      justifyContent: 'space-between',
      marginBottom: 16,
      gap: 12,
    },
    headerDesktop: {
      flexDirection: 'row',
      justifyContent: 'space-between',
      alignItems: 'flex-end',
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
      fontSize: 14 * fontScale,
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
      fontSize: 16 * fontScale,
    },
    alertBannerText: {
      color: colors.warning,
      fontWeight: '700',
      fontSize: 13 * fontScale,
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
      fontSize: 14 * fontScale,
      color: colors.text,
    },
    chips: {
      flexDirection: 'row',
      gap: 8,
      flexWrap: 'wrap',
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
      fontSize: 13 * fontScale,
    },
    chipTextActive: {
      color: colors.background,
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
      fontSize: 16 * fontScale,
      fontWeight: '700',
      color: colors.text,
    },
    cardCategoria: {
      fontSize: 12 * fontScale,
      color: colors.textMuted,
      marginTop: 2,
    },
    cardQuantidade: {
      fontSize: 14 * fontScale,
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
      fontSize: 11 * fontScale,
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
      fontSize: 16 * fontScale,
      fontWeight: '700',
      color: colors.text,
      marginBottom: 6,
    },
    emptyText: {
      fontSize: 14 * fontScale,
      color: colors.textMuted,
      textAlign: 'center',
      paddingHorizontal: 24,
    },

    menuButton: {
      position: 'absolute',
      top: 8,
      right: 8,
      width: 28,
      height: 28,
      borderRadius: 14,
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 1,
    },
    menuButtonText: {
      fontSize: 20 * fontScale,
      fontWeight: '800',
      color: colors.textMuted,
      lineHeight: 22 * fontScale,
    },
    modalOverlay: {
      flex: 1,
      backgroundColor: 'rgba(0,0,0,0.5)',
      alignItems: 'center',
      justifyContent: 'center',
      padding: 24,
    },
    modalBox: {
      width: '100%',
      maxWidth: 400,
      backgroundColor: colors.surface,
      borderRadius: 16,
      padding: 20,
      borderWidth: 1,
      borderColor: colors.border,
    },
    modalTitle: {
      fontSize: 18 * fontScale,
      fontWeight: '800',
      color: colors.text,
      marginBottom: 8,
    },
    modalText: {
      fontSize: 14 * fontScale,
      color: colors.textMuted,
      marginBottom: 16,
    },
    modalError: {
      fontSize: 13 * fontScale,
      color: colors.danger,
      fontWeight: '600',
      marginBottom: 12,
    },
    modalActions: {
      flexDirection: 'row',
      justifyContent: 'flex-end',
      gap: 10,
    },
    modalCancel: {
      paddingVertical: 10,
      paddingHorizontal: 16,
      borderRadius: 10,
      borderWidth: 1,
      borderColor: colors.border,
    },
    modalCancelText: {
      color: colors.text,
      fontWeight: '600',
      fontSize: 14 * fontScale,
    },
    modalDelete: {
      paddingVertical: 10,
      paddingHorizontal: 16,
      borderRadius: 10,
      backgroundColor: colors.danger,
      minWidth: 90,
      alignItems: 'center',
    },
    modalDeleteText: {
      color: '#fff',
      fontWeight: '700',
      fontSize: 14 * fontScale,
    },
    cardMenuAberto: {
      zIndex: 10,
    },
    dropdown: {
      position: 'absolute',
      top: 38,
      right: 8,
      minWidth: 130,
      backgroundColor: colors.surface,
      borderRadius: 10,
      borderWidth: 1,
      borderColor: colors.border,
      zIndex: 20,
      elevation: 6,
      shadowColor: '#000',
      shadowOpacity: 0.15,
      shadowRadius: 8,
      shadowOffset: { width: 0, height: 4 },
      overflow: 'hidden',
    },
    dropdownItem: {
      paddingVertical: 10,
      paddingHorizontal: 14,
    },
    dropdownText: {
      fontSize: 14 * fontScale,
      fontWeight: '600',
      color: colors.text,
    },
    dropdownTextDanger: {
      color: colors.danger,
    },
    dropdownDivider: {
      height: 1,
      backgroundColor: colors.border,
    },
    modalLabel: {
      fontSize: 13 * fontScale,
      fontWeight: '600',
      color: colors.textMuted,
      marginBottom: 4,
    },
    modalInput: {
      backgroundColor: colors.background,
      borderWidth: 1,
      borderColor: colors.border,
      borderRadius: 10,
      paddingHorizontal: 12,
      paddingVertical: 10,
      fontSize: 14 * fontScale,
      color: colors.text,
      marginBottom: 12,
    },
    modalSave: {
      paddingVertical: 10,
      paddingHorizontal: 16,
      borderRadius: 10,
      backgroundColor: colors.primary,
      minWidth: 90,
      alignItems: 'center',
    },
    modalSaveText: {
      color: '#fff',
      fontWeight: '700',
      fontSize: 14 * fontScale,
    },
  })
}
