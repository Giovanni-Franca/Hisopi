import { router, useNavigation } from 'expo-router'
import * as Clipboard from 'expo-clipboard'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { ActivityIndicator, Pressable, StyleSheet, Text, View } from 'react-native'
import { KeyboardAwareScrollView } from 'react-native-keyboard-aware-scroll-view'

import { useAccessibility } from '@/src/context/AccessibilityContext'
import { AuthInput } from '@/src/components/auth/AuthInput'
import {
  ApiKeyItem,
  ChaveCriada,
  EspacoResumo,
  criarEspacoComChave,
  gerarApiKey,
  listarApiKeys,
  revogarApiKey,
} from '@/src/services/apiKeyService'
import {listarEspacosDoUsuario} from '@/src/services/espacoService'

import { useResponsive } from '@/src/hooks/useResponsive'

const TIPOS_ESPACO = ['ORGANIZACAO','PESSOAL']
function formatarUso(valor: string | null) {
  if (!valor || valor === 'null') return 'Nunca usada'
  const d = new Date(valor)
  return Number.isNaN(d.getTime()) ? valor : d.toLocaleString('pt-BR')
}

export default function ApiKeyScreen() {
  const { isDesktop } = useResponsive()
  const { colors, fontScale } = useAccessibility()
  const navigation = useNavigation()
  const styles = useMemo(() => createStyles(colors, fontScale), [colors, fontScale])

  const [espacos, setEspacos] = useState<EspacoResumo[]>([])
  const [idEspaco, setIdEspaco] = useState<number | null>(null)
  const [chaves, setChaves] = useState<ApiKeyItem[]>([])

  const [loading, setLoading] = useState(true)
  const [loadingChaves, setLoadingChaves] = useState(false)
  const [saving, setSaving] = useState(false)
  const [erro, setErro] = useState('')

  const [nomeNovaChave, setNomeNovaChave] = useState('')
  const [chaveCriada, setChaveCriada] = useState<ChaveCriada | null>(null)
  const [copiada, setCopiada] = useState(false)
  const [confirmarRevogar, setConfirmarRevogar] = useState<string | null>(null)

  const [novoEspacoAberto, setNovoEspacoAberto] = useState(false)
  const [nomeEspaco, setNomeEspaco] = useState('')
  const [tipoEspaco, setTipoEspaco] = useState(TIPOS_ESPACO[0])
  const [nomeChaveEspaco, setNomeChaveEspaco] = useState('')

  const mensagem = (e: unknown, padrao: string) => (e instanceof Error ? e.message : padrao)

  const carregarChaves = useCallback(async (id: number) => {
    setLoadingChaves(true)
    try {
      setChaves(await listarApiKeys(id))
    } catch (e) {
      setChaves([])
      setErro(mensagem(e, 'Não foi possível carregar as chaves.'))
    } finally {
      setLoadingChaves(false)
    }
  }, [])

  useEffect(() => {
    ;(async () => {
      try {
        const lista = await listarEspacosDoUsuario()
        setEspacos(lista)
        if (lista.length > 0) setIdEspaco(lista[0].id)
      } catch (e) {
        setErro(mensagem(e, 'Não foi possível carregar seus espaços.'))
      } finally {
        setLoading(false)
      }
    })()
  }, [])

  useEffect(() => {
    setConfirmarRevogar(null)
    if (idEspaco != null) carregarChaves(idEspaco)
  }, [idEspaco, carregarChaves])

  function selecionarEspaco(id: number) {
    setChaveCriada(null) 
    setIdEspaco(id)
  }

  async function handleGerar() {
    setErro('')
    if (idEspaco == null || !nomeNovaChave.trim()) {
      setErro('Informe um nome para a chave.')
      return
    }
    try {
      setSaving(true)
      const criada = await gerarApiKey(idEspaco, nomeNovaChave.trim())
      setChaveCriada(criada)
      setCopiada(false)
      setNomeNovaChave('')
      await carregarChaves(idEspaco)
    } catch (e) {
      setErro(mensagem(e, 'Não foi possível gerar a chave. Tente novamente.'))
    } finally {
      setSaving(false)
    }
  }

  async function handleRevogar(idKey: string) {
    if (idEspaco == null) return
    setErro('')
    try {
      setSaving(true)
      await revogarApiKey(idEspaco, idKey)
      setConfirmarRevogar(null)
      await carregarChaves(idEspaco)
    } catch (e) {
      setErro(mensagem(e, 'Não foi possível revogar a chave.'))
    } finally {
      setSaving(false)
    }
  }

  async function handleCriarEspaco() {
    setErro('')
    if (!nomeEspaco.trim() || !nomeChaveEspaco.trim()) {
      setErro('Preencha o nome do espaço e o nome da chave.')
      return
    }
    try {
      setSaving(true)
      const criada = await criarEspacoComChave({
        nomeEspaco: nomeEspaco.trim(),
        tipo: tipoEspaco,
        nomeChave: nomeChaveEspaco.trim(),
      })
      setEspacos((prev) => [...prev, { id: criada.espacoId, nome: nomeEspaco.trim() }])
      setIdEspaco(criada.espacoId)
      setChaveCriada(criada)
      setCopiada(false)
      setNomeEspaco('')
      setNomeChaveEspaco('')
      setNovoEspacoAberto(false)
    } catch (e) {
      setErro(mensagem(e, 'Não foi possível criar o espaço. Tente novamente.'))
    } finally {
      setSaving(false)
    }
  }

  async function handleCopiar() {
    if (!chaveCriada) return
    await Clipboard.setStringAsync(chaveCriada.chave)
    setCopiada(true)
  }

  function voltar() {
    if (navigation.canGoBack()) router.back()
    else router.replace('/perfil' as any)
  }

  if (loading) {
    return (
      <View style={[styles.container, styles.center]}>
        <ActivityIndicator color={colors.primary} />
      </View>
    )
  }

  const espacoAtual = espacos.find((e) => e.id === idEspaco)

  return (
    <KeyboardAwareScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
      enableOnAndroid
      keyboardShouldPersistTaps="handled"
    >
      
      <View style={[styles.card, isDesktop && styles.cardDesktop]}>
        {!isDesktop &&(
          <Pressable
            onPress={voltar}
            style={styles.backButton}
            accessibilityRole="button"
            accessibilityLabel="Voltar"
            hitSlop={8}
          >
            <Text style={styles.backButtonText}>← Voltar</Text>
          </Pressable>
        )}
        <Text style={styles.title}>Chaves de API</Text>
        <Text style={styles.subtitle}>
          Cada chave dá a um sistema externo acesso a um único espaço, com o papel de Gerente.
          A chave completa aparece apenas uma vez, na criação.
        </Text>

        {erro ? <Text style={styles.errorBanner}>{erro}</Text> : null}

        {/* Espaço */}
        <Text style={styles.sectionTitle}>Espaço</Text>
        {espacos.length === 0 ? (
          <Text style={styles.hint}>
            Você ainda não tem espaços. Crie um abaixo, já com a primeira chave.
          </Text>
        ) : (
          <View style={styles.chipRow}>
            {espacos.map((e) => {
              const selected = e.id === idEspaco
              return (
                <Pressable
                  key={e.id}
                  accessibilityRole="radio"
                  accessibilityLabel={`Espaço ${e.nome}`}
                  accessibilityState={{ selected }}
                  onPress={() => selecionarEspaco(e.id)}
                  style={[styles.chip, selected && styles.chipSelected]}
                >
                  <Text style={[styles.chipText, selected && styles.chipTextSelected]}>
                    {e.nome}
                  </Text>
                </Pressable>
              )
            })}
          </View>
        )}

        {/* Chave recém-criada */}
        {chaveCriada ? (
          <View style={styles.newKeyBox} accessibilityLiveRegion="polite">
            <Text style={styles.newKeyTitle}>Chave criada: {chaveCriada.nome}</Text>
            <Text style={styles.hint}>
              Copie agora e guarde em local seguro. Depois de fechar este aviso, não será
              possível ver a chave de novo.
            </Text>
            <Text selectable style={styles.keyValue}>
              {chaveCriada.chave}
            </Text>
            <View style={styles.rowButtons}>
              <Pressable style={[styles.button, styles.buttonPrimary]} onPress={handleCopiar}>
                <Text style={styles.buttonPrimaryText}>
                  {copiada ? 'Copiada' : 'Copiar chave'}
                </Text>
              </Pressable>
              <Pressable style={styles.button} onPress={() => setChaveCriada(null)}>
                <Text style={styles.buttonText}>Já guardei</Text>
              </Pressable>
            </View>
          </View>
        ) : null}

        {/* Gerar nova chave */}
        {espacoAtual ? (
          <>
            <Text style={styles.sectionTitle}>Nova chave para “{espacoAtual.nome}”</Text>
            <AuthInput
              label="Nome da chave"
              placeholder="Ex: Integração ERP"
              value={nomeNovaChave}
              onChangeText={setNomeNovaChave}
            />
            <Pressable
              style={[styles.submitButton, saving && styles.submitButtonDisabled]}
              onPress={handleGerar}
              disabled={saving}
            >
              <Text style={styles.submitButtonText}>{saving ? 'Gerando...' : 'Gerar chave'}</Text>
            </Pressable>

            {/* Lista */}
            <Text style={styles.sectionTitle}>Chaves do espaço</Text>
            {loadingChaves ? (
              <ActivityIndicator color={colors.primary} />
            ) : chaves.length === 0 ? (
              <Text style={styles.hint}>
                Nenhuma chave ainda. Gere a primeira acima para conectar um sistema externo.
              </Text>
            ) : (
              chaves.map((k) => (
                <View key={k.id} style={[styles.keyRow, !k.ativa && styles.keyRowRevoked]}>
                  <View style={styles.keyInfo}>
                    <Text style={styles.keyName}>{k.nome}</Text>
                    <Text style={styles.keyMeta}>
                    </Text>
                    <Text style={styles.keyMeta}>Último uso: {formatarUso(k.ultimoUso)}</Text>
                    {!k.ativa ? <Text style={styles.revoked}>Revogada</Text> : null}
                  </View>

                  {k.ativa ? (
                    confirmarRevogar === k.id ? (
                      <View style={styles.rowButtons}>
                        <Pressable
                          style={[styles.button, styles.buttonDanger]}
                          onPress={() => handleRevogar(k.id)}
                          disabled={saving}
                          accessibilityLabel={`Confirmar revogação de ${k.nome}`}
                        >
                          <Text style={styles.buttonDangerText}>Revogar</Text>
                        </Pressable>
                        <Pressable style={styles.button} onPress={() => setConfirmarRevogar(null)}>
                          <Text style={styles.buttonText}>Cancelar</Text>
                        </Pressable>
                      </View>
                    ) : (
                      <Pressable
                        style={[styles.button, styles.buttonOutlineDanger]}
                        onPress={() => setConfirmarRevogar(k.id)}
                        accessibilityLabel={`Revogar chave ${k.nome}`}
                        accessibilityHint="Pede confirmação antes de revogar"
                      >
                        <Text style={styles.buttonOutlineDangerText}>Revogar</Text>
                      </Pressable>
                    )
                  ) : null}
                </View>
              ))
            )}
            {chaves.some((k) => k.ativa) ? (
              <Text style={styles.hint}>
                Revogar bloqueia o sistema que usa a chave imediatamente. Para trocar sem
                interrupção, gere a nova chave, atualize o sistema e só então revogue a antiga.
              </Text>
            ) : null}
          </>
        ) : null}

        {/* Novo espaço com chave */}
        <Pressable
          onPress={() => setNovoEspacoAberto((v) => !v)}
          accessibilityRole="button"
          accessibilityState={{ expanded: novoEspacoAberto }}
          style={styles.toggleRow}
        >
          <Text style={styles.sectionTitle}>Novo espaço com chave</Text>
          <Text style={styles.toggleText}>{novoEspacoAberto ? 'Fechar' : 'Abrir'}</Text>
        </Pressable>

        {novoEspacoAberto ? (
          <>
            <Text style={styles.hint}>
              Cria um espaço novo, define você como dono e já gera a primeira chave.
            </Text>
            <AuthInput
              label="Nome do espaço"
              placeholder="Ex: Cozinha do Parceiro X"
              value={nomeEspaco}
              onChangeText={setNomeEspaco}
            />

            {TIPOS_ESPACO.length > 1 ? (
              <View style={styles.chipRow}>
                {TIPOS_ESPACO.map((t) => {
                  const selected = t === tipoEspaco
                  return (
                    <Pressable
                      key={t}
                      accessibilityRole="radio"
                      accessibilityLabel={`Tipo ${t}`}
                      accessibilityState={{ selected }}
                      onPress={() => setTipoEspaco(t)}
                      style={[styles.chip, selected && styles.chipSelected]}
                    >
                      <Text style={[styles.chipText, selected && styles.chipTextSelected]}>
                        {t}
                      </Text>
                    </Pressable>
                  )
                })}
              </View>
            ) : null}

            <AuthInput
              label="Nome da chave"
              placeholder="Ex: Integração ERP"
              value={nomeChaveEspaco}
              onChangeText={setNomeChaveEspaco}
            />
            <Pressable
              style={[styles.submitButton, saving && styles.submitButtonDisabled]}
              onPress={handleCriarEspaco}
              disabled={saving}
            >
              <Text style={styles.submitButtonText}>
                {saving ? 'Criando...' : 'Criar espaço e chave'}
              </Text>
            </Pressable>
          </>
        ) : null}
      </View>
    </KeyboardAwareScrollView>
  )
}

function createStyles(
  colors: typeof import('@/src/theme/colors').colors,
  fontScale: number,
) {
  return StyleSheet.create({
    container: { flex: 1, backgroundColor: colors.surface },
    center: { alignItems: 'center', justifyContent: 'center' },
    content: {
      alignItems: 'center',
    },
    card: {
      width: '100%',
      backgroundColor: colors.surface,
      padding: 28,
      gap: 14,
    },
    cardDesktop: {
      elevation: 3,
      maxWidth: 720,
      margin: 24,
      borderColor: colors.border,
      borderWidth: 1,
      borderRadius: 20,
      shadowColor: '#000',
      shadowOpacity: 0.12,
      shadowRadius: 16,
      shadowOffset: { width: 0, height: 8 },
    },
    backButton: {
      minHeight: 44,
      alignSelf: 'flex-start',
      justifyContent: 'center',
      borderRadius: 10,
      borderWidth: 1,
      borderColor: 'transparent',
      marginBottom: 16,
    },
    backButtonText: { color: colors.text, fontSize: 14 * fontScale, fontWeight: '700' },
    title: { fontSize: 24 * fontScale, fontWeight: '800', color: colors.text },
    subtitle: { fontSize: 14 * fontScale, color: colors.textMuted, lineHeight: 20 * fontScale },
    sectionTitle: {
      fontSize: 18 * fontScale,
      fontWeight: '800',
      color: colors.text,
      marginTop: 10,
    },
    hint: { fontSize: 13 * fontScale, color: colors.textMuted, lineHeight: 20 * fontScale },
    errorBanner: {
      backgroundColor: colors.dangerSoft,
      color: colors.danger,
      padding: 12,
      borderRadius: 10,
      fontSize: 14 * fontScale,
    },

    chipRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
    chip: {
      borderWidth: 2,
      borderColor: colors.border,
      borderRadius: 10,
      paddingHorizontal: 12,
      paddingVertical: 10,
      minHeight: 44,
      justifyContent: 'center',
    },
    chipSelected: { backgroundColor: colors.primary, borderColor: colors.primary },
    chipText: { fontSize: 13 * fontScale, fontWeight: '600', color: colors.text },
    chipTextSelected: { color: '#fff' },

    submitButton: {
      backgroundColor: colors.primary,
      paddingVertical: 15,
      borderRadius: 12,
      alignItems: 'center',
    },
    submitButtonDisabled: { opacity: 0.6 },
    submitButtonText: { color: '#fff', fontSize: 16 * fontScale, fontWeight: '700' },

    button: {
      borderWidth: 2,
      borderColor: colors.border,
      borderRadius: 10,
      paddingHorizontal: 14,
      paddingVertical: 12,
      minHeight: 44,
      alignItems: 'center',
      justifyContent: 'center',
    },
    buttonText: { fontSize: 13 * fontScale, fontWeight: '700', color: colors.text },
    buttonPrimary: { backgroundColor: colors.primary, borderColor: colors.primary },
    buttonPrimaryText: { fontSize: 14 * fontScale, fontWeight: '700', color: '#fff' },
    buttonDanger: { backgroundColor: colors.danger, borderColor: colors.danger },
    buttonDangerText: { fontSize: 13 * fontScale, fontWeight: '700', color: '#fff' },
    buttonOutlineDanger: { borderColor: colors.danger },
    buttonOutlineDangerText: { fontSize: 13 * fontScale, fontWeight: '700', color: colors.danger },
    rowButtons: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },

    newKeyBox: {
      gap: 10,
      borderWidth: 2,
      borderColor: colors.primary,
      backgroundColor: colors.accentSoft,
      borderRadius: 14,
      padding: 16,
    },
    newKeyTitle: { fontSize: 16 * fontScale, fontWeight: '800', color: colors.text },
    keyValue: {
      fontFamily: 'monospace',
      fontSize: 13 * fontScale,
      color: colors.text,
      backgroundColor: colors.background,
      borderColor: colors.border,
      borderWidth: 1,
      borderRadius: 8,
      padding: 12,
    },

    keyRow: {
      flexDirection: 'row',
      flexWrap: 'wrap',
      alignItems: 'center',
      justifyContent: 'space-between',
      gap: 12,
      borderWidth: 1,
      borderColor: colors.border,
      borderRadius: 12,
      padding: 12,
    },
    keyRowRevoked: { opacity: 0.6 },
    keyInfo: { flex: 1, minWidth: 160, gap: 2 },
    keyName: { fontSize: 15 * fontScale, fontWeight: '700', color: colors.text },
    keyMeta: { fontSize: 12 * fontScale, color: colors.textMuted },
    revoked: { fontSize: 12 * fontScale, fontWeight: '700', color: colors.danger },

    toggleRow: {
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'space-between',
      minHeight: 44,
      marginTop: 10,
      paddingTop: 14,
      borderTopWidth: 1,
      borderTopColor: colors.border,
    },
    toggleText: { fontSize: 13 * fontScale, fontWeight: '700', color: colors.primary },
  })
}