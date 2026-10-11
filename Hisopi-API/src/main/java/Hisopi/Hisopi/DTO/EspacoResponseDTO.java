package Hisopi.Hisopi.DTO;

import Hisopi.Hisopi.Enum.TipoEspaco;
import Hisopi.Hisopi.model.Espaco;

public record EspacoResponseDTO(
		Long id,
		String nome,
		TipoEspaco tipo
		
	) {
	public static EspacoResponseDTO de(Espaco e) {
        return new EspacoResponseDTO(e.getId(), e.getNome(), e.getTipo());
    }
}
