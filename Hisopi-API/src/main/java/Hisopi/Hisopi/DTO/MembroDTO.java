package Hisopi.Hisopi.DTO;

import Hisopi.Hisopi.Enum.PapelMembro;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MembroDTO(
		@NotBlank
		@Email
	    String email,
	    @NotNull
	    PapelMembro papel
	) {
	
	
}