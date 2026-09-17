import { ReactNode } from 'react'
import { StyleSheet, Text, View } from 'react-native'
import { KeyboardAwareScrollView } from 'react-native-keyboard-aware-scroll-view'

import { useResponsive } from '@/src/hooks/useResponsive'
import { colors } from '@/src/theme/colors'

type AuthShellProps = {
  title: string
  subtitle?: string
  children: ReactNode
  footer?: ReactNode
}

export function AuthShell({ title, subtitle, children, footer }: AuthShellProps) {
  const { isDesktop } = useResponsive()

  return (
    <View style={[styles.screen, isDesktop && styles.screenDesktop]}>
      {isDesktop && (
        <View style={styles.panel}>
          <View style={styles.panelCircleLarge} />
          <View style={styles.panelCircleSmall} />

          <View style={styles.panelContent}>
            <Text style={styles.panelBrand}>Controle de Insumos</Text>
            <Text style={styles.panelTagline}>
              Organize seu estoque, acompanhe validades e reduza o
              desperdício — em casa ou no seu negócio.
            </Text>
          </View>
        </View>
      )}

      {/* 
===============================================================
formulario
=============================================================== 
      */}

      <KeyboardAwareScrollView
        style={styles.formContainer}
        contentContainerStyle={[
          styles.formContent,
          isDesktop && styles.formContentDesktop,
        ]}
        enableOnAndroid
        keyboardShouldPersistTaps="handled"
      >
        <View style={[styles.card, isDesktop && styles.cardDesktop]}>
          {!isDesktop && (
            <Text style={styles.brandMobile}>Controle de Insumos</Text>
          )}

          <Text style={styles.title}>{title}</Text>

          {subtitle && <Text style={styles.subtitle}>{subtitle}</Text>}

          <View style={styles.body}>{children}</View>

          {footer && <View style={styles.footer}>{footer}</View>}
        </View>
      </KeyboardAwareScrollView>
    </View>
  )
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: colors.background,
  },

  screenDesktop: {
    flexDirection: 'row',
  },

  panel: {
    flex: 1,
    backgroundColor: colors.primary,
    justifyContent: 'center',
    padding: 56,
    overflow: 'hidden',
  },

  panelCircleLarge: {
    position: 'absolute',
    width: 420,
    height: 420,
    borderRadius: 210,
    backgroundColor: colors.secondary,
    opacity: 0.35,
    top: -120,
    left: -100,
  },

  panelCircleSmall: {
    position: 'absolute',
    width: 260,
    height: 260,
    borderRadius: 130,
    backgroundColor: colors.accent,
    opacity: 0.45,
    bottom: -60,
    right: -40,
  },

  panelContent: {
    maxWidth: 420,
  },

  panelBrand: {
    fontSize: 34,
    fontWeight: '800',
    color: '#fff',
    marginBottom: 16,
  },

  panelTagline: {
    fontSize: 17,
    lineHeight: 26,
    color: colors.accentSoft,
  },

  formContainer: {
    flex: 1,
  },

  formContent: {
    flexGrow: 1,
    justifyContent: 'center',
    padding: 24,
  },

  formContentDesktop: {
    alignItems: 'center',
    paddingHorizontal: 48,
  },

  card: {
    width: '100%',
  },

  cardDesktop: {
    maxWidth: 420,
  },

  brandMobile: {
    fontSize: 22,
    fontWeight: '800',
    color: colors.primary,
    textAlign: 'center',
    marginBottom: 24,
  },

  title: {
    fontSize: 28,
    fontWeight: '800',
    color: colors.text,
    marginBottom: 8,
  },

  subtitle: {
    fontSize: 15,
    color: colors.textMuted,
    lineHeight: 22,
    marginBottom: 28,
  },

  body: {
    gap: 4,
  },

  footer: {
    marginTop: 24,
    alignItems: 'center',
  },
})