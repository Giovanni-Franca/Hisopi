package Hisopi.Hisopi.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import Hisopi.Hisopi.DTO.NovaChaveDTO;
import Hisopi.Hisopi.DTO.NovoEspacoComChaveDTO;
import Hisopi.Hisopi.Enum.PapelMembro;
import Hisopi.Hisopi.infra.exception.NaoEncontradoException;
import Hisopi.Hisopi.infra.interceptor.AcessoEspaco;
import Hisopi.Hisopi.model.Espaco;
import Hisopi.Hisopi.model.Usuario;
import Hisopi.Hisopi.repository.EspacoApiKeyRepository;
import Hisopi.Hisopi.repository.EspacoRepository;
import Hisopi.Hisopi.services.ApiKeyService;
import Hisopi.Hisopi.services.ParceiroService;
import jakarta.validation.Valid;


@RestController
public class ApiKeyController {
	@Autowired
	private EspacoRepository repE;
	@Autowired
	private ApiKeyService apiKeyService;
	@Autowired
	private ParceiroService parceiroService;
	@Autowired
	private EspacoApiKeyRepository repK;
	
	@PostMapping("/api-keys/espaco")
    public ResponseEntity<ApiKeyService.ChaveCriada> criarEspacoComChave(
            @RequestBody @Valid NovoEspacoComChaveDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        return ResponseEntity.ok(parceiroService.criarEspacoComChave(dto, usuarioLogado));
    }

    @PostMapping("/espacos/{idEspaco}/api-keys")
    @AcessoEspaco(papelMinimo = PapelMembro.DONO)
    public ResponseEntity<ApiKeyService.ChaveCriada> gerarNova(
            @PathVariable Long idEspaco, @RequestBody @Valid NovaChaveDTO dto) {
        Espaco espaco = repE.findById(idEspaco)
            .orElseThrow(() -> new NaoEncontradoException("Espaço não encontrado"));
        return ResponseEntity.ok(apiKeyService.gerar(espaco, dto.nome()));
    }

    @GetMapping("/espacos/{idEspaco}/api-keys")
    @AcessoEspaco(papelMinimo = PapelMembro.DONO)
    public List<Map<String, Object>> listar(@PathVariable Long idEspaco) {
        return repK.findByEspacoId(idEspaco).stream().map(k -> Map.<String, Object>of(
            "id", k.getId(), "nome", k.getNome(), "prefixo", k.getPrefixo(),
            "papel", k.getPapel(), "ativa", k.isAtiva(),
            "ultimoUso", String.valueOf(k.getUltimoUso()))).toList();
    }

    @DeleteMapping("/espacos/{idEspaco}/api-keys/{idKey}")
    @AcessoEspaco(papelMinimo = PapelMembro.DONO)
    public ResponseEntity<Void> revogar(@PathVariable Long idEspaco, @PathVariable UUID idKey) {
        apiKeyService.revogar(idEspaco, idKey);
        return ResponseEntity.noContent().build();
    }

}
