package Hisopi.Hisopi.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import Hisopi.Hisopi.DTO.ReceitaInsumoResponseDTO;
import Hisopi.Hisopi.DTO.ReceitaResponseDTO;
import Hisopi.Hisopi.Enum.PapelMembro;
import Hisopi.Hisopi.Enum.TipoReceita;
import Hisopi.Hisopi.infra.exception.ConflitoException;
import Hisopi.Hisopi.infra.exception.NaoEncontradoException;
import Hisopi.Hisopi.infra.interceptor.AcessoEspaco;
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
    @AcessoEspaco(papelMinimo = PapelMembro.GERENTE)
    public ResponseEntity<ReceitaResponseDTO> criarReceita(
            @PathVariable Long idEspaco, @RequestBody @Valid ReceitaDTO dto) {

        Espaco espaco = repE.findById(idEspaco)
            .orElseThrow(() -> new NaoEncontradoException("Espaço não encontrado"));

        Receita receita = new Receita();
        receita.setEspaco(espaco);
        receita.setNome(dto.nome());
        receita.setTipo(dto.tipo());
        receita.setModoPreparo(dto.modoPreparo());
        repR.save(receita);

        return ResponseEntity.ok(ReceitaResponseDTO.de(receita));
    }

    @GetMapping
    public ResponseEntity<List<ReceitaResponseDTO>> listarReceitas(@PathVariable Long idEspaco) {
        return ResponseEntity.ok(
            repR.findByEspacoId(idEspaco).stream().map(ReceitaResponseDTO::de).toList());
    }

    @PostMapping("/{id}/insumos")
    @AcessoEspaco (papelMinimo = PapelMembro.GERENTE)
    public ResponseEntity<ReceitaInsumoResponseDTO> vincularInsumo(
            @PathVariable Long idEspaco, @PathVariable Long id,
            @RequestBody @Valid ReceitaInsumoDTO dto) {

        Receita receita = repR.findByIdAndEspacoId(id, idEspaco)
            .orElseThrow(() -> new NaoEncontradoException("Receita não encontrada"));

        Insumo insumo = repI.findByIdAndEspacoId(dto.idInsumo(), idEspaco)
            .orElseThrow(() -> new NaoEncontradoException("Insumo não encontrado"));

        if (repRI.existsByReceitaIdAndInsumoId(id, insumo.getId())) {
            throw new ConflitoException("Este insumo já está na ficha técnica da receita");
        }

        ReceitaInsumo vinculo = new ReceitaInsumo();
        vinculo.setReceita(receita);
        vinculo.setInsumo(insumo);
        vinculo.setQuantidadePorUnidade(dto.quantidadePorUnidade());
        repRI.save(vinculo);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ReceitaInsumoResponseDTO.de(vinculo));
    }

    @GetMapping("/{id}/insumos")
    public ResponseEntity<List<ReceitaInsumoResponseDTO>> listarFichaTecnica(
            @PathVariable Long idEspaco, @PathVariable Long id) {

        repR.findByIdAndEspacoId(id, idEspaco)
            .orElseThrow(() -> new NaoEncontradoException("Receita não encontrada"));

        return ResponseEntity.ok(
            repRI.findByReceitaId(id).stream().map(ReceitaInsumoResponseDTO::de).toList());
    }

    @GetMapping("/sugestoes")
    public ResponseEntity<List<ReceitaResponseDTO>> sugerirReceitas(@PathVariable Long idEspaco) {
        return ResponseEntity.ok(
            repR.findByEspacoIdAndTipo(idEspaco, TipoReceita.SUGESTAO_CONSUMO).stream()
                .map(ReceitaResponseDTO::de).toList());
    }

    @DeleteMapping("/{id}/insumos/{idReceitaInsumo}")
    @AcessoEspaco(papelMinimo = PapelMembro.GERENTE)
    public ResponseEntity<Void> removerVinculo(
            @PathVariable Long idEspaco, @PathVariable Long id,
            @PathVariable Long idReceitaInsumo) {

        repR.findByIdAndEspacoId(id, idEspaco)
            .orElseThrow(() -> new NaoEncontradoException("Receita não encontrada"));

        ReceitaInsumo vinculo = repRI.findByIdAndReceitaId(idReceitaInsumo, id)
            .orElseThrow(() -> new NaoEncontradoException("Vínculo não encontrado nesta receita"));

        repRI.delete(vinculo);
        return ResponseEntity.noContent().build();
    }
}