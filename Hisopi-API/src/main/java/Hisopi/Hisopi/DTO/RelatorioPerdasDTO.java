package Hisopi.Hisopi.DTO;

import java.util.List;

public record RelatorioPerdasDTO(
        List<MovimentacaoResponseDTO> perdasPorValidade,
        List<MovimentacaoResponseDTO> perdasPorOutroMotivo,
        double quantidadeTotalPerdida
) {}