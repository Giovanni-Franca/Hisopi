package Hisopi.Hisopi.DTO;

import java.time.LocalDateTime;

import Hisopi.Hisopi.Enum.TipoMovimentacao;
import Hisopi.Hisopi.model.MovimentacaoEstoque;

public record MovimentacaoResponse(Long idMovimentacao, TipoMovimentacao tipoMovimentacao,
	    Double quantidade, String motivo, LocalDateTime dataMovimentacao) {
	    public static MovimentacaoResponse from(MovimentacaoEstoque m) {
	        return new MovimentacaoResponse(m.getId(), m.getTipo(), m.getQuantidade(), m.getMotivo(), m.getDataMovimentacao());
	    }
	}