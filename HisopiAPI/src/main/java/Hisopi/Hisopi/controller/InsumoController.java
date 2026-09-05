package Hisopi.Hisopi.controller;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Hisopi.Hisopi.DTO.InsumoDTO;
import Hisopi.Hisopi.DTO.LoteDTO;
import Hisopi.Hisopi.DTO.PerdaDTO;
import Hisopi.Hisopi.model.Espaco;
import Hisopi.Hisopi.model.Insumo;
import Hisopi.Hisopi.model.LoteInsumo;
import Hisopi.Hisopi.model.MovimentacaoEstoque;
import Hisopi.Hisopi.model.MovimentacaoEstoque.TipoMovimentacao;
import Hisopi.Hisopi.repository.EspacoRepository;
import Hisopi.Hisopi.repository.InsumoRepository;
import Hisopi.Hisopi.repository.LoteInsumoRepository;
import Hisopi.Hisopi.repository.MovimentacaoEstoqueRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


@Tag(name = "insumos", description = "Controle de insumos, lotes, validade e registro de perdas")
@RestController
@RequestMapping(value = "/espacos/{idEspaco}/insumos")
public class InsumoController {

    @Autowired
    private InsumoRepository repI;
    @Autowired
    private LoteInsumoRepository repL;
    @Autowired
    private MovimentacaoEstoqueRepository repM;
    @Autowired
    private EspacoRepository repE;

    // =====================================================
    // INSUMOS
    // =====================================================

    @PostMapping
    public ResponseEntity<Insumo> criarInsumo(
            @PathVariable Long idEspaco, @RequestBody @Valid InsumoDTO dto) {

        Espaco espaco = repE.findById(idEspaco)
            .orElseThrow(() -> new RuntimeException("Espaço não encontrado"));

        Insumo insumo = new Insumo();
        insumo.setEspaco(espaco);
        insumo.setNome(dto.nome());
        insumo.setCategoria(dto.categoria());
        insumo.setUnidadeMedida(dto.unidadeMedida());
        insumo.setEstoqueAtual(0.0);
        insumo.setEstoqueMinimo(dto.estoqueMinimo());
        insumo.setCustoUnitario(dto.custoUnitario());
        insumo.setAtivo(true);

        repI.save(insumo);

        return ResponseEntity.ok(insumo);
    }

    @GetMapping
    public ResponseEntity<?> listarInsumos(@PathVariable Long idEspaco) {
        return ResponseEntity.ok(repI.findByEspacoIdAndAtivoTrue(idEspaco));
    }

    @GetMapping("/baixoEstoque")
    public ResponseEntity<?> listarBaixoEstoque(@PathVariable Long idEspaco) {
        List<Insumo> baixoEstoque = repI.findByEspacoIdAndAtivoTrue(idEspaco).stream()
            .filter(i -> i.getEstoqueAtual() <= i.getEstoqueMinimo())
            .toList();

        return ResponseEntity.ok(baixoEstoque);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Insumo> editarInsumo(
            @PathVariable Long idEspaco, @PathVariable Long id,
            @RequestBody @Valid InsumoDTO dto) {

        Optional<Insumo> insumoExistente = repI.findById(id);

        if (insumoExistente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Insumo insumo = insumoExistente.get();
        insumo.setNome(dto.nome());
        insumo.setCategoria(dto.categoria());
        insumo.setUnidadeMedida(dto.unidadeMedida());
        insumo.setEstoqueMinimo(dto.estoqueMinimo());
        insumo.setCustoUnitario(dto.custoUnitario());

        repI.save(insumo);

        return ResponseEntity.ok(insumo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> desativarInsumo(
            @PathVariable Long idEspaco, @PathVariable Long id) {

        Optional<Insumo> insumoOpt = repI.findById(id);

        if (insumoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Insumo insumo = insumoOpt.get();
        insumo.setAtivo(false);
        repI.save(insumo);

        return ResponseEntity.ok(Map.of("message", "Insumo desativado"));
    }

    // =====================================================
    // LOTES (entrada de estoque + FEFO)
    // =====================================================

    @PostMapping("/{id}/lotes")
    public ResponseEntity<LoteInsumo> registrarEntrada(
            @PathVariable Long idEspaco, @PathVariable Long id,
            @RequestBody @Valid LoteDTO dto) {

        Insumo insumo = repI.findById(id)
            .orElseThrow(() -> new RuntimeException("Insumo não encontrado"));

        LoteInsumo lote = new LoteInsumo();
        lote.setInsumo(insumo);
        lote.setQuantidadeInicial(dto.quantidade());
        lote.setQuantidadeAtual(dto.quantidade());
        lote.setDataEntrada(LocalDate.now());
        lote.setDataValidade(dto.dataValidade());
        lote.setFornecedor(dto.fornecedor());
        repL.save(lote);

        insumo.setEstoqueAtual(insumo.getEstoqueAtual() + dto.quantidade());
        repI.save(insumo);

        registrarMovimentacao(insumo, lote, TipoMovimentacao.ENTRADA, dto.quantidade(), null);

        return ResponseEntity.ok(lote);
    }

    @GetMapping("/{id}/lotes")
    public ResponseEntity<?> listarLotes(@PathVariable Long idEspaco, @PathVariable Long id) {
        // Já ordenado por validade ASC — o primeiro item é o próximo a vencer (FEFO)
        return ResponseEntity.ok(
            repL.findByInsumoIdAndQuantidadeAtualGreaterThanOrderByDataValidadeAsc(id, 0.0)
        );
    }

    @GetMapping("/vencendo")
    public ResponseEntity<?> listarVencendo(
            @PathVariable Long idEspaco, @RequestParam(defaultValue = "7") int dias) {

        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(dias);

        return ResponseEntity.ok(
            repL.findByInsumoEspacoIdAndDataValidadeBetweenAndQuantidadeAtualGreaterThan(
                idEspaco, hoje, limite, 0.0)
        );
    }

    @PutMapping("/lotes/{idLote}/perda")
    public ResponseEntity<Map<String, String>> registrarPerda(
            @PathVariable Long idEspaco, @PathVariable Long idLote,
            @RequestBody @Valid PerdaDTO dto) {

        Optional<LoteInsumo> loteOpt = repL.findById(idLote);

        if (loteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        LoteInsumo lote = loteOpt.get();
        Double quantidadePerdida = dto.quantidade() != null
            ? dto.quantidade()
            : lote.getQuantidadeAtual();

        lote.setQuantidadeAtual(lote.getQuantidadeAtual() - quantidadePerdida);
        repL.save(lote);

        Insumo insumo = lote.getInsumo();
        insumo.setEstoqueAtual(insumo.getEstoqueAtual() - quantidadePerdida);
        repI.save(insumo);

        registrarMovimentacao(insumo, lote, dto.tipo(), quantidadePerdida, dto.motivo());

        return ResponseEntity.ok(Map.of("message", "Perda registrada"));
    }

    private void registrarMovimentacao(
            Insumo insumo, LoteInsumo lote, TipoMovimentacao tipo,
            Double quantidade, String motivo) {

        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setInsumo(insumo);
        mov.setLote(lote);
        mov.setTipo(tipo);
        mov.setQuantidade(quantidade);
        mov.setMotivo(motivo);
        mov.setDataMovimentacao(LocalDateTime.now());
        repM.save(mov);
    }
}