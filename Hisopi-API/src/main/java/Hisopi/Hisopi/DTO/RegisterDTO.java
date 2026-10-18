 package Hisopi.Hisopi.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterDTO(
		@NotBlank
		String nome,
		@NotBlank
		@Email
		String login, 
		@NotBlank
		@Size (min = 6)
		String senha
		) {

}
