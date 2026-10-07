package Hisopi.Hisopi.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record InsumoDTO(
		@NotBlank
	    String nome,
	    @NotNull
	    String categoria,
	    @NotNull
	    String unidadeMedida,
	    @PositiveOrZero
	    Double estoqueMinimo,
	    @PositiveOrZero
	    Double custoUnitario
	) {}