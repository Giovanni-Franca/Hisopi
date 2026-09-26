package Hisopi.Hisopi.DTO;

import Hisopi.Hisopi.Enum.TipoReceita;

public record ReceitaDTO(
	    String nome,
	    TipoReceita tipo,
	    String modoPreparo
	) {}