import { createContext, useContext, useState, useEffect } from 'react'

import * as authService from '@/src/services/authService'
import { getAccessToken } from '@/src/services/api'
import type { Usuario } from '@/src/services/authService'

type AuthContextType = {
  usuario: Usuario | null
  authenticated: boolean
  loading: boolean
  login: (email: string, senha: string) => Promise<void>
  cadastrar: (nome: string, email: string, senha: string) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextType>({} as AuthContextType)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function restoreSession() {
      const token = await getAccessToken()

      if (token) {
        try {
          const dadosUsuario = await authService.buscarUsuarioLogado()
          setUsuario(dadosUsuario)
        } catch {
          setUsuario(null)
        }
      }

      setLoading(false)
    }

    restoreSession()
  }, [])

  async function login(email: string, senha: string) {
    await authService.login(email, senha)
    const dadosUsuario = await authService.buscarUsuarioLogado()
    setUsuario(dadosUsuario)
  }

  async function cadastrar(nome: string, email: string, senha: string) {
    await authService.cadastrar(nome, email, senha)
    const dadosUsuario = await authService.buscarUsuarioLogado()
    setUsuario(dadosUsuario)
  }

  async function logout() {
    await authService.logout()
    setUsuario(null)
  }

  return (
    <AuthContext.Provider
      value={{
        usuario,
        authenticated: !!usuario,
        loading,
        login,
        cadastrar,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}