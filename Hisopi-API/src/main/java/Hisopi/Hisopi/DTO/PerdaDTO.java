package Hisopi.Hisopi.DTO;

import Hisopi.Hisopi.model.MovimentacaoEstoque.TipoMovimentacao;

public record PerdaDTO(
	    Double quantidade, // opcional — se nulo, descarta o restante do lote
	    TipoMovimentacao tipo,
	    String motivo
	) {}