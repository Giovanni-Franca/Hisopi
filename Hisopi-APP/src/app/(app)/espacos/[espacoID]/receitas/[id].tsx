import { router, useFocusEffect, useLocalSearchParams } from 'expo-router'
import { useCallback, useMemo, useState } from 'react'
import {
  ActivityIndicator,
  FlatList,
  Modal,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native'

import { AuthInput } from '@/src/components/auth/AuthInput'
import { colors } from '@/src/theme/colors'
import { listarInsumos, type Insumo } from '@/src/services/insumoService'
import {
  listarReceitas,
  listarFichaTecnica,
  vincularInsumo,
  removerVinculo,
  type Receita,
  type ReceitaInsumo,
  type TipoReceita,
} from '@/src/services/receitaService'

const TIPO_LABEL: Record<TipoReceita, string> = {
  PRODUTO_VENDA: 'Ficha técnica',
  SUGESTAO_CONSUMO: 'Sugestão de consumo',
}

export default function ReceitaDetalheScreen() {
  const { espacoID, id } = useLocalSearchParams<{ espacoID: string; id: string }>()

  const [receita, setReceita] = useState<Receita | null>(null)
  const [fichaTecnica, setFichaTecnica] = useState<ReceitaInsumo[]>([])
  const [insumosDisponiveis, setInsumosDisponiveis] = useState<Insumo[]>([])
  const [loading, setLoading] = useState(true)

  const [modalVisible, setModalVisible] = useState(false)
  const [insumoSelecionado, setInsumoSelecionado] = useState<Insumo | null>(null)
  const [quantidade, setQuantidade] = useState('')
  const [erroModal, setErroModal] = useState('')
  const [saving, setSaving] = useState(false)

  const load = useCallback(async () => {
    if (!espacoID || !id) return

    try {
      const [todasReceitas, dataFicha, dataInsumos] = await Promise.all([
        listarReceitas(espacoID),
        listarFichaTecnica(espacoID, Number(id)),
        listarInsumos(espacoID),
      ])

      setReceita(todasReceitas.find((r) => r.id === Number(id)) ?? null)
      setFichaTecnica(dataFicha)
      setInsumosDisponiveis(dataInsumos)
    } catch (error) {
      console.log('Erro ao carregar receita:', error)
    } finally {
      setLoading(false)
    }
  }, [espacoID, id])

  useFocusEffect(
    useCallback(() => {
      load()
    }, [load])
  )

  // Insumos que ainda não estão vinculados a esta receita — evita
  // deixar escolher o mesmo insumo duas vezes na mesma ficha técnica.
  const insumosParaVincular = useMemo(() => {
    const idsVinculados = new Set(fichaTecnica.map((f) => f.insumo.id))
    return insumosDisponiveis.filter((i) => !idsVinculados.has(i.id))
  }, [insumosDisponiveis, fichaTecnica])

  function abrirModal() {
    setInsumoSelecionado(null)
    setQuantidade('')
    setErroModal('')
    setModalVisible(true)
  }

  async function handleVincular() {
    setErroModal('')

    if (!insumoSelecionado) {
      setErroModal('Selecione um insumo.')
      return
    }

    const quantidadeNum = Number(quantidade.replace(',', '.'))

    if (!quantidade.trim() || Number.isNaN(quantidadeNum) || quantidadeNum <= 0) {
      setErroModal('Informe uma quantidade válida.')
      return
    }

    try {
      setSaving(true)

      await vincularInsumo(espacoID, Number(id), {
        idInsumo: insumoSelecionado.id,
        quantidadePorUnidade: quantidadeNum,
      })

      setModalVisible(false)
      await load()
    } catch (error) {
      setErroModal(
        error instanceof Error
          ? error.message
          : 'Não foi possível vincular o insumo.'
      )
    } finally {
      setSaving(false)
    }
  }

  async function handleRemover(idReceitaInsumo: number) {
    try {
      await removerVinculo(espacoID, Number(id), idReceitaInsumo)
      await load()
    } catch (error) {
      console.log('Erro ao remover vínculo:', error)
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
    <View style={styles.screen}>
      <View style={styles.content}>
        <Pressable
          style={styles.backButton}
          onPress={() => router.push(`/espacos/${espacoID}/receitas` as any)}
        >
          <Text style={styles.backButtonText}>Voltar</Text>
        </Pressable>

        <Text style={styles.title}>{receita?.nome ?? 'Receita'}</Text>

        {receita && (
          <View
            style={[
              styles.badge,
              receita.tipo === 'PRODUTO_VENDA'
                ? styles.badgeVenda
                : styles.badgeSugestao,
            ]}
          >
            <Text
              style={[
                styles.badgeText,
                receita.tipo === 'PRODUTO_VENDA'
                  ? styles.badgeTextVenda
                  : styles.badgeTextSugestao,
              ]}
            >
              {TIPO_LABEL[receita.tipo]}
            </Text>
          </View>
        )}

        {receita?.modoPreparo && (
          <View style={styles.preparoCard}>
            <Text style={styles.preparoTitle}>Modo de preparo</Text>
            <Text style={styles.preparoTexto}>{receita.modoPreparo}</Text>
          </View>
        )}

        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>Ficha técnica</Text>

          <Pressable style={styles.addButton} onPress={abrirModal}>
            <Text style={styles.addButtonText}>+ Adicionar insumo</Text>
          </Pressable>
        </View>

        <FlatList
          data={fichaTecnica}
          keyExtractor={(item) => String(item.id)}
          scrollEnabled={false}
          ItemSeparatorComponent={() => <View style={{ height: 10 }} />}
          ListEmptyComponent={
            <View style={styles.empty}>
              <Text style={styles.emptyText}>
                Nenhum insumo vinculado ainda. Adicione acima para montar a
                ficha técnica.
              </Text>
            </View>
          }
          renderItem={({ item }) => (
            <View style={styles.insumoRow}>
              <View style={{ flex: 1 }}>
                <Text style={styles.insumoNome}>{item.insumo.nome}</Text>
                <Text style={styles.insumoQuantidade}>
                  {item.quantidadePorUnidade} {item.insumo.unidadeMedida} por
                  unidade
                </Text>
              </View>

              <Pressable
                style={styles.removeButton}
                onPress={() => handleRemover(item.id)}
              >
                <Text style={styles.removeButtonText}>Remover</Text>
              </Pressable>
            </View>
          )}
        />
      </View>

      {/* =====================================================
          MODAL: VINCULAR INSUMO
      ===================================================== */}

      <Modal
        visible={modalVisible}
        transparent
        animationType="fade"
        onRequestClose={() => setModalVisible(false)}
      >
        <Pressable
          style={styles.modalOverlay}
          onPress={() => setModalVisible(false)}
        >
          <Pressable style={styles.modalBox} onPress={(e) => e.stopPropagation()}>
            <Text style={styles.modalTitle}>Adicionar insumo à ficha</Text>

            {erroModal ? <Text style={styles.errorBanner}>{erroModal}</Text> : null}

            {insumosParaVincular.length === 0 ? (
              <Text style={styles.modalText}>
                Todos os insumos disponíveis já estão vinculados a esta
                receita, ou você ainda não cadastrou nenhum insumo.
              </Text>
            ) : (
              <>
                <Text style={styles.fieldLabel}>Insumo</Text>

                <View style={styles.insumoPicker}>
                  {insumosParaVincular.map((insumo) => (
                    <Pressable
                      key={insumo.id}
                      style={[
                        styles.insumoOption,
                        insumoSelecionado?.id === insumo.id &&
                          styles.insumoOptionActive,
                      ]}
                      onPress={() => setInsumoSelecionado(insumo)}
                    >
                      <Text
                        style={[
                          styles.insumoOptionText,
                          insumoSelecionado?.id === insumo.id &&
                            styles.insumoOptionTextActive,
                        ]}
                      >
                        {insumo.nome}
                      </Text>
                    </Pressable>
                  ))}
                </View>

                <AuthInput
                  label={`Quantidade por unidade (${insumoSelecionado?.unidadeMedida ?? ''})`}
                  value={quantidade}
                  onChangeText={setQuantidade}
                  keyboardType="numeric"
                />
              </>
            )}

            <View style={styles.modalActions}>
              <Pressable
                style={styles.cancelButton}
                onPress={() => setModalVisible(false)}
              >
                <Text style={styles.cancelButtonText}>Cancelar</Text>
              </Pressable>

              {insumosParaVincular.length > 0 && (
                <Pressable
                  style={[styles.confirmButton, saving && styles.disabledButton]}
                  onPress={handleVincular}
                  disabled={saving}
                >
                  <Text style={styles.confirmButtonText}>
                    {saving ? 'Salvando...' : 'Adicionar'}
                  </Text>
                </Pressable>
              )}
            </View>
          </Pressable>
        </Pressable>
      </Modal>
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
    maxWidth: 760,
    width: '100%',
    alignSelf: 'center',
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

  title: {
    fontSize: 26,
    fontWeight: '800',
    color: colors.text,
    marginBottom: 10,
  },

  badge: {
    alignSelf: 'flex-start',
    paddingHorizontal: 12,
    paddingVertical: 5,
    borderRadius: 999,
    marginBottom: 20,
  },

  badgeVenda: {
    backgroundColor: colors.accentSoft,
  },

  badgeSugestao: {
    backgroundColor: colors.successSoft,
  },

  badgeText: {
    fontSize: 12,
    fontWeight: '700',
  },

  badgeTextVenda: {
    color: colors.primary,
  },

  badgeTextSugestao: {
    color: colors.success,
  },

  preparoCard: {
    backgroundColor: colors.surface,
    borderRadius: 14,
    padding: 16,
    borderWidth: 1,
    borderColor: colors.border,
    marginBottom: 24,
  },

  preparoTitle: {
    fontSize: 13,
    fontWeight: '700',
    color: colors.text,
    marginBottom: 6,
  },

  preparoTexto: {
    fontSize: 14,
    color: colors.textMuted,
    lineHeight: 21,
  },

  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 14,
  },

  sectionTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: colors.text,
  },

  addButton: {
    backgroundColor: colors.primary,
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 10,
  },

  addButtonText: {
    color: '#fff',
    fontWeight: '700',
    fontSize: 13,
  },

  empty: {
    paddingVertical: 30,
  },

  emptyText: {
    color: colors.textMuted,
    fontSize: 13,
    textAlign: 'center',
    lineHeight: 19,
  },

  insumoRow: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.surface,
    borderRadius: 12,
    padding: 14,
    borderWidth: 1,
    borderColor: colors.border,
  },

  insumoNome: {
    fontSize: 14,
    fontWeight: '700',
    color: colors.text,
  },

  insumoQuantidade: {
    fontSize: 12,
    color: colors.textMuted,
    marginTop: 2,
  },

  removeButton: {
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 9,
    backgroundColor: colors.dangerSoft,
  },

  removeButtonText: {
    color: colors.danger,
    fontWeight: '700',
    fontSize: 12,
  },

  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.45)',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 24,
  },

  modalBox: {
    width: '100%',
    maxWidth: 420,
    backgroundColor: '#fff',
    borderRadius: 18,
    padding: 24,
  },

  modalTitle: {
    fontSize: 20,
    fontWeight: '800',
    color: colors.text,
    marginBottom: 14,
  },

  modalText: {
    color: colors.textMuted,
    lineHeight: 20,
    fontSize: 14,
  },

  errorBanner: {
    backgroundColor: colors.dangerSoft,
    color: colors.danger,
    padding: 12,
    borderRadius: 10,
    marginBottom: 14,
    fontSize: 13,
  },

  fieldLabel: {
    fontSize: 13,
    fontWeight: '700',
    color: colors.text,
    marginBottom: 8,
  },

  insumoPicker: {
    maxHeight: 180,
    marginBottom: 16,
  },

  insumoOption: {
    paddingVertical: 10,
    paddingHorizontal: 12,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: colors.border,
    marginBottom: 6,
  },

  insumoOptionActive: {
    borderColor: colors.primary,
    backgroundColor: colors.accentSoft,
  },

  insumoOptionText: {
    fontSize: 13,
    color: colors.text,
  },

  insumoOptionTextActive: {
    color: colors.primary,
    fontWeight: '700',
  },

  modalActions: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    gap: 12,
    marginTop: 8,
  },

  cancelButton: {
    paddingVertical: 12,
    paddingHorizontal: 16,
    borderRadius: 12,
    backgroundColor: colors.background,
  },

  cancelButtonText: {
    color: colors.text,
    fontWeight: '700',
  },

  confirmButton: {
    paddingVertical: 12,
    paddingHorizontal: 16,
    borderRadius: 12,
    backgroundColor: colors.primary,
  },

  confirmButtonText: {
    color: '#fff',
    fontWeight: '700',
  },

  disabledButton: {
    opacity: 0.6,
  },
})