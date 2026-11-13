package Hisopi.Hisopi.DTO;

import Hisopi.Hisopi.model.Insumo;

public record InsumoResponse(Long idInsumo, String nomeInsumo, String categoria, String unidadeMedida,
	    Double estoqueAtual, Double estoqueMinimo, Double custoUnitario, boolean ativoInsumo) {
	    public static InsumoResponse from(Insumo i) {
	        return new InsumoResponse(i.getId(), i.getNome(), i.getCategoria(), i.getUnidadeMedida(),
	            i.getEstoqueAtual(), i.getEstoqueMinimo(), i.getCustoUnitario(), i.getAtivo());
	    }
	}