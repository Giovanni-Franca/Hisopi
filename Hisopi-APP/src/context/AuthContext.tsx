import { createContext, useContext, useState, useEffect } from 'react'
import AsyncStorage from '@react-native-async-storage/async-storage'

const TOKEN_KEY = '@controle_insumos:token'

type Usuario = {
  id: number
  nome: string
  email: string
  role: 'USER' | 'ADMIN'
}

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
      // TODO: quando o endpoint de auth estiver pronto, validar o
      // token salvo (ex: GET /auth/me) em vez de só checar se existe.
      const token = await AsyncStorage.getItem(TOKEN_KEY)

      if (token) {
        // Placeholder — troque por uma chamada real que valida o
        // token e retorna os dados do usuário autenticado.
      }

      setLoading(false)
    }

    restoreSession()
  }, [])

  async function login(email: string, senha: string) {
    // TODO: integrar com POST /auth/login
    // const { token, usuario } = await apiFetch('/auth/login', {
    //   method: 'POST',
    //   body: JSON.stringify({ email, senha }),
    // })
    // await AsyncStorage.setItem(TOKEN_KEY, token)
    // setUsuario(usuario)
    throw new Error('Endpoint de login ainda não integrado')
  }

  async function cadastrar(nome: string, email: string, senha: string) {
    // TODO: integrar com POST /auth/cadastro
    throw new Error('Endpoint de cadastro ainda não integrado')
  }

  async function logout() {
    await AsyncStorage.removeItem(TOKEN_KEY)
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