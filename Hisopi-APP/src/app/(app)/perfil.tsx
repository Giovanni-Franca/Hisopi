import { router, useNavigation } from 'expo-router'
import {
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native'
import { useMemo } from 'react'

import { useAuth } from '@/src/context/AuthContext'
import { useAccessibility } from '@/src/context/AccessibilityContext'
import { useResponsive } from '@/src/hooks/useResponsive'

export default function PerfilScreen() {
  const { usuario, logout } = useAuth()
  const { isDesktop } = useResponsive()
  const {
    highContrast,
    setHighContrast,
    fontSize,
    setFontSize,
    fontScale,
    colors,
  } = useAccessibility()
  const navigation = useNavigation()

  const styles = useMemo(
    () => createStyles(colors, fontScale),
    [colors, fontScale],
  )

  async function handleLogout() {
    await logout()
    router.replace('/login')
  }

  const inicial = usuario?.nome?.charAt(0)?.toUpperCase() ?? '?'
  const roleLabel =
    usuario?.role === 'ADMIN' ? 'Administrador' : 'Usuário'

  return (
    <ScrollView
      style={styles.screen}
      contentContainerStyle={styles.content}
    >
      <Pressable
        onPress={() => {
          if (navigation.canGoBack()) {
            router.back()
          } else {
            router.replace('/espacos')
          }
        }}
        style={styles.backButton}
        accessibilityRole="button"
        accessibilityLabel="Voltar"
        accessibilityHint="Retorna à tela anterior ou à lista de espaços"
        hitSlop={8}
      >
        <Text style={styles.backButtonText}>← Voltar</Text>
      </Pressable>
      <View style={[styles.card, isDesktop && styles.cardDesktop]}>
        <View style={styles.avatar}>
          <Text style={styles.avatarText}>{inicial}</Text>
        </View>

        <Text style={styles.nome}>
          {usuario?.nome ?? 'Usuário'}
        </Text>
        <Text style={styles.email}>
          {usuario?.email ?? 'E-mail não informado'}
        </Text>

        <View style={styles.roleBadge}>
          <Text style={styles.roleBadgeText}>{roleLabel}</Text>
        </View>

        <View style={styles.infoSection}>
          <InfoRow label="Nome" value={usuario?.nome ?? '—'} styles={styles} />
          <InfoRow label="E-mail" value={usuario?.email ?? '—'} styles={styles} />
          <InfoRow label="Tipo de conta" value={roleLabel} styles={styles} />
        </View>

        <View
          style={styles.accessibilitySection}
          accessibilityLabel="Configurações de acessibilidade"
        >
          <Text style={styles.sectionTitle}>Acessibilidade</Text>
          <Text style={styles.description}>
            Personalize a aparência para facilitar a leitura.
          </Text>

          <View style={styles.settingRow}>
            <View style={styles.settingTextContainer}>
              <Text style={styles.settingTitle}>Alto contraste</Text>
              <Text style={styles.description}>
                Aumenta a distinção entre textos, fundos e bordas.
              </Text>
            </View>

            <Pressable
              accessibilityRole="switch"
              accessibilityLabel="Alto contraste"
              accessibilityState={{ checked: highContrast }}
              onPress={() => setHighContrast(!highContrast)}
              style={[
                styles.switchButton,
                highContrast && styles.switchButtonActive,
              ]}
            >
              <Text
                style={[
                  styles.switchText,
                  highContrast && styles.switchTextActive,
                ]}
              >
                {highContrast ? 'Ativado' : 'Desativado'}
              </Text>
            </Pressable>
          </View>

          <Text style={styles.settingTitle}>Tamanho da fonte</Text>

          <View style={styles.fontOptions}>
            {([
              { value: 'normal', label: 'Normal' },
              { value: 'large', label: 'Grande' },
              { value: 'extraLarge', label: 'Extra grande' },
            ] as const).map((option) => {
              const selected = fontSize === option.value

              return (
                <Pressable
                  key={option.value}
                  accessibilityRole="radio"
                  accessibilityLabel={`Fonte ${option.label}`}
                  accessibilityState={{ selected }}
                  onPress={() => setFontSize(option.value)}
                  style={[
                    styles.fontOption,
                    selected && styles.fontOptionSelected,
                  ]}
                >
                  <Text
                    style={[
                      styles.fontOptionText,
                      selected && styles.fontOptionTextSelected,
                    ]}
                  >
                    {option.label}
                  </Text>
                </Pressable>
              )
            })}
          </View>

          <Text style={styles.previewLabel}>Prévia do texto</Text>
          <Text style={styles.previewText}>
            O Hisopi ajuda a organizar os insumos e acompanhar suas validades.
          </Text>
        </View>

        <Pressable
          accessibilityRole="button"
          accessibilityLabel="Sair da conta"
          onPress={handleLogout}
          style={styles.logoutButton}
        >
          <Text style={styles.logoutButtonText}>Sair da conta</Text>
        </Pressable>
      </View>
    </ScrollView>
  )
}

function InfoRow({
  label,
  value,
  styles,
}: {
  label: string
  value: string
  styles: ReturnType<typeof createStyles>
}) {
  return (
    <View style={styles.infoRow}>
      <Text style={styles.infoLabel}>{label}</Text>
      <Text style={styles.infoValue}>{value}</Text>
    </View>
  )
}

function createStyles(
  colors: {
    primary: string
    secondary: string
    accent: string
    accentSoft: string
    text: string
    textMuted: string
    background: string
    surface: string
    border: string
    danger: string
  },
  fontScale: number,
) {
  return StyleSheet.create({
    screen: {
      flex: 1,
      backgroundColor: colors.background,
    },
    content: {
      alignItems: 'center',
      padding: 24,
    },
    card: {
      width: '100%',
      maxWidth: 480,
      backgroundColor: colors.surface,
      borderColor: colors.border,
      borderWidth: 1,
      borderRadius: 20,
      padding: 24,
      alignItems: 'center',
      marginTop: 24,
    },
    cardDesktop: {
      marginTop: 40,
      elevation: 3,
      shadowColor: '#000',
      shadowOpacity: 0.12,
      shadowRadius: 16,
      shadowOffset: { width: 0, height: 8 },
    },
    avatar: {
      width: 84,
      height: 84,
      borderRadius: 42,
      backgroundColor: colors.primary,
      alignItems: 'center',
      justifyContent: 'center',
      marginBottom: 16,
    },
    avatarText: {
      fontSize: 32 * fontScale,
      fontWeight: '800',
      color: '#FFFFFF',
    },
    nome: {
      fontSize: 22 * fontScale,
      fontWeight: '800',
      color: colors.text,
      textAlign: 'center',
    },
    email: {
      fontSize: 14 * fontScale,
      color: colors.textMuted,
      marginTop: 4,
      textAlign: 'center',
    },
    roleBadge: {
      backgroundColor: colors.accentSoft,
      borderWidth: 1,
      borderColor: colors.border,
      paddingHorizontal: 14,
      paddingVertical: 6,
      borderRadius: 999,
      marginTop: 14,
    },
    roleBadgeText: {
      fontSize: 12 * fontScale,
      fontWeight: '700',
      color: colors.text,
    },
    infoSection: {
      width: '100%',
      marginTop: 28,
      gap: 14,
    },
    backButton: {
      minHeight: 44,
      alignSelf: 'flex-start',
      justifyContent: 'center',
      paddingHorizontal: 12,
      paddingVertical: 8,
      borderRadius: 10,
      borderWidth: 1,
      borderColor: colors.border,
      marginBottom: 20,
    },
    backButtonText: {
      color: colors.text,
      fontSize: 14 * fontScale,
      fontWeight: '700',
    },
    infoRow: {
      borderBottomWidth: 1,
      borderBottomColor: colors.border,
      paddingBottom: 12,
    },
    infoLabel: {
      fontSize: 12 * fontScale,
      fontWeight: '700',
      color: colors.textMuted,
      marginBottom: 4,
      textTransform: 'uppercase',
      letterSpacing: 0.5,
    },
    infoValue: {
      fontSize: 15 * fontScale,
      color: colors.text,
    },
    accessibilitySection: {
      width: '100%',
      marginTop: 28,
      paddingTop: 20,
      borderTopWidth: 1,
      borderTopColor: colors.border,
      gap: 12,
    },
    sectionTitle: {
      fontSize: 20 * fontScale,
      fontWeight: '800',
      color: colors.text,
    },
    description: {
      fontSize: 13 * fontScale,
      color: colors.textMuted,
      lineHeight: 20 * fontScale,
      flexShrink: 1,
    },
    settingRow: {
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'space-between',
      gap: 12,
      marginVertical: 8,
    },
    settingTextContainer: {
      flex: 1,
      gap: 4,
    },
    settingTitle: {
      fontSize: 15 * fontScale,
      fontWeight: '700',
      color: colors.text,
    },
    switchButton: {
      borderWidth: 2,
      borderColor: colors.border,
      borderRadius: 10,
      paddingHorizontal: 12,
      paddingVertical: 10,
      alignItems: 'center',
      justifyContent: 'center',
    },
    switchButtonActive: {
      backgroundColor: colors.primary,
      borderColor: colors.primary,
    },
    switchText: {
      fontSize: 12 * fontScale,
      fontWeight: '700',
      color: colors.text,
    },
    switchTextActive: {
      color: '#FFFFFF',
    },
    fontOptions: {
      flexDirection: 'row',
      flexWrap: 'wrap',
      gap: 8,
    },
    fontOption: {
      flexGrow: 1,
      borderWidth: 2,
      borderColor: colors.border,
      borderRadius: 10,
      paddingHorizontal: 10,
      paddingVertical: 12,
      alignItems: 'center',
    },
    fontOptionSelected: {
      backgroundColor: colors.primary,
      borderColor: colors.primary,
    },
    fontOptionText: {
      fontSize: 12 * fontScale,
      fontWeight: '600',
      color: colors.text,
      textAlign: 'center',
    },
    fontOptionTextSelected: {
      color: '#FFFFFF',
    },
    previewLabel: {
      fontSize: 12 * fontScale,
      fontWeight: '700',
      color: colors.textMuted,
      marginTop: 8,
    },
    previewText: {
      fontSize: 15 * fontScale,
      lineHeight: 23 * fontScale,
      color: colors.text,
      backgroundColor: colors.background,
      borderColor: colors.border,
      borderWidth: 1,
      borderRadius: 8,
      padding: 12,
      width: '100%',
    },
    logoutButton: {
      width: '100%',
      borderWidth: 2,
      borderColor: colors.danger,
      paddingVertical: 14,
      borderRadius: 12,
      alignItems: 'center',
      marginTop: 28,
    },
    logoutButtonText: {
      color: colors.danger,
      fontSize: 15 * fontScale,
      fontWeight: '700',
    },
  })
}
