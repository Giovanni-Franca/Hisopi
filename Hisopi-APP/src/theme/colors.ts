// Paleta base fornecida, com pequenos ajustes de tonalidade
// para estados de hover/pressed e para garantir contraste
// suficiente em texto sobre fundo colorido.
export const colors = {
  // Cor de ação principal (botões, links, elementos de destaque)
  primary: '#8E4585',
  primaryPressed: '#733A6C', // tom mais escuro para hover/press

  // Cor secundária (bordas ativas, ícones, detalhes)
  secondary: '#996666',

  // Cor decorativa (painel lateral, fundos suaves, badges)
  accent: '#DCA1A1',
  accentSoft: '#F3E4E4', // versão bem clara, para fundos de destaque sutil

  // Texto
  text: '#4A4A4A',
  textMuted: '#8A7A7A',

  // Neutros
  background: '#FBF7F6', // fundo geral, leve tom rosado
  surface: '#FFFFFF',    // cards
  border: '#E8DADA',

  // Semânticas (fora da paleta base, necessárias para feedback)
  danger: '#B3413E',
  dangerSoft: '#F7E3E2',
  success: '#3E7A5C',
  successSoft: '#E3F1EA',
}