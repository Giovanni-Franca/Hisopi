package Hisopi.Hisopi.DTO;

import Hisopi.Hisopi.Enum.TipoEspaco;
import jakarta.validation.constraints.NotBlank;

public record NovoEspacoComChaveDTO(
		@NotBlank 
		String nomeEspaco, 
		TipoEspaco tipo,
        @NotBlank 
        String nomeChave
        ) {}
