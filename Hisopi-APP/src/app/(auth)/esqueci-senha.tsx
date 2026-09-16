import { Link } from 'expo-router'
import { useState } from 'react'
import { Pressable, StyleSheet, Text } from 'react-native'

import { AuthShell } from '@/src/components/auth/AuthShell'
import { AuthInput } from '@/src/components/auth/AuthInput'
import { colors } from '@/src/theme/colors'

export default function EsqueciSenhaScreen() {
  const [email, setEmail] = useState('')
  const [erro, setErro] = useState('')
  const [enviado, setEnviado] = useState(false)
  const [loading, setLoading] = useState(false)

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

  async function handleEnviar() {
    setErro('')

    if (!email.trim()) {
      setErro('Informe seu e-mail.')
      return
    }

    if (!emailRegex.test(email.trim())) {
      setErro('Informe um e-mail válido.')
      return
    }

    try {
      setLoading(true)

      // TODO: integrar com POST /auth/esqueci-senha
      await new Promise((resolve) => setTimeout(resolve, 800))

      setEnviado(true)
    } catch {
      setErro('Não foi possível enviar o link. Tente novamente.')
    } finally {
      setLoading(false)
    }
  }

  if (enviado) {
    return (
      <AuthShell
        title="Verifique seu e-mail"
        subtitle={`Se ${email.trim()} estiver cadastrado, você vai receber um link para redefinir sua senha em instantes.`}
        footer={
          <Text style={styles.footerText}>
            <Link href="/login" style={styles.footerLink}>
              Voltar para o login
            </Link>
          </Text>
        }
      >
        <Pressable
          style={styles.buttonSecondary}
          onPress={() => setEnviado(false)}
        >
          <Text style={styles.buttonSecondaryText}>
            Usar outro e-mail
          </Text>
        </Pressable>
      </AuthShell>
    )
  }

  return (
    <AuthShell
      title="Esqueceu sua senha?"
      subtitle="Informe seu e-mail e enviaremos um link para você criar uma nova senha."
      footer={
        <Text style={styles.footerText}>
          Lembrou a senha?{' '}
          <Link href="/login" style={styles.footerLink}>
            Entrar
          </Link>
        </Text>
      }
    >
      {erro ? <Text style={styles.errorBanner}>{erro}</Text> : null}

      <AuthInput
        label="E-mail"
        placeholder="voce@email.com"
        value={email}
        onChangeText={setEmail}
        autoCapitalize="none"
        keyboardType="email-address"
        autoComplete="email"
      />

      <Pressable
        style={[styles.button, loading && styles.buttonDisabled]}
        onPress={handleEnviar}
        disabled={loading}
      >
        <Text style={styles.buttonText}>
          {loading ? 'Enviando...' : 'Enviar link de redefinição'}
        </Text>
      </Pressable>
    </AuthShell>
  )
}

const styles = StyleSheet.create({
  errorBanner: {
    backgroundColor: colors.dangerSoft,
    color: colors.danger,
    padding: 12,
    borderRadius: 10,
    marginBottom: 18,
    fontSize: 14,
  },

  button: {
    backgroundColor: colors.primary,
    paddingVertical: 15,
    borderRadius: 12,
    alignItems: 'center',
  },

  buttonDisabled: {
    opacity: 0.6,
  },

  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '700',
  },

  buttonSecondary: {
    borderWidth: 1,
    borderColor: colors.border,
    paddingVertical: 15,
    borderRadius: 12,
    alignItems: 'center',
  },

  buttonSecondaryText: {
    color: colors.text,
    fontSize: 15,
    fontWeight: '600',
  },

  footerText: {
    color: colors.textMuted,
    fontSize: 14,
  },

  footerLink: {
    color: colors.primary,
    fontWeight: '700',
  },
})