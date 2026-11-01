package Hisopi.Hisopi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import Hisopi.Hisopi.model.EspacoApiKey;

public interface EspacoApiKeyRepository extends JpaRepository<EspacoApiKey, UUID> {
    Optional<EspacoApiKey> findByHashAndAtivaTrue(String hash);
    List<EspacoApiKey> findByEspacoId(Long espacoId);
    Optional<EspacoApiKey> findByIdAndEspacoId(UUID id, Long espacoId);
}
