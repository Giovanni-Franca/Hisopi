import { router, useLocalSearchParams } from 'expo-router'
import { useState } from 'react'
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native'
import { KeyboardAwareScrollView } from 'react-native-keyboard-aware-scroll-view'

import { AuthInput } from '@/src/components/auth/AuthInput'
import { criarReceita, type TipoReceita } from '@/src/services/receitaService'
import { colors } from '@/src/theme/colors'

export default function AdicionarReceitaScreen() {
  const { espacoID } = useLocalSearchParams<{ espacoID: string }>()

  const [nome, setNome] = useState('')
  const [tipo, setTipo] = useState<TipoReceita>('PRODUTO_VENDA')
  const [modoPreparo, setModoPreparo] = useState('')
  const [erro, setErro] = useState('')
  const [saving, setSaving] = useState(false)

  async function handleSalvar() {
    setErro('')

    if (!nome.trim()) {
      setErro('Dê um nome para a receita.')
      return
    }

    try {
      setSaving(true)

      await criarReceita(espacoID, {
        nome: nome.trim(),
        tipo,
        modoPreparo: modoPreparo.trim() || null,
      })

      router.replace(`/espacos/${espacoID}/receitas` as any)
    } catch (error) {
      setErro(
        error instanceof Error
          ? error.message
          : 'Não foi possível cadastrar a receita. Tente novamente.'
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
      <Pressable
        style={styles.backButton}
        onPress={() => router.push(`/espacos/${espacoID}/receitas` as any)}
      >
        <Text style={styles.backButtonText}>Voltar</Text>
      </Pressable>

      <View style={styles.card}>
        <Text style={styles.title}>Nova receita</Text>
        <Text style={styles.subtitle}>
          Uma ficha técnica é para produtos que você vende — define
          quanto de cada insumo é usado. Uma sugestão de consumo é uma
          ideia para aproveitar insumos antes de vencerem.
        </Text>

        {erro ? <Text style={styles.errorBanner}>{erro}</Text> : null}

        <AuthInput
          label="Nome"
          placeholder="Ex: Pão francês, Omelete simples..."
          value={nome}
          onChangeText={setNome}
        />

        <Text style={styles.fieldLabel}>Tipo de receita</Text>

        <View style={styles.tipoRow}>
          <Pressable
            style={[
              styles.tipoCard,
              tipo === 'PRODUTO_VENDA' && styles.tipoCardActive,
            ]}
            onPress={() => setTipo('PRODUTO_VENDA')}
          >
            <Text
              style={[
                styles.tipoTitle,
                tipo === 'PRODUTO_VENDA' && styles.tipoTitleActive,
              ]}
            >
              Ficha técnica
            </Text>
            <Text style={styles.tipoDescricao}>
              Produto que você vende, com insumos precisos.
            </Text>
          </Pressable>

          <Pressable
            style={[
              styles.tipoCard,
              tipo === 'SUGESTAO_CONSUMO' && styles.tipoCardActive,
            ]}
            onPress={() => setTipo('SUGESTAO_CONSUMO')}
          >
            <Text
              style={[
                styles.tipoTitle,
                tipo === 'SUGESTAO_CONSUMO' && styles.tipoTitleActive,
              ]}
            >
              Sugestão de consumo
            </Text>
            <Text style={styles.tipoDescricao}>
              Ideia para aproveitar insumos antes de vencerem.
            </Text>
          </Pressable>
        </View>

        <Text style={styles.fieldLabel}>Modo de preparo (opcional)</Text>

        <TextInput
          value={modoPreparo}
          onChangeText={setModoPreparo}
          placeholder="Descreva o passo a passo..."
          placeholderTextColor={colors.textMuted}
          multiline
          numberOfLines={5}
          style={styles.textArea}
        />

        <Pressable
          style={[styles.submitButton, saving && styles.submitButtonDisabled]}
          onPress={handleSalvar}
          disabled={saving}
        >
          <Text style={styles.submitButtonText}>
            {saving ? 'Salvando...' : 'Criar receita'}
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
    maxWidth: 520,
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
    marginBottom: 22,
  },

  tipoCard: {
    flex: 1,
    borderWidth: 1.5,
    borderColor: colors.border,
    borderRadius: 14,
    padding: 14,
  },

  tipoCardActive: {
    borderColor: colors.primary,
    backgroundColor: colors.accentSoft,
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
    lineHeight: 15,
  },

  textArea: {
    backgroundColor: colors.background,
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 12,
    padding: 14,
    fontSize: 14,
    color: colors.text,
    minHeight: 110,
    textAlignVertical: 'top',
    marginBottom: 24,
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