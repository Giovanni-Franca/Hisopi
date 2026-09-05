package Hisopi.Hisopi.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import Hisopi.Hisopi.model.MovimentacaoEstoque;
import Hisopi.Hisopi.model.MovimentacaoEstoque.TipoMovimentacao;
import Hisopi.Hisopi.repository.MovimentacaoEstoqueRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "movimentacoes", description = "Histórico de movimentações e relatórios de desperdício")
@RestController
@RequestMapping(value = "/espacos/{idEspaco}")
public class MovimentacaoController {

    @Autowired
    private MovimentacaoEstoqueRepository repM;

    @GetMapping("/insumos/{idInsumo}/movimentacoes")
    public ResponseEntity<?> listarMovimentacoesDoInsumo(
            @PathVariable Long idEspaco, @PathVariable Long idInsumo) {

        return ResponseEntity.ok(repM.findByInsumoIdOrderByDataMovimentacaoDesc(idInsumo));
    }

    @GetMapping("/relatorios/perdas")
    public ResponseEntity<?> relatorioDePerdas(
            @PathVariable Long idEspaco,
            @RequestParam String inicio, @RequestParam String fim) {

        LocalDateTime dataInicio = LocalDate.parse(inicio).atStartOfDay();
        LocalDateTime dataFim = LocalDate.parse(fim).atTime(23, 59, 59);

        List<MovimentacaoEstoque> perdasValidade = repM
            .findByInsumoEspacoIdAndTipoAndDataMovimentacaoBetween(
                idEspaco, TipoMovimentacao.PERDA_VALIDADE, dataInicio, dataFim);

        List<MovimentacaoEstoque> perdasOutro = repM
            .findByInsumoEspacoIdAndTipoAndDataMovimentacaoBetween(
                idEspaco, TipoMovimentacao.PERDA_OUTRO, dataInicio, dataFim);

        double totalPerdido = perdasValidade.stream().mapToDouble(MovimentacaoEstoque::getQuantidade).sum()
            + perdasOutro.stream().mapToDouble(MovimentacaoEstoque::getQuantidade).sum();

        return ResponseEntity.ok(Map.of(
            "perdasPorValidade", perdasValidade,
            "perdasPorOutroMotivo", perdasOutro,
            "quantidadeTotalPerdida", totalPerdido
        ));
    }
}