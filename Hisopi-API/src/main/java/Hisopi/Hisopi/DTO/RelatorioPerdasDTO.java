package Hisopi.Hisopi.DTO;

import java.util.List;

public record RelatorioPerdasDTO(
	    List<MovimentacaoResponseDTO> perdasPorValidade,
	    List<MovimentacaoResponseDTO> perdasPorOutroMotivo,
	    Double quantidadeTotalPerdida,
	    List<MovimentacaoResponseDTO> saidasUso,
	    List<MovimentacaoResponseDTO> doacoes,
	    List<MovimentacaoResponseDTO> ajustes
	) {}