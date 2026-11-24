import DateTimePicker, {
  DateTimePickerEvent,
} from '@react-native-community/datetimepicker'
import { createElement, useState } from 'react'
import { Platform, Pressable, StyleSheet, Text, View } from 'react-native'

import { useAccessibility } from '@/src/context/AccessibilityContext'

type Props = {
  label: string
  value: string 
  onChange: (value: string) => void
  minimumDate?: Date
}

function formatarISO(date: Date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function parseISO(value: string) {
  if (!value) return new Date()
  const [y, m, d] = value.split('-').map(Number)
  return new Date(y, m - 1, d, 12)
}

function formatarBR(value: string) {
  if (!value) return 'Selecionar data'
  const [y, m, d] = value.split('-')
  return `${d}/${m}/${y}`
}

export function DateInput({ label, value, onChange, minimumDate }: Props) {
  const { colors, fontScale } = useAccessibility()
  const [show, setShow] = useState(false)

  function handleChange(event: DateTimePickerEvent, selected?: Date) {
    if (Platform.OS === 'android') setShow(false)
    if (event.type === 'set' && selected) {
      onChange(formatarISO(selected))
    }
  }

  return (
    <View style={{ marginBottom: 14 }}>
      <Text
        style={{
          color: colors.text,
          fontSize: 13 * fontScale,
          fontWeight: '600',
          marginBottom: 6,
        }}
      >
        {label}
      </Text>

      {Platform.OS === 'web' ? (
        createElement('input', {
          type: 'date',
          value,
          min: minimumDate ? formatarISO(minimumDate) : undefined,
          onChange: (e: any) => onChange(e.target.value),
          style: {
            padding: 12,
            borderRadius: 12,
            border: `1px solid ${colors.border}`,
            backgroundColor: colors.background,
            color: colors.text,
            fontSize: 15 * fontScale,
          },
        })
      ) : (
        <>
          <Pressable
            onPress={() => setShow(true)}
            style={{
              padding: 14,
              borderRadius: 12,
              borderWidth: 1,
              borderColor: colors.border,
              backgroundColor: colors.background,
            }}
          >
            <Text
              style={{
                color: value ? colors.text : colors.textMuted,
                fontSize: 15 * fontScale,
              }}
            >
              {formatarBR(value)}
            </Text>
          </Pressable>

          {show && (
            <DateTimePicker
              value={parseISO(value)}
              mode="date"
              display={Platform.OS === 'ios' ? 'inline' : 'default'}
              minimumDate={minimumDate}
              onChange={handleChange}
            />
          )}
        </>
      )}
    </View>
  )
}