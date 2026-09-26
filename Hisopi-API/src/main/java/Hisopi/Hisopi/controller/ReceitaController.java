package Hisopi.Hisopi.controller;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Hisopi.Hisopi.DTO.ReceitaDTO;
import Hisopi.Hisopi.DTO.ReceitaInsumoDTO;
import Hisopi.Hisopi.Enum.PapelMembro;
import Hisopi.Hisopi.Enum.TipoReceita;
import Hisopi.Hisopi.infra.security.interceptor.AcessoEspaco;
import Hisopi.Hisopi.model.Espaco;
import Hisopi.Hisopi.model.Insumo;
import Hisopi.Hisopi.model.Receita;
import Hisopi.Hisopi.model.ReceitaInsumo;
import Hisopi.Hisopi.repository.EspacoRepository;
import Hisopi.Hisopi.repository.InsumoRepository;
import Hisopi.Hisopi.repository.ReceitaInsumoRepository;
import Hisopi.Hisopi.repository.ReceitaRepository;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/espacos/{idEspaco}/receitas")
@AcessoEspaco
public class ReceitaController {

    @Autowired
    private ReceitaRepository repR;
    @Autowired
    private ReceitaInsumoRepository repRI;
    @Autowired
    private InsumoRepository repI;
    @Autowired
    private EspacoRepository repE;

    @PostMapping
    public ResponseEntity<Receita> criarReceita(
            @PathVariable Long idEspaco, @RequestBody @Valid ReceitaDTO dto) {

        Espaco espaco = repE.findById(idEspaco)
            .orElseThrow(() -> new RuntimeException("Espaço não encontrado"));

        Receita receita = new Receita();
        receita.setEspaco(espaco);
        receita.setNome(dto.nome());
        receita.setTipo(dto.tipo());
        receita.setModoPreparo(dto.modoPreparo());
        repR.save(receita);

        return ResponseEntity.ok(receita);
    }

    @GetMapping
    public ResponseEntity<?> listarReceitas(@PathVariable Long idEspaco) {
        return ResponseEntity.ok(repR.findByEspacoId(idEspaco));
    }

    @PostMapping("/{id}/insumos")
    @AcessoEspaco (papelMinimo = PapelMembro.GERENTE)
    public ResponseEntity<ReceitaInsumo> vincularInsumo(
            @PathVariable Long idEspaco, @PathVariable Long id,
            @RequestBody @Valid ReceitaInsumoDTO dto) {

        Receita receita = repR.findById(id)
            .orElseThrow(() -> new RuntimeException("Receita não encontrada"));

        Insumo insumo = repI.findById(dto.idInsumo())
            .orElseThrow(() -> new RuntimeException("Insumo não encontrado"));

        ReceitaInsumo vinculo = new ReceitaInsumo();
        vinculo.setReceita(receita);
        vinculo.setInsumo(insumo);
        vinculo.setQuantidadePorUnidade(dto.quantidadePorUnidade());
        repRI.save(vinculo);

        return ResponseEntity.ok(vinculo);
    }

    @GetMapping("/{id}/insumos")
    public ResponseEntity<?> listarFichaTecnica(@PathVariable Long idEspaco, @PathVariable Long id) {
        return ResponseEntity.ok(repRI.findByReceitaId(id));
    }

    @GetMapping("/sugestoes")
    public ResponseEntity<?> sugerirReceitas(@PathVariable Long idEspaco) {
        List<Receita> sugestoes = repR.findByTipo(TipoReceita.SUGESTAO_CONSUMO);
        return ResponseEntity.ok(sugestoes);

    }

    @DeleteMapping("/{id}/insumos/{idReceitaInsumo}")
    public ResponseEntity<Map<String, String>> removerVinculo(
            @PathVariable Long idEspaco, @PathVariable Long id,
            @PathVariable Long idReceitaInsumo) {

        if (!repRI.existsById(idReceitaInsumo)) {
            return ResponseEntity.notFound().build();
        }

        repRI.deleteById(idReceitaInsumo);
        return ResponseEntity.ok(Map.of("message", "Vínculo removido da ficha técnica"));
    }
}