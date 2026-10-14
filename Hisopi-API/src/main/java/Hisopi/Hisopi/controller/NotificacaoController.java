
package Hisopi.Hisopi.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import Hisopi.Hisopi.DTO.NotificacaoResponseDTO;
import Hisopi.Hisopi.infra.interceptor.AcessoEspaco;
import Hisopi.Hisopi.model.Notificacao;
import Hisopi.Hisopi.services.NotificacaoService;

@RestController
@RequestMapping("/espacos/{idEspaco}/notificacoes")
@AcessoEspaco
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    public ResponseEntity<List<NotificacaoResponseDTO>> listar(
            @PathVariable Long idEspaco) {

        List<NotificacaoResponseDTO> notificacoes =
                notificacaoService.listarPorEspaco(idEspaco)
                        .stream()
                        .map(NotificacaoResponseDTO::de)
                        .toList();

        return ResponseEntity.ok(notificacoes);
    }

    @PatchMapping("/{idNotificacao}/lida")
    public ResponseEntity<Void> marcarComoLida(
            @PathVariable Long idEspaco,
            @PathVariable Long idNotificacao) {

        notificacaoService.marcarComoLida(idEspaco, idNotificacao);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/verificar")
    public ResponseEntity<String> verificarAgora(
            @PathVariable Long idEspaco) {

        int criadas = notificacaoService
                .verificarLotesProximosDoVencimento(idEspaco);

        return ResponseEntity.ok(
                "Verificação concluída. Notificações criadas: " + criadas
        );
    }

}
