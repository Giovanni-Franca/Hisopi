
package Hisopi.Hisopi.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Hisopi.Hisopi.Enum.TipoNotificacao;
import Hisopi.Hisopi.model.LoteInsumo;
import Hisopi.Hisopi.model.Notificacao;
import Hisopi.Hisopi.repository.LoteInsumoRepository;
import Hisopi.Hisopi.repository.NotificacaoRepository;

@Service
public class NotificacaoService {

    private static final int DIAS_ANTECEDENCIA = 7;

    private final LoteInsumoRepository repLote;
    private final NotificacaoRepository repN;

    public NotificacaoService(
            LoteInsumoRepository repLote,
            NotificacaoRepository repN) {
        this.repLote = repLote;
        this.repN = repN;
    }

    @Transactional
    public int verificarLotesProximosDoVencimento(Long idEspaco) {
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(DIAS_ANTECEDENCIA);

        List<LoteInsumo> lotes = repLote
                .findByInsumoEspacoIdAndDataValidadeBetweenAndQuantidadeAtualGreaterThan(
                        idEspaco, hoje, limite, 0.0
                );

        int criadas = 0;

        for (LoteInsumo lote : lotes) {
            boolean jaExiste = repN
                    .existsByLoteIdAndTipoAndDataValidadeReferencia(
                            lote.getId(),
                            TipoNotificacao.LOTE_PROXIMO_VENCIMENTO,
                            lote.getDataValidade()
                    );

            if (jaExiste) {
                continue;
            }

            long diasRestantes = hoje.until(lote.getDataValidade()).getDays();

            Notificacao notificacao = new Notificacao();
            notificacao.setEspaco(lote.getInsumo().getEspaco());
            notificacao.setLote(lote);
            notificacao.setTipo(TipoNotificacao.LOTE_PROXIMO_VENCIMENTO);
            notificacao.setTitulo("Lote próximo do vencimento");
            notificacao.setMensagem(
                    lote.getInsumo().getNome()
                            + " vence em "
                            + diasRestantes
                            + (diasRestantes == 1 ? " dia." : " dias.")
            );
            notificacao.setDataValidadeReferencia(lote.getDataValidade());

            repN.save(notificacao);
            criadas++;
        }

        return criadas;
    }


    @Transactional(readOnly = true)
    public List<Notificacao> listarPorEspaco(Long idEspaco) {
        return repN.findByEspacoIdOrderByCriadaEmDesc(idEspaco);
    }

    @Transactional
    public void marcarComoLida(Long idEspaco, Long idNotificacao) {
        Notificacao notificacao = repN
                .findByIdAndEspacoId(idNotificacao, idEspaco)
                .orElseThrow(() -> new Hisopi.Hisopi.infra.exception
                        .NaoEncontradoException("Notificação não encontrada"));

        notificacao.setLida(true);
    }
}
