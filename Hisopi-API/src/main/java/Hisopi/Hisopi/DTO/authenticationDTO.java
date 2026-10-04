package Hisopi.Hisopi.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record authenticationDTO(
		@NotBlank
		String login,
		@NotBlank
		@Size(min = 8)
		String senha
) {}
