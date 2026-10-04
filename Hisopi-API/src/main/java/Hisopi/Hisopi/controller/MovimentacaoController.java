package Hisopi.Hisopi.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Hisopi.Hisopi.DTO.MovimentacaoResponseDTO;
import Hisopi.Hisopi.DTO.RelatorioPerdasDTO;
import Hisopi.Hisopi.Enum.TipoMovimentacao;
import Hisopi.Hisopi.infra.exception.NaoEncontradoException;
import Hisopi.Hisopi.infra.exception.RegraNegocioException;
import Hisopi.Hisopi.infra.interceptor.AcessoEspaco;
import Hisopi.Hisopi.model.MovimentacaoEstoque;
import Hisopi.Hisopi.repository.InsumoRepository;
import Hisopi.Hisopi.repository.MovimentacaoEstoqueRepository;

@RestController
@RequestMapping(value = "/espacos/{idEspaco}")
@AcessoEspaco
public class MovimentacaoController {

    @Autowired
    private MovimentacaoEstoqueRepository repM;
    @Autowired
    private InsumoRepository repI;

    @GetMapping("/insumos/{idInsumo}/movimentacoes")
    public ResponseEntity<List<MovimentacaoResponseDTO>> listarMovimentacoesDoInsumo(
            @PathVariable Long idEspaco, @PathVariable Long idInsumo) {

        repI.findByIdAndEspacoId(idInsumo, idEspaco)
            .orElseThrow(() -> new NaoEncontradoException("Insumo não encontrado"));

        List<MovimentacaoResponseDTO> lista = repM
            .findByInsumoIdOrderByDataMovimentacaoDesc(idInsumo).stream()
            .map(MovimentacaoResponseDTO::de)
            .toList();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/relatorios/perdas")
    public ResponseEntity<RelatorioPerdasDTO> relatorioDePerdas(
            @PathVariable Long idEspaco,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        if (inicio.isAfter(fim)) {
            throw new RegraNegocioException("A data inicial não pode ser maior que a final");
        }

        // [inicio 00:00, dia seguinte ao fim 00:00): cobre o último dia inteiro
        LocalDateTime de = inicio.atStartOfDay();
        LocalDateTime ate = fim.plusDays(1).atStartOfDay();

        List<MovimentacaoEstoque> perdas = repM.buscarPorTipos(
            idEspaco,
            List.of(TipoMovimentacao.PERDA_VALIDADE, TipoMovimentacao.PERDA_OUTRO),
            de, ate);

        Map<Boolean, List<MovimentacaoResponseDTO>> grupos = perdas.stream()
            .map(MovimentacaoResponseDTO::de)
            .collect(Collectors.partitioningBy(
                m -> m.tipo() == TipoMovimentacao.PERDA_VALIDADE));

        double total = perdas.stream()
            .mapToDouble(MovimentacaoEstoque::getQuantidade)
            .sum();

        return ResponseEntity.ok(new RelatorioPerdasDTO(
            grupos.get(true),
            grupos.get(false),
            total));
    }
}