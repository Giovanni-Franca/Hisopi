import { apiFetch } from './api'

export type EspacoResumo = { id: number; nome: string }

export type ApiKeyItem = {
  id: string
  nome: string
  prefixo: string
  papel: string
  ativa: boolean
  ultimoUso: string | null
}

export type ChaveCriada = {
  id: string
  espacoId: number
  nome: string
  chave: string
}

async function comMensagemAmigavel<T>(chamada: () => Promise<T>): Promise<T> {
  try {
    return await chamada()
  } catch (e) {
    if ((e as { status?: number })?.status === 403) {
      throw new Error('Você precisa ser dono do espaço para gerenciar chaves.')
    }
    throw e
  }
}



export function listarApiKeys(idEspaco: number | string): Promise<ApiKeyItem[]> {
  return comMensagemAmigavel(() => apiFetch(`/espacos/${idEspaco}/api-keys`))
}

export function gerarApiKey(
  idEspaco: number | string,
  nome: string,
): Promise<ChaveCriada> {
  return comMensagemAmigavel(() =>
    apiFetch(`/espacos/${idEspaco}/api-keys`, {
      method: 'POST',
      body: JSON.stringify({ nome }),
    }),
  )
}

export async function revogarApiKey(
  idEspaco: number | string,
  idKey: string,
): Promise<void> {
  await comMensagemAmigavel(() =>
    apiFetch(`/espacos/${idEspaco}/api-keys/${idKey}`, { method: 'DELETE' }),
  )
}

export function criarEspacoComChave(body: {
  nomeEspaco: string
  tipo: string
  nomeChave: string
}): Promise<ChaveCriada> {
  return apiFetch('/api-keys/espaco', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}