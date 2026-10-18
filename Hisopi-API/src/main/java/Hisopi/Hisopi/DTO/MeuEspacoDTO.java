package Hisopi.Hisopi.DTO;

import Hisopi.Hisopi.Enum.PapelMembro;
import Hisopi.Hisopi.Enum.TipoEspaco;
import Hisopi.Hisopi.model.Espaco;
import Hisopi.Hisopi.model.MembroEspaco;

public record MeuEspacoDTO( 
		Long idEspaco,
        String nome,
        TipoEspaco tipo,
        PapelMembro meuPapel
) {
    public static MeuEspacoDTO de(MembroEspaco m) {
        Espaco e = m.getEspaco();
        return new MeuEspacoDTO(e.getId(), e.getNome(), e.getTipo(), m.getPapel());
    }
}