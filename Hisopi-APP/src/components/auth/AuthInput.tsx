import { StyleSheet, Text, TextInput, TextInputProps, View } from 'react-native'
import { useId } from 'react'

import { colors } from '@/src/theme/colors'

type AuthInputProps = TextInputProps & {
  label: string
  error?: string
}

export function AuthInput({ label, error, style, ...rest }: AuthInputProps) {
  const errorId = useId()

  return (
    <View style={styles.wrapper}>
      <Text style={styles.label} maxFontSizeMultiplier={1.5}>
        {label}
      </Text>

      <TextInput
        placeholderTextColor={colors.textMuted}
        style={[styles.input, error && styles.inputError, style]}
        maxFontSizeMultiplier={1.5}
        accessibilityLabel={label}
        aria-invalid={!!error}
        aria-describedby={error ? errorId : undefined} 
        accessibilityHint={error ?? undefined} 
        {...rest}
      />

      {error ? (
        <Text
          nativeID={errorId}
          style={styles.errorText}
          accessibilityRole="alert"
          accessibilityLiveRegion="polite"
          maxFontSizeMultiplier={1.5}
        >
          {error}
        </Text>
      ) : null}
    </View>
  )
}

const styles = StyleSheet.create({
  wrapper: {
    marginBottom: 18,
  },

  label: {
    fontSize: 13,
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
    fontSize: 15,
    color: colors.text,
  },

  inputError: {
    borderColor: colors.danger,
  },

  errorText: {
    marginTop: 6,
    fontSize: 12,
    color: colors.danger,
  },
})