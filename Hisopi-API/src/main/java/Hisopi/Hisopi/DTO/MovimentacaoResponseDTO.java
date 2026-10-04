package Hisopi.Hisopi.DTO;

import java.time.LocalDateTime;

import Hisopi.Hisopi.Enum.TipoMovimentacao;
import Hisopi.Hisopi.model.Insumo;
import Hisopi.Hisopi.model.MovimentacaoEstoque;

public record MovimentacaoResponseDTO(
     Long id,
     TipoMovimentacao tipo,
     Double quantidade,
     String motivo,
     LocalDateTime dataMovimentacao,
     Long idInsumo,
     String nomeInsumo,
     String unidadeMedida
) {
 public static MovimentacaoResponseDTO de(MovimentacaoEstoque m) {
     Insumo i = m.getInsumo();
     return new MovimentacaoResponseDTO(
             m.getId(),
             m.getTipo(),
             m.getQuantidade(),
             m.getMotivo(),
             m.getDataMovimentacao(),
             i.getId(),
             i.getNome(),
             i.getUnidadeMedida() != null ? i.getUnidadeMedida().toString() : null
     );
 }
}