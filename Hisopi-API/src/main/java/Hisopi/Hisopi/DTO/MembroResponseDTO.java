package Hisopi.Hisopi.DTO;

import Hisopi.Hisopi.Enum.PapelMembro;
import Hisopi.Hisopi.model.MembroEspaco;

public record MembroResponseDTO(
		Long id,
	    Long idUsuario,
	    String nome,
	    String email,
	    PapelMembro papel
	) {
	    public static MembroResponseDTO de(MembroEspaco m) {
	        return new MembroResponseDTO(
	            m.getId(),
	            m.getUsuario().getId(),
	            m.getUsuario().getNome(),
	            m.getUsuario().getEmail(),
	            m.getPapel()
	        );
	    }
	}