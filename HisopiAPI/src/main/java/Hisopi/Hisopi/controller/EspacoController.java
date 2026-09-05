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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Hisopi.Hisopi.DTO.EspacoDTO;
import Hisopi.Hisopi.DTO.MembroDTO;
import Hisopi.Hisopi.model.Espaco;
import Hisopi.Hisopi.model.MembroEspaco;
import Hisopi.Hisopi.model.MembroEspaco.PapelMembro;
import Hisopi.Hisopi.model.Usuario;
import Hisopi.Hisopi.repository.EspacoRepository;
import Hisopi.Hisopi.repository.MembroEspacoRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "espacos", description = "Gerenciamento de espaços (pessoais ou organizacionais) e seus membros")
@RestController
@RequestMapping(value = "/espacos")
public class EspacoController {

    @Autowired
    private EspacoRepository repE;
    @Autowired
    private MembroEspacoRepository repM;
    @Autowired
    private UsuarioRepository repU;

    // TODO: substituir por Long vindo do token/contexto de autenticação
    // quando o login for integrado. Por ora, aceita como query param.
    @PostMapping
    public ResponseEntity<Espaco> criarEspaco(
            @RequestBody @Valid EspacoDTO dto, @RequestParam Long idUsuarioCriador) {

        Usuario usuario = repU.findById(idUsuarioCriador)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Espaco espaco = new Espaco();
        espaco.setNome(dto.nome());
        espaco.setTipo(dto.tipo());
        repE.save(espaco);

        MembroEspaco membro = new MembroEspaco();
        membro.setEspaco(espaco);
        membro.setUsuario(usuario);
        membro.setPapel(PapelMembro.DONO);
        repM.save(membro);

        return ResponseEntity.ok(espaco);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Espaco> buscarEspaco(@PathVariable Long id) {
        return repE.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> listarEspacosDoUsuario(@PathVariable Long idUsuario) {
        List<MembroEspaco> memberships = repM.findByEspacoId(idUsuario); // ajustar: ver nota abaixo
        return ResponseEntity.ok(memberships);
    }

    @PostMapping("/{id}/membros")
    public ResponseEntity<MembroEspaco> adicionarMembro(
            @PathVariable Long id, @RequestBody @Valid MembroDTO dto) {

        Espaco espaco = repE.findById(id)
            .orElseThrow(() -> new RuntimeException("Espaço não encontrado"));

        Usuario usuario = repU.findById(dto.idUsuario())
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (repM.existsByEspacoIdAndUsuarioId(id, dto.idUsuario())) {
            return ResponseEntity.badRequest().build();
        }

        MembroEspaco membro = new MembroEspaco();
        membro.setEspaco(espaco);
        membro.setUsuario(usuario);
        membro.setPapel(dto.papel());
        repM.save(membro);

        return ResponseEntity.ok(membro);
    }

    @GetMapping("/{id}/membros")
    public ResponseEntity<?> listarMembros(@PathVariable Long id) {
        return ResponseEntity.ok(repM.findByEspacoId(id));
    }

    @DeleteMapping("/{id}/membros/{idMembro}")
    public ResponseEntity<Map<String, String>> removerMembro(
            @PathVariable Long id, @PathVariable Long idMembro) {

        if (!repM.existsById(idMembro)) {
            return ResponseEntity.notFound().build();
        }

        repM.deleteById(idMembro);
        return ResponseEntity.ok(Map.of("message", "Membro removido do espaço"));
    }
}