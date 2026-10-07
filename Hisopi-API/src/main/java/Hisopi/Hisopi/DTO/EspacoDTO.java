package Hisopi.Hisopi.DTO;

import Hisopi.Hisopi.Enum.TipoEspaco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EspacoDTO(
		@NotBlank
	    String nome,
	    @NotNull
	    TipoEspaco tipo
	) {}
