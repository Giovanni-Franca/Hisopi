package Hisopi.Hisopi.DTO;

import jakarta.validation.constraints.NotBlank;

public record NovaChaveDTO(
		@NotBlank String nome
		) {}
