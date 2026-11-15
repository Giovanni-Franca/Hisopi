import { apiFetch } from './api'

export type Insumo = {
  id: number
  nome: string
  categoria: Categoria | null
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
  insumo?: {
    id: number
    nome: string
    unidadeMedida: string
  }
}

export type Categoria =
  | 'CARNES'
  | 'CEREAIS'
  | 'GRAOS'
  | 'OVOS'
  | 'LATICINIOS'
  | 'MASSAS'
  | 'FRUTAS'
  | 'LEGUMES'
  | 'VERDURAS'
  | 'FARINACEOS'
  | 'OLEOS_E_GORDURAS'
  | 'TEMPEROS'
  | 'BEBIDAS' 
  | 'OUTROS'

export const CATEGORIAS: { value: Categoria; label: string }[] = [
  { value: 'CARNES', label: 'Carnes'},
  { value: 'CEREAIS', label: 'Cereais'},
  { value: 'GRAOS', label: 'Grãos' },
  { value: 'OVOS', label: 'Ovos'},
  { value: 'LATICINIOS', label: 'Laticínios' },
  { value: 'MASSAS', label: 'Massas' },
  { value: 'FRUTAS', label: 'Frutas'},
  { value: 'LEGUMES', label: 'Legumes' },
  { value: 'VERDURAS', label: 'Verduras' },
  { value: 'FARINACEOS', label: 'Farináceos' },
  { value: 'OLEOS_E_GORDURAS', label: 'Óleos e gorduras' },
  { value: 'TEMPEROS', label: 'Temperos'},
  { value: 'BEBIDAS', label: 'Bebidas'},
  { value: 'OUTROS', label: 'Outros'},
]

export type UnidadeMedida = 'KG' | 'G' | 'L' | 'ML' | 'UN' | 'CX' | 'PCT' | 'DUZIA'

export const UNIDADES_MEDIDA: {
  value: UnidadeMedida
  label: string
  sigla: string
}[] = [
  { value: 'KG', label: 'Quilograma (kg)', sigla: 'kg' },
  { value: 'G', label: 'Grama (g)', sigla: 'g' },
  { value: 'L', label: 'Litro (l)', sigla: 'l' },
  { value: 'ML', label: 'Mililitro (ml)', sigla: 'ml' },
  { value: 'UN', label: 'Unidade (un)', sigla: 'un' },
  { value: 'CX', label: 'Caixa (cx)', sigla: 'cx' },
  { value: 'PCT', label: 'Pacote (pct)', sigla: 'pct' },
  { value: 'DUZIA', label: 'Dúzia (dz)', sigla: 'dz' },
]

export type TipoPerda = 
| 'SAIDA_USO'
| 'PERDA_VALIDADE' 
| 'PERDA_OUTRO'
| 'DOACAO'
| 'AJUSTE'

export const TIPOS_PERDA: { value: TipoPerda; label: string }[] = [
  { value: 'SAIDA_USO', label: 'Utilizado em preparo'},
  { value: 'PERDA_VALIDADE', label: 'Perda por validade' },
  { value: 'PERDA_OUTRO', label: 'Outro motivo' },
  { value: 'DOACAO', label: 'Insumos doados'},
  { value: 'AJUSTE', label: 'Ajuste de inventário'}
]

export type InsumoPayload = {
  nome: string
  categoria?: Categoria | null
  unidadeMedida: string
  estoqueMinimo: number
  custoUnitario?: number | null
}

export type LotePayload = {
  quantidade: number
  dataValidade: string        //YYYY-MM-DD
  fornecedor?: string | null
}

export type PerdaPayload = {
  quantidade?: number 
  tipo: TipoPerda
  motivo?: string | null
}

export function labelCategoria(categoria: Categoria | null): string {
  return CATEGORIAS.find((c) => c.value === categoria)?.label ?? '—'
}

export function siglaUnidade(unidade: UnidadeMedida): string {
  return UNIDADES_MEDIDA.find((u) => u.value === unidade)?.sigla ?? unidade
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