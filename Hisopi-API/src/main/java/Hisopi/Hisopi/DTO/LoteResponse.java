package Hisopi.Hisopi.DTO;

import java.time.LocalDate;

import Hisopi.Hisopi.model.LoteInsumo;

public record LoteResponse(Long idLote, Long idInsumo, String nomeInsumo, Double quantidadeInicial,
	    Double quantidadeAtual, LocalDate dataEntrada, LocalDate dataValidade, String fornecedor, boolean vencido) {
	    public static LoteResponse from(LoteInsumo l) {
	        return new LoteResponse(l.getId(), l.getInsumo().getId(), l.getInsumo().getNome(),
	            l.getQuantidadeInicial(), l.getQuantidadeAtual(), l.getDataEntrada(), l.getDataValidade(),
	            l.getFornecedor(), l.getDataValidade().isBefore(LocalDate.now()));
	    }
	}