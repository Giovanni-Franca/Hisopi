package Hisopi.Hisopi.DTO;

public record InsumoDTO(
	    String nome,
	    String categoria,
	    String unidadeMedida,
	    Double estoqueMinimo,
	    Double custoUnitario
	) {}