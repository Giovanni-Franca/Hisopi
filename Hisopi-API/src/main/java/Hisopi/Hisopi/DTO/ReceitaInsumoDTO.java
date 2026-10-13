package Hisopi.Hisopi.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReceitaInsumoDTO(
		@NotNull
	    Long idInsumo,
	    @NotNull
	    @Positive
	    Double quantidadePorUnidade
	) {}