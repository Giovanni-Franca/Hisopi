
package Hisopi.Hisopi.repository;

import java.util.Optional;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import Hisopi.Hisopi.Enum.TipoNotificacao;
import Hisopi.Hisopi.model.Notificacao;

public interface NotificacaoRepository
        extends JpaRepository<Notificacao, Long> {

    boolean existsByLoteIdAndTipoAndDataValidadeReferencia(
            Long idLote,
            TipoNotificacao tipo,
            LocalDate dataValidadeReferencia
    );

    List<Notificacao> findByEspacoIdOrderByCriadaEmDesc(
            Long idEspaco
    );

    java.util.Optional<Notificacao> findByIdAndEspacoId(
            Long idNotificacao,
            Long idEspaco
    );
}
