package Hisopi.Hisopi.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record InsumoDTO(@JsonAlias("nomeInsumo")
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