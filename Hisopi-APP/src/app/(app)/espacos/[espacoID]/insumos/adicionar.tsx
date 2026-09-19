import { router, useLocalSearchParams } from 'expo-router'
import { useState } from 'react'
import { Pressable, StyleSheet, Text, View } from 'react-native'
import { KeyboardAwareScrollView } from 'react-native-keyboard-aware-scroll-view'

import { AuthInput } from '@/src/components/auth/AuthInput'
import { criarInsumo } from '@/src/services/insumoService'
import { colors } from '@/src/theme/colors'

export default function AdicionarInsumoScreen() {
  const { espacoId } = useLocalSearchParams<{ espacoId: string }>()

  const [nome, setNome] = useState('')
  const [categoria, setCategoria] = useState('')
  const [unidadeMedida, setUnidadeMedida] = useState('')
  const [estoqueMinimo, setEstoqueMinimo] = useState('')
  const [custoUnitario, setCustoUnitario] = useState('')
  const [erro, setErro] = useState('')
  const [saving, setSaving] = useState(false)

  async function handleSalvar() {
    setErro('')

    if (!nome.trim() || !unidadeMedida.trim() || !estoqueMinimo.trim()) {
      setErro('Preencha nome, unidade de medida e estoque mínimo.')
      return
    }

    const estoqueMinimoNum = Number(estoqueMinimo.replace(',', '.'))

    if (Number.isNaN(estoqueMinimoNum) || estoqueMinimoNum < 0) {
      setErro('Estoque mínimo precisa ser um número válido.')
      return
    }

    try {
      setSaving(true)

      await criarInsumo(espacoId, {
        nome: nome.trim(),
        categoria: categoria.trim() || null,
        unidadeMedida: unidadeMedida.trim(),
        estoqueMinimo: estoqueMinimoNum,
        custoUnitario: custoUnitario
          ? Number(custoUnitario.replace(',', '.'))
          : null,
      })

      router.replace(`/${espacoId}/insumos` as any)
    } catch (error) {
      setErro(
        error instanceof Error
          ? error.message
          : 'Não foi possível cadastrar o insumo. Tente novamente.'
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
        onPress={() => router.push(`/${espacoId}/insumos` as any)}
      >
        <Text style={styles.backButtonText}>Voltar</Text>
      </Pressable>

      <View style={styles.card}>
        <Text style={styles.title}>Cadastrar insumo</Text>
        <Text style={styles.subtitle}>
          Insumos são os itens que compõem seu estoque — farinha, leite,
          embalagens, etc.
        </Text>

        {erro ? <Text style={styles.errorBanner}>{erro}</Text> : null}

        <AuthInput
          label="Nome"
          placeholder="Ex: Farinha de trigo"
          value={nome}
          onChangeText={setNome}
        />

        <AuthInput
          label="Categoria (opcional)"
          placeholder="Ex: Grãos, Laticínios..."
          value={categoria}
          onChangeText={setCategoria}
        />

        <AuthInput
          label="Unidade de medida"
          placeholder="Ex: kg, g, ml, l, un"
          value={unidadeMedida}
          onChangeText={setUnidadeMedida}
          autoCapitalize="none"
        />

        <AuthInput
          label="Estoque mínimo"
          placeholder="Quantidade para alertar reposição"
          value={estoqueMinimo}
          onChangeText={setEstoqueMinimo}
          keyboardType="numeric"
        />

        <AuthInput
          label="Custo unitário (opcional)"
          placeholder="Preço por unidade de medida"
          value={custoUnitario}
          onChangeText={setCustoUnitario}
          keyboardType="numeric"
        />

        <Pressable
          style={[styles.submitButton, saving && styles.submitButtonDisabled]}
          onPress={handleSalvar}
          disabled={saving}
        >
          <Text style={styles.submitButtonText}>
            {saving ? 'Salvando...' : 'Cadastrar insumo'}
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

  submitButton: {
    backgroundColor: colors.primary,
    paddingVertical: 15,
    borderRadius: 12,
    alignItems: 'center',
    marginTop: 8,
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