package Hisopi.Hisopi.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Hisopi.Hisopi.DTO.EspacoDTO;
import Hisopi.Hisopi.DTO.MembroDTO;
import Hisopi.Hisopi.Enum.PapelMembro;
import Hisopi.Hisopi.infra.exception.ConflitoException;
import Hisopi.Hisopi.infra.exception.NaoEncontradoException;
import Hisopi.Hisopi.infra.interceptor.AcessoEspaco;
import Hisopi.Hisopi.model.Espaco;
import Hisopi.Hisopi.model.MembroEspaco;
import Hisopi.Hisopi.model.Usuario;
import Hisopi.Hisopi.repository.EspacoRepository;
import Hisopi.Hisopi.repository.MembroEspacoRepository;
import Hisopi.Hisopi.repository.UsuarioRepository;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/espacos")
public class EspacoController {

    @Autowired
    private EspacoRepository repE;
    @Autowired
    private MembroEspacoRepository repM;
    @Autowired
    private UsuarioRepository repU;

    
    @PostMapping
    public ResponseEntity<Espaco> criarEspaco(@RequestBody @Valid EspacoDTO dto, @AuthenticationPrincipal Usuario usuarioLogado) {
        Espaco espaco = new Espaco();
        espaco.setNome(dto.nome());
        espaco.setTipo(dto.tipo());
        repE.save(espaco);

        MembroEspaco membro = new MembroEspaco();
        membro.setEspaco(espaco);
        membro.setUsuario(usuarioLogado);
        membro.setPapel(PapelMembro.DONO);
        repM.save(membro);

        return ResponseEntity.ok(espaco);
    }

    @GetMapping("/{idEspaco}")
    @AcessoEspaco
    public ResponseEntity<Espaco> buscarEspaco(@PathVariable Long idEspaco, @AuthenticationPrincipal Usuario usuarioLogado) {
        return repE.findById(idEspaco)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/minhas")
    public ResponseEntity<?> listarMeusEspacos(@AuthenticationPrincipal Usuario usuarioLogado) {
        List<MembroEspaco> memberships = repM.findByUsuarioId(usuarioLogado.getId());
        return ResponseEntity.ok(memberships);
    }

    @PostMapping("/{idEspaco}/membros")
    @AcessoEspaco(papelMinimo = PapelMembro.ADMIN)
    public ResponseEntity<?> adicionarMembro(@PathVariable Long idEspaco, @RequestBody @Valid MembroDTO dto) {
        Espaco espaco = repE.findById(idEspaco)
            .orElseThrow(() -> new NaoEncontradoException("Espaço não encontrado"));
        Usuario usuario = (Usuario) repU.findByEmail(dto.email());
        
        if (usuario == null) {
            throw new NaoEncontradoException("Usuário não encontrado");
        }

        if (repM.existsByEspacoIdAndUsuarioId(idEspaco, usuario.getId())) {
            throw new ConflitoException("Usuário já adicionado");
        }

        MembroEspaco membro = new MembroEspaco();
        membro.setEspaco(espaco);
        membro.setUsuario(usuario);
        membro.setPapel(dto.papel());
        repM.save(membro);

        return ResponseEntity.ok(membro);
    }

    @GetMapping("/{idEspaco}/membros")
    @AcessoEspaco
    public ResponseEntity<?> listarMembros(@PathVariable Long idEspaco) {
        return ResponseEntity.ok(repM.findByEspacoId(idEspaco));
    }

    @DeleteMapping("/{idEspaco}/membros/{idMembro}")
    @AcessoEspaco(papelMinimo = PapelMembro.ADMIN)
    public ResponseEntity<Map<String, String>> removerMembro(@PathVariable Long idEspaco, @PathVariable Long idMembro) {
    	MembroEspaco alvo = repM.findById(idMembro)
    		    .filter(m -> m.getEspaco().getId().equals(idEspaco))
    		    .orElseThrow(() -> new NaoEncontradoException("Membro não encontrado neste espaço"));

        repM.delete(alvo);
        return ResponseEntity.ok(Map.of("message", "Membro removido do espaço"));
    }
}