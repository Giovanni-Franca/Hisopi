package Hisopi.Hisopi.DTO;

import org.springframework.security.core.userdetails.UserDetails;

import Hisopi.Hisopi.Enum.PapelMembro;

public record MembroDTO(
	    UserDetails email,
	    PapelMembro papel
	) {}