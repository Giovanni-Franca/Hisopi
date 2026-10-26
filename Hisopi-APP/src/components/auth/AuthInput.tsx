
import { useMemo, useId } from 'react'
import {
  StyleSheet,
  Text,
  TextInput,
  type TextInputProps,
  View,
} from 'react-native'

import { useAccessibility } from '@/src/context/AccessibilityContext'

type AuthInputProps = TextInputProps & {
  label: string
  error?: string
}

export function AuthInput({
  label,
  error,
  style,
  ...rest
}: AuthInputProps) {
  const errorId = useId()
  const { colors, fontScale } = useAccessibility()

  const styles = useMemo(
    () =>
      StyleSheet.create({
        wrapper: {
          marginBottom: 18,
        },
        label: {
          fontSize: 13 * fontScale,
          fontWeight: '700',
          color: colors.text,
          marginBottom: 6,
        },
        input: {
          backgroundColor: colors.surface,
          borderWidth: 1,
          borderColor: colors.border,
          borderRadius: 12,
          paddingHorizontal: 14,
          paddingVertical: 13,
          fontSize: 15 * fontScale,
          color: colors.text,
        },
        inputError: {
          borderColor: colors.danger,
        },
        errorText: {
          marginTop: 6,
          fontSize: 12 * fontScale,
          color: colors.danger,
        },
      }),
    [colors, fontScale],
  )

  return (
    <View style={styles.wrapper}>
      <Text style={styles.label}>{label}</Text>

      <TextInput
        placeholderTextColor={colors.textMuted}
        style={[styles.input, error && styles.inputError, style]}
        accessibilityLabel={label}
        accessibilityHint={error ?? undefined}
        accessibilityState={{ disabled: rest.editable === false }}
        {...rest}
      />

      {error ? (
        <Text
          nativeID={errorId}
          style={styles.errorText}
          accessibilityRole="alert"
          accessibilityLiveRegion="polite"
        >
          {error}
        </Text>
      ) : null}
    </View>
  )
}
