package Hisopi.Hisopi.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import Hisopi.Hisopi.Enum.PapelMembro;
import Hisopi.Hisopi.infra.ApiKey.ApiKeyPrincipal;
import Hisopi.Hisopi.infra.exception.NaoEncontradoException;
import Hisopi.Hisopi.model.Espaco;
import Hisopi.Hisopi.model.EspacoApiKey;
import Hisopi.Hisopi.repository.EspacoApiKeyRepository;
import jakarta.transaction.Transactional;

@Service
public class ApiKeyService {

	private static final SecureRandom RANDOM = new SecureRandom();
    private final EspacoApiKeyRepository repo;
    public ApiKeyService(EspacoApiKeyRepository repo) { this.repo = repo; }

    public record ChaveCriada(UUID id, Long espacoId, String nome, String chave) {}

    @Transactional
    public ChaveCriada gerar(Espaco espaco, String nome) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String chave = "esp_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        EspacoApiKey k = new EspacoApiKey();
        k.setNome(nome);
        k.setPrefixo(chave.substring(0, 12));
        k.setHash(hash(chave));
        k.setEspaco(espaco);
        k.setPapel(PapelMembro.GERENTE);
        repo.save(k);

        return new ChaveCriada(k.getId(), espaco.getId(), nome, chave);
    }

    @Transactional
    public Optional<ApiKeyPrincipal> validar(String chave) {
        return repo.findByHashAndAtivaTrue(hash(chave)).map(k -> {
            if (k.getUltimoUso() == null
                    || k.getUltimoUso().isBefore(LocalDateTime.now().minusMinutes(1))) {
                k.setUltimoUso(LocalDateTime.now());
            }
            return new ApiKeyPrincipal(k.getId(), k.getEspaco().getId(), k.getPapel());
        });
    }

    @Transactional
    public void revogar(Long espacoId, UUID keyId) {
        repo.findByIdAndEspacoId(keyId, espacoId)
            .orElseThrow(() -> new NaoEncontradoException("Chave não encontrada"))
            .setAtiva(false);
    }

    private static String hash(String chave) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(chave.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}