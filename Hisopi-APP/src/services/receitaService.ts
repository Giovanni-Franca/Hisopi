import { apiFetch } from './api'

export type TipoReceita = 'PRODUTO_VENDA' | 'SUGESTAO_CONSUMO'

export type Receita = {
  id: number
  nome: string
  tipo: TipoReceita
  modoPreparo: string | null
  criadoEm: string
}

export type ReceitaInsumo = {
  id: number
  quantidadePorUnidade: number
  insumo: {
    id: number
    nome: string
    unidadeMedida: string
  }
}

export type ReceitaPayload = {
  nome: string
  tipo: TipoReceita
  modoPreparo?: string | null
}

export type ReceitaInsumoPayload = {
  idInsumo: number
  quantidadePorUnidade: number
}

export async function listarReceitas(idEspaco: string | number): Promise<Receita[]> {
  return apiFetch(`/espacos/${idEspaco}/receitas`)
}

export async function criarReceita(
  idEspaco: string | number,
  payload: ReceitaPayload
): Promise<Receita> {
  return apiFetch(`/espacos/${idEspaco}/receitas`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function listarFichaTecnica(
  idEspaco: string | number,
  idReceita: number
): Promise<ReceitaInsumo[]> {
  return apiFetch(`/espacos/${idEspaco}/receitas/${idReceita}/insumos`)
}

export async function vincularInsumo(
  idEspaco: string | number,
  idReceita: number,
  payload: ReceitaInsumoPayload
): Promise<ReceitaInsumo> {
  return apiFetch(`/espacos/${idEspaco}/receitas/${idReceita}/insumos`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function removerVinculo(
  idEspaco: string | number,
  idReceita: number,
  idReceitaInsumo: number
): Promise<{ message: string }> {
  return apiFetch(
    `/espacos/${idEspaco}/receitas/${idReceita}/insumos/${idReceitaInsumo}`,
    { method: 'DELETE' }
  )
}