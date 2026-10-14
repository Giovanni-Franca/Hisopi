
package Hisopi.Hisopi.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import Hisopi.Hisopi.repository.EspacoRepository;

@Component
public class NotificacaoScheduler {

    private final EspacoRepository repE;
    private final NotificacaoService notificacaoService;

    public NotificacaoScheduler(
            EspacoRepository repE,
            NotificacaoService notificacaoService) {
        this.repE = repE;
        this.notificacaoService = notificacaoService;
    }

    // Executa diariamente às 08h, no horário de Brasília.
    @Scheduled(cron = "0 0 8 * * *", zone = "America/Sao_Paulo")
    public void verificarVencimentos() {
        repE.findAll().forEach(
                espaco -> notificacaoService
                        .verificarLotesProximosDoVencimento(espaco.getId())
        );
    }
}
