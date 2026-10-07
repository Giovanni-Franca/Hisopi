package Hisopi.Hisopi.DTO;

import Hisopi.Hisopi.Enum.TipoMovimentacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PerdaDTO(
		@Positive
	    Double quantidade, // opcional — se nulo, descarta o restante do lote
	    @NotNull
	    TipoMovimentacao tipo,
	    String motivo
	) {}