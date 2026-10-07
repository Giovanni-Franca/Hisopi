package Hisopi.Hisopi.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record authenticationDTO(
		@NotBlank (message = "Login é obrigatório")
		String login,
		@NotBlank (message = "Senha é obrigatória")
		@Size(min = 8)
		String senha
) {}
