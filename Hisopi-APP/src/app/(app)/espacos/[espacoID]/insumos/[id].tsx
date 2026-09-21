import { router, useLocalSearchParams, useFocusEffect } from 'expo-router'
import { useCallback, useState } from 'react'
import {
  ActivityIndicator,
  FlatList,
  Modal,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native'

import { AuthInput } from '@/src/components/auth/AuthInput'
import { colors } from '@/src/theme/colors'
import {
  listarInsumos,
  listarLotes,
  registrarLote,
  registrarPerda,
  type Insumo,
  type LoteInsumo,
} from '@/src/services/insumoService'

const DIAS_ALERTA_VENCIMENTO = 7

function diasAteVencimento(dataValidade: string) {
  const hoje = new Date()
  const validade = new Date(dataValidade)
  const diffMs = validade.getTime() - hoje.getTime()
  return Math.ceil(diffMs / (1000 * 60 * 60 * 24))
}

export default function InsumoDetalheScreen() {
  const { espacoID, id } = useLocalSearchParams<{ espacoID: string; id: string }>()

  const [insumo, setInsumo] = useState<Insumo | null>(null)
  const [lotes, setLotes] = useState<LoteInsumo[]>([])
  const [loading, setLoading] = useState(true)

  const [loteModalVisible, setLoteModalVisible] = useState(false)
  const [novaQuantidade, setNovaQuantidade] = useState('')
  const [novaValidade, setNovaValidade] = useState('')
  const [novoFornecedor, setNovoFornecedor] = useState('')
  const [savingLote, setSavingLote] = useState(false)
  const [erroLote, setErroLote] = useState('')

  const [perdaModalVisible, setPerdaModalVisible] = useState(false)
  const [loteSelecionado, setLoteSelecionado] = useState<LoteInsumo | null>(null)
  const [motivoPerda, setMotivoPerda] = useState('')
  const [savingPerda, setSavingPerda] = useState(false)

  const load = useCallback(async () => {
    if (!espacoID || !id) return

    try {
      const [todosInsumos, dataLotes] = await Promise.all([
        listarInsumos(espacoID),
        listarLotes(espacoID, Number(id)),
      ])

      setInsumo(todosInsumos.find((i) => i.id === Number(id)) ?? null)
      setLotes(dataLotes)
    } catch (error) {
      console.log('Erro ao carregar insumo:', error)
    } finally {
      setLoading(false)
    }
  }, [espacoID, id])

  useFocusEffect(
    useCallback(() => {
      load()
    }, [load])
  )

  async function handleRegistrarLote() {
    setErroLote('')

    if (!novaQuantidade.trim() || !novaValidade.trim()) {
      setErroLote('Preencha quantidade e data de validade.')
      return
    }

    try {
      setSavingLote(true)

      await registrarLote(espacoID, Number(id), {
        quantidade: Number(novaQuantidade.replace(',', '.')),
        dataValidade: novaValidade.trim(),
        fornecedor: novoFornecedor.trim() || null,
      })

      setLoteModalVisible(false)
      setNovaQuantidade('')
      setNovaValidade('')
      setNovoFornecedor('')

      await load()
    } catch (error) {
      setErroLote(
        error instanceof Error
          ? error.message
          : 'Não foi possível registrar o lote.'
      )
    } finally {
      setSavingLote(false)
    }
  }

  async function handleRegistrarPerda() {
    if (!loteSelecionado) return

    const tipo =
      diasAteVencimento(loteSelecionado.dataValidade) <= 0
        ? 'PERDA_VALIDADE'
        : 'PERDA_OUTRO'

    try {
      setSavingPerda(true)

      await registrarPerda(espacoID, loteSelecionado.id, {
        tipo,
        motivo: motivoPerda.trim() || null,
      })

      setPerdaModalVisible(false)
      setLoteSelecionado(null)
      setMotivoPerda('')

      await load()
    } catch (error) {
      console.log('Erro ao registrar perda:', error)
    } finally {
      setSavingPerda(false)
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
          onPress={() => router.push(`/espacos/${espacoID}/insumos` as any)}
        >
          <Text style={styles.backButtonText}>Voltar</Text>
        </Pressable>

        <Text style={styles.title}>{insumo?.nome ?? 'Insumo'}</Text>

        <Text style={styles.subtitle}>
          {insumo?.estoqueAtual} {insumo?.unidadeMedida} em estoque · mínimo{' '}
          {insumo?.estoqueMinimo} {insumo?.unidadeMedida}
        </Text>

        <Pressable
          style={styles.addLoteButton}
          onPress={() => setLoteModalVisible(true)}
        >
          <Text style={styles.addLoteButtonText}>+ Registrar entrada de lote</Text>
        </Pressable>

        <Text style={styles.sectionTitle}>
          Lotes (ordenados por validade — o mais próximo de vencer primeiro)
        </Text>

        <FlatList
          data={lotes}
          keyExtractor={(item) => String(item.id)}
          scrollEnabled={false}
          ItemSeparatorComponent={() => <View style={{ height: 12 }} />}
          ListEmptyComponent={
            <View style={styles.empty}>
              <Text style={styles.emptyText}>
                Nenhum lote ativo. Registre uma entrada acima.
              </Text>
            </View>
          }
          renderItem={({ item }) => {
            const dias = diasAteVencimento(item.dataValidade)
            const vencendo = dias <= DIAS_ALERTA_VENCIMENTO

            return (
              <View style={[styles.loteCard, vencendo && styles.loteCardVencendo]}>
                <View style={{ flex: 1 }}>
                  <Text style={styles.loteQuantidade}>
                    {item.quantidadeAtual} {insumo?.unidadeMedida}
                  </Text>

                  <Text style={styles.loteValidade}>
                    Validade:{' '}
                    {new Date(item.dataValidade).toLocaleDateString('pt-BR')}
                    {vencendo && (
                      <Text style={styles.loteValidadeAlerta}>
                        {'  '}
                        {dias <= 0 ? '· vencido' : `· vence em ${dias}d`}
                      </Text>
                    )}
                  </Text>

                  {item.fornecedor && (
                    <Text style={styles.loteFornecedor}>
                      Fornecedor: {item.fornecedor}
                    </Text>
                  )}
                </View>

                <Pressable
                  style={styles.perdaButton}
                  onPress={() => {
                    setLoteSelecionado(item)
                    setPerdaModalVisible(true)
                  }}
                >
                  <Text style={styles.perdaButtonText}>Registrar perda</Text>
                </Pressable>
              </View>
            )
          }}
        />
      </View>

      {/* =====================================================
          MODAL: NOVO LOTE
      ===================================================== */}

      <Modal
        visible={loteModalVisible}
        transparent
        animationType="fade"
        onRequestClose={() => setLoteModalVisible(false)}
      >
        <Pressable
          style={styles.modalOverlay}
          onPress={() => setLoteModalVisible(false)}
        >
          <Pressable style={styles.modalBox} onPress={(e) => e.stopPropagation()}>
            <Text style={styles.modalTitle}>Registrar entrada de lote</Text>

            {erroLote ? <Text style={styles.errorBanner}>{erroLote}</Text> : null}

            <AuthInput
              label={`Quantidade (${insumo?.unidadeMedida ?? ''})`}
              value={novaQuantidade}
              onChangeText={setNovaQuantidade}
              keyboardType="numeric"
            />

            <AuthInput
              label="Validade"
              placeholder="AAAA-MM-DD"
              value={novaValidade}
              onChangeText={setNovaValidade}
            />

            <AuthInput
              label="Fornecedor (opcional)"
              value={novoFornecedor}
              onChangeText={setNovoFornecedor}
            />

            <View style={styles.modalActions}>
              <Pressable
                style={styles.cancelButton}
                onPress={() => setLoteModalVisible(false)}
              >
                <Text style={styles.cancelButtonText}>Cancelar</Text>
              </Pressable>

              <Pressable
                style={[styles.confirmButton, savingLote && styles.disabledButton]}
                onPress={handleRegistrarLote}
                disabled={savingLote}
              >
                <Text style={styles.confirmButtonText}>
                  {savingLote ? 'Salvando...' : 'Registrar'}
                </Text>
              </Pressable>
            </View>
          </Pressable>
        </Pressable>
      </Modal>

      {/* =====================================================
          MODAL: REGISTRAR PERDA
      ===================================================== */}

      <Modal
        visible={perdaModalVisible}
        transparent
        animationType="fade"
        onRequestClose={() => setPerdaModalVisible(false)}
      >
        <Pressable
          style={styles.modalOverlay}
          onPress={() => setPerdaModalVisible(false)}
        >
          <Pressable style={styles.modalBox} onPress={(e) => e.stopPropagation()}>
            <Text style={styles.modalTitle}>Registrar perda do lote</Text>

            <Text style={styles.modalText}>
              {loteSelecionado?.quantidadeAtual} {insumo?.unidadeMedida} serão
              descartados deste lote.
            </Text>

            <AuthInput
              label="Motivo (opcional)"
              value={motivoPerda}
              onChangeText={setMotivoPerda}
            />

            <View style={styles.modalActions}>
              <Pressable
                style={styles.cancelButton}
                onPress={() => setPerdaModalVisible(false)}
              >
                <Text style={styles.cancelButtonText}>Cancelar</Text>
              </Pressable>

              <Pressable
                style={[styles.confirmButton, savingPerda && styles.disabledButton]}
                onPress={handleRegistrarPerda}
                disabled={savingPerda}
              >
                <Text style={styles.confirmButtonText}>
                  {savingPerda ? 'Registrando...' : 'Confirmar perda'}
                </Text>
              </Pressable>
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
  },

  subtitle: {
    fontSize: 14,
    color: colors.textMuted,
    marginTop: 4,
    marginBottom: 20,
  },

  addLoteButton: {
    backgroundColor: colors.primary,
    paddingVertical: 14,
    borderRadius: 12,
    alignItems: 'center',
    marginBottom: 24,
  },

  addLoteButtonText: {
    color: '#fff',
    fontWeight: '700',
    fontSize: 15,
  },

  sectionTitle: {
    fontSize: 14,
    fontWeight: '700',
    color: colors.text,
    marginBottom: 12,
  },

  empty: {
    alignItems: 'center',
    paddingVertical: 40,
  },

  emptyText: {
    color: colors.textMuted,
    textAlign: 'center',
  },

  loteCard: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    backgroundColor: colors.surface,
    borderRadius: 14,
    padding: 16,
    borderWidth: 1,
    borderColor: colors.border,
  },

  loteCardVencendo: {
    borderColor: colors.warning,
    backgroundColor: colors.warningSoft,
  },

  loteQuantidade: {
    fontSize: 16,
    fontWeight: '800',
    color: colors.text,
  },

  loteValidade: {
    color: colors.textMuted,
    marginTop: 4,
    fontSize: 13,
  },

  loteValidadeAlerta: {
    color: colors.warning,
    fontWeight: '700',
  },

  loteFornecedor: {
    color: colors.textMuted,
    marginTop: 2,
    fontSize: 12,
  },

  perdaButton: {
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 10,
    backgroundColor: colors.dangerSoft,
  },

  perdaButtonText: {
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
    maxWidth: 400,
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
    marginBottom: 14,
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