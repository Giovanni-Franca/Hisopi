package Hisopi.Hisopi.infra.ApiKey;

import java.util.UUID;

import Hisopi.Hisopi.Enum.PapelMembro;

public record ApiKeyPrincipal(UUID keyId, Long espacoId, PapelMembro papel) {
	
}
