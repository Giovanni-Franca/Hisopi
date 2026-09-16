import { Link, router } from 'expo-router'
import { useState } from 'react'
import { Pressable, StyleSheet, Text } from 'react-native'

import { AuthShell } from '@/src/components/auth/AuthShell'
import { AuthInput } from '@/src/components/auth/AuthInput'
import { useAuth } from '@/src/context/AuthContext'
import { colors } from '@/src/theme/colors'

export default function LoginScreen() {
  const { login } = useAuth()

  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState('')
  const [loading, setLoading] = useState(false)

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

  async function handleLogin() {
    setErro('')

    if (!email.trim() || !senha.trim()) {
      setErro('Preencha e-mail e senha.')
      return
    }

    if (!emailRegex.test(email.trim())) {
      setErro('Informe um e-mail válido.')
      return
    }

    try {
      setLoading(true)
      await login(email.trim(), senha)
      router.replace('/')
    } catch (error) {
      setErro(
        error instanceof Error
          ? error.message
          : 'Não foi possível entrar. Tente novamente.'
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthShell
      title="Bem-vindo de volta"
      subtitle="Entre para continuar controlando seus insumos."
      footer={
        <Text style={styles.footerText}>
          Ainda não tem conta?{' '}
          <Link href="/cadastro" style={styles.footerLink}>
            Cadastre-se
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

      <AuthInput
        label="Senha"
        placeholder="Sua senha"
        value={senha}
        onChangeText={setSenha}
        secureTextEntry
        autoComplete="password"
      />

      <Link href="/esqueci-senha" style={styles.forgotLink}>
        Esqueceu sua senha?
      </Link>

      <Pressable
        style={[styles.button, loading && styles.buttonDisabled]}
        onPress={handleLogin}
        disabled={loading}
      >
        <Text style={styles.buttonText}>
          {loading ? 'Entrando...' : 'Entrar'}
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

  forgotLink: {
    alignSelf: 'flex-end',
    color: colors.secondary,
    fontSize: 13,
    fontWeight: '600',
    marginBottom: 24,
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

  footerText: {
    color: colors.textMuted,
    fontSize: 14,
  },

  footerLink: {
    color: colors.primary,
    fontWeight: '700',
  },
})