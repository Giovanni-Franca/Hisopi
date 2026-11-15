package Hisopi.Hisopi.DTO;

import Hisopi.Hisopi.Enum.UnidadeMedida;
import Hisopi.Hisopi.model.ReceitaInsumo;

public record ReceitaInsumoResponseDTO(
    Long id, 
    Long idInsumo, 
    String nomeInsumo, 
    UnidadeMedida unidadeMedida, 
    Double quantidadePorUnidade) {
    public static ReceitaInsumoResponseDTO de(ReceitaInsumo v) {
        return new ReceitaInsumoResponseDTO(
            v.getId(), v.getInsumo().getId(), v.getInsumo().getNome(),
            v.getInsumo().getUnidadeMedida(), v.getQuantidadePorUnidade());
    }
}