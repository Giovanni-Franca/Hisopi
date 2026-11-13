package Hisopi.Hisopi.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;

import Hisopi.Hisopi.Enum.TipoMovimentacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PerdaDTO(@JsonAlias("tipoMovimentacao")
		@Positive
	    Double quantidade, // opcional — se nulo, descarta o restante do lote
	    @NotNull
	    TipoMovimentacao tipo,
	    String motivo
	) {}