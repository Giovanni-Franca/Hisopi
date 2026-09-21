import { router, useFocusEffect, useLocalSearchParams } from 'expo-router'
import { useCallback, useState } from 'react'
import {
  ActivityIndicator,
  FlatList,
  Modal,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native'

import { useAuth } from '@/src/context/AuthContext'
import { AuthInput } from '@/src/components/auth/AuthInput'
import { colors } from '@/src/theme/colors'
import { buscarEspaco, type PapelMembro } from '@/src/services/espacoService'
import {
  listarMembros,
  convidarMembro,
  removerMembro,
  type MembroEspaco,
} from '@/src/services/membroService'

const PAPEL_LABEL: Record<PapelMembro, string> = {
  DONO: 'Dono',
  ADMIN: 'Admin',
  GERENTE: 'Gerente',
  OPERADOR: 'Operador',
}

// DONO não é atribuível ao convidar — só existe um, criado
// automaticamente junto com o espaço.
const PAPEIS_CONVITE: PapelMembro[] = ['ADMIN', 'GERENTE', 'OPERADOR']

export default function MembrosScreen() {
  const { espacoID } = useLocalSearchParams<{ espacoID: string }>()
  const { usuario } = useAuth()

  const [tipoEspaco, setTipoEspaco] = useState<'PESSOAL' | 'ORGANIZACAO' | null>(null)
  const [membros, setMembros] = useState<MembroEspaco[]>([])
  const [loading, setLoading] = useState(true)

  const [modalVisible, setModalVisible] = useState(false)
  const [email, setEmail] = useState('')
  const [papel, setPapel] = useState<PapelMembro>('OPERADOR')
  const [erroModal, setErroModal] = useState('')
  const [saving, setSaving] = useState(false)

  const load = useCallback(async () => {
    if (!espacoID) return

    try {
      const [espaco, dataMembros] = await Promise.all([
        buscarEspaco(espacoID),
        listarMembros(espacoID),
      ])

      setTipoEspaco(espaco.tipo)
      setMembros(dataMembros)
    } catch (error) {
      console.log('Erro ao carregar membros:', error)
    } finally {
      setLoading(false)
    }
  }, [espacoID])

  useFocusEffect(
    useCallback(() => {
      load()
    }, [load])
  )

  function abrirModal() {
    setEmail('')
    setPapel('OPERADOR')
    setErroModal('')
    setModalVisible(true)
  }

  async function handleConvidar() {
    setErroModal('')

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

    if (!email.trim() || !emailRegex.test(email.trim())) {
      setErroModal('Informe um e-mail válido.')
      return
    }

    try {
      setSaving(true)
      await convidarMembro(espacoID, email.trim(), papel)
      setModalVisible(false)
      await load()
    } catch (error) {
      setErroModal(
        error instanceof Error
          ? error.message
          : 'Não foi possível adicionar este membro.'
      )
    } finally {
      setSaving(false)
    }
  }

  async function handleRemover(membro: MembroEspaco) {
    try {
      await removerMembro(espacoID, membro.id)
      await load()
    } catch (error) {
      console.log('Erro ao remover membro:', error)
    }
  }

  if (loading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    )
  }

  // Espaços pessoais não têm conceito de membros além do próprio dono.
  if (tipoEspaco === 'PESSOAL') {
    return (
      <View style={styles.center}>
        <Text style={styles.blockedTitle}>Não disponível</Text>
        <Text style={styles.blockedText}>
          Espaços pessoais não possuem outros membros além de você.
        </Text>
        <Pressable
          style={styles.blockedButton}
          onPress={() => router.push(`/espacos/${espacoID}` as any)}
        >
          <Text style={styles.blockedButtonText}>Voltar ao dashboard</Text>
        </Pressable>
      </View>
    )
  }

  return (
    <View style={styles.screen}>
      <View style={styles.content}>
        <View style={styles.header}>
          <View>
            <Text style={styles.title}>Membros</Text>
            <Text style={styles.subtitle}>
              {membros.length} {membros.length === 1 ? 'pessoa' : 'pessoas'} com acesso a este espaço
            </Text>
          </View>

          <Pressable style={styles.newButton} onPress={abrirModal}>
            <Text style={styles.newButtonText}>+ Convidar</Text>
          </Pressable>
        </View>

        <FlatList
          data={membros}
          keyExtractor={(item) => String(item.id)}
          scrollEnabled={false}
          ItemSeparatorComponent={() => <View style={{ height: 10 }} />}
          renderItem={({ item }) => {
            const souEu = item.usuario.id === usuario?.id
            const ehDono = item.papel === 'DONO'

            return (
              <View style={styles.membroCard}>
                <View style={styles.avatar}>
                  <Text style={styles.avatarText}>
                    {item.usuario.nome.charAt(0).toUpperCase()}
                  </Text>
                </View>

                <View style={{ flex: 1 }}>
                  <Text style={styles.membroNome}>
                    {item.usuario.nome} {souEu && '(você)'}
                  </Text>
                  <Text style={styles.membroEmail}>{item.usuario.email}</Text>
                </View>

                <View style={styles.papelBadge}>
                  <Text style={styles.papelBadgeText}>
                    {PAPEL_LABEL[item.papel]}
                  </Text>
                </View>

                {/* O dono não pode ser removido por aqui — evitaria um
                    espaço sem ninguém responsável por ele. */}
                {!ehDono && !souEu && (
                  <Pressable
                    style={styles.removeButton}
                    onPress={() => handleRemover(item)}
                  >
                    <Text style={styles.removeButtonText}>Remover</Text>
                  </Pressable>
                )}
              </View>
            )
          }}
        />
      </View>

      {/* =====================================================
          MODAL: CONVIDAR MEMBRO
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
            <Text style={styles.modalTitle}>Convidar membro</Text>
            <Text style={styles.modalSubtitle}>
              A pessoa já precisa ter uma conta cadastrada no Hisopi.
            </Text>

            {erroModal ? <Text style={styles.errorBanner}>{erroModal}</Text> : null}

            <AuthInput
              label="E-mail"
              placeholder="pessoa@email.com"
              value={email}
              onChangeText={setEmail}
              autoCapitalize="none"
              keyboardType="email-address"
            />

            <Text style={styles.fieldLabel}>Papel</Text>

            <View style={styles.papelRow}>
              {PAPEIS_CONVITE.map((p) => (
                <Pressable
                  key={p}
                  style={[styles.papelOption, papel === p && styles.papelOptionActive]}
                  onPress={() => setPapel(p)}
                >
                  <Text
                    style={[
                      styles.papelOptionText,
                      papel === p && styles.papelOptionTextActive,
                    ]}
                  >
                    {PAPEL_LABEL[p]}
                  </Text>
                </Pressable>
              ))}
            </View>

            <View style={styles.modalActions}>
              <Pressable
                style={styles.cancelButton}
                onPress={() => setModalVisible(false)}
              >
                <Text style={styles.cancelButtonText}>Cancelar</Text>
              </Pressable>

              <Pressable
                style={[styles.confirmButton, saving && styles.disabledButton]}
                onPress={handleConvidar}
                disabled={saving}
              >
                <Text style={styles.confirmButtonText}>
                  {saving ? 'Adicionando...' : 'Adicionar'}
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
    padding: 24,
  },

  content: {
    padding: 20,
    maxWidth: 720,
    width: '100%',
    alignSelf: 'center',
  },

  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-end',
    marginBottom: 20,
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
  },

  newButtonText: {
    color: '#fff',
    fontWeight: '700',
    fontSize: 14,
  },

  membroCard: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    backgroundColor: colors.surface,
    borderRadius: 14,
    padding: 14,
    borderWidth: 1,
    borderColor: colors.border,
  },

  avatar: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
  },

  avatarText: {
    color: '#fff',
    fontSize: 17,
    fontWeight: '800',
  },

  membroNome: {
    fontSize: 14,
    fontWeight: '700',
    color: colors.text,
  },

  membroEmail: {
    fontSize: 12,
    color: colors.textMuted,
    marginTop: 2,
  },

  papelBadge: {
    backgroundColor: colors.accentSoft,
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 999,
  },

  papelBadgeText: {
    fontSize: 11,
    fontWeight: '700',
    color: colors.primary,
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

  blockedTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: colors.text,
    marginBottom: 8,
  },

  blockedText: {
    fontSize: 14,
    color: colors.textMuted,
    textAlign: 'center',
    marginBottom: 24,
  },

  blockedButton: {
    backgroundColor: colors.primary,
    paddingVertical: 14,
    paddingHorizontal: 24,
    borderRadius: 12,
  },

  blockedButtonText: {
    color: '#fff',
    fontWeight: '700',
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
    marginBottom: 4,
  },

  modalSubtitle: {
    fontSize: 13,
    color: colors.textMuted,
    marginBottom: 18,
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
    marginBottom: 10,
  },

  papelRow: {
    flexDirection: 'row',
    gap: 8,
    marginBottom: 20,
  },

  papelOption: {
    flex: 1,
    paddingVertical: 10,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: colors.border,
    alignItems: 'center',
  },

  papelOptionActive: {
    borderColor: colors.primary,
    backgroundColor: colors.accentSoft,
  },

  papelOptionText: {
    fontSize: 12,
    fontWeight: '700',
    color: colors.textMuted,
  },

  papelOptionTextActive: {
    color: colors.primary,
  },

  modalActions: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    gap: 12,
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