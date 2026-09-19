import { apiFetch } from './api'

export type TipoEspaco = 'PESSOAL' | 'ORGANIZACAO'
export type PapelMembro = 'DONO' | 'ADMIN' | 'GERENTE' | 'OPERADOR'

export type Espaco = {
  id: number
  nome: string
  tipo: TipoEspaco
  criadoEm: string
}

// GET /espacos/usuario/{id} retorna os vínculos (MembroEspaco) do
// usuário, cada um com o espaço aninhado — não uma lista de Espaco
// "pura". Normalizamos isso no service para a tela não precisar
// conhecer esse detalhe do backend.
type MembroEspacoResponse = {
  id: number
  papel: PapelMembro
  entradaEm: string
  espaco: Espaco
}

export type EspacoComPapel = Espaco & { papel: PapelMembro }

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