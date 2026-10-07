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
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Hisopi.Hisopi.DTO.EspacoDTO;
import Hisopi.Hisopi.DTO.MembroDTO;
import Hisopi.Hisopi.DTO.MembroResponseDTO;
import Hisopi.Hisopi.DTO.MeuEspacoDTO;
import Hisopi.Hisopi.Enum.PapelMembro;
import Hisopi.Hisopi.infra.exception.AcessoNegadoException;
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
        return ResponseEntity.ok(
        		repE.findById(idEspaco)
        		.orElseThrow(() -> new NaoEncontradoException("Espaço não encontrado")));
    }

    @GetMapping("/minhas")
    public ResponseEntity<List<MeuEspacoDTO>> listarMeusEspacos(@AuthenticationPrincipal Usuario usuarioLogado) {
        List<MeuEspacoDTO> lista = repM.findByUsuarioId(usuarioLogado.getId()).stream()
                .map(MeuEspacoDTO::de)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/{idEspaco}/membros")
    @AcessoEspaco(papelMinimo = PapelMembro.ADMIN)
    public ResponseEntity<?> adicionarMembro(@PathVariable Long idEspaco, @RequestBody @Valid MembroDTO dto, @RequestAttribute("membroLogado") MembroEspaco logado) {
        if(!logado.getPapel().superior(dto.papel())) {
        	throw new AcessoNegadoException("Não é possível atribuir um papel igual ou maior ao seu");
        }
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

        return ResponseEntity.ok("Usuário adicionado");
    }

    @GetMapping("/{idEspaco}/membros")
    @AcessoEspaco
    public ResponseEntity<?> listarMembros(@PathVariable Long idEspaco) {
    	List<MembroResponseDTO> lista = repM.findByEspacoId(idEspaco).stream()
    	        .map(MembroResponseDTO::de)
    	        .toList();
    	    return ResponseEntity.ok(lista);
	}

    @DeleteMapping("/{idEspaco}/membros/{idMembro}")
    @AcessoEspaco(papelMinimo = PapelMembro.ADMIN)
    public ResponseEntity<Map<String, String>> removerMembro(@PathVariable Long idEspaco, @PathVariable Long idMembro,@RequestAttribute("membroLogado") MembroEspaco logado) {
    	MembroEspaco alvo = repM.findById(idMembro)
    		    .filter(m -> m.getEspaco().getId().equals(idEspaco))
    		    .orElseThrow(() -> new NaoEncontradoException("Membro não encontrado"));
    	
    	if(!logado.getPapel().superior(alvo.getPapel())) {
        	throw new AcessoNegadoException("Não é possível atribuir um papel igual ou maior ao seu");
        }
    	
        repM.delete(alvo);
        return ResponseEntity.ok(Map.of("message", "Membro removido do espaço"));
    }
}