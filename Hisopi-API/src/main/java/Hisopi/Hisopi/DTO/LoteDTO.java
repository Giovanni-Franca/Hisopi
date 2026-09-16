package Hisopi.Hisopi.DTO;

import java.time.LocalDate;

public record LoteDTO(
	    Double quantidade,
	    LocalDate dataValidade,
	    String fornecedor
	) {}