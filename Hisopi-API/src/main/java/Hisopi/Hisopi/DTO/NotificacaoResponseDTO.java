
package Hisopi.Hisopi.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

import Hisopi.Hisopi.Enum.TipoNotificacao;
import Hisopi.Hisopi.model.Notificacao;

public record NotificacaoResponseDTO(
        Long id,
        TipoNotificacao tipo,
        String titulo,
        String mensagem,
        LocalDate dataValidade,
        LocalDateTime criadaEm,
        boolean lida,
        Long idLote,
        Long idInsumo,
        String nomeInsumo
) {
    public static NotificacaoResponseDTO de(Notificacao n) {
        return new NotificacaoResponseDTO(
                n.getId(),
                n.getTipo(),
                n.getTitulo(),
                n.getMensagem(),
                n.getLote().getDataValidade(),
                n.getCriadaEm(),
                n.isLida(),
                n.getLote().getId(),
                n.getLote().getInsumo().getId(),
                n.getLote().getInsumo().getNome()
        );
    }
}
