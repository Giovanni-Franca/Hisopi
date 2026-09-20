import { apiFetch } from './api'

export type TipoEspaco = 'PESSOAL' | 'ORGANIZACAO'
export type PapelMembro = 'DONO' | 'ADMIN' | 'GERENTE' | 'OPERADOR'

export type Espaco = {
  id: number
  nome: string
  tipo: TipoEspaco
  criadoEm: string
}
type MembroEspacoResponse = {
  id: number
  papel: PapelMembro
  entradaEm: string
  espaco: Espaco
}

export type EspacoComPapel = Espaco & { papel: PapelMembro }

export async function buscarEspaco(id: string | number): Promise<Espaco> {
  return apiFetch(`/espacos/${id}`)
}

export async function listarEspacosDoUsuario(): Promise<EspacoComPapel[]> {
  const memberships: MembroEspacoResponse[] = await apiFetch('/espacos/minhas')

  return memberships.map((m) => ({
    ...m.espaco,
    papel: m.papel,
  }))
}

export async function criarEspaco(nome: string, tipo: TipoEspaco): Promise<Espaco> {
  return apiFetch('/espacos', {
    method: 'POST',
    body: JSON.stringify({ nome, tipo }),
  })
}