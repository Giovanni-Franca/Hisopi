import { apiFetch } from './api'

export type TipoEspaco = 'PESSOAL' | 'ORGANIZACAO'
export type PapelMembro = 'DONO' | 'ADMIN' | 'GERENTE' | 'OPERADOR'

export type Espaco = {
  id: number
  nome: string
  tipo: TipoEspaco
  criadoEm?: string
}

type MeuEspacoResponse = {
  idEspaco: number
  nome: string
  tipo: TipoEspaco
  meuPapel: PapelMembro
}
export type EspacoComPapel = Pick<Espaco, 'id' | 'nome' | 'tipo'> & {
  papel: PapelMembro
}

export async function buscarEspaco(id: string | number): Promise<Espaco> {
  return apiFetch(`/espacos/${id}`)
}

export async function listarEspacosDoUsuario(): Promise<EspacoComPapel[]> {
  const lista: MeuEspacoResponse[] = await apiFetch('/espacos/minhas')

  return lista.map((e) => ({
    id: e.idEspaco,
    nome: e.nome,
    tipo: e.tipo,
    papel: e.meuPapel,
  }))
}

export async function criarEspaco(nome: string, tipo: TipoEspaco): Promise<Espaco> {
  return apiFetch('/espacos', {
    method: 'POST',
    body: JSON.stringify({ nome, tipo }),
  })
}