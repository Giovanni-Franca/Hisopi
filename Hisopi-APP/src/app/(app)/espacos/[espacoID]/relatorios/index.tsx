import { useLocalSearchParams } from 'expo-router'
import { useCallback, useEffect, useState } from 'react'
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native'

import { AuthInput } from '@/src/components/auth/AuthInput'
import { colors } from '@/src/theme/colors'
import {
  listarInsumosResumo,
  listarMovimentacoes,
  relatorioPerdas,
  type InsumoResumo,
  type Movimentacao,
  type RelatorioPerdas,
} from '@/src/services/relatorioService'

type Aba = 'perdas' | 'historico'

const TIPO_LABEL: Record<string, string> = {
  ENTRADA: 'Entrada',
  PERDA_VALIDADE: 'Perda por validade',
  PERDA_OUTRO: 'Outra perda',
}

const DATA_REGEX = /^\d{4}-\d{2}-\d{2}$/

function isoDia(d: Date) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const dia = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${dia}`
}

function diasAtras(n: number) {
  const d = new Date()
  d.setDate(d.getDate() - n)
  return isoDia(d)
}

function inicioDoMes() {
  const d = new Date()
  return isoDia(new Date(d.getFullYear(), d.getMonth(), 1))
}

function formatarData(iso: string) {
  const d = new Date(iso)
  return Number.isNaN(d.getTime()) ? iso : d.toLocaleString('pt-BR')
}

function formatarQtd(valor: number, unidade?: string) {
  const n = Number(valor).toLocaleString('pt-BR', { maximumFractionDigits: 3 })
  return unidade ? `${n} ${unidade}` : n
}

function mensagemErro(e: unknown, padrao: string) {
  return e instanceof Error && e.message ? e.message : padrao
}

export default function MovimentacoesScreen() {
  const { espacoID, insumoId } = useLocalSearchParams<{
    espacoID: string
    insumoId?: string
  }>()

  const [aba, setAba] = useState<Aba>(insumoId ? 'historico' : 'perdas')

  // ---------- relatório de perdas ----------
  const [inicio, setInicio] = useState(inicioDoMes())
  const [fim, setFim] = useState(isoDia(new Date()))
  const [relatorio, setRelatorio] = useState<RelatorioPerdas | null>(null)
  const [loadingRel, setLoadingRel] = useState(false)
  const [erroRel, setErroRel] = useState('')

  // ---------- histórico por insumo ----------
  const [insumos, setInsumos] = useState<InsumoResumo[]>([])
  const [insumoSel, setInsumoSel] = useState<number | null>(
    insumoId ? Number(insumoId) : null
  )
  const [movs, setMovs] = useState<Movimentacao[]>([])
  const [loadingHist, setLoadingHist] = useState(false)
  const [erroHist, setErroHist] = useState('')

  const gerarRelatorio = useCallback(async () => {
    setErroRel('')

    if (!DATA_REGEX.test(inicio) || !DATA_REGEX.test(fim)) {
      setErroRel('Use o formato AAAA-MM-DD nas duas datas.')
      return
    }
    if (inicio > fim) {
      setErroRel('A data inicial não pode ser maior que a final.')
      return
    }

    try {
      setLoadingRel(true)
      setRelatorio(await relatorioPerdas(espacoID, inicio, fim))
    } catch (e) {
      setRelatorio(null)
      setErroRel(mensagemErro(e, 'Não foi possível gerar o relatório.'))
    } finally {
      setLoadingRel(false)
    }
  }, [espacoID, inicio, fim])

  // gera o relatório do mês ao abrir
  useEffect(() => {
    gerarRelatorio()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // carrega a lista de insumos ao abrir a aba de histórico
  useEffect(() => {
    if (aba !== 'historico' || insumos.length > 0 || !espacoID) return

    listarInsumosResumo(espacoID)
      .then(setInsumos)
      .catch((e) => setErroHist(mensagemErro(e, 'Não foi possível carregar os insumos.')))
  }, [aba, espacoID, insumos.length])

  // carrega as movimentações quando um insumo é selecionado
  useEffect(() => {
    if (insumoSel == null || !espacoID) return

    setErroHist('')
    setLoadingHist(true)
    listarMovimentacoes(espacoID, insumoSel)
      .then(setMovs)
      .catch((e) => {
        setMovs([])
        setErroHist(mensagemErro(e, 'Não foi possível carregar o histórico.'))
      })
      .finally(() => setLoadingHist(false))
  }, [espacoID, insumoSel])

  function aplicarAtalho(dias: number | 'mes') {
    setInicio(dias === 'mes' ? inicioDoMes() : diasAtras(dias))
    setFim(isoDia(new Date()))
  }

  const insumoAtual = insumos.find((i) => i.id === insumoSel)

  return (
    <ScrollView style={styles.screen} contentContainerStyle={styles.content}>
      <Text style={styles.title}>Movimentações</Text>
      <Text style={styles.subtitle}>Acompanhe perdas e o histórico de cada insumo.</Text>

      <View style={styles.tabs}>
        <Pressable
          style={[styles.tab, aba === 'perdas' && styles.tabActive]}
          onPress={() => setAba('perdas')}
        >
          <Text style={[styles.tabText, aba === 'perdas' && styles.tabTextActive]}>
            Relatório de perdas
          </Text>
        </Pressable>
        <Pressable
          style={[styles.tab, aba === 'historico' && styles.tabActive]}
          onPress={() => setAba('historico')}
        >
          <Text style={[styles.tabText, aba === 'historico' && styles.tabTextActive]}>
            Histórico do insumo
          </Text>
        </Pressable>
      </View>

      {/* =====================================================
          ABA: RELATÓRIO DE PERDAS
      ===================================================== */}
      {aba === 'perdas' && (
        <View>
          <View style={styles.card}>
            <View style={styles.dateRow}>
              <View style={{ flex: 1 }}>
                <AuthInput
                  label="De"
                  placeholder="AAAA-MM-DD"
                  value={inicio}
                  onChangeText={setInicio}
                  autoCapitalize="none"
                />
              </View>
              <View style={{ flex: 1 }}>
                <AuthInput
                  label="Até"
                  placeholder="AAAA-MM-DD"
                  value={fim}
                  onChangeText={setFim}
                  autoCapitalize="none"
                />
              </View>
            </View>

            <View style={styles.chipRow}>
              <Pressable style={styles.chip} onPress={() => aplicarAtalho(7)}>
                <Text style={styles.chipText}>Últimos 7 dias</Text>
              </Pressable>
              <Pressable style={styles.chip} onPress={() => aplicarAtalho(30)}>
                <Text style={styles.chipText}>Últimos 30 dias</Text>
              </Pressable>
              <Pressable style={styles.chip} onPress={() => aplicarAtalho('mes')}>
                <Text style={styles.chipText}>Este mês</Text>
              </Pressable>
            </View>

            {erroRel ? <Text style={styles.errorBanner}>{erroRel}</Text> : null}

            <Pressable
              style={[styles.primaryButton, loadingRel && styles.disabledButton]}
              onPress={gerarRelatorio}
              disabled={loadingRel}
            >
              <Text style={styles.primaryButtonText}>
                {loadingRel ? 'Gerando...' : 'Gerar relatório'}
              </Text>
            </Pressable>
          </View>

          {loadingRel && <ActivityIndicator style={{ marginTop: 24 }} color={colors.primary} />}

          {!loadingRel && relatorio && (
            <>
              <View style={styles.totalCard}>
                <Text style={styles.totalLabel}>Total perdido no período</Text>
                <Text style={styles.totalValue}>
                  {formatarQtd(relatorio.quantidadeTotalPerdida)}
                </Text>
                <Text style={styles.totalHint}>
                  {relatorio.perdasPorValidade.length} por validade ·{' '}
                  {relatorio.perdasPorOutroMotivo.length} por outros motivos
                </Text>
              </View>

              <ListaMovimentacoes
                titulo="Perdas por validade"
                itens={relatorio.perdasPorValidade}
                vazio="Nenhuma perda por validade neste período."
                mostrarInsumo
              />
              <ListaMovimentacoes
                titulo="Perdas por outros motivos"
                itens={relatorio.perdasPorOutroMotivo}
                vazio="Nenhuma outra perda neste período."
                mostrarInsumo
              />
            </>
          )}
        </View>
      )}

      {/* =====================================================
          ABA: HISTÓRICO POR INSUMO
      ===================================================== */}
      {aba === 'historico' && (
        <View>
          <View style={styles.card}>
            <Text style={styles.fieldLabel}>Escolha um insumo</Text>

            {insumos.length === 0 && !erroHist ? (
              <ActivityIndicator color={colors.primary} />
            ) : (
              <View style={styles.chipRow}>
                {insumos.map((i) => (
                  <Pressable
                    key={i.id}
                    style={[styles.chip, insumoSel === i.id && styles.chipActive]}
                    onPress={() => setInsumoSel(i.id)}
                  >
                    <Text
                      style={[styles.chipText, insumoSel === i.id && styles.chipTextActive]}
                    >
                      {i.nome}
                    </Text>
                  </Pressable>
                ))}
              </View>
            )}
          </View>

          {erroHist ? <Text style={styles.errorBanner}>{erroHist}</Text> : null}

          {loadingHist && <ActivityIndicator style={{ marginTop: 24 }} color={colors.primary} />}

          {!loadingHist && insumoSel != null && !erroHist && (
            <ListaMovimentacoes
              titulo={insumoAtual ? `Histórico de ${insumoAtual.nome}` : 'Histórico'}
              itens={movs}
              vazio="Este insumo ainda não tem movimentações."
              unidadePadrao={insumoAtual?.unidadeMedida}
            />
          )}

          {insumoSel == null && insumos.length > 0 && (
            <Text style={styles.emptyText}>Selecione um insumo para ver as movimentações.</Text>
          )}
        </View>
      )}
    </ScrollView>
  )
}

// =========================================================
// lista reutilizável
// =========================================================

function ListaMovimentacoes({
  titulo,
  itens,
  vazio,
  mostrarInsumo,
  unidadePadrao,
}: {
  titulo: string
  itens: Movimentacao[]
  vazio: string
  mostrarInsumo?: boolean
  unidadePadrao?: string
}) {
  return (
    <View style={{ marginTop: 20 }}>
      <Text style={styles.sectionTitle}>{titulo}</Text>

      {itens.length === 0 ? (
        <Text style={styles.emptyText}>{vazio}</Text>
      ) : (
        <View style={{ gap: 10 }}>
          {itens.map((m) => {
            const nome = m.insumo?.nome ?? m.nomeInsumo
            const unidade = m.insumo?.unidadeMedida ?? m.unidadeMedida ?? unidadePadrao
            const entrada = m.tipo === 'ENTRADA'

            return (
              <View key={m.id} style={styles.movCard}>
                <View style={{ flex: 1 }}>
                  {mostrarInsumo && nome ? <Text style={styles.movNome}>{nome}</Text> : null}
                  <Text style={styles.movTipo}>{TIPO_LABEL[m.tipo] ?? m.tipo}</Text>
                  {m.motivo ? <Text style={styles.movMotivo}>{m.motivo}</Text> : null}
                  <Text style={styles.movData}>{formatarData(m.dataMovimentacao)}</Text>
                </View>

                <Text style={[styles.movQtd, entrada ? styles.qtdEntrada : styles.qtdPerda]}>
                  {entrada ? '+' : '−'}
                  {formatarQtd(m.quantidade, unidade)}
                </Text>
              </View>
            )
          })}
        </View>
      )}
    </View>
  )
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: colors.background,
  },

  content: {
    padding: 20,
    maxWidth: 720,
    width: '100%',
    alignSelf: 'center',
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

  tabs: {
    flexDirection: 'row',
    gap: 8,
    marginBottom: 16,
  },

  tab: {
    flex: 1,
    paddingVertical: 11,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: colors.border,
    alignItems: 'center',
  },

  tabActive: {
    borderColor: colors.primary,
    backgroundColor: colors.accentSoft,
  },

  tabText: {
    fontSize: 13,
    fontWeight: '700',
    color: colors.textMuted,
  },

  tabTextActive: {
    color: colors.primary,
  },

  card: {
    backgroundColor: colors.surface,
    borderRadius: 14,
    padding: 16,
    borderWidth: 1,
    borderColor: colors.border,
  },

  dateRow: {
    flexDirection: 'row',
    gap: 12,
  },

  fieldLabel: {
    fontSize: 13,
    fontWeight: '700',
    color: colors.text,
    marginBottom: 10,
  },

  chipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
    marginBottom: 12,
  },

  chip: {
    paddingVertical: 8,
    paddingHorizontal: 14,
    borderRadius: 999,
    borderWidth: 1,
    borderColor: colors.border,
  },

  chipActive: {
    borderColor: colors.primary,
    backgroundColor: colors.accentSoft,
  },

  chipText: {
    fontSize: 12,
    fontWeight: '700',
    color: colors.textMuted,
  },

  chipTextActive: {
    color: colors.primary,
  },

  primaryButton: {
    backgroundColor: colors.primary,
    paddingVertical: 13,
    borderRadius: 12,
    alignItems: 'center',
  },

  primaryButtonText: {
    color: '#fff',
    fontWeight: '700',
    fontSize: 14,
  },

  disabledButton: {
    opacity: 0.6,
  },

  errorBanner: {
    backgroundColor: colors.dangerSoft,
    color: colors.danger,
    padding: 12,
    borderRadius: 10,
    marginVertical: 12,
    fontSize: 13,
  },

  totalCard: {
    marginTop: 16,
    backgroundColor: colors.dangerSoft,
    borderRadius: 14,
    padding: 18,
  },

  totalLabel: {
    fontSize: 13,
    fontWeight: '700',
    color: colors.danger,
  },

  totalValue: {
    fontSize: 32,
    fontWeight: '800',
    color: colors.danger,
    marginTop: 4,
  },

  totalHint: {
    fontSize: 12,
    color: colors.textMuted,
    marginTop: 4,
  },

  sectionTitle: {
    fontSize: 16,
    fontWeight: '800',
    color: colors.text,
    marginBottom: 10,
  },

  emptyText: {
    fontSize: 13,
    color: colors.textMuted,
    marginTop: 8,
  },

  movCard: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    backgroundColor: colors.surface,
    borderRadius: 12,
    padding: 14,
    borderWidth: 1,
    borderColor: colors.border,
  },

  movNome: {
    fontSize: 14,
    fontWeight: '700',
    color: colors.text,
  },

  movTipo: {
    fontSize: 13,
    color: colors.text,
    marginTop: 2,
  },

  movMotivo: {
    fontSize: 12,
    color: colors.textMuted,
    marginTop: 2,
  },

  movData: {
    fontSize: 11,
    color: colors.textMuted,
    marginTop: 4,
  },

  movQtd: {
    fontSize: 15,
    fontWeight: '800',
  },

  qtdEntrada: {
    color: colors.primary,
  },

  qtdPerda: {
    color: colors.danger,
  },
})