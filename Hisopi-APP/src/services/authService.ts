import { apiFetch, saveTokens, clearTokens } from './api'

export type Usuario = {
  id: number
  nome: string
  email: string
  role: 'USER' | 'ADMIN'
}

type LoginResponse = {
  accessToken: string
  refreshToken: string
}

export async function login(email: string, senha: string): Promise<void> {
  const data: LoginResponse = await apiFetch('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ login: email, senha }),
    skipAuthRetry: true, 
  })

  if (!data?.accessToken || !data?.refreshToken) {
    throw new Error('Resposta de login inválida do servidor.')
  }

  await saveTokens(data.accessToken, data.refreshToken)
}

export async function cadastrar(nome: string, email: string, senha: string): Promise<void> {
  await apiFetch('/auth/register', {
    method: 'POST',
    body: JSON.stringify({ nome, login: email, senha, role: 'USER' }),
    skipAuthRetry: true,
  })
  await login(email, senha)
}

export async function buscarUsuarioLogado(): Promise<Usuario> {
  return apiFetch('/auth/me')
}

export async function logout(): Promise<void> {
  await clearTokens()
}