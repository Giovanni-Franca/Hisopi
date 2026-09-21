import { router } from 'expo-router'
import { useState } from 'react'
import { Pressable, StyleSheet, Text, View } from 'react-native'
import { KeyboardAwareScrollView } from 'react-native-keyboard-aware-scroll-view'

import { useAuth } from '@/src/context/AuthContext'
import { AuthInput } from '@/src/components/auth/AuthInput'
import { criarEspaco, type TipoEspaco } from '@/src/services/espacoService'
import { colors } from '@/src/theme/colors'
import { useResponsive } from '@/src/hooks/useResponsive'

export default function NovoEspacoScreen() {
  const { usuario } = useAuth()

  const [nome, setNome] = useState('')
  const [tipo, setTipo] = useState<TipoEspaco>('PESSOAL')
  const [erro, setErro] = useState('')
  const [saving, setSaving] = useState(false)
  const { isDesktop } = useResponsive()

  async function handleCriar() {
    setErro('')

    if (!nome.trim()) {
      setErro('Dê um nome para o seu espaço.')
      return
    }

    if (!usuario) return

    try {
      setSaving(true)
      await criarEspaco(nome.trim(), tipo)
      router.replace('/espacos')
    } catch (error) {
      setErro(
        error instanceof Error
          ? error.message
          : 'Não foi possível criar o espaço. Tente novamente.'
      )
    } finally {
      setSaving(false)
    }
  }

  return (
    <KeyboardAwareScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
      enableOnAndroid
      keyboardShouldPersistTaps="handled"
    >
      {!isDesktop && (
      <Pressable style={styles.backButton} onPress={() => router.push(`/espacos`)}>
        <Text style={styles.backButtonText}>Voltar</Text>
      </Pressable>
      )}

      <View style={styles.card}>
        <Text style={styles.title}>Criar novo espaço</Text>
        <Text style={styles.subtitle}>
          Um espaço pessoal é para controlar sua própria despensa. Um
          espaço de organização permite adicionar outras pessoas
          trabalhando junto com você.
        </Text>

        {erro ? <Text style={styles.errorBanner}>{erro}</Text> : null}

        <AuthInput
          label="Nome do espaço"
          placeholder="Ex: Minha casa, Padaria do João..."
          value={nome}
          onChangeText={setNome}
        />

        <Text style={styles.fieldLabel}>Tipo de espaço</Text>

        <View style={styles.tipoRow}>
          <Pressable
            style={[
              styles.tipoCard,
              tipo === 'PESSOAL' && styles.tipoCardActive,
            ]}
            onPress={() => setTipo('PESSOAL')}
          >
            <Text style={styles.tipoIcon}>P</Text>
            <Text
              style={[
                styles.tipoTitle,
                tipo === 'PESSOAL' && styles.tipoTitleActive,
              ]}
            >
              Pessoal
            </Text>
            <Text style={styles.tipoDescricao}>
              Só você usa. Ideal para sua casa.
            </Text>
          </Pressable>

          <Pressable
            style={[
              styles.tipoCard,
              tipo === 'ORGANIZACAO' && styles.tipoCardActive,
            ]}
            onPress={() => setTipo('ORGANIZACAO')}
          >
            <Text style={styles.tipoIcon}>O</Text>
            <Text
              style={[
                styles.tipoTitle,
                tipo === 'ORGANIZACAO' && styles.tipoTitleActive,
              ]}
            >
              Organização
            </Text>
            <Text style={styles.tipoDescricao}>
              Convide colaboradores. Ideal para negócios.
            </Text>
          </Pressable>
        </View>

        <Pressable
          style={[styles.submitButton, saving && styles.submitButtonDisabled]}
          onPress={handleCriar}
          disabled={saving}
        >
          <Text style={styles.submitButtonText}>
            {saving ? 'Criando...' : 'Criar espaço'}
          </Text>
        </Pressable>
      </View>
    </KeyboardAwareScrollView>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },

  content: {
    flexGrow: 1,
    padding: 20,
    paddingBottom: 40,
    alignItems: 'center',
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

  card: {
    width: '100%',
    maxWidth: 480,
    backgroundColor: colors.surface,
    borderRadius: 20,
    padding: 28,
  },

  title: {
    fontSize: 24,
    fontWeight: '800',
    color: colors.text,
    marginBottom: 8,
  },

  subtitle: {
    fontSize: 14,
    color: colors.textMuted,
    lineHeight: 20,
    marginBottom: 24,
  },

  errorBanner: {
    backgroundColor: colors.dangerSoft,
    color: colors.danger,
    padding: 12,
    borderRadius: 10,
    marginBottom: 18,
    fontSize: 14,
  },

  fieldLabel: {
    fontSize: 13,
    fontWeight: '700',
    color: colors.text,
    marginBottom: 10,
  },

  tipoRow: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 28,
  },

  tipoCard: {
    flex: 1,
    borderWidth: 1.5,
    borderColor: colors.border,
    borderRadius: 14,
    padding: 16,
    alignItems: 'center',
  },

  tipoCardActive: {
    borderColor: colors.primary,
    backgroundColor: colors.accentSoft,
  },

  tipoIcon: {
    fontSize: 28,
    marginBottom: 8,
  },

  tipoTitle: {
    fontSize: 14,
    fontWeight: '700',
    color: colors.text,
    marginBottom: 4,
  },

  tipoTitleActive: {
    color: colors.primary,
  },

  tipoDescricao: {
    fontSize: 11,
    color: colors.textMuted,
    textAlign: 'center',
    lineHeight: 15,
  },

  submitButton: {
    backgroundColor: colors.primary,
    paddingVertical: 15,
    borderRadius: 12,
    alignItems: 'center',
  },

  submitButtonDisabled: {
    opacity: 0.6,
  },

  submitButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '700',
  },
})