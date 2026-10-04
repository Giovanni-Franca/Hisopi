import { apiFetch } from './api'

export type Movimentacao = {
  id: number
  tipo: string // ENTRADA, PERDA_VALIDADE, PERDA_OUTRO...
  quantidade: number
  motivo?: string | null
  dataMovimentacao: string
  // tolera tanto a entidade (insumo aninhado) quanto um DTO plano
  insumo?: { id: number; nome: string; unidadeMedida?: string } | null
  nomeInsumo?: string
  unidadeMedida?: string
}

export type RelatorioPerdas = {
  perdasPorValidade: Movimentacao[]
  perdasPorOutroMotivo: Movimentacao[]
  quantidadeTotalPerdida: number
}

export type InsumoResumo = {
  id: number
  nome: string
  unidadeMedida?: string
}

export function listarInsumosResumo(idEspaco: string): Promise<InsumoResumo[]> {
  return apiFetch(`/espacos/${idEspaco}/insumos`)
}

export function listarMovimentacoes(
  idEspaco: string,
  idInsumo: number
): Promise<Movimentacao[]> {
  return apiFetch(`/espacos/${idEspaco}/insumos/${idInsumo}/movimentacoes`)
}

// inicio e fim no formato YYYY-MM-DD
export function relatorioPerdas(
  idEspaco: string,
  inicio: string,
  fim: string
): Promise<RelatorioPerdas> {
  const query = `inicio=${encodeURIComponent(inicio)}&fim=${encodeURIComponent(fim)}`
  return apiFetch(`/espacos/${idEspaco}/relatorios/perdas?${query}`)
}