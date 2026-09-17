import AsyncStorage from '@react-native-async-storage/async-storage'

import { API_URL } from './config'

const ACCESS_TOKEN_KEY = '@hisopi:accessToken'
const REFRESH_TOKEN_KEY = '@hisopi:refreshToken'

export async function getAccessToken() {
  const token = await AsyncStorage.getItem(ACCESS_TOKEN_KEY)
  return isValidToken(token) ? token : null
}

export async function getRefreshToken() {
  const token = await AsyncStorage.getItem(REFRESH_TOKEN_KEY)
  return isValidToken(token) ? token : null
}

function isValidToken(value: string | null): value is string {
  return !!value && value !== 'undefined' && value !== 'null'
}

export async function saveTokens(accessToken: string, refreshToken: string) {
  if (!isValidToken(accessToken) || !isValidToken(refreshToken)) {
    throw new Error('Tokens inválidos recebidos do servidor.')
  }

  await AsyncStorage.multiSet([
    [ACCESS_TOKEN_KEY, accessToken],
    [REFRESH_TOKEN_KEY, refreshToken],
  ])
}

export async function clearTokens() {
  await AsyncStorage.multiRemove([ACCESS_TOKEN_KEY, REFRESH_TOKEN_KEY])
}

class ApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.status = status
  }
}

let refreshPromise: Promise<string | null> | null = null

async function refreshAccessToken(): Promise<string | null> {
  if (refreshPromise) return refreshPromise

  refreshPromise = (async () => {
    const refreshToken = await getRefreshToken()
    if (!refreshToken) return null

    try {
      const response = await fetch(`${API_URL}/auth/refresh`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken }),
      })

      if (!response.ok) {
        await clearTokens()
        return null
      }

      const data = await response.json()
      await saveTokens(data.token, data.refreshToken)
      return data.token
    } catch {
      await clearTokens()
      return null
    } finally {
      refreshPromise = null
    }
  })()

  return refreshPromise
}

type ApiFetchOptions = RequestInit & {
  skipAuthRetry?: boolean
}

export async function apiFetch(path: string, options: ApiFetchOptions = {}) {
  const { skipAuthRetry, ...fetchOptions } = options

  const accessToken = await getAccessToken()

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(fetchOptions.headers as Record<string, string> | undefined),
  }

  if (accessToken) {
    headers.Authorization = `Bearer ${accessToken}`
  }

  let response = await fetch(`${API_URL}${path}`, {
    ...fetchOptions,
    headers,
  })

  if (response.status === 401 && !skipAuthRetry) {
    const newToken = await refreshAccessToken()

    if (newToken) {
      response = await fetch(`${API_URL}${path}`, {
        ...fetchOptions,
        headers: { ...headers, Authorization: `Bearer ${newToken}` },
      })
    }
  }

  if (!response.ok) {
    let message = 'Erro ao comunicar com o servidor.'

    try {
      const body = await response.json()
      message = body.message ?? body.error ?? message
    } catch {
    }

    throw new ApiError(message, response.status)
  }

  const text = await response.text()
  return text ? JSON.parse(text) : null
}