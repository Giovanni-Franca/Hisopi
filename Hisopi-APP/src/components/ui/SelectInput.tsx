import { useMemo, useState } from 'react'
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native'

import { useAccessibility } from '@/src/context/AccessibilityContext'

type Option<T extends string> = { value: T; label: string }

type Props<T extends string> = {
  label: string
  options: Option<T>[]
  value: T | null
  onChange: (value: T | null) => void
  placeholder?: string
  allowClear?: boolean
}

export function SelectInput<T extends string>({
  label,
  options,
  value,
  onChange,
  placeholder = 'Selecione',
  allowClear = true,
}: Props<T>) {
  const { colors, fontScale } = useAccessibility()
  const styles = useMemo(() => createStyles(colors, fontScale), [colors, fontScale])
  const [open, setOpen] = useState(false)

  const selecionado = options.find((o) => o.value === value)

  function escolher(novo: T | null) {
    onChange(novo)
    setOpen(false)
  }

  return (
    <View style={[styles.wrapper, open && styles.wrapperOpen]}>
      <Text style={styles.label}>{label}</Text>

      <Pressable
        style={[styles.trigger, open && styles.triggerOpen]}
        onPress={() => setOpen((o) => !o)}
        accessibilityRole="button"
        accessibilityLabel={label}
        accessibilityState={{ expanded: open }}
      >
        <Text
          style={[styles.triggerText, !selecionado && styles.placeholder]}
          numberOfLines={1}
        >
          {selecionado?.label ?? placeholder}
        </Text>
        <Text style={styles.arrow}>{open ? '▴' : '▾'}</Text>
      </Pressable>

      {open && (
        <View style={styles.dropdown}>
          <ScrollView
            style={styles.list}
            nestedScrollEnabled
            keyboardShouldPersistTaps="handled"
          >
            {allowClear && (
              <Pressable style={styles.option} onPress={() => escolher(null)}>
                <Text style={[styles.optionText, styles.placeholder]}>Nenhuma</Text>
              </Pressable>
            )}

            {options.map((item) => {
              const ativo = item.value === value
              return (
                <Pressable
                  key={item.value}
                  style={[styles.option, ativo && styles.optionActive]}
                  onPress={() => escolher(item.value)}
                >
                  <Text style={[styles.optionText, ativo && styles.optionTextActive]}>
                    {item.label}
                  </Text>
                  {ativo ? <Text style={styles.check}>✓</Text> : null}
                </Pressable>
              )
            })}
          </ScrollView>
        </View>
      )}
    </View>
  )
}

function createStyles(
  colors: typeof import('@/src/theme/colors').colors,
  fontScale: number,
) {
  return StyleSheet.create({
    wrapper: {
      marginBottom: 18,
      position: 'relative',
    },
    // garante que a lista aberta fique por cima dos campos seguintes
    wrapperOpen: {
      zIndex: 10,
      elevation: 10,
    },
    label: {
      fontSize: 14 * fontScale,
      fontWeight: '600',
      color: colors.text,
      marginBottom: 8,
    },
    trigger: {
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'space-between',
      borderWidth: 1,
      borderColor: colors.border,
      backgroundColor: colors.surface,
      borderRadius: 12,
      paddingVertical: 14,
      paddingHorizontal: 14,
    },
    triggerOpen: {
      borderColor: colors.primary,
    },
    triggerText: { flex: 1, fontSize: 15 * fontScale, color: colors.text },
    placeholder: { color: colors.textMuted },
    arrow: { fontSize: 14 * fontScale, color: colors.textMuted, marginLeft: 8 },
    dropdown: {
      position: 'absolute',
      top: '100%',
      left: 0,
      right: 0,
      marginTop: 4,
      backgroundColor: colors.surface,
      borderWidth: 1,
      borderColor: colors.border,
      borderRadius: 12,
      overflow: 'hidden',
      shadowColor: '#000',
      shadowOpacity: 0.12,
      shadowRadius: 8,
      shadowOffset: { width: 0, height: 4 },
      elevation: 8,
    },
    list: {
      maxHeight: 220,
    },
    option: {
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'space-between',
      paddingVertical: 13,
      paddingHorizontal: 14,
    },
    optionActive: { backgroundColor: colors.background },
    optionText: { fontSize: 15 * fontScale, color: colors.text },
    optionTextActive: { fontWeight: '700', color: colors.primary },
    check: { fontSize: 15 * fontScale, color: colors.primary, fontWeight: '700' },
  })
}