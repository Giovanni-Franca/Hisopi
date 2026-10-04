package Hisopi.Hisopi.DTO;

import Hisopi.Hisopi.Enum.TipoReceita;
import Hisopi.Hisopi.model.Receita;

public record ReceitaResponseDTO(
		Long id, 
		String nome, 
		TipoReceita tipo, 
		String modoPreparo
		) {
    public static ReceitaResponseDTO de(Receita r) {
        return new ReceitaResponseDTO(r.getId(), r.getNome(), r.getTipo(), r.getModoPreparo());
    }
}