package Hisopi.Hisopi.DTO;

public record ErrorResponseDTO(
	int status,
	String erro,
	String mensagem
) {}
