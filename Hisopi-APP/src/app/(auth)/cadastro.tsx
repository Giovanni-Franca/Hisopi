import { Link, router } from 'expo-router'
import { useState } from 'react'
import { Pressable, StyleSheet, Text } from 'react-native'

import { AuthShell } from '@/src/components/auth/AuthShell'
import { AuthInput } from '@/src/components/auth/AuthInput'
import { useAuth } from '@/src/context/AuthContext'
import { colors } from '@/src/theme/colors'

export default function CadastroScreen() {
  const { cadastrar } = useAuth()

  const [nome, setNome] = useState('')
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [confirmarSenha, setConfirmarSenha] = useState('')
  const [erro, setErro] = useState('')
  const [loading, setLoading] = useState(false)

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

  async function handleCadastro() {
    setErro('')

    if (!nome.trim() || !email.trim() || !senha || !confirmarSenha) {
      setErro('Preencha todos os campos.')
      return
    }

    if (!emailRegex.test(email.trim())) {
      setErro('Informe um e-mail válido.')
      return
    }

    if (senha.length < 6) {
      setErro('A senha precisa ter pelo menos 6 caracteres.')
      return
    }

    if (senha !== confirmarSenha) {
      setErro('As senhas não coincidem.')
      return
    }

    try {
      setLoading(true)
      await cadastrar(nome.trim(), email.trim(), senha)
      router.replace('/')
    } catch (error) {
      setErro(
        error instanceof Error
          ? error.message
          : 'Não foi possível criar sua conta. Tente novamente.'
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthShell
      title="Crie sua conta"
      subtitle="Comece a organizar seu estoque e evitar desperdício hoje."
      footer={
        <Text style={styles.footerText}>
          Já tem conta?{' '}
          <Link href="/login" style={styles.footerLink}>
            Entrar
          </Link>
        </Text>
      }
    >
      {erro ? <Text style={styles.errorBanner}>{erro}</Text> : null}

      <AuthInput
        label="Nome"
        placeholder="Seu nome"
        value={nome}
        onChangeText={setNome}
        autoComplete="name"
      />

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
        placeholder="Pelo menos 6 caracteres"
        value={senha}
        onChangeText={setSenha}
        secureTextEntry
        autoComplete="new-password"
      />

      <AuthInput
        label="Confirmar senha"
        placeholder="Repita a senha"
        value={confirmarSenha}
        onChangeText={setConfirmarSenha}
        secureTextEntry
        autoComplete="new-password"
      />

      <Pressable
        style={[styles.button, loading && styles.buttonDisabled]}
        onPress={handleCadastro}
        disabled={loading}
      >
        <Text style={styles.buttonText}>
          {loading ? 'Criando conta...' : 'Criar conta'}
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
    marginTop: 8,
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