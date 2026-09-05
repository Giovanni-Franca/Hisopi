package Hisopi.Hisopi.DTO;

import Hisopi.Hisopi.model.Receita.TipoReceita;

public record ReceitaDTO(
	    String nome,
	    TipoReceita tipo,
	    String modoPreparo
	) {}