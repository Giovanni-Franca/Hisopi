package Hisopi.Hisopi.DTO;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LoteDTO(
		@NotNull
		@Positive
	    Double quantidade,
	    @NotNull
	    @FutureOrPresent
	    LocalDate dataValidade,
	    String fornecedor
	) {}