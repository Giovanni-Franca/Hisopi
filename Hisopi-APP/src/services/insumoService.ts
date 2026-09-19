import { apiFetch } from './api'

export type Insumo = {
  id: number
  nome: string
  categoria: string | null
  unidadeMedida: string
  estoqueAtual: number
  estoqueMinimo: number
  custoUnitario: number | null
  ativo: boolean
}

export type LoteInsumo = {
  id: number
  quantidadeInicial: number
  quantidadeAtual: number
  dataEntrada: string
  dataValidade: string
  fornecedor: string | null
}

export type InsumoPayload = {
  nome: string
  categoria?: string | null
  unidadeMedida: string
  estoqueMinimo: number
  custoUnitario?: number | null
}

export type LotePayload = {
  quantidade: number
  dataValidade: string // 'YYYY-MM-DD'
  fornecedor?: string | null
}

export type PerdaPayload = {
  quantidade?: number // se omitido, descarta o restante do lote
  tipo: 'PERDA_VALIDADE' | 'PERDA_OUTRO'
  motivo?: string | null
}

export async function listarInsumos(idEspaco: string | number): Promise<Insumo[]> {
  return apiFetch(`/espacos/${idEspaco}/insumos`)
}

export async function listarBaixoEstoque(idEspaco: string | number): Promise<Insumo[]> {
  return apiFetch(`/espacos/${idEspaco}/insumos/baixoEstoque`)
}

export async function listarVencendo(
  idEspaco: string | number,
  dias = 7
): Promise<LoteInsumo[]> {
  return apiFetch(`/espacos/${idEspaco}/insumos/vencendo?dias=${dias}`)
}

export async function criarInsumo(
  idEspaco: string | number,
  payload: InsumoPayload
): Promise<Insumo> {
  return apiFetch(`/espacos/${idEspaco}/insumos`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function editarInsumo(
  idEspaco: string | number,
  idInsumo: number,
  payload: InsumoPayload
): Promise<Insumo> {
  return apiFetch(`/espacos/${idEspaco}/insumos/${idInsumo}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export async function desativarInsumo(
  idEspaco: string | number,
  idInsumo: number
): Promise<{ message: string }> {
  return apiFetch(`/espacos/${idEspaco}/insumos/${idInsumo}`, {
    method: 'DELETE',
  })
}

export async function listarLotes(
  idEspaco: string | number,
  idInsumo: number
): Promise<LoteInsumo[]> {
  // Já vem ordenado por validade ASC no backend — o primeiro item é
  // sempre o próximo lote a vencer (FEFO).
  return apiFetch(`/espacos/${idEspaco}/insumos/${idInsumo}/lotes`)
}

export async function registrarLote(
  idEspaco: string | number,
  idInsumo: number,
  payload: LotePayload
): Promise<LoteInsumo> {
  return apiFetch(`/espacos/${idEspaco}/insumos/${idInsumo}/lotes`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function registrarPerda(
  idEspaco: string | number,
  idLote: number,
  payload: PerdaPayload
): Promise<{ message: string }> {
  return apiFetch(`/espacos/${idEspaco}/insumos/lotes/${idLote}/perda`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}