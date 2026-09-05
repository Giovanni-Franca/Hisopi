package Hisopi.Hisopi.DTO;

import Hisopi.Hisopi.model.MembroEspaco.PapelMembro;

public record MembroDTO(
	    Long idUsuario,
	    PapelMembro papel
	) {}