package Hisopi.Hisopi.DTO;

import Hisopi.Hisopi.Enum.TipoReceita;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReceitaDTO(
		@NotBlank
	    String nome,
	    @NotNull
	    TipoReceita tipo,
	    String modoPreparo
	) {}