import { apiFetch } from './api'
import type { PapelMembro } from './espacoService'

export type MembroEspaco = {
  id: number
  papel: PapelMembro
  entradaEm: string
  usuario: {
    id: number
    nome: string
    email: string
  }
}

export async function listarMembros(idEspaco: string | number): Promise<MembroEspaco[]> {
  return apiFetch(`/espacos/${idEspaco}/membros`)
}

export async function convidarMembro(
  idEspaco: string | number,
  email: string,
  papel: PapelMembro
): Promise<MembroEspaco> {
  return apiFetch(`/espacos/${idEspaco}/membros`, {
    method: 'POST',
    body: JSON.stringify({ email, papel }),
  })
}

export async function removerMembro(
  idEspaco: string | number,
  idMembro: number
): Promise<{ message: string }> {
  return apiFetch(`/espacos/${idEspaco}/membros/${idMembro}`, {
    method: 'DELETE',
  })
}