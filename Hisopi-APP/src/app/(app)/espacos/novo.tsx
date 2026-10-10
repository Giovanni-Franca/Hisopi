import { router } from 'expo-router'
import { useState } from 'react'
import { AccessibilityInfo,Pressable, StyleSheet, Text, View } from 'react-native'
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
  const[erroNome, setErroNome] = useState('')
  const { isDesktop } = useResponsive()

  function mostrarErro(mensagem: string) {
    setErro(mensagem)
    AccessibilityInfo.announceForAccessibility(mensagem)
  }

  async function handleCriar() {
    setErro('')

    if (!nome.trim()) {
      setErroNome('Dê um nome para o seu espaço.')
      return
    }

    if (!usuario) return

    try {
      setSaving(true)
      await criarEspaco(nome.trim(), tipo)
      router.replace('/espacos')
    } catch (error) {
      mostrarErro(
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
      <Pressable
        style={styles.backButton}
        onPress={() => router.push('/espacos')}
        accessibilityRole="button"
        accessibilityLabel="Voltar para a lista de espaços"
        hitSlop={8}
      >
        <Text style={styles.backButtonText} maxFontSizeMultiplier={1.4}>
          Voltar
        </Text>
      </Pressable>
    )}

    <View style={styles.card}>
      <Text
        style={styles.title}
        accessibilityRole="header"
        maxFontSizeMultiplier={1.5}
      >
        Criar novo espaço
      </Text>
      <Text style={styles.subtitle} maxFontSizeMultiplier={1.5}>
        Um espaço pessoal é para controlar sua própria despensa. Um
        espaço de organização permite adicionar outras pessoas
        trabalhando junto com você.
      </Text>

      {erro ? (
        <Text
          style={styles.errorBanner}
          accessibilityRole="alert"
          accessibilityLiveRegion="assertive"
          maxFontSizeMultiplier={1.5}
        >
          {erro}
        </Text>
      ) : null}

      <AuthInput
        label="Nome do espaço"
        placeholder="Ex: Minha casa, Padaria do João..."
        value={nome}
        onChangeText={setNome}
        aria-required
        error={erroNome}
      />

      <Text
        style={styles.fieldLabel}
        nativeID="tipo-espaco-label"
        maxFontSizeMultiplier={1.5}
      >
        Tipo de espaço
      </Text>

      <View
        style={styles.tipoRow}
        accessibilityRole="radiogroup"
        accessibilityLabel="Tipo de espaço"
        aria-labelledby="tipo-espaco-label"
      >
        <Pressable
          style={[
            styles.tipoCard,
            tipo === 'PESSOAL' && styles.tipoCardActive,
          ]}
          onPress={() => setTipo('PESSOAL')}
          accessibilityRole="radio"
          accessibilityState={{ checked: tipo === 'PESSOAL' }}
          accessibilityLabel="Pessoal. Só você usa. Ideal para sua casa."
        >
          <View style={styles.tipoContent}>
            <Text
              style={styles.tipoIcon}
              accessibilityElementsHidden
              importantForAccessibility="no-hide-descendants"
            >
              P
            </Text>
            <Text
              style={[
                styles.tipoTitle,
                tipo === 'PESSOAL' && styles.tipoTitleActive,
              ]}
              maxFontSizeMultiplier={1.4}
            >
              Pessoal
            </Text>
            <Text style={styles.tipoDescricao} maxFontSizeMultiplier={1.4}>
              Só você usa. Ideal para sua casa.
            </Text>
          </View>
        </Pressable>

        <Pressable
          style={[
            styles.tipoCard,
            tipo === 'ORGANIZACAO' && styles.tipoCardActive,
          ]}
          onPress={() => setTipo('ORGANIZACAO')}
          accessibilityRole="radio"
          accessibilityState={{ checked: tipo === 'ORGANIZACAO' }}
          accessibilityLabel="Organização. Convide colaboradores. Ideal para negócios."
        >
          <View style={styles.tipoContent}>
            <Text
              style={styles.tipoIcon}
              accessibilityElementsHidden
              importantForAccessibility="no-hide-descendants"
            >
              O
            </Text>
            <Text
              style={[
                styles.tipoTitle,
                tipo === 'ORGANIZACAO' && styles.tipoTitleActive,
              ]}
              maxFontSizeMultiplier={1.4}
            >
              Organização
            </Text>
            <Text style={styles.tipoDescricao} maxFontSizeMultiplier={1.4}>
              Convide colaboradores. Ideal para negócios.
            </Text>
          </View>
        </Pressable>
      </View>

      <Pressable
        style={[styles.submitButton, saving && styles.submitButtonDisabled]}
        onPress={handleCriar}
        disabled={saving}
        accessibilityRole="button"
        accessibilityLabel={saving ? 'Criando espaço, aguarde' : 'Criar espaço'}
        accessibilityState={{ disabled: saving, busy: saving }}
        hitSlop={8}
      >
        <Text style={styles.submitButtonText} maxFontSizeMultiplier={1.4}>
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

  submitButtonDisabled: {
    opacity: 0.6,
  },

  submitButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '700',
  },
  backButton: {
    alignSelf: 'flex-start',
    backgroundColor: colors.text,
    minHeight: 44,
    justifyContent: 'center', // novo
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderRadius: 12,
    marginBottom: 20,
  },

  tipoCard: {
    flex: 1,
    minHeight: 44, // novo
    borderWidth: 1.5,
    borderColor: colors.border,
    borderRadius: 14,
    padding: 16,
  },

  // novo: o alignItems saiu do tipoCard e veio para cá
  tipoContent: {
    alignItems: 'center',
    pointerEvents: 'none',
  },

  tipoDescricao: {
    fontSize: 12, // era 11
    color: colors.textMuted,
    textAlign: 'center',
    lineHeight: 16,
  },

  submitButton: {
    backgroundColor: colors.primary,
    minHeight: 48, // novo
    justifyContent: 'center', // novo
    paddingVertical: 15,
    borderRadius: 12,
    alignItems: 'center',
  },

})