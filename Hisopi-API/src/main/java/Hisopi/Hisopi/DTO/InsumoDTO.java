package Hisopi.Hisopi.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;

import Hisopi.Hisopi.Enum.Categoria;
import Hisopi.Hisopi.Enum.UnidadeMedida;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record InsumoDTO(@JsonAlias("nomeInsumo")
		@NotBlank
	    String nome,
	    Categoria categoria,
	    @NotNull
	    UnidadeMedida unidadeMedida,
	    @PositiveOrZero
	    Double estoqueMinimo,
	    @PositiveOrZero
	    Double custoUnitario
	) {}