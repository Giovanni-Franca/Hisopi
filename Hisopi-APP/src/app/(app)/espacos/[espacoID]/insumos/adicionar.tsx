import { router, useLocalSearchParams } from 'expo-router'
import { useState, useMemo  } from 'react'
import { Pressable, StyleSheet, Text, View } from 'react-native'
import { KeyboardAwareScrollView } from 'react-native-keyboard-aware-scroll-view'

import { useAccessibility } from '@/src/context/AccessibilityContext'

import { AuthInput } from '@/src/components/auth/AuthInput'
import { criarInsumo, CATEGORIAS, Categoria, UNIDADES_MEDIDA, UnidadeMedida } from '@/src/services/insumoService'
import { useResponsive } from '@/src/hooks/useResponsive'
import { SelectInput } from '@/src/components/ui/SelectInput'

export default function AdicionarInsumoScreen() {
  const { espacoID } = useLocalSearchParams<{ espacoID: string }>()
  const { isDesktop } = useResponsive()
  const { colors, fontScale } = useAccessibility()

  const styles = useMemo(
    () => createStyles(colors, fontScale),
    [colors, fontScale],
  )
  const [nome, setNome] = useState('')
  const [categoria, setCategoria] = useState<Categoria | null>(null)
  const [unidadeMedida, setUnidadeMedida] = useState<UnidadeMedida | null>(null)
  const [estoqueMinimo, setEstoqueMinimo] = useState('')
  const [custoUnitario, setCustoUnitario] = useState('')
  const [erro, setErro] = useState('')
  const [saving, setSaving] = useState(false)

  async function handleSalvar() {
    setErro('')

    if (!nome.trim() || !unidadeMedida || !estoqueMinimo.trim()) {
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

      await criarInsumo(espacoID, {
        nome: nome.trim(),
        categoria: categoria || null,
        unidadeMedida: unidadeMedida.trim(),
        estoqueMinimo: estoqueMinimoNum,
        custoUnitario: custoUnitario
          ? Number(custoUnitario.replace(',', '.'))
          : null,
      })

      router.replace(`/espacos/${espacoID}/insumos` as any)
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

        <SelectInput
          label="Categoria (opcional)"
          placeholder="Selecione uma categoria"
          options={CATEGORIAS}
          value={categoria}
          onChange={setCategoria}
        />

        <SelectInput
          label="Unidade de medida"
          placeholder="Selecione a unidade"
          options={UNIDADES_MEDIDA}
          value={unidadeMedida}
          onChange={setUnidadeMedida}
          allowClear={false}
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

function createStyles(
  colors: typeof import('@/src/theme/colors').colors,
  fontScale: number,
) {
  return StyleSheet.create({
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
    card: {
      width: '100%',
      maxWidth: 480,
      backgroundColor: colors.surface,
      borderRadius: 20,
      padding: 28,
      borderWidth: 1,
      borderColor: colors.border,
    },
    title: {
      fontSize: 24 * fontScale,
      fontWeight: '800',
      color: colors.text,
      marginBottom: 8,
    },
    subtitle: {
      fontSize: 14 * fontScale,
      color: colors.textMuted,
      lineHeight: 20 * fontScale,
      marginBottom: 24,
    },
    errorBanner: {
      backgroundColor: colors.dangerSoft,
      color: colors.danger,
      padding: 12,
      borderRadius: 10,
      marginBottom: 18,
      fontSize: 14 * fontScale,
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
      fontSize: 16 * fontScale,
      fontWeight: '700',
    },
  })
}